#!/usr/bin/env bash
# =============================================================================
#  deploy-runtime.sh  —  self-hosted API + Postman Insights stack (AWS VM)
# =============================================================================
#  Stands up the demo APIs + the Postman Insights agent on the VM, so the
#  Insights-powered parts of the API Catalog light up (Runtime Health, observed
#  endpoints, error rates), plus the async MQTT bridge and refund webhook.
# =============================================================================
#
# Run ON the VM (Ubuntu 22.04), from the repo's runtime-vm/ dir:
#   cd postman-api-catalog-demo/runtime-vm
#   cp .env.example .env && nano .env      # HOSTNAME, POSTMAN_API_KEY, INSIGHTS_*, webhook URLs
#   ./deploy-runtime.sh
# (Reach the VM via AWS SSM — public SSH is blocked by the subnet NACL. CI runs this same script.)
#
# What it does (all native, no Docker):
#   - installs Node 20, Caddy, and the Postman Insights agent
#   - pins webhook hosts in /etc/hosts (glibc can't follow Postman's *.webhook.pstmn.io CNAME)
#   - `npm ci` at the repo root (express + mqtt; skipped when package-lock is unchanged)
#   - runs each API as a systemd `node` (Express) service on a loopback port (payments gets
#     REFUND_WEBHOOK_URL); Caddy terminates TLS on <HOSTNAME> and path-routes to them
#   - runs the Insights agent + (if NOTIFICATION_WEBHOOK_URL set) the MQTT bridge as services
#
# Idempotent: safe to re-run after editing .env.
set -euo pipefail

echo "============================================================"
echo " deploy-runtime.sh — self-hosted API + Insights stack (AWS VM)"
echo "============================================================"

# Sanity: this is meant to run on the Linux VM, not a macOS laptop.
if [ "$(uname -s)" != "Linux" ]; then
  echo "WARNING: not Linux. deploy-runtime.sh is meant to run ON the AWS VM (via SSM)."
  read -r -p "Continue anyway? [y/N] " ans
  [ "$ans" = "y" ] || [ "$ans" = "Y" ] || { echo "Aborted."; exit 1; }
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

[ -f "$ENV_FILE" ] || { echo "ERROR: $ENV_FILE not found. Run: cp .env.example .env && edit it."; exit 1; }
# shellcheck disable=SC1090
set -a; source "$ENV_FILE"; set +a

: "${HOSTNAME:?set HOSTNAME in .env}"
: "${POSTMAN_API_KEY:?set POSTMAN_API_KEY in .env}"
: "${INSIGHTS_WORKSPACE_ID:?set INSIGHTS_WORKSPACE_ID in .env}"
: "${INSIGHTS_SYSTEM_ENV:?set INSIGHTS_SYSTEM_ENV in .env}"
ORDERS_PORT="${ORDERS_PORT:-8787}"
PAYMENTS_PORT="${PAYMENTS_PORT:-8788}"
USERS_PORT="${USERS_PORT:-8789}"
RUN_USER="${RUN_USER:-${SUDO_USER:-$USER}}"
[ "$RUN_USER" = "root" ] && RUN_USER="ubuntu"   # never run the services as root
                                                # (SSM runs as root; sudo -u ubuntu leaves SUDO_USER=root)

echo "==> Repo root: $REPO_ROOT   Hostname: $HOSTNAME   Service user: $RUN_USER"

# ---------------------------------------------------------------------------
# 0a. Pin webhook DNS. Postman's *.webhook.pstmn.io resolves via a CNAME whose
#     target contains a literal "*", which glibc getaddrinfo (used by fetch/curl,
#     i.e. the MQTT bridge and the payment-refund webhook) refuses to follow on
#     Linux — so outbound webhooks silently fail (they worked on Cloudflare's edge).
#     systemd-resolved CAN resolve it, so we pin host->IP in /etc/hosts (glibc reads
#     that first, bypassing the broken CNAME). Generic: pins whatever URLs are set.
# ---------------------------------------------------------------------------
pin_webhook_dns() {
  local url="$1"; [ -n "$url" ] || return 0
  local host; host=$(printf '%s' "$url" | sed -E 's#^[a-z]+://##; s#[/:].*##')
  [ -n "$host" ] || return 0
  local ips; ips=$(resolvectl query "$host" 2>/dev/null | grep -oE '([0-9]{1,3}\.){3}[0-9]{1,3}' | sort -u)
  [ -n "$ips" ] || { echo "==> WARN: could not resolve $host to pin in /etc/hosts"; return 0; }
  sudo sed -i "\#[[:space:]]${host}\$#d" /etc/hosts 2>/dev/null || true
  for ip in $ips; do echo "$ip $host" | sudo tee -a /etc/hosts >/dev/null; done
  echo "==> pinned $host -> $(echo $ips | tr '\n' ' ')in /etc/hosts"
}
pin_webhook_dns "${NOTIFICATION_WEBHOOK_URL:-}"
pin_webhook_dns "${REFUND_WEBHOOK_URL:-}"

