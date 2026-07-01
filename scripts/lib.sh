#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

log() {
  printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

die() {
  printf 'Error: %s\n' "$*" >&2
  exit 1
}

load_env() {
  if [[ -f "$ROOT_DIR/.env" ]]; then
    # shellcheck disable=SC1091
    set -a
    source "$ROOT_DIR/.env"
    set +a
  fi
}

require_cmd() {
  local cmd="$1"
  command -v "$cmd" >/dev/null 2>&1 || die "Missing required command: $cmd"
}

api_name() {
  case "$1" in
    orders) echo "Orders API" ;;
    payments) echo "Payments API" ;;
    users) echo "Users API" ;;
    *) die "Unknown API: $1 (expected orders, payments, or users)" ;;
  esac
}

worker_name() {
  case "$1" in
    orders) echo "postman-api-catalog-demo-orders" ;;
    payments) echo "postman-api-catalog-demo-payments" ;;
    users) echo "postman-api-catalog-demo-users" ;;
    *) die "Unknown API: $1" ;;
  esac
}

wrangler_config() {
  echo "$ROOT_DIR/apis/$1/wrangler.toml"
}

state_file() {
  echo "$ROOT_DIR/.demo-state.env"
}

write_state() {
  local key="$1"
  local value="$2"
  local file
  file="$(state_file)"
  touch "$file"
  if grep -q "^${key}=" "$file" 2>/dev/null; then
    sed -i '' "s|^${key}=.*|${key}=${value}|" "$file"
  else
    printf '%s=%s\n' "$key" "$value" >>"$file"
  fi
}

read_state() {
  local key="$1"
  local file
  file="$(state_file)"
  if [[ ! -f "$file" ]]; then
    return 1
  fi
  grep "^${key}=" "$file" | tail -n1 | cut -d= -f2-
}

ensure_wrangler() {
  require_cmd npm
  if [[ ! -d "$ROOT_DIR/node_modules/wrangler" ]]; then
    log "Installing wrangler (first run)..."
    (cd "$ROOT_DIR" && npm install)
  fi
}

wrangler_bin() {
  echo "$ROOT_DIR/node_modules/.bin/wrangler"
}
