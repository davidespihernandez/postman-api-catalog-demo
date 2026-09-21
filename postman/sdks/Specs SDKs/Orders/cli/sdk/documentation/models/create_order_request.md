# CreateOrderRequest

**Properties**

| Name       | Type                            | Required | Description                |
| :--------- | :------------------------------ | :------- | :------------------------- |
| CustomerID | string                          | ✅       | Customer placing the order |
| Total      | float64                         | ✅       |                            |
| Status     | orders.CreateOrderRequestStatus | ❌       | Initial order status       |
| Currency   | string                          | ❌       | ISO 4217 currency code     |

# CreateOrderRequestStatus

Initial order status

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| Pending    | string | ✅       | "pending"    |
| Processing | string | ✅       | "processing" |
| Shipped    | string | ✅       | "shipped"    |
| Cancelled  | string | ✅       | "cancelled"  |
