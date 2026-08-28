#!/usr/bin/env bash
# =============================================================================
#  deploy-runtime.sh  —  SELF-HOSTED API + Postman Insights stack (GCP VM)
# =============================================================================
#  This is NOT the Cloudflare demo. It stands up a self-hosted copy of the APIs
#  on an GCP VM plus the Postman Insights agent, so the Insights-powered parts
#  of the API Catalog light up (Runtime Health, observed endpoints, error rates).
#
#    Cloudflare Workers demo ......... ../demo.sh        (run on your laptop)
#    Self-hosted API + Insights ...... this script       (run ON the GCP VM)
#
#  The two are independent and both stay live — see ../GCP-INSIGHTS.md.
# =============================================================================
#
# Run ON the GCP VM (Ubuntu 22.04), from the repo's runtime-vm/ dir:
#   cd postman-api-catalog-demo/runtime-vm
#   cp .env.example .env && nano .env      # fill HOSTNAME, POSTMAN_API_KEY, INSIGHTS_*
#   ./deploy-runtime.sh
#
# What it does (all native, no Docker):
#   - installs Node 20, Caddy, and the Postman Insights agent
#   - `npm ci` at the repo root (pulls wrangler from devDependencies)
#   - runs each API as a systemd service via `wrangler dev` on a loopback port
#   - configures Caddy to terminate TLS on <HOSTNAME> and path-route to those ports
#   - (if DUCKDNS_* set) keeps <HOSTNAME> pointed at the VM's current public IP
#   - runs the Insights agent as a systemd service, reporting to your workspace/system-env
#
# Idempotent: safe to re-run after editing .env.
set -euo pipefail

echo "============================================================"
echo " deploy-runtime.sh — SELF-HOSTED API + Insights stack (VM)"
echo " (Cloudflare demo is a separate script: ../demo.sh)"
echo "============================================================"

# Sanity: this is meant to run on the Linux VM, not the macOS laptop that runs demo.sh.
if [ "$(uname -s)" != "Linux" ]; then
  echo "WARNING: this is not Linux. deploy-runtime.sh is meant to run ON the GCP VM."
  echo "         For the Cloudflare Workers demo on your laptop, use ../demo.sh instead."
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
if ! command -v node >/dev/null || [ "$(node -v | cut -c2-3)" -lt 22 ]; then
  echo "==> Installing Node 22 (Wrangler requires Node >= 22)"
  curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
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
# 2. Install repo deps (wrangler from devDependencies) — only when needed.
#    Skips npm ci when node_modules is intact and package-lock.json is unchanged,
#    so routine code-only redeploys are fast (~seconds). On an actual install we
#    stop the services + normalize ownership first (avoids EACCES on the miniflare
#    cache in node_modules/.mf if an earlier run left root-owned files).
# ---------------------------------------------------------------------------
WRANGLER="$REPO_ROOT/node_modules/.bin/wrangler"
LOCK="$REPO_ROOT/package-lock.json"
MARK="$REPO_ROOT/node_modules/.deploy-lock-hash"
lock_hash() { sha256sum "$LOCK" 2>/dev/null | cut -d' ' -f1; }
if [ -x "$WRANGLER" ] && [ -f "$MARK" ] && [ "$(lock_hash)" = "$(cat "$MARK" 2>/dev/null)" ]; then
  echo "==> deps unchanged — skipping npm ci"
else
  echo "==> installing deps (npm ci)"
  sudo systemctl stop wrangler-orders wrangler-payments wrangler-users 2>/dev/null || true
  sudo chown -R "$RUN_USER":"$RUN_USER" "$REPO_ROOT" 2>/dev/null || true
  ( cd "$REPO_ROOT" && npm ci )
  lock_hash > "$MARK"
fi
[ -x "$WRANGLER" ] || { echo "ERROR: wrangler not found at $WRANGLER"; exit 1; }

