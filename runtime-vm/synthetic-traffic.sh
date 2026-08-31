#!/usr/bin/env bash
# Synthetic traffic generator — keeps Runtime Health populated between demos.
#
# The Insights agent only reports endpoints it observes, so availability / p95 /
# error-rate need a steady trickle. This exercises all three APIs (single host,
# path-routed) including the demo hooks (?delay= for latency, ?status= for errors)
# so the dashboard shows meaningful, non-flat data.
#
# Usage (on the VM):
#   ./synthetic-traffic.sh                    # one pass, reads HOSTNAME from .env
#   HOSTNAME=18-157-170-15.nip.io ./synthetic-traffic.sh
#
# Run every minute via cron (`crontab -e`):
#   * * * * * /home/ubuntu/postman-api-catalog-demo/runtime-vm/synthetic-traffic.sh >/dev/null 2>&1
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[ -f "$SCRIPT_DIR/.env" ] && { set -a; source "$SCRIPT_DIR/.env"; set +a; }
: "${HOSTNAME:?set HOSTNAME (in .env or env)}"
BASE="https://$HOSTNAME"

hit() { curl -s -o /dev/null -w "%{http_code} %{time_total}s  $2 $1\n" -X "$2" "$1" "${@:3}"; }

# Healthy baseline traffic (the majority) ----------------------------------
for _ in 1 2 3; do
  hit "$BASE/orders"   GET
  hit "$BASE/payments" GET
  hit "$BASE/users"    GET
done
hit "$BASE/health"          GET
hit "$BASE/orders/ord-001"  GET
hit "$BASE/users"           POST -H 'content-type: application/json' -d '{"name":"Synthetic User","email":"syn@example.com"}'

# A little latency (feeds p95 / latency-regression) ------------------------
hit "$BASE/orders?delay=250"    GET
hit "$BASE/payments?delay=600"  GET

# A few errors (feeds 4xx / server-error metrics) — a small minority -------
hit "$BASE/orders/does-not-exist" GET      # natural 404
hit "$BASE/payments?status=503"   GET      # simulated 5xx

echo "--- synthetic pass done: $(date '+%F %T') ---"
