# Postman API Catalog Demo

Real HTTPS APIs for repeatable **API Catalog** demos, on **three backends** you switch between
with Postman environments:

```
Cloudflare Workers  →  the classic demo (CI/CD, spec, tests)         ./demo.sh
GCP VM + Insights   →  self-hosted twin, on-demand (start/stop)      ./control.sh gcp …
AWS EC2 + Insights  →  self-hosted twin, always-on (EU, via SSM)     ./control.sh aws …
```

The Insights agent can't run on serverless Cloudflare Workers, so the GCP/AWS VMs host self-hosted
twins the agent can observe. All stay live; pick one per demo.

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

| Doc | Audience |
|-----|----------|
| [SE-INSTALL.md](SE-INSTALL.md) | SE one-time setup — Cloudflare (includes deploy) |
| [DEMO-STEPS.md](DEMO-STEPS.md) | Customer demo (Postman app only) |
| [CI-CD.md](CI-CD.md) | GitHub Actions + Postman CLI (Orders QA) |
| [GCP-INSIGHTS.md](GCP-INSIGHTS.md) | Runtime Health via a **GCP** VM + Insights agent (replication runbook) |
| [AWS-INSIGHTS.md](AWS-INSIGHTS.md) | Runtime Health via an **AWS** EC2 VM + Insights agent, always-on via SSM (replication runbook) |
| [runtime-vm/README.md](runtime-vm/README.md) | The shared VM stack internals (deploy-runtime.sh) |

**Control scripts:** `./demo.sh` manages Cloudflare; `./control.sh <cf\|gcp\|aws\|all>
<start\|stop\|reset\|status\|urls>` manages all three (GCP settings in `runtime-vm/gcp/gcp.env`,
AWS in `runtime-vm/aws/aws.env`).

## Setup (before the demo)

```bash
./demo.sh setup
./demo.sh setup-subdomain   # one-time: pick your *.workers.dev name
./demo.sh deploy
./demo.sh urls
```

`setup-subdomain` must run in an **interactive terminal** (wrangler prompts for your subdomain). After that, `deploy` is non-interactive.

Sync the `postman/` folder into your workspace (collections, environments, globals). See [SE-INSTALL.md](SE-INSTALL.md).

## Postman assets

```
postman/collections/     QA + Doc collections (native Postman format)
postman/environments/    Production / Mock per API + MQTT notifications
postman/globals/
```

| Spec / collection | Purpose |
|-------------------|---------|
| `orders.yaml`, `payments.yaml`, `users.yaml` | Backend REST APIs (workers) |
| `payment-refund-webhook.yaml` | Inbound refund event contract |
| `notifications.asyncapi.yaml` | MQTT async message contract |
| `Orders - QA`, `Payments - QA`, `Users - QA` | CRUD validation |
| `Payments - Doc` → Refund a payment | REST → webhook (sync async) |
| Manual **MQTT** collection request | Publish directly to broker topic (you create) |

Set `REFUND_WEBHOOK_URL` in `.env` before deploy. For MQTT webhook proof, run `./demo.sh mqtt-bridge` (see SE-INSTALL).

## Requirements

Node.js 18+, curl, Cloudflare (free), Postman Enterprise with API Catalog, Postman desktop app (for MQTT requests).