# ---------------------------------------------------------------------------
# 0. Swap — small-RAM boxes (GCP/AWS 1 GB micro) need it so the stack doesn't OOM.
#    Auto-skipped when the VM already has >=2 GB RAM or swap is already on.
# ---------------------------------------------------------------------------
mem_mb=$(free -m | awk '/^Mem:/{print $2}')
if [ "${mem_mb:-9999}" -lt 2000 ] && ! sudo swapon --show | grep -q .; then
  echo "==> Low RAM (${mem_mb}MB) — enabling a 2G swapfile"
  if [ ! -f /swapfile ]; then
    sudo fallocate -l 2G /swapfile 2>/dev/null || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
  fi
  sudo swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
fi

# ---------------------------------------------------------------------------
# 1. System packages: Node 20, Caddy, curl
# ---------------------------------------------------------------------------
if ! command -v node >/dev/null || [ "$(node -v | cut -c2-3)" -lt 20 ]; then
  echo "==> Installing Node 20 (runtime for the Express API servers)"
  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
  sudo apt-get install -y nodejs
fi

if ! command -v caddy >/dev/null; then
  echo "==> Installing Caddy"
  sudo apt-get install -y debian-keyring debian-archive-keyring apt-transport-https curl
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list >/dev/null
  sudo apt-get update
  sudo apt-get install -y caddy
fi

if ! command -v postman-insights-agent >/dev/null; then
  echo "==> Installing Postman Insights agent"
  bash -c "$(curl -L https://releases.observability.postman.com/scripts/install-postman-insights-agent.sh)"
fi
AGENT_BIN="$(command -v postman-insights-agent)"

# ---------------------------------------------------------------------------
# 2. Install repo deps (express, mqtt) — only when needed.
#    Skips npm ci when node_modules is intact and package-lock.json is unchanged,
#    so routine code-only redeploys are fast (~seconds). On an actual install we
#    stop the API services + normalize ownership first.
# ---------------------------------------------------------------------------
DEP="$REPO_ROOT/node_modules/express"
LOCK="$REPO_ROOT/package-lock.json"
MARK="$REPO_ROOT/node_modules/.deploy-lock-hash"
lock_hash() { sha256sum "$LOCK" 2>/dev/null | cut -d' ' -f1; }
if [ -d "$DEP" ] && [ -f "$MARK" ] && [ "$(lock_hash)" = "$(cat "$MARK" 2>/dev/null)" ]; then
  echo "==> deps unchanged — skipping npm ci"
else
  echo "==> installing deps (npm ci)"
  sudo systemctl stop api-orders api-payments api-users 2>/dev/null || true
  sudo chown -R "$RUN_USER":"$RUN_USER" "$REPO_ROOT" 2>/dev/null || true
  ( cd "$REPO_ROOT" && npm ci )
  lock_hash > "$MARK"
fi
[ -d "$DEP" ] || { echo "ERROR: express not installed (npm ci failed?)"; exit 1; }

# ---------------------------------------------------------------------------
# 3. systemd services — one Node (Express) server per API, on a loopback port.
# ---------------------------------------------------------------------------
NODE="$(command -v node)"
# Remove superseded wrangler-* units from the earlier (Cloudflare-Worker) stack.
for old in wrangler-orders wrangler-payments wrangler-users; do
  if [ -f "/etc/systemd/system/$old.service" ]; then
    echo "==> removing old unit: $old"
    sudo systemctl disable --now "$old" 2>/dev/null || true
    sudo rm -f "/etc/systemd/system/$old.service"
  fi
done

write_api_unit() {
  local name="$1" port="$2" extra_env="${3:-}"
  echo "==> systemd unit: api-$name (port $port)"
  sudo tee "/etc/systemd/system/api-$name.service" >/dev/null <<UNIT
[Unit]
Description=$name API (Node/Express) — Postman catalog demo
After=network.target

[Service]
Type=simple
User=$RUN_USER
WorkingDirectory=$REPO_ROOT
Environment=PORT=$port
Environment=HOME=/home/$RUN_USER
$extra_env
ExecStart=$NODE $REPO_ROOT/apis/$name/src/server.mjs
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
UNIT
}

