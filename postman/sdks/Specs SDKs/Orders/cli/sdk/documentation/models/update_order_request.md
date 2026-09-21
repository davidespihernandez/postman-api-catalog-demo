# UpdateOrderRequest

**Properties**

| Name       | Type                            | Required | Description |
| :--------- | :------------------------------ | :------- | :---------- |
| CustomerID | string                          | ✅       |             |
| Total      | float64                         | ✅       |             |
| Status     | orders.UpdateOrderRequestStatus | ✅       |             |
| Currency   | string                          | ❌       |             |

# UpdateOrderRequestStatus

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| Pending    | string | ✅       | "pending"    |
| Processing | string | ✅       | "processing" |
| Shipped    | string | ✅       | "shipped"    |
| Cancelled  | string | ✅       | "cancelled"  |
