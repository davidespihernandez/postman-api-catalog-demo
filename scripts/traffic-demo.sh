#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/lib.sh"

load_env

if [[ -f "$(state_file)" ]]; then
  # shellcheck disable=SC1091
  set -a
  source "$(state_file)"
  set +a
fi

hit() {
  local label="$1"
  local url="$2"
  local code
  code="$(curl -sS -o /dev/null -w '%{http_code}' "$url")"
  log "$label → HTTP $code ($url)"
}

path_for_api() {
  case "$1" in
    orders) echo "orders" ;;
    payments) echo "payments" ;;
    users) echo "users" ;;
  esac
}

send_traffic() {
  local api="$1"
  local key url path
  key="$(echo "$api" | tr '[:lower:]' '[:upper:]')_API_URL"
  url="${!key:-}"
  path="$(path_for_api "$api")"

  if [[ -z "$url" ]]; then
    log "SKIP $api — ${key} not set"
    return 0
  fi

  log "Traffic for $(api_name "$api") at $url"
  hit "OK list" "${url}/${path}"
  hit "Slow (2s)" "${url}/${path}?delay=2000"
  hit "Simulated 500" "${url}/${path}?error=500"
  hit "Health" "${url}/health"
  printf '\n'
}

send_traffic orders
send_traffic payments
send_traffic users

log "Done. Run the Postman collection or a monitor against these URLs."
log "Open API Catalog → integrated service → Test tab to see collection/monitor metrics."
