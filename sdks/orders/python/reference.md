# Reference
## System
<details><summary><code>client.system.<a href="src/postman_api_catalog_demo/system/client.py">get_health</a>() -> HealthResponse</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.system.get_health()

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.system.<a href="src/postman_api_catalog_demo/system/client.py">get_open_api</a>() -> OpenApiDocument</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.system.get_open_api()

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Orders
<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">list_orders</a>() -> OrderListResponse</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.list_orders()

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">create_order</a>(...) -> Order</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.create_order(
    customer_id="usr-002",
    total=99.5,
    status="pending",
    currency="USD",
)

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**customer_id:** `str` — Customer placing the order
    
</dd>
</dl>

<dl>
<dd>

**total:** `float` 
    
</dd>
</dl>

<dl>
<dd>

**status:** `typing.Optional[CreateOrderRequestStatus]` — Initial order status
    
</dd>
</dl>

<dl>
<dd>

**currency:** `typing.Optional[str]` — ISO 4217 currency code
    
</dd>
</dl>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">get_order</a>(...) -> Order</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.get_order(
    id="ord-001",
)

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `str` — Order identifier
    
</dd>
</dl>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">update_order</a>(...) -> Order</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.update_order(
    id="ord-001",
    customer_id="usr-003",
    total=149.99,
    status="processing",
    currency="EUR",
)

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `str` — Order identifier
    
</dd>
</dl>

<dl>
<dd>

**customer_id:** `str` 
    
</dd>
</dl>

<dl>
<dd>

**total:** `float` 
    
</dd>
</dl>

<dl>
<dd>

**status:** `UpdateOrderRequestStatus` 
    
</dd>
</dl>

<dl>
<dd>

**currency:** `typing.Optional[str]` 
    
</dd>
</dl>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">delete_order</a>(...)</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.delete_order(
    id="ord-001",
)

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `str` — Order identifier
    
</dd>
</dl>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.orders.<a href="src/postman_api_catalog_demo/orders/client.py">patch_order</a>(...) -> Order</code></summary>
<dl>
<dd>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```python
from postman_api_catalog_demo import PostmanApiCatalogDemoApi
from postman_api_catalog_demo.environment import PostmanApiCatalogDemoApiEnvironment

client = PostmanApiCatalogDemoApi(
    environment=PostmanApiCatalogDemoApiEnvironment.DEFAULT,
)

client.orders.patch_order(
    id="ord-001",
    status="shipped",
)

```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `str` — Order identifier
    
</dd>
</dl>

<dl>
<dd>

**customer_id:** `typing.Optional[str]` 
    
</dd>
</dl>

<dl>
<dd>

**total:** `typing.Optional[float]` 
    
</dd>
</dl>

<dl>
<dd>

**status:** `typing.Optional[PatchOrderRequestStatus]` 
    
</dd>
</dl>

<dl>
<dd>

**currency:** `typing.Optional[str]` 
    
</dd>
</dl>

<dl>
<dd>

**request_options:** `typing.Optional[RequestOptions]` — Request-specific configuration.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

