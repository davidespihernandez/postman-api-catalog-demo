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
WRANGLER="$(wrangler_bin)"

log "Removing $(api_name "$API") worker..."
if "$WRANGLER" delete --config "$CONFIG" --force 2>/dev/null; then
  log "Worker deleted."
else
  log "Worker was not deployed (or already removed)."
fi

KEY="$(echo "$API" | tr '[:lower:]' '[:upper:]')_API_URL"
if [[ -f "$(state_file)" ]]; then
  sed -i '' "/^${KEY}=/d" "$(state_file)" 2>/dev/null || true
fi
