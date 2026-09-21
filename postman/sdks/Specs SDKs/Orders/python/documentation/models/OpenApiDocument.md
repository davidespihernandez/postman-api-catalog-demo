# OpenApiDocument

OpenAPI 3.0 document

**Properties**

| Name       | Type          | Required | Description |
| :--------- | :------------ | :------- | :---------- |
| openapi    | str           | ✅       |             |
| info       | Info          | ✅       |             |
| paths      | dict          | ✅       |             |
| servers    | List[Servers] | ❌       |             |
| components | dict          | ❌       |             |

# Info

**Properties**

| Name        | Type | Required | Description |
| :---------- | :--- | :------- | :---------- |
| title       | str  | ✅       |             |
| version     | str  | ✅       |             |
| description | str  | ❌       |             |

# Servers

**Properties**

| Name        | Type | Required | Description |
| :---------- | :--- | :------- | :---------- |
| url         | str  | ❌       |             |
| description | str  | ❌       |             |
