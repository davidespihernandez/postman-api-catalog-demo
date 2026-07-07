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

openapi_spec_path() {
  case "$1" in
    orders) echo "$ROOT_DIR/orders.yaml" ;;
    payments) echo "$ROOT_DIR/payments.yaml" ;;
    users) echo "$ROOT_DIR/users.yaml" ;;
    *) die "Unknown API: $1" ;;
  esac
}

stage_openapi_for_deploy() {
  local api="$1"
  local src dest
  src="$(openapi_spec_path "$api")"
  dest="$ROOT_DIR/apis/$api/openapi.json"
  [[ -f "$src" ]] || die "OpenAPI spec not found: $src (expected repo-root ${api}.yaml)"
  cp "$src" "$dest"
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

ensure_wrangler_auth() {
  ensure_wrangler
  local wrangler whoami_log whoami_status
  wrangler="$(wrangler_bin)"
  whoami_log="$(mktemp)"
  set +e
  "$wrangler" whoami >"$whoami_log" 2>&1
  whoami_status=$?
  set -e
  if [[ "$whoami_status" -ne 0 ]]; then
    cat "$whoami_log" >&2
    if grep -Eqi '403 Forbidden|malformed response from the API' "$whoami_log"; then
      rm -f "$whoami_log"
      wrangler_hint_cloudflare_403
    elif [[ -n "${CLOUDFLARE_API_TOKEN:-}" ]]; then
      rm -f "$whoami_log"
      die "CLOUDFLARE_API_TOKEN is set but wrangler whoami failed. Check token permissions and CLOUDFLARE_ACCOUNT_ID in .env."
    else
      rm -f "$whoami_log"
      die "Not logged in to Cloudflare. Run: ./demo.sh setup"
    fi
  fi
  rm -f "$whoami_log"
}

wrangler_hint_cloudflare_403() {
  cat >&2 <<'EOF'
Error: Cloudflare API returned 403 Forbidden (GET /user).

This is usually one of:
  1. Expired OAuth session — run: npx wrangler logout && ./demo.sh setup
  2. Corporate proxy/VPN blocking api.cloudflare.com — try a personal hotspot, or use an API token (see .env.example)
  3. Invalid CLOUDFLARE_API_TOKEN in .env — create a new token with the "Edit Cloudflare Workers" template

API token setup (works well on locked-down networks):
  1. https://dash.cloudflare.com/profile/api-tokens → Create Token → Edit Cloudflare Workers
  2. Copy Account ID from the Cloudflare dashboard URL
  3. Add to .env:
       CLOUDFLARE_API_TOKEN=...
       CLOUDFLARE_ACCOUNT_ID=...
  4. npx wrangler logout   # clear stale OAuth if login keeps failing
  5. ./demo.sh deploy
EOF
  exit 1
}

wrangler_hint_workers_subdomain() {
  local log_file="${1:-}"
  local onboarding_url=""
  if [[ -n "$log_file" && -f "$log_file" ]]; then
    onboarding_url="$(grep -Eo 'https://dash.cloudflare.com/[a-f0-9]+/workers/onboarding' "$log_file" | head -n1 || true)"
  fi
  if [[ -z "$onboarding_url" && -n "${CLOUDFLARE_ACCOUNT_ID:-}" ]]; then
    onboarding_url="https://dash.cloudflare.com/${CLOUDFLARE_ACCOUNT_ID}/workers/onboarding"
  fi

  cat >&2 <<EOF
Error: This Cloudflare account has no workers.dev subdomain yet (one-time setup).

Option A — interactive CLI (recommended):
  ./demo.sh setup-subdomain
  Answer yes when wrangler asks to register a subdomain, then run ./demo.sh deploy

Option B — Cloudflare dashboard:
  Workers & Pages → change "Your subdomain" (workers.dev)
EOF
  if [[ -n "$onboarding_url" ]]; then
    printf '  %s\n' "$onboarding_url" >&2
  fi
  exit 1
}

extract_worker_url() {
  local output_file="$1"
  local name="$2"
  local url

  url="$(grep -Eo 'https://[a-zA-Z0-9._-]+\.workers\.dev' "$output_file" | tail -n1 || true)"
  if [[ -n "$url" ]]; then
    printf '%s' "$url"
    return 0
  fi

  url="$(grep -Eo "${name}\\.[a-zA-Z0-9._-]+\\.workers\\.dev" "$output_file" | head -n1 || true)"
  if [[ -n "$url" ]]; then
    printf 'https://%s' "$url"
    return 0
  fi

  return 1
}

wait_for_health() {
  local url="$1"
  local label="${2:-API}"
  local health_url="${url%/}/health"
  local max_attempts="${HEALTH_CHECK_RETRIES:-18}"
  local delay="${HEALTH_CHECK_DELAY_SEC:-10}"
  local attempt http_code curl_status body_file err_file

  require_cmd curl
  body_file="$(mktemp)"
  err_file="$(mktemp)"

  for ((attempt = 1; attempt <= max_attempts; attempt++)); do
    set +e
    http_code="$(curl -sS --connect-timeout 10 -m 15 -o "$body_file" -w '%{http_code}' "$health_url" 2>"$err_file")"
    curl_status=$?
    set -e

    if [[ "$curl_status" -eq 0 && "$http_code" == "200" ]]; then
      cp "$body_file" /tmp/demo-health.json
      rm -f "$body_file" "$err_file"
      if [[ "$attempt" -gt 1 ]]; then
        log "$label health OK after $attempt attempts: $health_url"
      fi
      return 0
    fi

    if [[ "$attempt" -lt "$max_attempts" ]]; then
      if [[ "$curl_status" -ne 0 ]]; then
        log "$label health attempt $attempt/$max_attempts failed (curl exit $curl_status — DNS/SSL may still be propagating). Retrying in ${delay}s..."
      else
        log "$label health attempt $attempt/$max_attempts failed (HTTP $http_code). Retrying in ${delay}s..."
      fi
      sleep "$delay"
    fi
  done

  if [[ -s "$err_file" ]]; then
    cat "$err_file" >&2
  fi
  rm -f "$body_file" "$err_file"
  die "Health check failed for $health_url after $max_attempts attempts (~$((max_attempts * delay))s). New workers.dev subdomains can take a few minutes — wait, then run: ./demo.sh smoke"
}
