# Order

**Properties**

| Name       | Type               | Required | Description                             |
| :--------- | :----------------- | :------- | :-------------------------------------- |
| ID         | string             | ✅       | Unique order identifier                 |
| CustomerID | string             | ✅       | ID of the customer who placed the order |
| Status     | orders.OrderStatus | ✅       | Order lifecycle status                  |
| Total      | float64            | ✅       | Order total amount                      |
| Currency   | string             | ✅       | ISO 4217 currency code                  |

# OrderStatus

Order lifecycle status

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| Pending    | string | ✅       | "pending"    |
| Processing | string | ✅       | "processing" |
| Shipped    | string | ✅       | "shipped"    |
| Cancelled  | string | ✅       | "cancelled"  |
