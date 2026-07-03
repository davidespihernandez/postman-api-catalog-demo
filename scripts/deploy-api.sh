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
ensure_wrangler_auth

CONFIG="$(wrangler_config "$API")"
NAME="$(worker_name "$API")"
WRANGLER="$(wrangler_bin)"

log "Deploying $(api_name "$API") ($NAME)..."

DEPLOY_LOG="$(mktemp)"
trap 'rm -f "$DEPLOY_LOG"' EXIT

set +e
"$WRANGLER" deploy --config "$CONFIG" >"$DEPLOY_LOG" 2>&1
DEPLOY_STATUS=$?
set -e

cat "$DEPLOY_LOG"

if [[ "$DEPLOY_STATUS" -ne 0 ]]; then
  if grep -Eqi 'register a workers\.dev subdomain' "$DEPLOY_LOG"; then
    wrangler_hint_workers_subdomain "$DEPLOY_LOG"
  fi
  if grep -Eqi 'not authenticated|log in|login|oauth token' "$DEPLOY_LOG"; then
    die "Wrangler is not logged in. Run: ./demo.sh setup"
  fi
  die "wrangler deploy failed for $NAME (exit $DEPLOY_STATUS). See output above."
fi

if ! URL="$(extract_worker_url "$DEPLOY_LOG" "$NAME")"; then
  die "Could not detect worker URL from wrangler output. Set $(echo "$API" | tr '[:lower:]' '[:upper:]')_API_URL in .env manually."
fi

KEY="$(echo "$API" | tr '[:lower:]' '[:upper:]')_API_URL"
write_state "$KEY" "$URL"
log "Saved $KEY=$URL"

wait_for_health "$URL" "$(api_name "$API")"
log "Health check OK: ${URL}/health"
cat /tmp/demo-health.json
printf '\n'