# ---------------------------------------------------------------------------
# 3. systemd services — one wrangler dev per API
#    NOTE: wrangler dev runs in LOCAL mode (workerd) — no Cloudflare login needed.
#          Distinct --inspector-port avoids the default 9229 clashing across the three.
# ---------------------------------------------------------------------------
write_wrangler_unit() {
  local name="$1" config="$2" port="$3" inspector="$4"
  echo "==> systemd unit: wrangler-$name (port $port)"
  sudo tee "/etc/systemd/system/wrangler-$name.service" >/dev/null <<UNIT
[Unit]
Description=wrangler dev — $name API (Postman catalog demo)
After=network.target

[Service]
Type=simple
User=$RUN_USER
WorkingDirectory=$REPO_ROOT
Environment=CI=1
Environment=WRANGLER_SEND_METRICS=false
Environment=HOME=/home/$RUN_USER
ExecStart=$WRANGLER dev --config $config --ip 127.0.0.1 --port $port --inspector-port $inspector
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
UNIT
}

write_wrangler_unit orders   "apis/orders/wrangler.toml"   "$ORDERS_PORT"   9400
write_wrangler_unit payments "apis/payments/wrangler.toml" "$PAYMENTS_PORT" 9401
write_wrangler_unit users    "apis/users/wrangler.toml"    "$USERS_PORT"    9402

# ---------------------------------------------------------------------------
# 4. systemd service — Postman Insights agent (needs root for packet capture)
#    apidump auto-discovers endpoints from observed traffic and reports to the
#    catalog system identified by --workspace-id / --system-env.
# ---------------------------------------------------------------------------
echo "==> systemd unit: postman-insights"
sudo tee "/etc/systemd/system/postman-insights.service" >/dev/null <<UNIT
[Unit]
Description=Postman Insights agent — observe catalog demo APIs
After=network.target wrangler-orders.service wrangler-payments.service wrangler-users.service

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
# 4b. DuckDNS updater — keeps $HOSTNAME pointed at the VM's current public IP.
#     The GCP ephemeral IP changes on each start/stop, so refresh on boot + every 5 min.
# ---------------------------------------------------------------------------
if [ -n "${DUCKDNS_TOKEN:-}" ] && [ -n "${DUCKDNS_SUBDOMAIN:-}" ]; then
  echo "==> systemd unit: duckdns updater ($DUCKDNS_SUBDOMAIN.duckdns.org)"
  sudo tee /etc/systemd/system/duckdns.service >/dev/null <<UNIT
[Unit]
Description=DuckDNS updater for $DUCKDNS_SUBDOMAIN.duckdns.org
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/bin/curl -fsS "https://www.duckdns.org/update?domains=$DUCKDNS_SUBDOMAIN&token=$DUCKDNS_TOKEN&ip="
UNIT
  sudo tee /etc/systemd/system/duckdns.timer >/dev/null <<UNIT
[Unit]
Description=Refresh DuckDNS every 5 minutes

[Timer]
OnBootSec=15
OnUnitActiveSec=5min

[Install]
WantedBy=timers.target
UNIT
  sudo systemctl daemon-reload
  sudo systemctl enable --now duckdns.timer
  sudo systemctl start duckdns.service   # set the A record to the current IP now
  echo "==> DuckDNS record updated for $DUCKDNS_SUBDOMAIN.duckdns.org"
fi

# ---------------------------------------------------------------------------
# 5. Caddy — TLS + reverse proxy. Single host ($HOSTNAME), path-routed to the
#    three backends (DuckDNS gives one name, so we route by top-level path).
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
sudo systemctl enable --now wrangler-orders wrangler-payments wrangler-users postman-insights
sudo systemctl reload caddy || sudo systemctl restart caddy

echo
echo "==> Done. Base URL: https://$HOSTNAME"
echo "     health:   https://$HOSTNAME/health"
echo "     orders:   https://$HOSTNAME/orders"
echo "     payments: https://$HOSTNAME/payments"
echo "     users:    https://$HOSTNAME/users"
echo
echo "Check status:   systemctl status wrangler-orders postman-insights caddy"
echo "Tail agent log: journalctl -u postman-insights -f"
echo
echo "NOTE: first HTTPS hit may take a few seconds while Caddy fetches Let's Encrypt certs."
