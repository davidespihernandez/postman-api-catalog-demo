# Retail Commerce APIs — Documentation

This workspace contains the API documentation for the three core services of the Retail Commerce platform: **Orders**, **Payments**, and **Users**. All services are self-hosted on AWS (Node/Express behind Caddy) and expose a RESTful JSON API.

## Overview

| Service | Base URL | Collection |
|---------|----------|------------|
| Orders | `{{baseUrl}}` | Orders - Doc |
| Payments | `{{baseUrl}}` | Payments - Doc |
| Users | `{{baseUrl}}` | Users - Doc |

> **Tip:** All endpoints (except `/openapi.json`) support optional query parameters for testing:
> - `delay` / `wait` — milliseconds to wait before responding
> - `status` / `error` — HTTP status code to force (e.g. `?status=500` to simulate a server error)

---

## Orders API

Full CRUD management for customer orders.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health` | Health check |
| `GET` | `/openapi.json` | OpenAPI specification |
| `GET` | `/orders` | List all orders |
| `POST` | `/orders` | Create a new order |
| `GET` | `/orders/:id` | Get an order by ID |
| `PUT` | `/orders/:id` | Replace an order (full update) |
| `PATCH` | `/orders/:id` | Partially update an order |
| `DELETE` | `/orders/:id` | Delete an order |

### GET /orders — List orders

Returns a paginated-style list of all orders.

**Response 200 — Paginated-style order list**
```json
{
  "data": [...],
  "total": 10
}
```

### POST /orders — Create order

**Request body**
```json
{
  "customerId": "usr-002",
  "total": 99.5,
  "status": "pending",
  "currency": "USD"
}
```

**Response 201 — Order created**

**Response 400 — Invalid request** (missing or invalid fields)

**Response 500 — Internal server error** (simulatable with `?status=500`)

### GET /orders/:id — Get order by ID

**Response 200 — Order found**

**Response 404 — Order not found**

### PUT /orders/:id — Replace order

Replaces the full order resource. Same request body as POST.

**Response 200 — Order updated**

**Response 404 — Order not found**

**Response 400 — Invalid request**

### PATCH /orders/:id — Partially update order

Send only the fields you want to update.

**Response 200 — Order patched**

**Response 404 — Order not found**

### DELETE /orders/:id — Delete order

**Response 200 — Order deleted**

**Response 404 — Order not found**

---

## Payments API

Full CRUD management for payments, plus a dedicated refund endpoint.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health` | Health check |
| `GET` | `/openapi.json` | OpenAPI specification |
| `GET` | `/payments` | List all payments |
| `POST` | `/payments` | Create a new payment |
| `GET` | `/payments/:id` | Get a payment by ID |
| `PUT` | `/payments/:id` | Replace a payment (full update) |
| `PATCH` | `/payments/:id` | Partially update a payment |
| `DELETE` | `/payments/:id` | Delete a payment |
| `POST` | `/payments/refund` | Refund a payment |

### POST /payments — Create payment

**Request body**
```json
{
  "orderId": "ord-001",
  "amount": 49.99,
  "status": "completed",
  "currency": "USD"
}
```

**Response 201 — Payment created**

**Response 400 — Invalid request**

**Response 500 — Internal server error** (simulatable with `?status=500`)

### GET /payments/:id — Get payment by ID

**Response 200 — Payment found**

**Response 404 — Payment not found**

### PUT /payments/:id — Replace payment

**Response 200 — Payment updated**

**Response 404 — Payment not found**

**Response 400 — Invalid request**

### PATCH /payments/:id — Partially update payment

**Response 200 — Payment patched**

**Response 404 — Payment not found**

### DELETE /payments/:id — Delete payment

**Response 200 — Payment deleted**

**Response 404 — Payment not found**

### POST /payments/refund — Refund a payment

Refunds a payment by ID. On success, the Payments service asynchronously publishes a `payment.refunded` event to the external **Payment Refund Webhook API**.

**Request body**
```json
{
  "paymentId": "pay-001",
  "reason": "customer_request"
}
```

**Response 200 — Refund processed**

**Response 404 — Payment not found**

**Response 400 — Invalid request**

---

## Users API

Full CRUD management for user accounts.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health` | Health check |
| `GET` | `/openapi.json` | OpenAPI specification |
| `GET` | `/users` | List all users |
| `POST` | `/users` | Create a new user |
| `GET` | `/users/:id` | Get a user by ID |
| `PUT` | `/users/:id` | Replace a user (full update) |
| `PATCH` | `/users/:id` | Partially update a user |
| `DELETE` | `/users/:id` | Delete a user |

### POST /users — Create user

**Request body**
```json
{
  "name": "QA Test User",
  "email": "qa.test@example.com",
  "role": "customer"
}
```

**Response 201 — User created**

**Response 400 — Invalid request**

**Response 500 — Internal server error** (simulatable with `?status=500`)

### GET /users/:id — Get user by ID

**Response 200 — User found**

**Response 404 — User not found**

### PUT /users/:id — Replace user

**Response 200 — User updated**

**Response 404 — User not found**

**Response 400 — Invalid request**

### PATCH /users/:id — Partially update user

**Response 200 — User patched**

**Response 404 — User not found**

### DELETE /users/:id — Delete user

**Response 200 — User deleted**

**Response 404 — User not found**

---

## Common Patterns

### Health Check

All three services expose `GET /health`. A healthy response looks like:

```json
{ "status": "ok" }
```

### OpenAPI Specification

All three services expose `GET /openapi.json` returning the full OpenAPI 3.x document for that service. This endpoint does **not** support the `delay`/`status` simulation parameters.

### Error Simulation

Append `?status=<code>` to any endpoint (except `/openapi.json`) to force a specific HTTP response code. Useful for testing error-handling logic in clients.

### Collections

- [Orders - Doc](collection/53522859-0cc5e110-e343-488d-866d-195c6694abfb)
- [Payments - Doc](collection/53522859-3b2eb094-4abe-443f-be17-5c0f5fb2a1ad)
- [Users - Doc](collection/53522859-c1a24ffb-679a-4357-8015-f980737b1bf8)