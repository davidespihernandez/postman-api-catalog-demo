# Replication runbook — self-hosted APIs on AWS + Postman Insights

This is the **step-by-step guide to stand up the whole demo on a fresh AWS account** so another SE
can reproduce it. It provisions an **always-on** EC2 VM that runs three plain **Node/Express** APIs,
the **Postman Insights agent** (so the API Catalog's Runtime Health / observed endpoints / error
rates stay populated 24/7), the async **MQTT notifications** bridge, and the **payment refund
webhook** — all behind Caddy TLS, managed over **AWS SSM** (no public SSH), and deployed by a real
**GitHub Actions CI/CD pipeline**.

> New to the repo? Read [`README.md`](README.md) (overview + how to demo each feature) first, then
> follow this to build your own copy.

## What you'll build

```
        GitHub Actions (CI/CD)                         AWS EC2 (t4g.small, always-on, eu-central-1)
   PR:   spec lint + QA vs fresh code                  ┌──────────────────────────────────────────────┐
   push: + sync to Postman Cloud                       │ Caddy :443 (TLS, one host <eip>.nip.io)        │
         + deploy ──OIDC──▶ SSM ──▶ git pull ─────────▶│   └ path-routed → node×3 (127.0.0.1:8787/8/9)  │
                                                        │        Orders / Payments / Users (Express)     │
   Postman Cloud  ◀── workspace push (git = truth)      │   Insights agent ── observes loopback ──▶ Runtime Health
                                                        │   mqtt-bridge ◀── broker.hivemq.com:1883 ◀── Postman
   Postman webhooks ◀── payment.refunded / notification.processed
                                                        └──────────────────────────────────────────────┘
```

**Base URL** ends up as `https://<elastic-ip-with-dashes>.nip.io` → `/orders`, `/payments`,
`/users`, `/health`.

## Prerequisites

- **Postman Enterprise** with **API Catalog + Insights** enabled, and a **Postman API key** (Account
  → API keys → `PMAK-…`). This one key is used by the Insights agent *and* the CI.
- An **AWS account** where you can create EC2 / IAM / SSM resources, and the **`aws` CLI** logged in
  (`aws sso login`, or static creds). `AWS_REGION=eu-central-1` throughout.
- A **GitHub repo** (fork of this one) if you want the CI/CD pipeline.
- Local: `git`, Node 20+, `bash` (for `control.sh`).

## Roadmap (do these in order)

1. **Postman side** — get the workspace connected and grab the three IDs the VM needs.
2. **Provision the VM** — EC2 `t4g.small` + Elastic IP.
3. **Enable SSM** — IAM instance profile + the API key as a SecureString, so you can manage it
   without SSH.
4. **Deploy** — one SSM bootstrap that clones the repo, writes `runtime-vm/.env`, runs
   `deploy-runtime.sh`.
5. **Keep Runtime Health fresh** — install the 1-minute synthetic-traffic cron.
6. **CI/CD (optional)** — GitHub OIDC role so pushes auto-deploy.

---

## 1. Postman side

1. **Connect the workspace to git.** This repo *is* the workspace source of truth (Postman "Local"
   view is the git files under `postman/` + the root `*.yaml` specs; "Cloud" view is the published
   mirror). Import/clone it into a Postman workspace, or point a new git-connected workspace at your
   fork. `.postman/resources.yaml` maps every local file to its Cloud object id.
2. **Note the collection + environment IDs.** The CI references a couple by id (Orders QA collection,
   Orders Production AWS environment) — you'll find yours in `.postman/resources.yaml` after the
   first sync, and you'll paste them into `.github/workflows/ci-cd.yml` in step 6.
3. **Create the Insights "system".** In the API Catalog, open the API whose Runtime Health you want
   to populate → the **"No endpoint data available"** / **Insights** panel gives you the exact
   `apidump` command, including **`--workspace-id`** and **`--system-env`**. Copy those two UUIDs —
   they become `INSIGHTS_WORKSPACE_ID` and `INSIGHTS_SYSTEM_ENV` in `runtime-vm/.env`.
4. **(For the webhook demos) create Postman webhooks.** Create a webhook URL for refunds and one for
   notifications (`…webhook.pstmn.io`). These become `REFUND_WEBHOOK_URL` and
   `NOTIFICATION_WEBHOOK_URL`.

You now have: `POSTMAN_API_KEY`, `INSIGHTS_WORKSPACE_ID`, `INSIGHTS_SYSTEM_ENV`, and (optional) two
webhook URLs.

---

## 2. Provision the VM (one-time, from your laptop)

