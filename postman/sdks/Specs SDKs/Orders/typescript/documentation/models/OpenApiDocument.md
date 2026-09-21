# OpenApiDocument

OpenAPI 3.0 document

**Properties**

| Name       | Type                    | Required | Description |
| :--------- | :---------------------- | :------- | :---------- |
| openapi    | string                  | ✅       |             |
| info       | Info                    | ✅       |             |
| paths      | any                     | ✅       |             |
| servers    | [Servers](Servers.md)[] | ❌       |             |
| components | any                     | ❌       |             |

# Info

**Properties**

| Name        | Type   | Required | Description |
| :---------- | :----- | :------- | :---------- |
| title       | string | ✅       |             |
| version     | string | ✅       |             |
| description | string | ❌       |             |
