# Runtime-VM stack — Part 2 (deploy the observable backend + Insights agent)

**Part 2** of getting Insights/Runtime data into the API Catalog. Do **Part 1** first — provision
the GCP VM per [`../GCP-INSIGHTS.md`](../GCP-INSIGHTS.md) (VM running, 80/443 open in the VPC
firewall, a DuckDNS hostname + token ready).

> This runs **alongside** Cloudflare, it does not replace it. Both backends stay live; you switch
> between them with Postman environments.

## What this deploys (all native on the VM — no Docker)

```
synthetic-traffic cron / demo runs ──HTTPS──▶ Caddy (:443, auto Let's Encrypt)
        https://<HOSTNAME>/orders|/payments|/users        │  path-routed, plaintext on 127.0.0.1
                                                           ▼
              wrangler dev × 3  (:8787 / :8788 / :8789)  ◀── Insights agent (apidump) sniffs
              (your exact Worker code, unchanged)             loopback + reports to the catalog
```

- **One DuckDNS host, path-routed** — `/orders`→8787, `/payments`→8788, `/users`→8789 (paths not
  stripped; each worker sees its own root paths). Default (`/`, `/health`) → orders.
- **Three `wrangler dev` systemd services** — the existing Worker code as-is (local/workerd mode,
  no Cloudflare login). **Requires Node ≥ 22** (the script installs it).
- **Caddy** — TLS termination + reverse proxy.
- **DuckDNS updater** (systemd timer) — repoints the hostname to the VM's current IP on every
  boot, so the ephemeral IP changing on start/stop doesn't matter.
- **Postman Insights agent** — `apidump --workspace-id … --system-env …`, captures the loopback
  hop (confirmed: `learn mode on interfaces lo, ens4`) and reports to the catalog.
- **Swap** — a 2 GB swapfile is auto-added on the 1 GB box so the stack doesn't OOM.

## Deploy

```bash
# on the VM
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo/runtime-vm
cp .env.example .env
nano .env     # HOSTNAME, DUCKDNS_SUBDOMAIN, DUCKDNS_TOKEN, POSTMAN_API_KEY,
              # INSIGHTS_WORKSPACE_ID, INSIGHTS_SYSTEM_ENV
./deploy-runtime.sh
```

`deploy-runtime.sh` is idempotent — re-run it after any `.env` change.

## Verify

```bash
systemctl status wrangler-orders wrangler-payments wrangler-users postman-insights caddy
curl https://$HOSTNAME/health          # {"status":"ok","service":"orders-api",...}
curl https://$HOSTNAME/orders          # list
journalctl -u postman-insights -f      # should show it capturing + uploading
```

Generate traffic so the agent has something to report:

```bash
./synthetic-traffic.sh                 # one pass across all three APIs
crontab -e                             # steady trickle:
# * * * * * /home/ubuntu/postman-api-catalog-demo/runtime-vm/synthetic-traffic.sh >/dev/null 2>&1
```

Within ~5–8 min the agent discovers endpoints and **Runtime Health** in the catalog leaves `N/A`.
The scorecard score recomputes on Postman's schedule (~hourly; "Stale" = showing last computed).

## Managing the VM (from your laptop)

Use the unified [`../control.sh`](../control.sh) (needs `gcp/gcp.env`):

```bash
./control.sh gcp start     # boot for a demo (~$0.005/hr while up)
./control.sh gcp status    # VM + service health + URLs
./control.sh gcp reset     # restart all services on the VM
./control.sh gcp stop      # shut down -> ~$0 (IPv4 released; catalog keeps 7 days of data)
```

## Wire up the Postman environments

1. Import `postman/environments/GCP *.environment.yaml` into Postman; set each `baseUrl` to your
   single host, `https://<HOSTNAME>` (collections append `/orders`, `/payments`, `/users`).
2. In a demo, switch the collection's environment between **Production \*** (Cloudflare) and
   **GCP \*** (self-hosted) — same tests, different backend; only the GCP one feeds Runtime Health.

> No GitHub Actions for the GCP backend — it isn't always running. Traffic comes from the on-VM
> `synthetic-traffic.sh` cron (while the VM is up) and your live demo runs. CI stays Cloudflare-only.

## Gotchas we already handled (so you don't have to)

| Gotcha | Resolution |
|--------|------------|
| Wrangler needs **Node ≥ 22** (Node 20 crash-loops) | `deploy-runtime.sh` installs Node 22. |
| `apis/*/openapi.json` were git-ignored → build fails on a fresh clone | Now committed to the repo. |
| Agent capturing loopback | Works out of the box — `apidump` listens on `lo` + `ens4`. |
| 1 GB RAM OOM risk | 2 GB swapfile auto-added. |
| Ephemeral IP changes on start/stop | DuckDNS updater fixes the hostname on boot. |

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Caddy TLS cert fails | Hostname must resolve to the VM (`dig +short $HOSTNAME`) and 80/443 open in the VPC firewall (GCP-INSIGHTS.md Part 2). |
| `wrangler` crash-loops with "requires Node.js v22" | Node < 22; re-run `./deploy-runtime.sh` (installs 22). |
| Build error `Could not resolve "../openapi.json"` | `git pull` — the openapi.json files must be present (they're committed now). |
| Agent runs but catalog stays N/A | Give it 5–8 min after first traffic; confirm `POSTMAN_API_KEY` / `--workspace-id` / `--system-env`; ensure traffic hits the VM (not Cloudflare). |
| Agent logs intermittent `502` on upload | Postman's Insights ingestion (alpha) hiccups; enough batches get through. Persistent? Contact live.insights.alpha@postman.com. |
