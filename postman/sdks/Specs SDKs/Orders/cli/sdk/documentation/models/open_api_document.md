# OpenAPIDocument

OpenAPI 3.0 document

**Properties**

| Name       | Type             | Required | Description |
| :--------- | :--------------- | :------- | :---------- |
| Openapi    | string           | ✅       |             |
| Info       | system.Info      | ✅       |             |
| Paths      | any              | ✅       |             |
| Servers    | []system.Servers | ❌       |             |
| Components | any              | ❌       |             |

# Info

**Properties**

| Name        | Type   | Required | Description |
| :---------- | :----- | :------- | :---------- |
| Title       | string | ✅       |             |
| Version     | string | ✅       |             |
| Description | string | ❌       |             |

# Servers

**Properties**

| Name        | Type   | Required | Description |
| :---------- | :----- | :------- | :---------- |
| URL         | string | ❌       |             |
| Description | string | ❌       |             |
