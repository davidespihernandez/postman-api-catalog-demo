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
  local key url
  key="$(echo "$api" | tr '[:lower:]' '[:upper:]')_API_URL"
  url="${!key:-}"

  if [[ -z "$url" ]]; then
    log "SKIP $api — ${key} not set (deploy first or add to .demo-state.env)"
    return 1
  fi

  wait_for_health "$url" "$api"
  log "OK $api → ${url}/health"
  cat /tmp/demo-health.json
  printf '\n'
}

report_partial_deploy() {
  local missing=()
  local api key
  for api in orders payments users; do
    key="$(echo "$api" | tr '[:lower:]' '[:upper:]')_API_URL"
    if [[ -z "${!key:-}" ]]; then
      missing+=("$api")
    fi
  done
  if ((${#missing[@]} > 0 && ${#missing[@]} < 3)); then
    log "Partial deploy — missing: ${missing[*]}. Run ./demo.sh deploy to finish."
  fi
}

run_all_smoke() {
  local had_any=0 had_skip=0
  local api
  for api in orders payments users; do
    if check_api "$api"; then
      had_any=1
    else
      had_skip=1
    fi
  done
  if [[ "$had_any" -eq 1 && "$had_skip" -eq 1 ]]; then
    report_partial_deploy
  fi
  if [[ "$had_any" -eq 0 ]]; then
    die "No APIs deployed. Run ./demo.sh deploy first."
  fi
}

case "$TARGET" in
  orders) check_api orders ;;
  payments) check_api payments ;;
  users) check_api users ;;
  all) run_all_smoke ;;
  *)
    die "Usage: $0 [orders|payments|users|all]"
    ;;
esac

