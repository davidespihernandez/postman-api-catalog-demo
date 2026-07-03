# Postman API Catalog Demo

Real HTTPS APIs on **Cloudflare Workers** (free tier) for repeatable **API Catalog** demos.

```
Setup (terminal once)  →  Cloudflare Workers
Customer demo          →  Postman app only (catalog + collections)
```

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

| Doc | Audience |
|-----|----------|
| [SE-INSTALL.md](SE-INSTALL.md) | SE one-time setup (includes deploy) |
| [DEMO-STEPS.md](DEMO-STEPS.md) | Customer demo (Postman app only) |

## Setup (before the demo)

```bash
./demo.sh setup
./demo.sh setup-subdomain   # one-time: pick your *.workers.dev name
./demo.sh deploy
./demo.sh urls
```

`setup-subdomain` must run in an **interactive terminal** (wrangler prompts for your subdomain). After that, `deploy` is non-interactive.

Import environment + three QA collections from `postman/`. Import each API's OpenAPI spec from `{{*ApiUrl}}/openapi.json` to generate documentation collections. Full walkthrough: [SE-INSTALL.md](SE-INSTALL.md).

## Demo commands

| Command | Purpose |
|---------|---------|
| `./demo.sh setup` | Cloudflare login + npm install |
| `./demo.sh setup-subdomain` | One-time `*.workers.dev` subdomain (interactive) |
| `./demo.sh deploy` | Deploy all APIs + smoke test |
| `./demo.sh reset` | Same as deploy (redeploy all) |
| `./demo.sh smoke` | Health-check all workers |
| `./demo.sh urls` | Print worker URLs |

## Postman assets

| File | Purpose |
|------|---------|
| `*-qa.collection.json` | CRUD validation per API (create → get → list → PUT → PATCH → delete → 404) |
| `demo.environment.json` | Worker base URLs |

Documentation collections are **generated from OpenAPI** in Postman — not committed to this repo.

## Requirements

Node.js 18+, curl, Cloudflare (free), Postman Enterprise with API Catalog.
