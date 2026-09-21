# OrdersService

A list of all methods in the `OrdersService` service. Click on the method name to view detailed information about that method.

| Methods                     | Description |
| :-------------------------- | :---------- |
| [listOrders](#listorders)   |             |
| [createOrder](#createorder) |             |
| [getOrder](#getorder)       |             |
| [updateOrder](#updateorder) |             |
| [patchOrder](#patchorder)   |             |
| [deleteOrder](#deleteorder) |             |

## listOrders

- HTTP Method: `GET`
- Endpoint: `/orders`

**Return Type**

`OrderListResponse`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.OrderListResponse;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    OrderListResponse response = ordersSdk.orders.listOrders();

    System.out.println(response);
  }
}

```

## createOrder

- HTTP Method: `POST`
- Endpoint: `/orders`

**Parameters**

| Name               | Type                                                  | Required | Description  |
| :----------------- | :---------------------------------------------------- | :------- | :----------- |
| createOrderRequest | [CreateOrderRequest](../models/CreateOrderRequest.md) | ✅       | Request Body |

**Return Type**

`Order`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.CreateOrderRequest;
import com.orderssdk.models.CreateOrderRequestStatus;
import com.orderssdk.models.Order;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
      .customerId("usr-002")
      .total(99.5D)
      .status(CreateOrderRequestStatus.PENDING)
      .currency("USD")
      .build();

    Order response = ordersSdk.orders.createOrder(createOrderRequest);

    System.out.println(response);
  }
}

```

## getOrder

- HTTP Method: `GET`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type   | Required | Description      |
| :--- | :----- | :------- | :--------------- |
| id   | String | ✅       | Order identifier |

**Return Type**

`Order`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.Order;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    Order response = ordersSdk.orders.getOrder("ord-001");

    System.out.println(response);
  }
}

```

## updateOrder

- HTTP Method: `PUT`
- Endpoint: `/orders/{id}`

**Parameters**

| Name               | Type                                                  | Required | Description      |
| :----------------- | :---------------------------------------------------- | :------- | :--------------- |
| id                 | String                                                | ✅       | Order identifier |
| updateOrderRequest | [UpdateOrderRequest](../models/UpdateOrderRequest.md) | ✅       | Request Body     |

**Return Type**

`Order`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.Order;
import com.orderssdk.models.UpdateOrderRequest;
import com.orderssdk.models.UpdateOrderRequestStatus;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    UpdateOrderRequest updateOrderRequest = UpdateOrderRequest.builder()
      .customerId("usr-003")
      .total(149.99D)
      .status(UpdateOrderRequestStatus.PENDING)
      .currency("EUR")
      .build();

    Order response = ordersSdk.orders.updateOrder("ord-001", updateOrderRequest);

    System.out.println(response);
  }
}

```

## patchOrder

- HTTP Method: `PATCH`
- Endpoint: `/orders/{id}`

**Parameters**

| Name              | Type                                                | Required | Description      |
| :---------------- | :-------------------------------------------------- | :------- | :--------------- |
| id                | String                                              | ✅       | Order identifier |
| patchOrderRequest | [PatchOrderRequest](../models/PatchOrderRequest.md) | ✅       | Request Body     |

**Return Type**

`Order`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.Order;
import com.orderssdk.models.PatchOrderRequest;
import com.orderssdk.models.PatchOrderRequestStatus;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    PatchOrderRequest patchOrderRequest = PatchOrderRequest.builder()
      .customerId("usr-003")
      .total(149.99D)
      .status(PatchOrderRequestStatus.PENDING)
      .currency("EUR")
      .build();

    Order response = ordersSdk.orders.patchOrder("ord-001", patchOrderRequest);

    System.out.println(response);
  }
}

```

## deleteOrder

- HTTP Method: `DELETE`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type   | Required | Description      |
| :--- | :----- | :------- | :--------------- |
| id   | String | ✅       | Order identifier |

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    ordersSdk.orders.deleteOrder("ord-001");
  }
}

```
