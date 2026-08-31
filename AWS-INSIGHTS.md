# Self-hosted test API on AWS + Postman Insights (for the API Catalog)

An **always-on** self-hosted deployment of the demo APIs on a small AWS EC2 VM, running the
**Postman Insights agent** so the Insights-powered parts of the API Catalog (Runtime Health,
observed endpoints, error rates) stay populated 24/7. This is the **sole backend** — the APIs are
plain Node/Express servers (no Cloudflare). The VM also hosts the async
**MQTT notifications** bridge and the **payment refund webhook**.

## Why AWS / cost

- **EC2 `t4g.small`** (ARM, 2 GB) in **eu-central-1** (EU-local). Always-on for continuous Insights.
- Cost ≈ **instance ~$12/mo + public IPv4 ~$3.6/mo + ~12 GB EBS ~$1/mo ≈ $16/mo**. (AWS charges
  for public IPv4 like GCP; a stopped instance still bills the Elastic IP, so always-on is the
  intended mode here.) Cheaper `t4g.micro` (1 GB, +swap) is possible if you want to trim ~$6/mo.
- Reuses `runtime-vm/deploy-runtime.sh` (Node 20, Caddy, Insights agent, swap-if-needed).

## The enterprise gotcha: no public SSH → use SSM

The org's subnet **Network ACL denies inbound SSH (22)** except from corporate IPs, but **allows
80/443 from anywhere** (so the demo URLs are public). So we **manage the box with AWS Systems
Manager (SSM)** — the agent connects *outbound*, no inbound SSH needed. This matches the org's
security posture. `control.sh` uses SSM under the hood.

Prereqs: `aws` CLI + SSO login (`aws sso login`), permission to create EC2/IAM/SSM resources.

## Provision (one-time, from your laptop)

```bash
export AWS_REGION=eu-central-1 AWS_PAGER=""
AMI=$(aws ec2 describe-images --owners 099720109477 \
  --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-arm64-server-*" "Name=state,Values=available" \
  --query 'reverse(sort_by(Images,&CreationDate))[0].ImageId' --output text)
VPC=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)
SUBNET=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC --query 'Subnets[0].SubnetId' --output text)

# SSH key pair (used only if corp-network SSH is available; SSM is the primary path)
aws ec2 import-key-pair --key-name postman-insights --public-key-material fileb://~/.ssh/postman-insights.pub

# security group (80/443 world-open; 22 is still gated by the NACL)
SG=$(aws ec2 create-security-group --group-name postman-insights-sg --description "postman insights" --vpc-id $VPC --query GroupId --output text)
for p in 22 80 443; do aws ec2 authorize-security-group-ingress --group-id $SG --protocol tcp --port $p --cidr 0.0.0.0/0; done

# launch t4g.small + static Elastic IP
IID=$(aws ec2 run-instances --image-id $AMI --instance-type t4g.small --key-name postman-insights \
  --security-group-ids $SG --subnet-id $SUBNET --associate-public-ip-address \
  --block-device-mappings '[{"DeviceName":"/dev/sda1","Ebs":{"VolumeSize":12,"VolumeType":"gp3"}}]' \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=postman-insights-demo}]' \
  --query 'Instances[0].InstanceId' --output text)
aws ec2 wait instance-running --instance-ids $IID
ALLOC=$(aws ec2 allocate-address --domain vpc --query AllocationId --output text)
aws ec2 associate-address --instance-id $IID --allocation-id $ALLOC
EIP=$(aws ec2 describe-addresses --allocation-ids $ALLOC --query 'Addresses[0].PublicIp' --output text)
echo "Elastic IP: $EIP  ->  host: ${EIP//./-}.nip.io"
```

### Enable SSM (management without SSH)

```bash
# IAM role + instance profile
aws iam create-role --role-name postman-insights-ssm --assume-role-policy-document \
  '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
aws iam attach-role-policy --role-name postman-insights-ssm --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
aws iam create-instance-profile --instance-profile-name postman-insights-ssm
aws iam add-role-to-instance-profile --instance-profile-name postman-insights-ssm --role-name postman-insights-ssm

# store the Postman API key as a SecureString (kept out of command logs)
aws ssm put-parameter --name /postman-insights/postman-api-key --type SecureString --value 'PMAK-...'
KMS=$(aws kms describe-key --key-id alias/aws/ssm --query KeyMetadata.Arn --output text)
aws iam put-role-policy --role-name postman-insights-ssm --policy-name read-postman-secrets --policy-document \
  "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"ssm:GetParameter\",\"ssm:GetParameters\"],\"Resource\":\"arn:aws:ssm:$AWS_REGION:ACCOUNT_ID:parameter/postman-insights/*\"},{\"Effect\":\"Allow\",\"Action\":\"kms:Decrypt\",\"Resource\":\"$KMS\"}]}"

aws ec2 associate-iam-instance-profile --instance-id $IID --iam-instance-profile Name=postman-insights-ssm
aws ec2 reboot-instances --instance-ids $IID     # reboot so the SSM agent picks up the role
# wait until: aws ssm describe-instance-information ... PingStatus == Online
```

### Deploy (via SSM — no SSH)

Send a bootstrap that clones the repo, fetches the API key from SSM, writes `runtime-vm/.env`, and
runs `./deploy-runtime.sh`. Then install the `synthetic-traffic.sh` cron (`* * * * *`). No DuckDNS —
the Elastic IP is static, so `<eip>.nip.io` is stable.

`runtime-vm/.env` values:
- `HOSTNAME=<eip-dashes>.nip.io`
- `POSTMAN_API_KEY` (fetched from the SSM SecureString), `INSIGHTS_WORKSPACE_ID`, `INSIGHTS_SYSTEM_ENV`
- `REFUND_WEBHOOK_URL` — the deploy sets it in the payments service's environment so
  `/payments/refund` can POST the `payment.refunded` event
- `NOTIFICATION_WEBHOOK_URL` — enables the always-on `mqtt-bridge` (subscribes to
  `broker.hivemq.com:1883` / `postman-api-catalog-demo/notifications`, forwards to this webhook)

`deploy-runtime.sh` also **pins each webhook host in `/etc/hosts`**: Postman's `*.webhook.pstmn.io`
has a CNAME with a literal `*` that Linux glibc won't follow, so the bridge/refund `fetch` would
fail without the pin.

## Manage it

```bash
cp runtime-vm/aws/aws.env.example runtime-vm/aws/aws.env   # set AWS_INSTANCE_ID + AWS_HOST
./control.sh status     # EC2 state + services + health (via SSM)
./control.sh reset      # restart the services
./control.sh urls       # the 3 API URLs
```

## Security
- No public SSH (NACL-enforced); management via SSM only.
- API key stored as an SSM **SecureString**; on the VM it lives in `runtime-vm/.env` (root-only).
  Nothing secret is committed. `runtime-vm/aws/aws.env` (instance id / host, non-secret) is git-ignored.
- If a key was ever shared in plaintext, rotate it in Postman + update the SSM param.
