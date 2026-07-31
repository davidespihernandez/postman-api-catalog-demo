# Runtime Health via Postman Insights (Oracle Cloud Free VM)

How to light up the **Runtime Health** panel in the API Catalog (P95 latency, availability,
latency regression, 4xx error rate). These metrics are populated **only** by the Postman
Insights agent observing live traffic — there is no CLI/CI path for them.

> One-time setup for Solutions Engineers. Do this **before** a customer demo, not during it.

> ⚠️ **Both backends are permanent — this is not a migration.** The Cloudflare Workers stay
> live *and* the Oracle VM runs alongside them, forever. You pick which one a demo uses via
> **Postman environments** (see "Environment strategy" below). Never run `./demo.sh undeploy` or
> delete the Cloudflare workers — they remain a first-class backend. The Oracle VM simply adds a
> second, self-hosted backend that can also feed Runtime Health via the Insights agent.

## Environment strategy — two backends, pick per situation

Keep both backends and switch with a dropdown. Each Postman environment already carries a
`baseUrl`; we keep the Cloudflare ones and add a parallel Oracle set:

| Situation | Environment | `baseUrl` points at |
|-----------|-------------|---------------------|
| Standard demo, fast edge, zero infra | **Production *** (existing) | `https://…workers.dev` (Cloudflare) |
| Showing **Runtime Health** / Insights / self-hosted story | **Oracle *** (new, added in Part 2) | `https://<your-hostname>` (Oracle VM) |

Same collections, same tests — only `baseUrl` changes. This makes a great demo beat on its own:
*"same API contract and test suite, Cloudflare edge vs. self-hosted VM, one dropdown."* The
Insights agent only observes the Oracle VM, so Runtime Health reflects traffic sent to the
**Oracle** environment (or a scheduled synthetic run against it).

---

## Why we need a VM (and can't use Cloudflare)

Our backends run as **Cloudflare Workers** (serverless V8 isolates). The Insights agent is a
**passive traffic sniffer** — it runs as a sidecar/daemon next to the service and captures
packets off a network interface (libpcap for plaintext, eBPF for HTTPS). A Cloudflare Worker
has **no host, no container, and no network interface** you can attach a sidecar to, so the
agent cannot run there.

The fix: host an **observable copy of the backend on a real Linux VM**, run the Insights agent
beside it, and point our test traffic (GitHub Actions collection + perf runs) at that VM. The
agent reports what it sees to the catalog and Runtime Health populates.

**Why Oracle Cloud:** it's the only major cloud with an **Always Free** (forever, not 12-month)
Linux VM that gives you **root + a public IP + full packet-capture privileges** — everything the
agent needs. Fly.io killed its free tier in 2024; Google Cloud Run and Render's free tier are
sandboxed and block raw-socket capture; Cloudflare/Vercel/Netlify have the same serverless
problem we're trying to escape.

```
GitHub Actions ──HTTPS──▶ Caddy (:443, auto Let's Encrypt)
                              │  plaintext HTTP (loopback)
                              ▼
                     backend (wrangler dev, 127.0.0.1:80xx)  ◀── Insights agent sniffs
                                                                  (libpcap, plaintext — no eBPF)
```

Terminating TLS at Caddy and letting the agent sniff the **plaintext** hop behind it means we
avoid eBPF and kernel-version headaches entirely.

This document covers **Part 1 — provisioning the VM**. Part 2 (the Docker/Caddy/agent stack that
runs on it) is tracked separately and dropped onto the box once it exists.

---

## What you'll end up with

| Item | Value |
|------|-------|
| Cloud | Oracle Cloud Infrastructure (OCI), **Always Free** tier |
| VM | 1 Ubuntu 22.04 instance, ARM `VM.Standard.A1.Flex`, ~1 OCPU / 6 GB |
| Access | SSH key-based login as `ubuntu` |
| Networking | Public IPv4, ingress open on 22 / 80 / 443 |
| Hostname | A free DNS name (DuckDNS or nip.io) → your public IP, so Caddy can get a TLS cert |
| Software | Docker Engine + Compose plugin |

