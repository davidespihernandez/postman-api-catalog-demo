#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$ROOT_DIR/scripts/lib.sh"

usage() {
  cat <<'EOF'
Postman API Catalog demo helper

Usage:
  ./demo.sh setup              One-time Cloudflare login + npm install
  ./demo.sh reset              Remove payments worker; keep orders baseline
  ./demo.sh baseline           Deploy orders + smoke test (State 1)
  ./demo.sh add <api>          Deploy API (e.g. payments) + smoke test (State 2)
  ./demo.sh register <api|all> Register service(s) in API Catalog
  ./demo.sh traffic-demo       Mixed OK/slow/error traffic for catalog metrics
  ./demo.sh smoke [api|all]    Health checks
  ./demo.sh urls               Print deployed worker URLs

Demo flow:
  ./demo.sh reset && ./demo.sh baseline && ./demo.sh register orders
  # show Orders in API Catalog
  ./demo.sh add payments && ./demo.sh register payments
  ./demo.sh traffic-demo
  # run Postman collection → catalog Test tab metrics
EOF
}

register_api() {
  local api="$1"
  node "$ROOT_DIR/scripts/register-api.mjs" "$api"
}

register_all() {
  local api
  for api in orders payments users; do
    register_api "$api" || true
  done
}

cmd="${1:-}"
arg="${2:-}"

load_env

case "$cmd" in
  setup)
    require_cmd npm
    require_cmd curl
    (cd "$ROOT_DIR" && npm install)
    ensure_wrangler
    log "Log in to Cloudflare (browser opens once):"
    "$(wrangler_bin)" login
    log "Setup complete. Copy .env.example to .env and set POSTMAN_API_KEY."
    ;;
  reset)
    "$ROOT_DIR/scripts/undeploy-api.sh" payments || true
    log "Reset complete. Run ./demo.sh baseline before the demo."
    log "Remove stale Payments entries in API Catalog → Service discovery if needed."
    ;;
  baseline)
    "$ROOT_DIR/scripts/deploy-api.sh" orders
    "$ROOT_DIR/scripts/smoke-test.sh" orders
    log "State 1 ready: Orders API only."
    log "Run ./demo.sh register orders, then integrate in API Catalog."
    ;;
  add)
    [[ -n "$arg" ]] || die "Usage: ./demo.sh add <payments|users>"
    "$ROOT_DIR/scripts/deploy-api.sh" "$arg"
    "$ROOT_DIR/scripts/smoke-test.sh" "$arg"
    log "Deployed $arg. Run ./demo.sh register $arg and integrate in API Catalog."
    ;;
  register)
    [[ -n "$arg" ]] || die "Usage: ./demo.sh register <orders|payments|users|all>"
    if [[ "$arg" == "all" ]]; then
      register_all
    else
      register_api "$arg"
    fi
    ;;
  traffic-demo)
    "$ROOT_DIR/scripts/traffic-demo.sh"
    ;;
  smoke)
    "$ROOT_DIR/scripts/smoke-test.sh" "${arg:-all}"
    ;;
  urls)
    if [[ -f "$(state_file)" ]]; then
      cat "$(state_file)"
    else
      log "No .demo-state.env yet. Run ./demo.sh baseline first."
    fi
    ;;
  -h | --help | help | "")
    usage
    ;;
  *)
    die "Unknown command: $cmd (run ./demo.sh --help)"
    ;;
esac
