# Orders

A list of all methods in the `Orders` service. Click on the method name to view detailed information about that method.

| Methods                     | Description |
| :-------------------------- | :---------- |
| [ListOrders](#listorders)   |             |
| [CreateOrder](#createorder) |             |
| [GetOrder](#getorder)       |             |
| [UpdateOrder](#updateorder) |             |
| [PatchOrder](#patchorder)   |             |
| [DeleteOrder](#deleteorder) |             |

## ListOrders

- HTTP Method: `GET`
- Endpoint: `/orders`

**Parameters**

| Name | Type    | Required | Description                 |
| :--- | :------ | :------- | :-------------------------- |
| ctx  | Context | ✅       | Default go language context |

**Return Type**

`OrderListResponse`

**Example Usage Code Snippet**

```go
orders orders list-orders
```

## CreateOrder

- HTTP Method: `POST`
- Endpoint: `/orders`

**Parameters**

| Name               | Type               | Required | Description                 |
| :----------------- | :----------------- | :------- | :-------------------------- |
| ctx                | Context            | ✅       | Default go language context |
| createOrderRequest | CreateOrderRequest | ✅       |                             |

**Return Type**

`Order`

**Example Usage Code Snippet**

```go
orders orders create-order --body '{"customerId":"usr-002","total":99.5,"status":"<create_order_request_status>","currency":"USD"}'
```

## GetOrder

- HTTP Method: `GET`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type    | Required | Description                 |
| :--- | :------ | :------- | :-------------------------- |
| ctx  | Context | ✅       | Default go language context |
| id   | string  | ✅       | Order identifier            |

**Return Type**

`Order`

**Example Usage Code Snippet**

```go
orders orders get-order --id "ord-001"
```

## UpdateOrder

- HTTP Method: `PUT`
- Endpoint: `/orders/{id}`

**Parameters**

| Name               | Type               | Required | Description                 |
| :----------------- | :----------------- | :------- | :-------------------------- |
| ctx                | Context            | ✅       | Default go language context |
| id                 | string             | ✅       | Order identifier            |
| updateOrderRequest | UpdateOrderRequest | ✅       |                             |

**Return Type**

`Order`

**Example Usage Code Snippet**

```go
orders orders update-order --id "ord-001" --body '{"customerId":"usr-003","total":149.99,"status":"<update_order_request_status>","currency":"EUR"}'
```

## PatchOrder

- HTTP Method: `PATCH`
- Endpoint: `/orders/{id}`

**Parameters**

| Name              | Type              | Required | Description                 |
| :---------------- | :---------------- | :------- | :-------------------------- |
| ctx               | Context           | ✅       | Default go language context |
| id                | string            | ✅       | Order identifier            |
| patchOrderRequest | PatchOrderRequest | ✅       |                             |

**Return Type**

`Order`

**Example Usage Code Snippet**

```go
orders orders patch-order --id "ord-001" --body '{"customerId":"usr-003","total":149.99,"status":"<patch_order_request_status>","currency":"EUR"}'
```

## DeleteOrder

- HTTP Method: `DELETE`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type    | Required | Description                 |
| :--- | :------ | :------- | :-------------------------- |
| ctx  | Context | ✅       | Default go language context |
| id   | string  | ✅       | Order identifier            |

**Return Type**

`any`

**Example Usage Code Snippet**

```go
orders orders delete-order --id "ord-001"
```
