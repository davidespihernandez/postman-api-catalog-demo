# API Catalog demo — presenter script (SE)

**Audience:** Customer stakeholders  
**Duration:** ~15 minutes  
**Prerequisite:** [SE-INSTALL.md](SE-INSTALL.md) completed — all APIs deployed, workspace and catalog configured.

**No terminal during the demo.** Stay in the Postman app.

---

## What the customer should take away

| Value | What you show |
|-------|----------------|
| **Portfolio view** | Every API in one catalog — ownership, health, tags |
| **Per-service depth** | Spec quality, test results, latency — one click |
| **Operational signal** | QA collections feeding the **Test** environment |
| **Governance** | Scorecards and lint rules on OpenAPI (if configured) |

---

## Why this setup works

```text
Cloudflare Workers (pre-deployed, full CRUD)
        ↓
OpenAPI spec imported → documentation collections generated in Postman
        ↓
Demo workspace: 3 QA collections (CRUD validation per API)
        ↓
API Catalog ← workspace linked via Manual import
        ↓
Integrated services: Orders, Payments, Users → Test tab metrics from collection runs
```

Each API serves `/openapi.json`. Import the spec into Postman to generate documentation collections. The committed **QA collections** validate CRUD end-to-end and feed the catalog **Test** tab.

---

## Demo flow (Postman only)

| Act | Time | Where |
|-----|------|--------|
| **1 — Catalog portfolio** | ~4 min | API Catalog overview |
| **2 — Service deep-dive** | ~4 min | Orders API detail |
| **3 — QA tests** | ~4 min | Orders — QA collection |
| **4 — Test tab** | ~3 min | Catalog Test tab after QA run |

Use **Payments** or **Users** for acts 2–4 if the customer cares more about those domains.

---

## Act 1 — Catalog portfolio (~4 min)

1. **Home → API Catalog**
2. Show **Integrated services** — three APIs already there
3. Point out columns: owner, health score, tags (if set)
4. Open **Overview** dashboard — aggregate view across services

Say: *“This is the inventory your platform team wishes they had — live, searchable, tied to real specs and tests.”*

---

## Act 2 — Service deep-dive (~4 min)

1. Click **Orders API**
2. **Overview** tab — health score breakdown (runtime / test / spec)
3. **Development** tab — OpenAPI from imported spec; governance / lint (if configured)
4. **Test** tab — mention runs will populate in Act 4

Say: *“We imported the OpenAPI spec to generate documentation. The QA collection validates every CRUD operation against the live API.”*

Optional: show generated docs collection or `{{baseUrl}}/openapi.json` (Production Orders environment) in a browser tab you opened before the call.

---

## Act 3 — QA tests (~4 min)

1. Switch to **API Catalog Demo** workspace
2. Open **Orders - QA** collection
3. **Run** collection (Collection Runner) — seven requests in sequence:
   - Create → Get → List → PUT → PATCH → Delete → Get (404)
4. All tests pass — real HTTPS against Cloudflare

Say: *“Each step chains to the next — create a record, verify it, update it, delete it, confirm it’s gone. The catalog ties this collection to Orders because the workspace is connected.”*

Optional: quickly run **Payments - QA** to show the same pattern across services.

---

## Act 4 — Test tab (~3 min)

1. Return to **API Catalog → Orders API → Test** tab
2. Show pass rate and assertions from the QA run
3. **Overview** — scorecard reflecting test activity

Say: *“Collection runs roll up to the catalog Test environment. Production runtime metrics — error rate over time, 7-day p95 — come from Insights when you instrument your stack.”*

---

## Strong closes (pick one)

- **Platform lead:** “One catalog for discovery, quality gates, and test signal — without a separate CMDB.”
- **API lead:** “Specs, tests, and health per service — Development and Test tabs map to how you already work.”
- **Engineering lead:** “Import OpenAPI, generate docs, add QA collections — catalog as source of truth on day one.”

---

## Customer Q&A

| Question | Answer |
|----------|--------|
| “Where do APIs come from?” | Workspace linked to catalog; also gateway connectors, Git, Insights for other teams. |
| “Is traffic production?” | Test tab = collection/monitor runs. Production tab = Insights Agent. |
| “Can we add APIs without scripts?” | Yes — import OpenAPI or collections into the workspace, refresh discovery, integrate. |
| “Why Cloudflare?” | Free, fast setup for demos; any HTTPS + OpenAPI backend works the same way. |

---

## Troubleshooting live

| Problem | Fix |
|---------|-----------|
| 404 on requests | Environment URLs wrong — fix before demo in SE-INSTALL |
| Test tab empty | Run QA collection first; confirm Test env linkage |
| Service missing | Integrated services vs Service discovery — integrate if pending |
| CRUD test fails | Redeploy workers (`./demo.sh deploy`) — APIs must include PUT/PATCH/DELETE |

Maintenance (not during demo): `./demo.sh deploy` and update environment URLs.
