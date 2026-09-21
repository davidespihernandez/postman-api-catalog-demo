# OrdersSdk Python SDK 1.0.0

Welcome to the OrdersSdk SDK documentation. This guide will help you get started with integrating and using the OrdersSdk SDK in your project.

## Versions

- SDK version: `1.0.0`

## About the API

Demo orders service for the Postman API Catalog walkthrough.

Optional query parameters on all endpoints except /openapi.json:

- delay or wait: milliseconds to wait before responding (max 30000)
- status or error: HTTP status code to return (e.g. 500); error=true also returns 500

## Table of Contents

- [Setup & Configuration](#setup--configuration)
  - [Supported Language Versions](#supported-language-versions)
  - [Installation](#installation)
- [Setting a Custom Timeout](#setting-a-custom-timeout)
- [Sample Usage](#sample-usage)
- [Async Usage](#async-usage)
- [Services](#services)
- [Models](#models)

# Setup & Configuration

## Supported Language Versions

This SDK is compatible with the following versions: `Python >= 3.9`

## Installation

To get started with the SDK, we recommend installing using `pip`:

```bash
pip install orders_sdk
```

If you are using Python 3, you can use `pip3` instead:

```bash
pip3 install orders_sdk
```

## Setting a Custom Timeout

You can set a custom timeout for the SDK's HTTP requests as follows:

```py
from orders_sdk import OrdersSdk

sdk = OrdersSdk(timeout=10)
```

# Sample Usage

Below is a comprehensive example demonstrating how to authenticate and call a simple endpoint:

```py
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.system.get_health()

print(result)

```

# Async Usage

The SDK includes an Async Client for making asynchronous API requests. This is useful for applications that need non-blocking operations, like web servers or apps with a graphical user interface.

```py
import asyncio
from orders_sdk import OrdersSdkAsync

sdk = OrdersSdkAsync(
    timeout=10
)


async def main():
  result = await sdk.system.get_health()
  print(result)

asyncio.run(main())
```

## Services

The SDK provides various services to interact with the API.

<details>
<summary>Below is a list of all available services with links to their detailed documentation:</summary>

| Name                                                     |
| :------------------------------------------------------- |
| [SystemService](documentation/services/SystemService.md) |
| [OrdersService](documentation/services/OrdersService.md) |

</details>

## Models

The SDK includes several models that represent the data structures used in API requests and responses. These models help in organizing and managing the data efficiently.

<details>
<summary>Below is a list of all available models with links to their detailed documentation:</summary>

| Name                                                               | Description          |
| :----------------------------------------------------------------- | :------------------- |
| [HealthResponse](documentation/models/HealthResponse.md)           |                      |
| [OpenApiDocument](documentation/models/OpenApiDocument.md)         | OpenAPI 3.0 document |
| [OrderListResponse](documentation/models/OrderListResponse.md)     |                      |
| [CreateOrderRequest](documentation/models/CreateOrderRequest.md)   |                      |
| [Order](documentation/models/Order.md)                             |                      |
| [UpdateOrderRequest](documentation/models/UpdateOrderRequest.md)   |                      |
| [PatchOrderRequest](documentation/models/PatchOrderRequest.md)     |                      |
| [ServerErrorResponse](documentation/models/ServerErrorResponse.md) |                      |
| [ErrorResponse](documentation/models/ErrorResponse.md)             |                      |

</details>
