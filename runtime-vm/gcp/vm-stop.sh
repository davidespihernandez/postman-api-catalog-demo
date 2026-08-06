#!/usr/bin/env bash
# Stop the GCP demo VM (run on your laptop). Stopped VM ≈ $0:
#   - ephemeral IPv4 released -> no IP charge
#   - e2-micro compute is free-tier
#   - 30 GB boot disk stays, within Always Free
set -euo pipefail
export PATH="/opt/homebrew/bin:$PATH"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[ -f "$SCRIPT_DIR/gcp.env" ] && { set -a; source "$SCRIPT_DIR/gcp.env"; set +a; }
: "${GCP_PROJECT:?set GCP_PROJECT (in gcp.env)}"
: "${GCP_ZONE:=us-west1-a}"
: "${GCP_INSTANCE:=postman-insights-demo}"

echo "==> Stopping $GCP_INSTANCE ($GCP_ZONE)…"
gcloud compute instances stop "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" -q
echo "==> Stopped. IPv4 released, billing paused. Data stays visible in the catalog for ~7 days."
