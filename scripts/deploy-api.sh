#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/lib.sh"

API="${1:-}"
if [[ -z "$API" ]]; then
  die "Usage: $0 <orders|payments|users>"
fi

load_env
ensure_wrangler

CONFIG="$(wrangler_config "$API")"
NAME="$(worker_name "$API")"
WRANGLER="$(wrangler_bin)"

log "Deploying $(api_name "$API") ($NAME)..."

DEPLOY_OUTPUT="$("$WRANGLER" deploy --config "$CONFIG" 2>&1)"
printf '%s\n' "$DEPLOY_OUTPUT"

URL="$(printf '%s\n' "$DEPLOY_OUTPUT" | sed -n 's/.*https:\/\/[a-zA-Z0-9.-]*workers\.dev.*/\0/p' | tail -n1)"
if [[ -z "$URL" ]]; then
  URL="$(printf '%s\n' "$DEPLOY_OUTPUT" | grep -Eo 'https://[a-zA-Z0-9.-]+\.workers\.dev' | tail -n1 || true)"
fi

if [[ -z "$URL" ]]; then
  die "Could not detect worker URL from wrangler output. Set $(echo "$API" | tr '[:lower:]' '[:upper:]')_API_URL in .env manually."
fi

KEY="$(echo "$API" | tr '[:lower:]' '[:upper:]')_API_URL"
write_state "$KEY" "$URL"
log "Saved $KEY=$URL"

HEALTH_URL="${URL}/health"
HTTP_CODE="$(curl -sS -o /tmp/demo-health.json -w '%{http_code}' "$HEALTH_URL")"
if [[ "$HTTP_CODE" != "200" ]]; then
  die "Health check failed for $HEALTH_URL (HTTP $HTTP_CODE)"
fi

log "Health check OK: $HEALTH_URL"
cat /tmp/demo-health.json
printf '\n'
