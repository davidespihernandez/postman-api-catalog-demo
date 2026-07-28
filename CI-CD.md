# CI/CD — Orders QA collection (Postman CLI)

Run **Orders - QA** from GitHub Actions to demo API testing in the pipeline. The workflow uses your **cloud** collection and environment (by ID), not files in this repo.

Workflow file: [`.github/workflows/postman-orders-qa.yml`](.github/workflows/postman-orders-qa.yml)

---

## One-time setup

### 1. Deploy Orders API

The QA collection hits the live worker. From repo root:

```bash
./demo.sh deploy
./demo.sh urls
```

Note the Orders URL (e.g. `https://postman-api-catalog-demo-orders.<subdomain>.workers.dev`).

### 2. Postman environment (cloud)

In the **API Catalog Demo** workspace, open **Production Orders** and set:

| Variable | Value |
|----------|--------|
| `baseUrl` | Your Orders worker URL from `./demo.sh urls` |

Save in Postman (syncs to cloud). The workflow uses environment ID `53522859-60759898-5954-4a4a-8ed6-645707a414fa` — if you recreated the environment, update the ID in the workflow file.

### 3. Verify collection locally

In Postman, run **Orders - QA** with **Production Orders** selected. All requests should pass before wiring CI.

### 4. Postman API key

1. Postman → **Settings** → **API keys** → **Generate API key**
2. Copy the key (starts with `PMAK-...`)

The key must access the workspace that owns the collection and environment.

### 5. GitHub secret

1. GitHub repo → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret**
3. Name: `POSTMAN_API_KEY`
4. Value: your Postman API key

### 6. Push the workflow

Commit and push `.github/workflows/postman-orders-qa.yml` to `main`.

---

## Verify CI

1. GitHub → **Actions** → **Orders API — QA tests**
2. **Run workflow** (manual) or push to `main`
3. Job should finish green if Orders worker is up and environment `baseUrl` is correct

---

## Customer demo (simple flow)

**Story:** *“The same QA collection we run in Postman is gated in CI — every push runs the full CRUD flow against production-like APIs.”*

| Step | Where | What to show |
|------|--------|--------------|
| 1 | Postman | **Orders - QA** collection — 7 chained CRUD requests |
| 2 | GitHub | Repo → **Actions** → workflow file (Postman CLI, collection + env IDs) |
| 3 | GitHub | **Run workflow** → live log → green check |
| 4 | Postman (optional) | API Catalog → Orders API → **Test** tab after a run (if catalog linked to collection runs) |

**Tip:** Trigger **workflow_dispatch** during the call so the audience sees the run live. Have `./demo.sh smoke orders` or a quick collection run as backup if GitHub is slow.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `401` / login failed | Check `POSTMAN_API_KEY` secret; regenerate key if needed |
| `404` on requests | Update **Production Orders** `baseUrl` in Postman cloud |
| Collection not found | Confirm collection ID in workflow; key must access that workspace |
| Flaky Create/Delete | Worker redeploy resets in-memory data — re-run collection; CRUD order matters |
| Wrong environment ID | Postman → environment → **Info** / sync metadata; update workflow `POSTMAN_ENVIRONMENT_ID` |

---

## Extending to Payments / Users

Duplicate the workflow (or add matrix jobs) with:

| API | Collection ID | Environment ID |
|-----|---------------|----------------|
| Orders | `53522859-54ec8184-2939-4cef-8528-56e1936bbae5` | `53522859-60759898-5954-4a4a-8ed6-645707a414fa` |
| Payments | *(your Payments - QA ID)* | `53522859-0c1c905b-c1c9-4abd-a4e5-f56885d2287c` |
| Users | *(your Users - QA ID)* | `53522859-3aac4c3c-d2bc-4aeb-b14a-433172452b1a` |

Get IDs from Postman (collection/environment info) or `.postman/resources.yaml` after sync.
