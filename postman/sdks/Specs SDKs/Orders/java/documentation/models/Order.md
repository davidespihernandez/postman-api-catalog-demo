# Order

**Properties**

| Name       | Type        | Required | Description                             |
| :--------- | :---------- | :------- | :-------------------------------------- |
| id         | String      | ✅       | Unique order identifier                 |
| customerId | String      | ✅       | ID of the customer who placed the order |
| status     | OrderStatus | ✅       | Order lifecycle status                  |
| total      | Double      | ✅       | Order total amount                      |
| currency   | String      | ✅       | ISO 4217 currency code                  |
