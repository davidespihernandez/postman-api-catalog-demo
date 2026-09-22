const http = require('http');
const PORT = process.env.PORT || 4500;

// Seed data — pre-populate state on first boot
async function seedIfEmpty() {
  const size = await pm.state.size();
  if (size === 0) {
    const seed = [
      { id: 'ord-001', customerId: 'usr-001', status: 'shipped', total: 49.99, currency: 'USD' },
      { id: 'ord-002', customerId: 'usr-002', status: 'pending', total: 99.50, currency: 'USD' },
      { id: 'ord-003', customerId: 'usr-003', status: 'processing', total: 149.99, currency: 'EUR' },
    ];
    for (const order of seed) {
      await pm.state.set('orders:' + order.id, order);
    }
    await pm.state.set('orders:ids', seed.map(o => o.id));
  }
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', chunk => { data += chunk; });
    req.on('end', () => {
      try { resolve(data ? JSON.parse(data) : {}); }
      catch (e) { reject(e); }
    });
    req.on('error', reject);
  });
}

function json(res, status, body) {
  res.writeHead(status, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(body));
}

function generateId() {
  return 'ord-' + Math.random().toString(36).slice(2, 10);
}

const server = http.createServer(async (req, res) => {
  await seedIfEmpty();

  const { method, url } = req;
  const urlObj = new URL(url, 'http://localhost');
  const pathname = urlObj.pathname;

  // @endpoint GET /health
  if (method === 'GET' && pathname === '/health') {
    json(res, 200, { status: 'ok', service: 'orders-api', version: '1.0.0' });
    return;
  }

  // @endpoint GET /openapi.json
  if (method === 'GET' && pathname === '/openapi.json') {
    json(res, 200, { openapi: '3.0.3', info: { title: 'Orders API', version: '1.0.0' }, paths: {} });
    return;
  }

  // @endpoint GET /orders
  if (method === 'GET' && pathname === '/orders') {
    const ids = (await pm.state.get('orders:ids')) || [];
    const orders = [];
    for (const id of ids) {
      const order = await pm.state.get('orders:' + id);
      if (order) orders.push(order);
    }
    json(res, 200, { data: orders, count: orders.length });
    return;
  }

  // @endpoint POST /orders
  if (method === 'POST' && pathname === '/orders') {
    let body;
    try { body = await readBody(req); } catch (e) {
      json(res, 400, { error: 'Invalid JSON body' });
      return;
    }
    if (!body.customerId || body.total === undefined) {
      json(res, 400, { error: 'customerId and total are required' });
      return;
    }
    const newOrder = {
      id: generateId(),
      customerId: body.customerId,
      status: body.status || 'pending',
      total: body.total,
      currency: body.currency || 'USD',
    };
    await pm.state.set('orders:' + newOrder.id, newOrder);
    const ids = (await pm.state.get('orders:ids')) || [];
    ids.push(newOrder.id);
    await pm.state.set('orders:ids', ids);
    json(res, 201, newOrder);
    return;
  }

  // @endpoint GET /orders/:id
  const getMatch = pathname.match(/^\/orders\/([^/]+)$/);
  if (getMatch) {
    const id = getMatch[1];

    if (method === 'GET') {
      const order = await pm.state.get('orders:' + id);
      if (!order) { json(res, 404, { error: 'Order not found' }); return; }
      json(res, 200, order);
      return;
    }

    // @endpoint PUT /orders/:id
    if (method === 'PUT') {
      const existing = await pm.state.get('orders:' + id);
      if (!existing) { json(res, 404, { error: 'Order not found' }); return; }
      let body;
      try { body = await readBody(req); } catch (e) {
        json(res, 400, { error: 'Invalid JSON body' });
        return;
      }
      if (!body.customerId || body.total === undefined || !body.status) {
        json(res, 400, { error: 'customerId, total and status are required' });
        return;
      }
      const updated = {
        id,
        customerId: body.customerId,
        status: body.status,
        total: body.total,
        currency: body.currency || existing.currency,
      };
      await pm.state.set('orders:' + id, updated);
      json(res, 200, updated);
      return;
    }

    // @endpoint PATCH /orders/:id
    if (method === 'PATCH') {
      const existing = await pm.state.get('orders:' + id);
      if (!existing) { json(res, 404, { error: 'Order not found' }); return; }
      let body;
      try { body = await readBody(req); } catch (e) {
        json(res, 400, { error: 'Invalid JSON body' });
        return;
      }
      const patched = { ...existing, ...body, id };
      await pm.state.set('orders:' + id, patched);
      json(res, 200, patched);
      return;
    }

    // @endpoint DELETE /orders/:id
    if (method === 'DELETE') {
      const existing = await pm.state.get('orders:' + id);
      if (!existing) { json(res, 404, { error: 'Order not found' }); return; }
      await pm.state.delete('orders:' + id);
      const ids = (await pm.state.get('orders:ids')) || [];
      await pm.state.set('orders:ids', ids.filter(i => i !== id));
      res.writeHead(204);
      res.end();
      return;
    }
  }

  json(res, 404, { error: 'Mock route not defined', method, url });
});

server.listen(PORT, () => console.log('Orders API Mock running on port ' + PORT));