**Why these choices:** EC2 **`t4g.small`** (ARM, 2 GB) in **eu-central-1**, always-on for continuous
Insights — the agent has to be running to observe traffic, so this isn't a start/stop workload.
Budget **≈ $16–19/month**; full breakdown in [Cost](#cost) below. `t4g.micro` (1 GB, +swap) trims
~$6/mo if you want to run leaner.

```bash
export AWS_REGION=eu-central-1 AWS_PAGER=""
AMI=$(aws ec2 describe-images --owners 099720109477 \
  --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-arm64-server-*" "Name=state,Values=available" \
  --query 'reverse(sort_by(Images,&CreationDate))[0].ImageId' --output text)
VPC=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)
SUBNET=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC --query 'Subnets[0].SubnetId' --output text)

# security group (80/443 world-open so the demo URLs are public; 22 stays gated by the subnet NACL)
SG=$(aws ec2 create-security-group --group-name postman-insights-sg --description "postman insights" --vpc-id $VPC --query GroupId --output text)
for p in 22 80 443; do aws ec2 authorize-security-group-ingress --group-id $SG --protocol tcp --port $p --cidr 0.0.0.0/0; done

# launch t4g.small + attach a static Elastic IP (so the nip.io hostname never changes)
IID=$(aws ec2 run-instances --image-id $AMI --instance-type t4g.small \
  --security-group-ids $SG --subnet-id $SUBNET --associate-public-ip-address \
  --block-device-mappings '[{"DeviceName":"/dev/sda1","Ebs":{"VolumeSize":12,"VolumeType":"gp3"}}]' \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=postman-insights-demo}]' \
  --query 'Instances[0].InstanceId' --output text)
aws ec2 wait instance-running --instance-ids $IID
ALLOC=$(aws ec2 allocate-address --domain vpc --query AllocationId --output text)
aws ec2 associate-address --instance-id $IID --allocation-id $ALLOC
EIP=$(aws ec2 describe-addresses --allocation-ids $ALLOC --query 'Addresses[0].PublicIp' --output text)
echo "Instance: $IID   Elastic IP: $EIP   ->   HOSTNAME: ${EIP//./-}.nip.io"
```

Keep `$IID` and the `HOSTNAME` — you'll need them below and in `aws.env`.

> **The enterprise gotcha — no public SSH.** Many orgs' subnets have a **Network ACL that denies
> inbound SSH (22)** except from corporate IPs, while allowing **80/443 from anywhere**. So even
> though the security group opens 22, you can't rely on SSH. We manage the box entirely over **AWS
> SSM** (the agent connects *outbound* — no inbound SSH needed), which matches that posture.
> `control.sh` and the CI both use SSM.

## 3. Enable SSM (management without SSH)

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# IAM role + instance profile (lets the VM's SSM agent register)
aws iam create-role --role-name postman-insights-ssm --assume-role-policy-document \
  '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
aws iam attach-role-policy --role-name postman-insights-ssm --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
aws iam create-instance-profile --instance-profile-name postman-insights-ssm
aws iam add-role-to-instance-profile --instance-profile-name postman-insights-ssm --role-name postman-insights-ssm

# store the Postman API key as a SecureString (kept out of SSM command logs)
aws ssm put-parameter --name /postman-insights/postman-api-key --type SecureString --value 'PMAK-...'
KMS=$(aws kms describe-key --key-id alias/aws/ssm --query KeyMetadata.Arn --output text)
aws iam put-role-policy --role-name postman-insights-ssm --policy-name read-postman-secrets --policy-document \
  "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"ssm:GetParameter\",\"ssm:GetParameters\"],\"Resource\":\"arn:aws:ssm:$AWS_REGION:$ACCOUNT_ID:parameter/postman-insights/*\"},{\"Effect\":\"Allow\",\"Action\":\"kms:Decrypt\",\"Resource\":\"$KMS\"}]}"

aws ec2 associate-iam-instance-profile --instance-id $IID --iam-instance-profile Name=postman-insights-ssm
aws ec2 reboot-instances --instance-ids $IID     # reboot so the SSM agent picks up the role

# wait until it registers (PingStatus == Online):
aws ssm describe-instance-information --query "InstanceInformationList[?InstanceId=='$IID'].PingStatus" --output text
```

## 4. Deploy (via SSM — no SSH)

One SSM command bootstraps the box: clone the repo, fetch the API key from the SecureString, write
`runtime-vm/.env`, and run `deploy-runtime.sh` (installs Node 20 + Caddy + the Insights agent,
writes the systemd units, brings everything up). Fill in the four Postman values first.

```bash
HOSTNAME="${EIP//./-}.nip.io"
read -r -d '' BOOTSTRAP <<EOF
set -e
cd ~
[ -d postman-api-catalog-demo ] || git clone https://github.com/<YOU>/postman-api-catalog-demo.git
cd postman-api-catalog-demo && git pull -q
KEY=\$(aws ssm get-parameter --name /postman-insights/postman-api-key --with-decryption --query Parameter.Value --output text --region $AWS_REGION)
cat > runtime-vm/.env <<ENV
HOSTNAME=$HOSTNAME
POSTMAN_API_KEY=\$KEY
INSIGHTS_WORKSPACE_ID=<from step 1>
INSIGHTS_SYSTEM_ENV=<from step 1>
REFUND_WEBHOOK_URL=<optional refund webhook>
NOTIFICATION_WEBHOOK_URL=<optional notification webhook>
ENV
cd runtime-vm && ./deploy-runtime.sh
EOF

# run it on the VM as the ubuntu user, via SSM
printf '{"commands":["sudo -u ubuntu -H bash -lc %s"]}' "$(printf '%q' "$BOOTSTRAP")" > /tmp/ssm.json
CID=$(aws ssm send-command --instance-ids "$IID" --document-name AWS-RunShellScript \
  --parameters file:///tmp/ssm.json --query Command.CommandId --output text)
# tail the result:
aws ssm get-command-invocation --command-id "$CID" --instance-id "$IID" --query StandardOutputContent --output text
```

What `deploy-runtime.sh` sets up (details in [`runtime-vm/README.md`](runtime-vm/README.md)):
- `api-orders` / `api-payments` / `api-users` — one `node` (Express) service each, on a loopback
  port. `api-payments` gets `REFUND_WEBHOOK_URL` so `POST /payments/refund` fires the
  `payment.refunded` webhook.
- `postman-insights` — the agent (`apidump --workspace-id … --system-env …`) observing loopback
  traffic → Runtime Health.
- `mqtt-bridge` — created only when `NOTIFICATION_WEBHOOK_URL` is set; subscribes to
  `broker.hivemq.com:1883` / `postman-api-catalog-demo/notifications` and forwards
  `notification.processed` to the webhook. Always-on, so nothing runs on a laptop.
- **Caddy** — TLS (Let's Encrypt) on `$HOSTNAME`, path-routes `/orders`, `/payments`, `/users`.
- **Webhook DNS pin** — Postman's `*.webhook.pstmn.io` has a CNAME with a literal `*` that Linux
  glibc won't follow, so outbound `fetch` (bridge + refund) would silently fail. The deploy resolves
  each webhook host via `resolvectl` and pins it in `/etc/hosts`. (No pin needed on macOS/Cloudflare —
  this is Linux-glibc-specific.)

The Elastic IP is static, so `<eip>.nip.io` is stable — **no DuckDNS / dynamic DNS needed**.

## 5. Keep Runtime Health fresh (synthetic traffic)

Insights only reports endpoints it *observes*, so availability / p95 / error-rate need a steady
trickle. Install the 1-minute cron on the VM (via SSM):

```bash
aws ssm send-command --instance-ids "$IID" --document-name AWS-RunShellScript \
  --parameters '{"commands":["sudo -u ubuntu bash -lc \"( crontab -l 2>/dev/null; echo \\\"* * * * * /home/ubuntu/postman-api-catalog-demo/runtime-vm/synthetic-traffic.sh >/dev/null 2>&1\\\" ) | crontab -\""]}'
```

`synthetic-traffic.sh` hits all three APIs (and their `?delay=`/`?status=` demo hooks) so the
dashboard shows meaningful, non-flat data. Runtime Health reflects roughly the last 7 days; allow
5–8 minutes after first traffic for it to populate.

## 6. CI/CD (optional but recommended)

`.github/workflows/ci-cd.yml` gives you the full pipeline: on **every PR and push** it runs spec
lint + a **QA** run + a **performance load-test** (`postman performance run`, `--pass-if p99<2000`),
all against the *freshly-built* code in the runner (a breaking or slow change fails here and can't
merge). On **push to main** it then runs `postman workspace push` (git → Postman Cloud) and
**deploys to AWS via SSM**, followed by a post-deploy smoke test. Deploy runs only if the QA and
performance gates pass. AWS auth is **keyless via GitHub OIDC** — no stored AWS keys; the only repo
secret is `POSTMAN_API_KEY`.

**a. Create the GitHub OIDC provider + deploy role** (once per AWS account):

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REPO="<YOU>/postman-api-catalog-demo"     # your fork

# OIDC provider for GitHub Actions (skip if it already exists)
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1

# role the workflow assumes — trust only pushes to main of your repo
aws iam create-role --role-name github-actions-postman-deploy --assume-role-policy-document "{
  \"Version\":\"2012-10-17\",
  \"Statement\":[{\"Effect\":\"Allow\",
    \"Principal\":{\"Federated\":\"arn:aws:iam::$ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com\"},
    \"Action\":\"sts:AssumeRoleWithWebIdentity\",
    \"Condition\":{\"StringEquals\":{\"token.actions.githubusercontent.com:aud\":\"sts.amazonaws.com\"},
      \"StringLike\":{\"token.actions.githubusercontent.com:sub\":\"repo:$REPO:ref:refs/heads/main\"}}}]}"

# minimum permissions: send the deploy command via SSM and read its result
aws iam put-role-policy --role-name github-actions-postman-deploy --policy-name ssm-deploy --policy-document "{
  \"Version\":\"2012-10-17\",
  \"Statement\":[
    {\"Effect\":\"Allow\",\"Action\":\"ssm:SendCommand\",
     \"Resource\":[\"arn:aws:ssm:$AWS_REGION::document/AWS-RunShellScript\",
                   \"arn:aws:ec2:$AWS_REGION:$ACCOUNT_ID:instance/$IID\"]},
    {\"Effect\":\"Allow\",\"Action\":[\"ssm:GetCommandInvocation\"],\"Resource\":\"*\"}]}"
echo "DEPLOY_ROLE=arn:aws:iam::$ACCOUNT_ID:role/github-actions-postman-deploy"
```

**b. Add the repo secret:** `POSTMAN_API_KEY` (Settings → Secrets and variables → Actions).

**c. Edit the `env:` block at the top of `.github/workflows/ci-cd.yml`** to your values:
`AWS_REGION`, `AWS_INSTANCE_ID` (`$IID`), `AWS_HOST` (`$HOSTNAME`), `DEPLOY_ROLE` (printed above),
and the Postman ids `ORDERS_QA_COLLECTION`, `ORDERS_PERF_COLLECTION`, and `AWS_ORDERS_ENV` (from
`.postman/resources.yaml`).

That's it — push to `main` and the pipeline lints, tests the new code, syncs the workspace, and
redeploys the VM.

---

## Cost

Rough **eu-central-1** on-demand pricing for the always-on setup. The bill is dominated by two
unavoidable always-on line items: the instance and the public IPv4 address.

| Item | Spec | ~Monthly |
|------|------|----------|
| EC2 instance | `t4g.small` (ARM, 2 vCPU / 2 GB), on-demand, 24×7 | **~$12–14** |
| Public IPv4 | 1 address, $0.005/hr (billed whether the instance is running **or stopped**) | **~$3.60** |
| EBS storage | 12 GB gp3 root volume | **~$1** |
| Data transfer out | demo + synthetic traffic is tiny; first 100 GB/mo is free | **~$0** |
| **Total** | | **≈ $16–19/mo** |

**Free / already-covered (no AWS charge):**
- **AWS SSM** (management), **Let's Encrypt** (TLS), **nip.io** (DNS), **HiveMQ** public broker (MQTT).
- **GitHub Actions** CI/CD — free for public repos; 2,000 min/mo free on private.
- **Postman** (Enterprise + Insights) and the **Insights agent** are licensed separately, not an AWS cost.

**Ways to trim:**
- `t4g.micro` (1 GB) instead of `t4g.small` → ~**-$6/mo** (the deploy adds a 2 GB swapfile so it fits).
- A 1-year **Compute Savings Plan / Reserved Instance** cuts the instance ~30–40% if this becomes permanent.
- **Don't** stop the instance to save money: a stopped instance still bills the Elastic IP *and* the
  EBS volume, and Insights stops observing — so you lose the whole point for almost no saving. Always-on
  is the intended (and cheapest-useful) mode. To actually stop paying, terminate the instance and
  **release the Elastic IP** (an *allocated but unassociated* EIP is also billed).

## Manage it

```bash
cp runtime-vm/aws/aws.env.example runtime-vm/aws/aws.env   # set AWS_INSTANCE_ID + AWS_HOST (+ region)
./control.sh status     # EC2 state + per-service health + /health code (via SSM)
./control.sh reset      # restart all services on the VM
./control.sh urls       # print the 3 API URLs
./control.sh start|stop # start/stop the instance (meant to be always-on)
```

Or straight from the VM shell (over SSM): `systemctl status api-orders postman-insights caddy`,
`journalctl -u postman-insights -f`, `journalctl -u mqtt-bridge -f`.

## Security

- **No public SSH** (NACL-enforced); all management via SSM (agent dials out).
- API key stored as an SSM **SecureString**; on the VM it lives in `runtime-vm/.env` (root-readable
  only). Nothing secret is committed — `runtime-vm/.env` and `runtime-vm/aws/aws.env` (non-secret
  instance id / host) are git-ignored.
- CI uses **GitHub OIDC** — no long-lived AWS keys in GitHub; the role trusts only pushes to `main`
  of your repo.
- If the key was ever shared in plaintext, **rotate it**: update it in Postman, the repo secret, and
  the SSM SecureString (`aws ssm put-parameter … --overwrite`), then `./control.sh reset`.