**Prerequisites:** a valid credit card (Oracle uses it for identity verification — a temporary
hold, no charge; prepaid/virtual cards are rejected), a phone number, and ~30–45 min.

---

## Part 1 — Create an Oracle Cloud account

1. Go to <https://www.oracle.com/cloud/free/> → **Start for free**.
2. Enter email, verify, then fill in account details.
   - **Home region matters and cannot be changed later.** Pick one close to you that has ARM
     (A1) capacity. If you hit "Out of host capacity" later (common with ARM), see Troubleshooting.
3. Verify your phone number and complete **credit-card verification**. You'll see a small
   temporary authorization hold that drops off; Always Free resources never charge unless you
   explicitly upgrade to Pay As You Go.
4. Wait for the account to finish provisioning (a few minutes), then sign in to the
   [OCI Console](https://cloud.oracle.com).

> **Stay on Always Free.** When creating anything, look for the green **"Always Free eligible"**
> label. If it's not labelled, don't create it.

---

## Part 2 — Provision the VM

1. In the Console, open the menu (☰) → **Compute → Instances → Create instance**.
2. **Name:** `postman-insights-demo`.
3. **Image and shape** → **Edit**:
   - **Image:** Canonical **Ubuntu 22.04** (Always Free eligible).
   - **Shape:** click **Change shape → Ampere (Arm) → `VM.Standard.A1.Flex`**. Set **1 OCPU /
     6 GB** (well inside the free limit of 2 OCPU / 12 GB). This leaves headroom for a second VM
     later if you want one.
     - *If ARM capacity is unavailable*, fall back to **`VM.Standard.E2.1.Micro`** (AMD, Always
       Free) — but it only has **1 GB RAM**, which is tight; prefer ARM and retry (see
       Troubleshooting).
4. **Networking:** leave the default — it will **Create new VCN** with a public subnet. Ensure
   **"Assign a public IPv4 address"** is **Yes**.
5. **Add SSH keys:** choose **Generate a key pair for me** and **download both** the private and
   public keys (or paste your own public key). Save the private key somewhere safe, e.g.:
   ```bash
   mkdir -p ~/.ssh && mv ~/Downloads/ssh-key-*.key ~/.ssh/oracle-insights.key
   chmod 600 ~/.ssh/oracle-insights.key
   ```
6. Click **Create**. When the instance state is **Running**, copy its **Public IP address** from
   the instance details page.
7. SSH in (default user for Ubuntu images is `ubuntu`):
   ```bash
   ssh -i ~/.ssh/oracle-insights.key ubuntu@<PUBLIC_IP>
   ```

---

## Part 3 — Open the network (two firewalls!)

Oracle blocks traffic in **two** places. You must open **both** or Caddy/HTTP will time out.

### 3a. Cloud firewall — Security List / NSG

1. From the instance page, click the **subnet** link → open its **Security List** (the default
   one) → **Add Ingress Rules**.
2. Add these ingress rules (Source CIDR `0.0.0.0/0`, IP Protocol **TCP**):

   | Port | Purpose |
   |------|---------|
   | 22  | SSH (usually already present) |
   | 80  | HTTP — Let's Encrypt challenge + redirect |
   | 443 | HTTPS — the public API URL |

### 3b. Host firewall — Ubuntu's iptables (the classic gotcha)

Oracle's Ubuntu images ship with **restrictive iptables rules** that drop 80/443 even after 3a.
On the VM, add explicit ACCEPT rules and persist them:

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save   # if missing: sudo apt-get install -y iptables-persistent
```

> If you ever see "port 80 open in OCI but connection times out," it's almost always 3b.

---

## Part 4 — Install Docker

On the VM:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# run docker without sudo
sudo usermod -aG docker $USER
newgrp docker

docker run --rm hello-world   # verify
```

---

## Part 5 — Point a hostname at the VM (for TLS)

Caddy needs a real hostname to fetch a Let's Encrypt certificate. Two free options:

- **nip.io (zero signup):** just use `<PUBLIC_IP-with-dashes>.nip.io`, e.g. for IP `130.61.1.2`
  the hostname is `130-61-1-2.nip.io`. Nothing to configure — it resolves automatically. Simplest
  for a quick demo.
- **DuckDNS (stable name, free):** sign in at <https://www.duckdns.org>, create a subdomain
  (e.g. `postman-insights.duckdns.org`), set its IP to your VM's public IP. Better if you want a
  name that survives an IP change.

Record your chosen hostname — Part 2's `Caddyfile` and the Postman environment URLs will use it.

---

## Verification checklist

Before moving on, confirm:

- [ ] `ssh -i ~/.ssh/oracle-insights.key ubuntu@<PUBLIC_IP>` works
- [ ] `docker run --rm hello-world` succeeds
- [ ] Ingress rules for 80/443 exist in the OCI Security List (3a)
- [ ] iptables ACCEPT rules for 80/443 saved on the host (3b)
- [ ] Your hostname resolves to the public IP: `dig +short <your-hostname>` returns the IP
- [ ] You have your Insights **workspace-id** and **system-env** from the catalog (the values in
      the Catalog's "No endpoint data available" panel — each SE has their own, generated from
      their own workspace)

---

## Next step — deploy the stack

With the VM ready, the next document/stack deploys onto it:

1. The three APIs via `wrangler dev` (the existing Worker code, unchanged) on loopback ports
2. **Caddy** terminating TLS on your hostname → proxying to the APIs
3. The **Postman Insights agent** sniffing the plaintext hop, run with:
   ```
   postman-insights-agent apidump --workspace-id <WORKSPACE_ID> --system-env <SYSTEM_ENV>
   ```
4. Add the parallel **Oracle *** Postman environments (`baseUrl` → `https://<your-hostname>`),
   keeping the existing **Production *** (Cloudflare) environments as-is.
5. Generate observed traffic against the VM URL — via a **parallel** CI job or a scheduled
   synthetic run — so the agent always has traffic to report. The existing Cloudflare-targeted
   jobs stay untouched.

**Validation gate before you rely on the Oracle environment in a live demo:**

- [ ] Runtime Health tiles in the catalog leave `N/A` and show real values
- [ ] Endpoints appear under "Top 5 Highest Error Rate Endpoints" (Insights sees your traffic)
- [ ] Collection tests pass against `https://<your-hostname>` exactly as they do on Cloudflare

Both backends remain available regardless — the gate is just "is the Oracle path demo-ready yet."

*(Part 2 stack — Dockerfile / docker-compose / Caddyfile / deploy script — is added to the repo
once built.)*

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| **"Out of host capacity"** creating an ARM A1 instance | Common for ARM. Retry in a few hours, try a different Availability Domain, or switch home-region strategy. Quick workaround: use the AMD `VM.Standard.E2.1.Micro` shape (1 GB RAM — tight but works for a light demo). |
| Port 80/443 open in OCI but connection times out | The host iptables rules (Part 3b) aren't applied/saved. Re-run the iptables commands and `netfilter-persistent save`. |
| SSH "Permission denied (publickey)" | Wrong user (use `ubuntu` for Ubuntu images), wrong key, or key perms not `600` (`chmod 600 <key>`). |
| Let's Encrypt cert fails in Caddy (Part 2) | Hostname doesn't resolve to the VM yet (check `dig +short`), or port 80 is blocked (Parts 3a/3b). |
| Credit card rejected at signup | Oracle rejects prepaid/virtual/single-use/PIN-debit cards. Use a standard credit card. |
| Worried about charges | Stay on **Always Free** (green label). Optionally set a **Budget** alert in the Console (Billing → Budgets) at $1 as a tripwire. |