# Payments reads env.REFUND_WEBHOOK_URL to POST the payment.refunded webhook on /payments/refund.
PAY_ENV=""
[ -n "${REFUND_WEBHOOK_URL:-}" ] && PAY_ENV="Environment=REFUND_WEBHOOK_URL=$REFUND_WEBHOOK_URL"
write_api_unit orders   "$ORDERS_PORT"
write_api_unit payments "$PAYMENTS_PORT" "$PAY_ENV"
write_api_unit users    "$USERS_PORT"

# ---------------------------------------------------------------------------
# 4. systemd service — Postman Insights agent (needs root for packet capture)
#    apidump auto-discovers endpoints from observed traffic and reports to the
#    catalog system identified by --workspace-id / --system-env.
# ---------------------------------------------------------------------------
echo "==> systemd unit: postman-insights"
sudo tee "/etc/systemd/system/postman-insights.service" >/dev/null <<UNIT
[Unit]
Description=Postman Insights agent — observe catalog demo APIs
After=network.target api-orders.service api-payments.service api-users.service

[Service]
Type=simple
User=root
Environment=POSTMAN_API_KEY=$POSTMAN_API_KEY
ExecStart=$AGENT_BIN apidump --workspace-id $INSIGHTS_WORKSPACE_ID --system-env $INSIGHTS_SYSTEM_ENV
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT

# ---------------------------------------------------------------------------
# 4b. MQTT -> Postman webhook bridge (async notifications demo).
#     Always-on consumer so nothing has to be started on a laptop.
#     Only created when NOTIFICATION_WEBHOOK_URL is set in .env.
# ---------------------------------------------------------------------------
if [ -n "${NOTIFICATION_WEBHOOK_URL:-}" ]; then
  echo "==> systemd unit: mqtt-bridge"
  sudo tee /etc/systemd/system/mqtt-bridge.service >/dev/null <<UNIT
[Unit]
Description=MQTT -> Postman webhook bridge (notifications demo)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=$RUN_USER
WorkingDirectory=$REPO_ROOT
Environment=NOTIFICATION_WEBHOOK_URL=$NOTIFICATION_WEBHOOK_URL
Environment=MQTT_BROKER_URL=${MQTT_BROKER_URL:-mqtt://broker.hivemq.com:1883}
Environment=MQTT_TOPIC=${MQTT_TOPIC:-postman-api-catalog-demo/notifications}
ExecStart=$(command -v node) $REPO_ROOT/scripts/mqtt-webhook-bridge.mjs
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
  sudo systemctl daemon-reload
  sudo systemctl enable --now mqtt-bridge
fi

# ---------------------------------------------------------------------------
# 5. Caddy — TLS + reverse proxy. Single host ($HOSTNAME, e.g. an Elastic-IP
#    nip.io name), path-routed to the three backends by top-level path.
#    Paths are NOT stripped — each worker expects /orders, /payments, /users at its root.
# ---------------------------------------------------------------------------
echo "==> Writing /etc/caddy/Caddyfile"
sudo tee /etc/caddy/Caddyfile >/dev/null <<CADDY
$HOSTNAME {
	@orders path /orders /orders/*
	handle @orders {
		reverse_proxy 127.0.0.1:$ORDERS_PORT
	}
	@payments path /payments /payments/*
	handle @payments {
		reverse_proxy 127.0.0.1:$PAYMENTS_PORT
	}
	@users path /users /users/*
	handle @users {
		reverse_proxy 127.0.0.1:$USERS_PORT
	}
	# default (/, /health, /openapi.json) -> orders backend
	handle {
		reverse_proxy 127.0.0.1:$ORDERS_PORT
	}
}
CADDY

# ---------------------------------------------------------------------------
# 6. Enable + (re)start everything
# ---------------------------------------------------------------------------
echo "==> Starting services"
sudo systemctl daemon-reload
sudo systemctl enable --now api-orders api-payments api-users postman-insights
sudo systemctl reload caddy || sudo systemctl restart caddy

echo
echo "==> Done. Base URL: https://$HOSTNAME"
echo "     health:   https://$HOSTNAME/health"
echo "     orders:   https://$HOSTNAME/orders"
echo "     payments: https://$HOSTNAME/payments"
echo "     users:    https://$HOSTNAME/users"
echo
echo "Check status:   systemctl status api-orders postman-insights caddy"
echo "Tail agent log: journalctl -u postman-insights -f"
echo
echo "NOTE: first HTTPS hit may take a few seconds while Caddy fetches Let's Encrypt certs."
