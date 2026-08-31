import { useEffect, useState } from "react";
import {
  listOrders,
  createOrder,
  updateOrder,
  deleteOrder,
} from "./api.js";

const STATUSES = ["pending", "paid", "shipped", "delivered", "cancelled"];

export default function App() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ customerId: "", total: "" });
  const [busy, setBusy] = useState(false);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      setOrders(await listOrders());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await createOrder(form);
      setForm({ customerId: "", total: "" });
      await refresh();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleAdvanceStatus(order) {
    const next = STATUSES[(STATUSES.indexOf(order.status) + 1) % STATUSES.length];
    setBusy(true);
    setError(null);
    try {
      await updateOrder(order.id, { status: next });
      await refresh();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete(order) {
    setBusy(true);
    setError(null);
    try {
      await deleteOrder(order.id);
      await refresh();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="app">
      <header>
        <h1>Orders</h1>
        <p className="subtitle">Postman API Catalog demo · AWS backend</p>
      </header>

      <section className="card">
        <h2>New order</h2>
        <form className="new-order" onSubmit={handleCreate} data-testid="create-form">
          <label>
            Customer ID
            <input
              data-testid="input-customerId"
              value={form.customerId}
              onChange={(e) => setForm({ ...form, customerId: e.target.value })}
              placeholder="usr-001"
              required
            />
          </label>
          <label>
            Total
            <input
              data-testid="input-total"
              type="number"
              step="0.01"
              min="0"
              value={form.total}
              onChange={(e) => setForm({ ...form, total: e.target.value })}
              placeholder="49.99"
              required
            />
          </label>
          <button type="submit" data-testid="submit-order" disabled={busy}>
            {busy ? "Working…" : "Create order"}
          </button>
        </form>
      </section>

      {error && (
        <p className="error" data-testid="error">
          {error}
        </p>
      )}

      <section className="card">
        <div className="table-head">
          <h2>All orders</h2>
          <button className="ghost" onClick={refresh} data-testid="refresh" disabled={busy}>
            Refresh
          </button>
        </div>
        {loading ? (
          <p data-testid="loading">Loading…</p>
        ) : (
          <table data-testid="orders-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Customer</th>
                <th>Status</th>
                <th>Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} data-testid={`order-row-${o.id}`}>
                  <td className="mono">{o.id}</td>
                  <td className="mono">{o.customerId}</td>
                  <td>
                    <span className={`badge badge-${o.status}`}>{o.status}</span>
                  </td>
                  <td>
                    {o.total.toLocaleString(undefined, {
                      style: "currency",
                      currency: o.currency || "USD",
                    })}
                  </td>
                  <td className="actions">
                    <button
                      className="ghost"
                      onClick={() => handleAdvanceStatus(o)}
                      data-testid={`advance-${o.id}`}
                      disabled={busy}
                    >
                      Advance status
                    </button>
                    <button
                      className="danger"
                      onClick={() => handleDelete(o)}
                      data-testid={`delete-${o.id}`}
                      disabled={busy}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan="5" className="empty">
                    No orders yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </section>
    </main>
  );
}
