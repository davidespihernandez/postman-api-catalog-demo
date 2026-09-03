# Postman API Catalog Demo

A repeatable demo of the **Postman API Catalog** end to end: three REST APIs, **Insights /
Runtime Health**, an async **MQTT notifications** flow, a **payment refund webhook**, a browser
frontend for traffic capture, and a real **CI/CD pipeline** — all self-hosted on **AWS**.

```
                         AWS EC2 (t4g.small, always-on, eu-central-1)
                         ┌───────────────────────────────────────────────┐
 clients / Postman  ───▶ │ Caddy :443 (TLS)  ── path-routed ──┐          │
 https://18-157-170-15    │                                     ▼          │
   -.nip.io/{orders,      │   node (Express) × 3 (127.0.0.1:8787/8/9)       │
    payments,users}       │   (Orders / Payments / Users workers)          │
                         │        ▲ loopback (plaintext)                   │
                         │   Postman Insights agent  ── observes ──▶ API Catalog Runtime Health
                         │   mqtt-bridge  ◀── broker.hivemq.com ── Postman (MQTT publish)
                         └───────────────────────────────────────────────┘
```

The APIs are plain **Node/Express** servers running on the VM — no Cloudflare anywhere. The whole box is managed over **AWS SSM** (no public SSH).

**Base URL:** `https://18-157-170-15.nip.io` → `/orders`, `/payments`, `/users`, `/health`.

| Doc | What |
|-----|------|
| [AWS-INSIGHTS.md](AWS-INSIGHTS.md) | Full replication runbook — stand up the whole thing on a fresh AWS account |
| [runtime-vm/README.md](runtime-vm/README.md) | The on-VM stack internals (`deploy-runtime.sh`) |
| [AGENTS.md](AGENTS.md) | Quick orientation for agents/contributors |

## Features & how to demo them

**1. REST APIs + contract tests** — Orders / Payments / Users, each with a `… - QA` collection.
Run any QA collection with the **`Production <API> AWS`** environment (`baseUrl =
https://18-157-170-15.nip.io`).

**2. Runtime Health (Insights)** — the Insights agent on the VM observes live traffic and populates
the catalog's **Runtime Health** (P95 latency, availability, 4xx rate). A 1-minute
`synthetic-traffic.sh` cron keeps it fresh. Runtime data reflects the last ~7 days.

**3. Async notifications (MQTT)** — publish a JSON message from the **Notifications (MQTT)**
collection to `broker.hivemq.com:1883`, topic `postman-api-catalog-demo/notifications`. The
always-on **`mqtt-bridge`** service on the VM forwards it to your notification webhook as
`notification.processed`. Nothing to run on your laptop.

**4. Payment refund webhook** — `POST /payments/refund` (`{"paymentId":"pay-001"}`) with the
**Production Payments AWS** env → the Payments worker POSTs a `payment.refunded` event to your
refund webhook.

**5. Browser frontend (Playwright)** — `frontend/` is a small React UI that drives the Orders API
from a browser so Postman can capture the traffic during Playwright tests (`npm run test:ui`,
analyzed via `npm run app:test`). Defaults to the AWS host.

## CI/CD pipeline (`.github/workflows/ci-cd.yml`)

Postman **local (git) is the source of truth**; the cloud workspace is the published mirror.

- **PR → main:** spec lint (local `*.yaml`) + QA collection + a performance load-test, all against
  the **freshly-built** Orders code (an ephemeral `node` server in the runner) — a breaking *or*
  slow change fails here.
- **push → main:** after the gates pass, **`postman workspace push`** (local → Postman Cloud) and
  **deploy to AWS** via SSM (`git pull` + `deploy-runtime.sh`), then a post-deploy smoke test.
- The **performance load-test** (`postman performance run`, 5 VUs, `--pass-if p99<2000`) runs on the
  same freshly-built code, on PRs and pushes alike, and gates the deploy. Deploy runs only if the QA
  and performance gates pass. AWS auth is via **GitHub OIDC** (no stored keys); the only repo secret
  is `POSTMAN_API_KEY`.

## Managing the AWS backend

```bash
cp runtime-vm/aws/aws.env.example runtime-vm/aws/aws.env   # set AWS_INSTANCE_ID + AWS_HOST
./control.sh status     # EC2 state + service health (via SSM)
./control.sh reset      # restart all services on the VM
./control.sh urls       # print the API URLs
./control.sh start|stop # start/stop the instance (it's meant to be always-on)
```

## Requirements
Postman Enterprise (API Catalog + Insights), the Postman CLI, an AWS account with SSM access, and
Node 20+ / `aws` CLI locally for `control.sh`. Deploying from scratch: see
[AWS-INSIGHTS.md](AWS-INSIGHTS.md).

**Running cost:** the always-on AWS backend is **≈ $16–19/month** (EC2 `t4g.small` + public IPv4 +
EBS). SSM, TLS, DNS, MQTT broker, and CI are free. Full breakdown in
[AWS-INSIGHTS.md → Cost](AWS-INSIGHTS.md#cost).
