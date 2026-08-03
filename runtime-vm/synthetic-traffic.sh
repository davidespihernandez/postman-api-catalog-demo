#!/usr/bin/env bash
# Synthetic traffic generator — keeps Runtime Health populated between demos.
#
# The Insights agent only reports metrics for endpoints it actually observes, so
# availability / p95 latency / error-rate need a steady trickle of requests. This
# script exercises all three APIs with a realistic mix, including the built-in
# demo hooks (?delay= for latency, ?status= for errors) so the dashboard shows
# meaningful, non-flat data.
#
# Usage:
#   ./synthetic-traffic.sh                 # one pass, reads HOSTNAME from .env
#   HOSTNAME=1-2-3-4.nip.io ./synthetic-traffic.sh
#
# Run every minute via cron (edit `crontab -e`):
#   * * * * * /home/ubuntu/postman-api-catalog-demo/runtime-vm/synthetic-traffic.sh >/dev/null 2>&1
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[ -f "$SCRIPT_DIR/.env" ] && { set -a; source "$SCRIPT_DIR/.env"; set +a; }
: "${HOSTNAME:?set HOSTNAME (in .env or env)}"

ORDERS="https://orders.$HOSTNAME"
PAYMENTS="https://payments.$HOSTNAME"
USERS="https://users.$HOSTNAME"

hit() { curl -s -o /dev/null -w "%{http_code} %{time_total}s  $2 $1\n" -X "$2" "$1" "${@:3}"; }

# Healthy baseline traffic (majority) --------------------------------------
for _ in 1 2 3; do
  hit "$ORDERS/orders"    GET
  hit "$PAYMENTS/payments" GET
  hit "$USERS/users"       GET
done
hit "$ORDERS/health"   GET
hit "$PAYMENTS/health" GET
hit "$USERS/health"    GET
hit "$ORDERS/orders/ord-001" GET
hit "$USERS/users"     POST -H 'content-type: application/json' -d '{"name":"Synthetic User","email":"syn@example.com"}'

# A little latency (feeds p95 / latency-regression) ------------------------
hit "$ORDERS/orders?delay=250"    GET
hit "$PAYMENTS/payments?delay=600" GET

# A few errors (feeds 4xx / server-error metrics) — kept a small minority ---
hit "$ORDERS/orders/does-not-exist" GET          # natural 404
hit "$PAYMENTS/payments?status=503" GET          # simulated 5xx

echo "--- synthetic pass done: $(date '+%F %T') ---"
