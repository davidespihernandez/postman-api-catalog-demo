# PatchOrderRequest

**Properties**

| Name       | Type                           | Required | Description |
| :--------- | :----------------------------- | :------- | :---------- |
| CustomerID | string                         | ❌       |             |
| Total      | float64                        | ❌       |             |
| Status     | orders.PatchOrderRequestStatus | ❌       |             |
| Currency   | string                         | ❌       |             |

# PatchOrderRequestStatus

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| Pending    | string | ✅       | "pending"    |
| Processing | string | ✅       | "processing" |
| Shipped    | string | ✅       | "shipped"    |
| Cancelled  | string | ✅       | "cancelled"  |
