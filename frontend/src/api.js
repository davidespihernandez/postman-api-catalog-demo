// Thin client for the Cloudflare Orders Worker.
// Every call here becomes browser -> API network traffic that Postman captures
// during the Playwright run and matches against the "Orders - QA" collection.

const BASE =
  import.meta.env.VITE_ORDERS_API_URL ??
  "https://postman-api-catalog-demo-orders.davidespi.workers.dev";

async function handle(res) {
  if (res.status === 204) return null;
  const body = await res.json();
  if (!res.ok) {
    throw new Error(body?.error ?? `Request failed with ${res.status}`);
  }
  return body;
}

export async function listOrders() {
  const body = await handle(await fetch(`${BASE}/orders`));
  return body.data;
}

export async function getOrder(id) {
  return handle(await fetch(`${BASE}/orders/${id}`));
}

export async function createOrder({ customerId, total, currency = "USD" }) {
  return handle(
    await fetch(`${BASE}/orders`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ customerId, total: Number(total), currency }),
    }),
  );
}

export async function updateOrder(id, patch) {
  return handle(
    await fetch(`${BASE}/orders/${id}`, {
      method: "PATCH",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(patch),
    }),
  );
}

export async function deleteOrder(id) {
  return handle(
    await fetch(`${BASE}/orders/${id}`, { method: "DELETE" }),
  );
}
