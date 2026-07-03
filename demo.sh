#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$ROOT_DIR/scripts/lib.sh"

usage() {
  cat <<'EOF'
Postman API Catalog demo — Cloudflare deploy helper

Usage:
  ./demo.sh setup              One-time Cloudflare login + npm install
  ./demo.sh setup-subdomain    One-time workers.dev subdomain (interactive)
  ./demo.sh deploy             Deploy all APIs (orders, payments, users) + smoke test
  ./demo.sh reset              Same as deploy (redeploy all workers)
  ./demo.sh add <api>          Deploy a single API + smoke test
  ./demo.sh smoke [api|all]    Health checks
  ./demo.sh urls               Print deployed worker URLs

Deploy runs during SE setup — not during the customer demo.
Catalog steps: see SE-INSTALL.md and DEMO-STEPS.md (Postman app only).
EOF
}

deploy_all() {
  ensure_wrangler_auth
  for api in orders payments users; do
    "$ROOT_DIR/scripts/deploy-api.sh" "$api"
  done
  "$ROOT_DIR/scripts/smoke-test.sh" all
  log "All APIs deployed. Paste ./demo.sh urls into Postman environment."
}

setup_subdomain() {
  ensure_wrangler_auth
  log "One-time: register your *.workers.dev subdomain."
  log "When wrangler asks, answer yes to register a subdomain."
  "$(wrangler_bin)" deploy --config "$(wrangler_config orders)"
  log "Subdomain registered (or already set)."
  log "DNS/SSL may take a few minutes — then run ./demo.sh deploy"
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
    if [[ -n "${CLOUDFLARE_API_TOKEN:-}" ]]; then
      log "Using CLOUDFLARE_API_TOKEN from .env (skipping browser login)"
      ensure_wrangler_auth
    else
      log "Log in to Cloudflare (browser opens once):"
      "$(wrangler_bin)" login
      ensure_wrangler_auth
    fi
    log "Setup complete. Run ./demo.sh setup-subdomain then ./demo.sh deploy (see SE-INSTALL.md)."
    ;;
  setup-subdomain)
    setup_subdomain
    ;;
  deploy | reset)
    deploy_all
    ;;
  baseline)
    deploy_all
    log "Note: 'baseline' is deprecated — use './demo.sh deploy'."
    ;;
  add)
    [[ -n "$arg" ]] || die "Usage: ./demo.sh add <orders|payments|users>"
    "$ROOT_DIR/scripts/deploy-api.sh" "$arg"
    "$ROOT_DIR/scripts/smoke-test.sh" "$arg"
    log "Deployed $arg."
    ;;
  smoke)
    "$ROOT_DIR/scripts/smoke-test.sh" "${arg:-all}"
    ;;
  urls)
    if [[ -f "$(state_file)" ]]; then
      cat "$(state_file)"
    else
      log "No .demo-state.env yet. Run ./demo.sh deploy first."
    fi
    ;;
  -h | --help | help | "")
    usage
    ;;
  *)
    die "Unknown command: $cmd (run ./demo.sh --help)"
    ;;
esac
