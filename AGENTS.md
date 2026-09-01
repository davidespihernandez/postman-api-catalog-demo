# Agent context: postman-api-catalog-demo

GitHub: https://github.com/davidespihernandez/postman-api-catalog-demo

## Purpose

Postman **API Catalog** demo: three REST APIs + Postman workspace (QA + Doc collections) +
**Insights/Runtime Health**, an async **MQTT** flow, and a **payment refund webhook** — all
self-hosted on **AWS** and driven through a real **CI/CD pipeline**.

## Architecture

```
AWS EC2 (t4g.small, always-on, eu-central-1), managed via SSM (no public SSH)
  Caddy :443 (TLS) → path-routed → node (Express) × 3 (Orders/Payments/Users)
  Postman Insights agent → observes loopback traffic → Runtime Health
  mqtt-bridge (systemd) ← broker.hivemq.com:1883 ← Postman MQTT publish
Base URL: https://18-157-170-15.nip.io/{orders,payments,users,health}
```

The APIs are plain Node/Express servers (no Cloudflare). The stack is
deployed by `runtime-vm/deploy-runtime.sh`; manage it with `./control.sh` (SSM-based).

## Layout
- `apis/<api>/` — Node/Express server (`src/server.mjs`), `openapi.json` (committed)
- `orders.yaml` / `payments.yaml` / `users.yaml` — Spec Hub OpenAPI specs (source of truth)
- `index.yaml` (AsyncAPI notifications), `payment-refund-webhook.yaml`
- `runtime-vm/` — the on-VM stack + `aws/` control config; `scripts/mqtt-webhook-bridge.mjs`
- `postman/` — collections (`* - QA`, `* - Doc`, `Notifications (MQTT)`), `Production * AWS` envs
- `frontend/` — React UI + Playwright browser-testing demo
- `.github/workflows/ci-cd.yml` — lint → QA (fresh code) → perf load-test (vs live AWS) → sync to cloud → deploy → smoke

## Key flows
- **Refund webhook:** `POST /payments/refund {"paymentId":"pay-001"}` → Payments worker POSTs
  `payment.refunded` to `REFUND_WEBHOOK_URL` (set in the payments service's env on the VM).
- **MQTT:** Postman publishes to `broker.hivemq.com:1883` topic `postman-api-catalog-demo/notifications`
  → the always-on `mqtt-bridge` forwards `notification.processed` to `NOTIFICATION_WEBHOOK_URL`.
- **CI/CD:** Postman local (git) is source of truth; deploy to AWS only if the QA gate (against the
  freshly-built code) **and** the performance gate (`postman performance run` vs live AWS, `--pass-if
  p99<2000`) both pass. The perf job also runs on manual `workflow_dispatch`.

## Gotchas
- Services run as `ubuntu` (never root — SSM runs as root). Node 20+.
- `*.webhook.pstmn.io` has a CNAME glibc won't follow on Linux → pinned in `/etc/hosts` by deploy.
- Manage via SSM (`./control.sh`), not SSH (subnet NACL blocks 22).

See `README.md` (overview + demos), `AWS-INSIGHTS.md` (provisioning), `runtime-vm/README.md` (stack).
