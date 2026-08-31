#!/usr/bin/env bash
# =============================================================================
#  control.sh — manage the self-hosted AWS demo backend (always-on EC2 via SSM)
# =============================================================================
#   Usage:  ./control.sh <action>
#     status : EC2 state + service health (api x3, insights agent, mqtt bridge, caddy)
#     reset  : restart all services on the VM
#     start  : start the instance (it is meant to be always-on)
#     stop   : stop the instance (note: EC2 still bills the Elastic IP while stopped)
#     urls   : print the API URLs
#
#   Settings: runtime-vm/aws/aws.env (copy from aws.env.example).
#   Management is via AWS SSM (the subnet NACL blocks public SSH), so you need the
#   aws CLI + an active SSO session. Secrets live only on the VM (runtime-vm/.env).
# =============================================================================
set -euo pipefail
export PATH="/usr/bin:/bin:/usr/sbin:/sbin:/opt/homebrew/bin:$PATH"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AWS_ENV="$ROOT/runtime-vm/aws/aws.env"

usage() { sed -n '3,15p' "$0" | sed 's/^# \{0,1\}//'; }

load_aws() {
  [ -f "$AWS_ENV" ] || { echo "ERROR: $AWS_ENV not found (cp aws.env.example aws.env)"; exit 1; }
  set -a; source "$AWS_ENV"; set +a
  : "${AWS_REGION:=eu-central-1}"; : "${AWS_INSTANCE_ID:?set AWS_INSTANCE_ID in aws.env}"; : "${AWS_HOST:=}"
  export AWS_REGION AWS_PAGER=""
}
assm() {  # run script "$1" on the instance via SSM (as ubuntu); echo stdout
  local b64 tmp cid s i
  b64=$(printf '%s' "$1" | base64 | tr -d '\n')
  tmp=$(mktemp)
  printf '{"commands":["echo %s | base64 --decode > /tmp/_ctl.sh","sudo -u ubuntu -H bash /tmp/_ctl.sh"]}' "$b64" > "$tmp"
  cid=$(aws ssm send-command --instance-ids "$AWS_INSTANCE_ID" --document-name AWS-RunShellScript --parameters "file://$tmp" --query 'Command.CommandId' --output text)
  for i in $(seq 1 30); do s=$(aws ssm get-command-invocation --command-id "$cid" --instance-id "$AWS_INSTANCE_ID" --query Status --output text 2>/dev/null); [ "$s" != "InProgress" ] && [ "$s" != "Pending" ] && break; sleep 5; done
  aws ssm get-command-invocation --command-id "$cid" --instance-id "$AWS_INSTANCE_ID" --query StandardOutputContent --output text
  rm -f "$tmp"
}
estate() { aws ec2 describe-instances --instance-ids "$AWS_INSTANCE_ID" --query 'Reservations[0].Instances[0].State.Name' --output text 2>/dev/null; }

do_status() {
  load_aws; local st; st="$(estate)"; echo "AWS EC2 ($AWS_INSTANCE_ID): ${st:-NOT FOUND}"
  [ "$st" = "running" ] || { echo "  (not running — run: ./control.sh start)"; return 0; }
  assm 'for s in api-orders api-payments api-users postman-insights mqtt-bridge caddy; do printf "  %-20s %s\n" "$s" "$(systemctl is-active "$s")"; done; printf "  health: %s https://'"$AWS_HOST"'/health\n" "$(curl -s -m10 -o /dev/null -w "%{http_code}" https://'"$AWS_HOST"'/health)"'
}
do_reset() { load_aws; assm 'sudo systemctl restart api-orders api-payments api-users postman-insights mqtt-bridge caddy; sleep 5; for s in api-orders api-payments api-users postman-insights mqtt-bridge caddy; do printf "%-20s %s\n" "$s" "$(systemctl is-active "$s")"; done'; }
do_start() { load_aws; echo "starting $AWS_INSTANCE_ID..."; aws ec2 start-instances --instance-ids "$AWS_INSTANCE_ID" --query 'StartingInstances[0].CurrentState.Name' --output text; }
do_stop()  { load_aws; echo "NOTE: this backend is meant to be always-on; a stopped instance still bills the Elastic IP."; aws ec2 stop-instances --instance-ids "$AWS_INSTANCE_ID" --query 'StoppingInstances[0].CurrentState.Name' --output text; }
do_urls()  { load_aws; for p in orders payments users; do echo "https://$AWS_HOST/$p"; done; }

case "${1:-}" in
  status) do_status ;;
  reset)  do_reset ;;
  start)  do_start ;;
  stop)   do_stop ;;
  urls)   do_urls ;;
  *) usage; exit 1 ;;
esac
