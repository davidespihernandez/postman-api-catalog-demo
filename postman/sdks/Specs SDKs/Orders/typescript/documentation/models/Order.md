# Order

**Properties**

| Name       | Type        | Required | Description                             |
| :--------- | :---------- | :------- | :-------------------------------------- |
| id         | string      | ✅       | Unique order identifier                 |
| customerId | string      | ✅       | ID of the customer who placed the order |
| status     | OrderStatus | ✅       | Order lifecycle status                  |
| total      | number      | ✅       | Order total amount                      |
| currency   | string      | ✅       | ISO 4217 currency code                  |

# OrderStatus

Order lifecycle status

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| PENDING    | string | ✅       | "pending"    |
| PROCESSING | string | ✅       | "processing" |
| SHIPPED    | string | ✅       | "shipped"    |
| CANCELLED  | string | ✅       | "cancelled"  |
