# Runtime-VM stack — Part 2 (deploy the observable backend + Insights agent)

This is **Part 2** of getting Runtime Health into the API Catalog. Do **Part 1** first —
provision the GCP VM per [`../GCP-INSIGHTS.md`](../GCP-INSIGHTS.md) (VM running, ports
80/443 open in the VPC firewall, Docker not required, a hostname resolving to the public IP).

> Reminder: this runs **alongside** Cloudflare, it does not replace it. See the dual-environment
> strategy in `GCP-INSIGHTS.md`.

## What this deploys (all native on the VM — no Docker)

```
GitHub Actions / synthetic traffic ──HTTPS──▶ Caddy (:443, auto Let's Encrypt)
   orders.<HOST>  payments.<HOST>  users.<HOST>      │  plaintext HTTP on 127.0.0.1
                                                     ▼
              wrangler dev × 3  (:8787 / :8788 / :8789)  ◀── Insights agent (apidump) sniffs
              (your exact Worker code, unchanged)             and reports to the catalog
```

- **Three `wrangler dev` systemd services** — run the existing Worker code as-is (local/workerd
  mode, no Cloudflare login), one per API on a loopback port.
- **Caddy** — terminates TLS and reverse-proxies each `<api>.<HOSTNAME>` subdomain to its backend.
  Per-API subdomains keep every path and the OpenAPI `servers` block correct.
- **Postman Insights agent** — a systemd service running `apidump --workspace-id … --system-env …`,
  observing the plaintext hop and populating Runtime Health.

## Deploy

```bash
# on the VM
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo/runtime-vm
cp .env.example .env
nano .env          # set HOSTNAME, POSTMAN_API_KEY, INSIGHTS_WORKSPACE_ID, INSIGHTS_SYSTEM_ENV
./deploy-runtime.sh
```

`deploy-runtime.sh` is idempotent — re-run it after any `.env` change.

## Verify

```bash
systemctl status wrangler-orders wrangler-payments wrangler-users postman-insights caddy
curl https://orders.$HOSTNAME/health          # {"status":"ok","service":"orders-api",...}
journalctl -u postman-insights -f             # should show it capturing traffic
```

Then generate some traffic and confirm the catalog:

```bash
./synthetic-traffic.sh                        # one pass against all three APIs
```

- Add it to cron for a steady trickle (keeps availability/latency fresh):
  `crontab -e` → `* * * * * $PWD/synthetic-traffic.sh >/dev/null 2>&1`
- Or rely on the scheduled GitHub Actions job `postman-orders-qa-gcp.yml` (every 30 min).

Within ~5–8 minutes the Insights agent discovers endpoints and **Runtime Health** in the catalog
should leave `N/A`.

## Wire up the Postman environments + CI

1. Import `postman/environments/GCP *.environment.yaml` into Postman; set each `baseUrl` to
   your real `https://<api>.<HOSTNAME>`.
2. In `.github/workflows/postman-orders-qa-gcp.yml`, set `POSTMAN_ENVIRONMENT_ID` to the
   **GCP Orders** environment's ID.
3. In a demo, switch the collection's environment between **Production \*** (Cloudflare) and
   **GCP \*** (self-hosted) — same tests, different backend, and only the GCP one feeds
   Runtime Health.

## ⚠️ Two things to verify on the VM (couldn't be tested before the box existed)

1. **Loopback capture.** The agent must sniff the plaintext Caddy→wrangler hop on `lo`. If
   `journalctl -u postman-insights` shows it seeing **no** traffic, check `postman-insights-agent
   apidump --help` for an interface/filter flag (e.g. select `lo`) and add it to the `ExecStart`
   in `/etc/systemd/system/postman-insights.service`, then
   `sudo systemctl daemon-reload && sudo systemctl restart postman-insights`.
2. **wrangler dev flags.** If `wrangler-<api>` services fail to start, run the `ExecStart` command
   by hand once to see the error. Older/newer wrangler may differ on `--inspector-port` /
   `--ip`; adjust the flags in `deploy-runtime.sh`’s `write_wrangler_unit` and re-run `./deploy-runtime.sh`.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Caddy TLS cert fails | Hostname must resolve to the VM (`dig +short orders.$HOSTNAME`) and ports 80/443 open in the **VPC firewall** (the Allow HTTP/HTTPS rules — GCP-INSIGHTS.md Part 2). |
| `wrangler: not found` | `npm ci` didn't run at the repo root, or Node < 20. Re-run `./deploy-runtime.sh`. |
| Agent runs but catalog stays N/A | Give it 5–8 min after first traffic; confirm `POSTMAN_API_KEY`, `--workspace-id`, `--system-env` are correct; ensure traffic is actually hitting the VM (not Cloudflare). |
| Port already in use | Another process on 8787–8789; change the ports in `.env` and re-run `./deploy-runtime.sh`. |
