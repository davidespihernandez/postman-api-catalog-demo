# Agent context: postman-api-catalog-demo

GitHub: https://github.com/davidespihernandez/postman-api-catalog-demo

## Purpose

Postman **API Catalog** demo: three REST APIs + Postman workspace (QA + Doc collections) +
**Insights/Runtime Health**, an async **MQTT** flow, and a **payment refund webhook** — all
self-hosted on **AWS**, driven through a real **CI/CD pipeline**, and documented via a branded
**Fern developer portal** (with an MCP server for AI agents) generated from the same OpenAPI specs.

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
- `fern/` — Fern developer portal (`docs.yml`; `apis/{orders,payments,users}/generators.yml` point at
  the root OpenAPI specs; `pages/*.mdx` guides). `fern check` / `fern docs dev` / `fern generate --docs`.
- `.github/workflows/ci-cd.yml` — jobs: `test` (lint+QA, the gate) · `performance` (load-test, runs in parallel, non-blocking) · `release` (sync + deploy to AWS, push-to-main) · `docs` (Fern publish, push-to-main). `release` and `docs` need `test` only.
- `.github/perf-local.env.yaml` — baseUrl for the perf load-test (`performance run` has no `--env-var`)

## Key flows
- **Refund webhook:** `POST /payments/refund {"paymentId":"pay-001"}` → Payments worker POSTs
  `payment.refunded` to `REFUND_WEBHOOK_URL` (set in the payments service's env on the VM).
- **MQTT:** Postman publishes to `broker.hivemq.com:1883` topic `postman-api-catalog-demo/notifications`
  → the always-on `mqtt-bridge` forwards `notification.processed` to `NOTIFICATION_WEBHOOK_URL`.
- **CI/CD:** git is source of truth. `test` (spec lint + QA vs freshly-built code) is **the gate** —
  a breaking change fails here → no merge/deploy. `performance` (`postman performance run`,
  `--pass-if p99<2000`) runs **in parallel and is non-blocking**. On push to main, `release`
  (workspace push + deploy to AWS via SSM) and `docs` (`fern generate --docs`) both gate on `test`
  only. The deploy step self-heals the VM's git remote (anonymous public URL, HTTP/1.1, retry).
- **Fern docs:** `api:` nav items auto-generate the API reference from the specs; `pages/*.mdx` are
  guides. MCP server at `<docs>/_mcp/server` (Ask Fern RAG, read-only), agent index at `<docs>/llms.txt`.

## Gotchas
- Services run as `ubuntu` (never root — SSM runs as root). Node 20+.
- `*.webhook.pstmn.io` has a CNAME glibc won't follow on Linux → pinned in `/etc/hosts` by deploy.
- Manage via SSM (`./control.sh`), not SSH (subnet NACL blocks 22).
- Collection `variables` must be a **list** (`- key:` / `value:`), not a map — `workspace push` 400s on the map form.
- Fern's docs MCP is read-only (RAG over docs); to *execute* the API, generate an MCP from the spec/collection in Postman AI Agent Builder.

## Replicating from a fork (for humans or AI agents)

To stand this up on new accounts, follow **`AWS-INSIGHTS.md`** end to end — it's the ordered,
self-contained runbook. Do this first, then the numbered steps:

0. **Replace all account-specific values** — see **"Make it yours (fork checklist)"** in
   `AWS-INSIGHTS.md` (base URL/host, AWS instance/region/deploy-role, fork owner in the deploy git
   URL, Postman collection/env IDs, Fern org + docs subdomain, repo secrets).
1. **Postman** — connect the git workspace to your team; capture `INSIGHTS_WORKSPACE_ID` / `INSIGHTS_SYSTEM_ENV`.
2. **VM** — provision EC2 `t4g.small` + Elastic IP; **enable SSM**; store the API key as an SSM SecureString.
3. **Deploy** — via SSM run `deploy-runtime.sh`; install the 1-min synthetic-traffic cron.
4. **CI/CD** — create the GitHub OIDC role; add repo secrets `POSTMAN_API_KEY` + `FERN_TOKEN`; edit the `env:` block in `.github/workflows/ci-cd.yml`.
5. **Fern portal** — set your org in `fern/`, `fern login`, `fern generate --docs`.

Prereqs: Postman Enterprise (API Catalog + Insights), an AWS account, a Fern account, Node 20+, and
the `aws` / `postman` / `fern-api` CLIs.

See `README.md` (overview + demos), `AWS-INSIGHTS.md` (full runbook + fork checklist),
`runtime-vm/README.md` (on-VM stack).
