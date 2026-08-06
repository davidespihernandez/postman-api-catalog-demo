#!/usr/bin/env bash
# Start the GCP demo VM (run on your laptop). Costs ~$0.005/hr for the IPv4 while up.
# Stop it again with vm-stop.sh when you're done to drop back to ~$0.
set -euo pipefail
export PATH="/opt/homebrew/bin:$PATH"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
[ -f "$SCRIPT_DIR/gcp.env" ] && { set -a; source "$SCRIPT_DIR/gcp.env"; set +a; }
: "${GCP_PROJECT:?set GCP_PROJECT (in gcp.env)}"
: "${GCP_ZONE:=us-west1-a}"
: "${GCP_INSTANCE:=postman-insights-demo}"

echo "==> Starting $GCP_INSTANCE ($GCP_ZONE)…"
gcloud compute instances start "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" -q

IP=$(gcloud compute instances describe "$GCP_INSTANCE" --zone "$GCP_ZONE" --project "$GCP_PROJECT" \
  --format='get(networkInterfaces[0].accessConfigs[0].natIP)')
NIP="${IP//./-}.nip.io"

echo
echo "==> Running. External IP: $IP"
echo "    nip.io hostname:    $NIP"
echo "    e.g.  https://orders.$NIP/health"
echo
echo "NOTE: the ephemeral IP changes on each start."
echo "  - DuckDNS setup: the VM's boot updater points your stable hostname here automatically — nothing to do."
echo "  - nip.io setup:  the hostname above changed; on the VM run"
echo "      cd ~/postman-api-catalog-demo/runtime-vm && sed -i \"s/^HOSTNAME=.*/HOSTNAME=$NIP/\" .env && ./deploy-runtime.sh"
echo "    and update the GCP Postman environments' baseUrl if you'll run collections live."
echo
echo "Stop it when done:  ./vm-stop.sh"
