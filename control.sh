#!/usr/bin/env bash
# =============================================================================
#  control.sh — one entry point to manage BOTH demo backends
# =============================================================================
#   Cloudflare Workers  (the classic demo)         -> wraps ./demo.sh
#   GCP VM + Insights   (self-hosted, runtime data) -> wraps gcloud + the VM
#
#   Usage:  ./control.sh <target> <action>
#     target:  cf | gcp | all
#     action:  start | stop | reset | status | urls
#
#   Examples:
#     ./control.sh gcp start      # boot the VM for a demo (~$0.005/hr while up)
#     ./control.sh gcp status     # VM + service health + URLs
#     ./control.sh gcp stop       # shut it down -> back to ~$0
#     ./control.sh cf status      # health-check the Cloudflare workers
#     ./control.sh all status     # both backends at a glance
#
#   GCP settings come from runtime-vm/gcp/gcp.env (copy from gcp.env.example).
#   Secrets never live here — the VM reads them from runtime-vm/.env (git-ignored).
# =============================================================================
set -euo pipefail
export PATH="/usr/bin:/bin:/usr/sbin:/sbin:/opt/homebrew/bin:$PATH"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GCP_ENV="$ROOT/runtime-vm/gcp/gcp.env"

usage() { sed -n '2,25p' "$0" | sed 's/^# \{0,1\}//'; }

# ---- GCP helpers -----------------------------------------------------------
load_gcp() {
  [ -f "$GCP_ENV" ] || { echo "ERROR: $GCP_ENV not found (cp gcp.env.example gcp.env)"; exit 1; }
  set -a; source "$GCP_ENV"; set +a
  : "${GCP_PROJECT:?set GCP_PROJECT in gcp.env}"
  : "${GCP_ZONE:=us-west1-a}"; : "${GCP_INSTANCE:=postman-insights-demo}"
  : "${SSH_KEY:=$HOME/.ssh/postman-insights}"; : "${DEMO_HOST:=}"
  SSH_KEY="${SSH_KEY/#\~/$HOME}"   # expand leading ~
}
gip()    { gcloud compute instances describe "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" --format='get(networkInterfaces[0].accessConfigs[0].natIP)' 2>/dev/null; }
gstate() { gcloud compute instances describe "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" --format='get(status)' 2>/dev/null; }
gssh()   { ssh -i "$SSH_KEY" -o ConnectTimeout=15 -o StrictHostKeyChecking=accept-new -o BatchMode=yes ubuntu@"$(gip)" "$@"; }
SVCS="wrangler-orders wrangler-payments wrangler-users postman-insights caddy"

gcp_start()  { load_gcp; echo "==> Starting $GCP_INSTANCE ($GCP_ZONE)…"; gcloud compute instances start "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" -q; echo "    IP: $(gip)"; echo "    URL: https://$DEMO_HOST  (DuckDNS + services auto-start on boot; give it ~60s)"; }
gcp_stop()   { load_gcp; echo "==> Stopping $GCP_INSTANCE…"; gcloud compute instances stop "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" -q; echo "    Stopped. IPv4 released (~\$0). Catalog data stays visible ~7 days."; }
gcp_reset()  { load_gcp; echo "==> Restarting services on $GCP_INSTANCE…"; gssh "sudo systemctl restart $SVCS; sleep 5; for s in $SVCS duckdns.timer; do printf '%-20s %s\n' \"\$s\" \"\$(systemctl is-active \$s)\"; done"; }
gcp_status() {
  load_gcp; local st; st="$(gstate)"; echo "GCP VM ($GCP_INSTANCE): ${st:-NOT FOUND}"
  [ "$st" = "RUNNING" ] || { echo "  (stopped — run: ./control.sh gcp start)"; return 0; }
  echo "  IP: $(gip)"
  gssh "for s in $SVCS duckdns.timer; do printf '  %-20s %s\n' \"\$s\" \"\$(systemctl is-active \$s)\"; done" || echo "  (ssh not ready yet)"
  [ -n "$DEMO_HOST" ] && echo "  health: $(curl -s -m10 -o /dev/null -w '%{http_code}' "https://$DEMO_HOST/health" 2>/dev/null || echo '???') https://$DEMO_HOST/health"
}
gcp_urls()   { load_gcp; for p in orders payments users; do echo "https://$DEMO_HOST/$p"; done; }

# ---- Cloudflare helpers (delegate to demo.sh) ------------------------------
cf_start()  { "$ROOT/demo.sh" deploy; }
cf_reset()  { "$ROOT/demo.sh" deploy; }
cf_status() { "$ROOT/demo.sh" smoke; }
cf_urls()   { "$ROOT/demo.sh" urls; }
cf_stop()   { echo "Cloudflare Workers are always-on and free — nothing to stop."; echo "To remove entirely: wrangler delete --config apis/<api>/wrangler.toml"; }

# ---- dispatch --------------------------------------------------------------
target="${1:-}"; action="${2:-}"
case "$target/$action" in
  gcp/start) gcp_start ;;  gcp/stop) gcp_stop ;;  gcp/reset) gcp_reset ;;  gcp/status) gcp_status ;;  gcp/urls) gcp_urls ;;
  cf/start)  cf_start ;;   cf/stop)  cf_stop ;;   cf/reset)  cf_reset ;;   cf/status)  cf_status ;;   cf/urls)  cf_urls ;;
  all/status) echo "── Cloudflare ──"; cf_status || true; echo; echo "── GCP ──"; gcp_status || true ;;
  all/start)  echo "── Cloudflare ──"; cf_start  || true; echo; echo "── GCP ──"; gcp_start  || true ;;
  all/stop)   gcp_stop || true; echo "(Cloudflare left running — always-on & free)" ;;
  *) usage; exit 1 ;;
esac
