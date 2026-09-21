# OrdersService

A list of all methods in the `OrdersService` service. Click on the method name to view detailed information about that method.

| Methods                     | Description |
| :-------------------------- | :---------- |
| [listOrders](#listorders)   |             |
| [createOrder](#createorder) |             |
| [getOrder](#getorder)       |             |
| [updateOrder](#updateorder) |             |
| [patchOrder](#patchorder)   |             |
| [deleteOrder](#deleteorder) |             |

## listOrders

- HTTP Method: `GET`
- Endpoint: `/orders`

**Return Type**

`OrderListResponse`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.orders.listOrders();

  console.log(data);
})();
```

## createOrder

- HTTP Method: `POST`
- Endpoint: `/orders`

**Parameters**

| Name | Type                                                  | Required | Description       |
| :--- | :---------------------------------------------------- | :------- | :---------------- |
| body | [CreateOrderRequest](../models/CreateOrderRequest.md) | ✅       | The request body. |

**Return Type**

`Order`

**Example Usage Code Snippet**

```typescript
import { CreateOrderRequest, OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const createOrderRequestStatus = 'pending';

  const createOrderRequest: CreateOrderRequest = {
    customerId: 'usr-002',
    total: 99.5,
    status: createOrderRequestStatus,
    currency: 'USD',
  };

  const data = await ordersSdk.orders.createOrder(createOrderRequest);

  console.log(data);
})();
```

## getOrder

- HTTP Method: `GET`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type   | Required | Description      |
| :--- | :----- | :------- | :--------------- |
| id   | string | ✅       | Order identifier |

**Return Type**

`Order`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.orders.getOrder('ord-001');

  console.log(data);
})();
```

## updateOrder

- HTTP Method: `PUT`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type                                                  | Required | Description       |
| :--- | :---------------------------------------------------- | :------- | :---------------- |
| body | [UpdateOrderRequest](../models/UpdateOrderRequest.md) | ✅       | The request body. |
| id   | string                                                | ✅       | Order identifier  |

**Return Type**

`Order`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk, UpdateOrderRequest } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const updateOrderRequestStatus = 'pending';

  const updateOrderRequest: UpdateOrderRequest = {
    customerId: 'usr-003',
    total: 149.99,
    status: updateOrderRequestStatus,
    currency: 'EUR',
  };

  const data = await ordersSdk.orders.updateOrder('ord-001', updateOrderRequest);

  console.log(data);
})();
```

## patchOrder

- HTTP Method: `PATCH`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type                                                | Required | Description       |
| :--- | :-------------------------------------------------- | :------- | :---------------- |
| body | [PatchOrderRequest](../models/PatchOrderRequest.md) | ✅       | The request body. |
| id   | string                                              | ✅       | Order identifier  |

**Return Type**

`Order`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk, PatchOrderRequest } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const patchOrderRequestStatus = 'pending';

  const patchOrderRequest: PatchOrderRequest = {
    customerId: 'usr-003',
    total: 149.99,
    status: patchOrderRequestStatus,
    currency: 'EUR',
  };

  const data = await ordersSdk.orders.patchOrder('ord-001', patchOrderRequest);

  console.log(data);
})();
```

## deleteOrder

- HTTP Method: `DELETE`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type   | Required | Description      |
| :--- | :----- | :------- | :--------------- |
| id   | string | ✅       | Order identifier |

**Example Usage Code Snippet**

```typescript
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.orders.deleteOrder('ord-001');

  console.log(data);
})();
```
