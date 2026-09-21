# Order

**Properties**

| Name        | Type        | Required | Description                             |
| :---------- | :---------- | :------- | :-------------------------------------- |
| id\_        | str         | ✅       | Unique order identifier                 |
| customer_id | str         | ✅       | ID of the customer who placed the order |
| status      | OrderStatus | ✅       | Order lifecycle status                  |
| total       | float       | ✅       | Order total amount                      |
| currency    | str         | ✅       | ISO 4217 currency code                  |

# OrderStatus

Order lifecycle status

**Properties**

| Name       | Type | Required | Description  |
| :--------- | :--- | :------- | :----------- |
| PENDING    | str  | ✅       | "pending"    |
| PROCESSING | str  | ✅       | "processing" |
| SHIPPED    | str  | ✅       | "shipped"    |
| CANCELLED  | str  | ✅       | "cancelled"  |
