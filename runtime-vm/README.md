# runtime-vm — the on-VM stack

Everything that runs on the AWS VM, deployed by **`deploy-runtime.sh`** (idempotent, native —
no Docker). See [`../AWS-INSIGHTS.md`](../AWS-INSIGHTS.md) for provisioning the VM itself.

## What it deploys

```
Caddy :443 (TLS, one host $HOSTNAME) ── path-routed ──┐
  /orders → 8787   /payments → 8788   /users → 8789    ▼
                    node (Express) × 3 (127.0.0.1)   ◀── Insights agent sniffs loopback
                                                     ── mqtt-bridge ◀ broker.hivemq.com
```

Systemd services created:
- **`api-orders` / `api-payments` / `api-users`** — the three Node/Express servers
  (`apis/<api>/src/server.mjs`), each on a loopback port. `api-payments` gets
  `REFUND_WEBHOOK_URL` in its environment so `/payments/refund` can POST the `payment.refunded`
  webhook.
- **`postman-insights`** — the Insights agent (`apidump`), observes loopback traffic → Runtime Health.
- **`mqtt-bridge`** — always-on MQTT consumer (created when `NOTIFICATION_WEBHOOK_URL` is set);
  subscribes to the broker/topic and forwards to the notification webhook.
- **Caddy** — TLS + single-host path routing.

Plus: a 2 GB **swapfile** on small boxes, and a **webhook DNS pin** in `/etc/hosts` (see gotchas).

## Config (`.env`, git-ignored — copy from `.env.example`)
`HOSTNAME` (e.g. `<elastic-ip-dashes>.nip.io`), `POSTMAN_API_KEY`, `INSIGHTS_WORKSPACE_ID`,
`INSIGHTS_SYSTEM_ENV`, optional `REFUND_WEBHOOK_URL`, `NOTIFICATION_WEBHOOK_URL`, and the loopback
ports.

## Deploy (on the VM)
```bash
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo/runtime-vm
cp .env.example .env && nano .env
./deploy-runtime.sh          # idempotent; re-run after any .env change
```
Reach the VM via **AWS SSM** (public SSH is blocked by the subnet NACL) — CI does exactly this
(`git pull` + `deploy-runtime.sh`), and `../control.sh reset` restarts services the same way.

## Verify
```bash
systemctl status api-orders api-payments api-users postman-insights mqtt-bridge caddy
curl https://$HOSTNAME/health          # {"status":"ok","service":"orders-api",...}
journalctl -u postman-insights -f      # capturing + uploading
journalctl -u mqtt-bridge -f           # "Subscribed." then "Forwarded … → webhook (200)"
```

## Gotchas already handled
| Gotcha | Handling |
|--------|----------|
| Services must not run as **root** (SSM runs as root) | `RUN_USER` never resolves to root; runs as `ubuntu` |
| Slow redeploys | `npm ci` is skipped when `package-lock.json` is unchanged (hash marker) |
| **Webhook DNS**: Postman's `*.webhook.pstmn.io` CNAME has a literal `*` that Linux glibc won't follow → `fetch`/refund/bridge fail | `pin_webhook_dns()` resolves each webhook host via `resolvectl` and pins it in `/etc/hosts` |

## Troubleshooting
| Symptom | Fix |
|---------|-----|
| Caddy TLS cert fails | `$HOSTNAME` must resolve to the VM and 80/443 open in the VPC security group |
| A service won't start | `journalctl -u api-orders -n 30` (etc.); confirm `node` present and `npm ci` ran |
| Webhook `fetch failed` / not received | confirm the webhook host is pinned in `/etc/hosts` (re-run `deploy-runtime.sh`); the URL must be current |
| Runtime Health stays N/A | give it 5–8 min after traffic; check `POSTMAN_API_KEY` / `--workspace-id` / `--system-env` |
