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

**6. Developer portal + AI/MCP (Fern)** — a branded **Fern** portal (`fern/`) auto-generated from the
same OpenAPI specs, with an interactive API Explorer. It also hosts an **MCP server**
(`<docs>/_mcp/server`) and an `llms.txt` index so AI agents can query the docs — add the MCP URL to a
Postman **AI request → Tools**. See **Developer portal (Fern)** below.

## CI/CD pipeline (`.github/workflows/ci-cd.yml`)

Postman **local (git) is the source of truth**; the cloud workspace is the published mirror.

- **PR & push:** spec lint (local `*.yaml`) + a QA collection run against the **freshly-built** Orders
  code (an ephemeral `node` server in the runner) — this is **the gate**; a breaking change fails here
  and can't merge or deploy.
- **performance:** a `postman performance run` load-test (5 VUs, `--pass-if p99<2000`) on the same
  freshly-built code, running **in parallel** with lint+QA. It's an independent signal and does **not**
  block deploy or docs.
- **push → main:** once lint+QA pass, **`postman workspace push`** (local → Postman Cloud) + **deploy
  to AWS** via SSM + post-deploy smoke, and the **Fern docs** republish — all in parallel.
- AWS auth is via **GitHub OIDC** (no stored keys). Repo secrets: `POSTMAN_API_KEY` and `FERN_TOKEN`.

## Developer portal (Fern)

A **Fern** developer portal (`fern/`) is generated from the same `orders.yaml` / `payments.yaml` /
`users.yaml` OpenAPI specs — one contract driving the Postman workspace, the CI, and the docs site.
(Fern is a Postman company.) It publishes a Home landing page + three API-reference sections with an
interactive **API Explorer** (wired to the live AWS host), plus guide pages for the refund webhook,
MQTT flow, and AI agents. With **Ask Fern** enabled it also serves an **MCP server** at
`<docs>/_mcp/server` and an agent index at `<docs>/llms.txt` (read-only RAG over the docs; to let an
agent *execute* the API, generate an MCP from the spec in Postman AI Agent Builder).

```bash
npm install -g fern-api
fern docs dev          # local preview at http://localhost:3210 (no login)
fern check             # validate docs + specs (runs in CI too)
fern login && fern generate --docs   # publish to <org>.docs.buildwithfern.com
```

**CI:** the `docs` job runs `fern check` on every PR/push, and publishes on push to `main` — but
only once a `FERN_TOKEN` repo secret exists (it skips cleanly until then). Get the token with
`fern token` (after `fern login`) or from the Fern dashboard's API keys page, then add it under
Settings → Secrets and variables → Actions. Editing a spec and re-running `fern generate --docs`
(or pushing to `main`) updates the portal — docs-as-code.

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
