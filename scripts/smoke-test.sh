#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/lib.sh"

TARGET="${1:-all}"
load_env

if [[ -f "$(state_file)" ]]; then
  # shellcheck disable=SC1091
  set -a
  source "$(state_file)"
  set +a
fi

check_api() {
  local api="$1"
  local key url health_code
  key="$(echo "$api" | tr '[:lower:]' '[:upper:]')_API_URL"
  url="${!key:-}"

  if [[ -z "$url" ]]; then
    log "SKIP $api — ${key} not set (deploy first or add to .demo-state.env)"
    return 0
  fi

  health_code="$(curl -sS -o /tmp/demo-smoke.json -w '%{http_code}' "${url}/health")"
  if [[ "$health_code" != "200" ]]; then
    die "$api health failed at ${url}/health (HTTP $health_code)"
  fi

  log "OK $api → ${url}/health"
  cat /tmp/demo-smoke.json
  printf '\n'
}

case "$TARGET" in
  orders) check_api orders ;;
  payments) check_api payments ;;
  users) check_api users ;;
  all)
    check_api orders
    check_api payments
    check_api users
    ;;
  *)
    die "Usage: $0 [orders|payments|users|all]"
    ;;
esac

