package com.orderssdk.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orderssdk.config.OrdersSdkConfig;
import com.orderssdk.config.RequestConfig;
import com.orderssdk.exceptions.ApiError;
import com.orderssdk.exceptions.BadRequestError;
import com.orderssdk.exceptions.InternalServerError;
import com.orderssdk.exceptions.NotFoundError;
import com.orderssdk.http.Environment;
import com.orderssdk.http.HttpMethod;
import com.orderssdk.http.ModelConverter;
import com.orderssdk.http.OrdersSdkResponse;
import com.orderssdk.http.util.RequestBuilder;
import com.orderssdk.models.CreateOrderRequest;
import com.orderssdk.models.ErrorResponse;
import com.orderssdk.models.Order;
import com.orderssdk.models.OrderListResponse;
import com.orderssdk.models.PatchOrderRequest;
import com.orderssdk.models.ServerErrorResponse;
import com.orderssdk.models.UpdateOrderRequest;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.exceptions.ValidationException;
import com.orderssdk.validation.validators.modelValidators.CreateOrderRequestValidator;
import com.orderssdk.validation.validators.modelValidators.PatchOrderRequestValidator;
import com.orderssdk.validation.validators.modelValidators.UpdateOrderRequestValidator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OrdersService Service
 */
public class OrdersService extends BaseService {

  private RequestConfig listOrdersConfig;
  private RequestConfig createOrderConfig;
  private RequestConfig getOrderConfig;
  private RequestConfig updateOrderConfig;
  private RequestConfig patchOrderConfig;
  private RequestConfig deleteOrderConfig;

  /**
   * Constructs a new instance of OrdersService.
   *
   * @param httpClient The HTTP client to use for requests
   * @param config The SDK configuration
   */
  public OrdersService(@NonNull OkHttpClient httpClient, OrdersSdkConfig config) {
    super(httpClient, config);
  }

  /**
   * Sets method-level configuration for {@code listOrders}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setListOrdersConfig(RequestConfig config) {
    this.listOrdersConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code createOrder}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setCreateOrderConfig(RequestConfig config) {
    this.createOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code getOrder}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setGetOrderConfig(RequestConfig config) {
    this.getOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code updateOrder}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setUpdateOrderConfig(RequestConfig config) {
    this.updateOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code patchOrder}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setPatchOrderConfig(RequestConfig config) {
    this.patchOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code deleteOrder}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public OrdersService setDeleteOrderConfig(RequestConfig config) {
    this.deleteOrderConfig = config;
    return this;
  }

  /**
   * List orders
   *
   * @return response of {@code OrderListResponse}
   */
  public OrderListResponse listOrders() throws ApiError {
    return this.listOrders(null);
  }

  /**
   * List orders
   *
   * @return response of {@code OrderListResponse}
   */
  public OrderListResponse listOrders(RequestConfig requestConfig) throws ApiError {
    return withRawResponse().listOrders(requestConfig).getData();
  }

  /**
   * List orders
   *
   * @return response of {@code CompletableFuture<OrderListResponse>}
   */
  public CompletableFuture<OrderListResponse> listOrdersAsync() throws ApiError {
    return this.listOrdersAsync(null);
  }

  /**
   * List orders
   *
   * @return response of {@code CompletableFuture<OrderListResponse>}
   */
  public CompletableFuture<OrderListResponse> listOrdersAsync(RequestConfig requestConfig)
    throws ApiError {
    return withRawResponse()
      .listOrdersAsync(requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildListOrdersRequest(RequestConfig resolvedConfig) {
    return new RequestBuilder(
      HttpMethod.GET,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders"
    ).build();
  }

  /**
   * Create order
   *
   * @param createOrderRequest {@link CreateOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order createOrder(@NonNull CreateOrderRequest createOrderRequest)
    throws ApiError, ValidationException {
    return this.createOrder(createOrderRequest, null);
  }

  /**
   * Create order
   *
   * @param createOrderRequest {@link CreateOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order createOrder(
    @NonNull CreateOrderRequest createOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse().createOrder(createOrderRequest, requestConfig).getData();
  }

  /**
   * Create order
   *
   * @param createOrderRequest {@link CreateOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> createOrderAsync(@NonNull CreateOrderRequest createOrderRequest)
    throws ApiError, ValidationException {
    return this.createOrderAsync(createOrderRequest, null);
  }

  /**
   * Create order
   *
   * @param createOrderRequest {@link CreateOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> createOrderAsync(
    @NonNull CreateOrderRequest createOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse()
      .createOrderAsync(createOrderRequest, requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildCreateOrderRequest(
    @NonNull CreateOrderRequest createOrderRequest,
    RequestConfig resolvedConfig
  ) throws ValidationException {
    new ViolationAggregator()
      .add(
        new CreateOrderRequestValidator("createOrderRequest")
          .required()
          .validate(createOrderRequest)
      )
      .validateAll();
    return new RequestBuilder(
      HttpMethod.POST,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders"
    )
      .setJsonContent(createOrderRequest)
      .build();
  }

  /**
   * Get order by ID
   *
   * @param id String Order identifier
   * @return response of {@code Order}
   */
  public Order getOrder(@NonNull String id) throws ApiError {
    return this.getOrder(id, null);
  }

  /**
   * Get order by ID
   *
   * @param id String Order identifier
   * @return response of {@code Order}
   */
  public Order getOrder(@NonNull String id, RequestConfig requestConfig) throws ApiError {
    return withRawResponse().getOrder(id, requestConfig).getData();
  }

  /**
   * Get order by ID
   *
   * @param id String Order identifier
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> getOrderAsync(@NonNull String id) throws ApiError {
    return this.getOrderAsync(id, null);
  }

  /**
   * Get order by ID
   *
   * @param id String Order identifier
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> getOrderAsync(@NonNull String id, RequestConfig requestConfig)
    throws ApiError {
    return withRawResponse()
      .getOrderAsync(id, requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildGetOrderRequest(@NonNull String id, RequestConfig resolvedConfig) {
    return new RequestBuilder(
      HttpMethod.GET,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders/{id}"
    )
      .setPathParameter("id", id)
      .build();
  }

  /**
   * Replace order
   *
   * @param id String Order identifier
   * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order updateOrder(@NonNull String id, @NonNull UpdateOrderRequest updateOrderRequest)
    throws ApiError, ValidationException {
    return this.updateOrder(id, updateOrderRequest, null);
  }

  /**
   * Replace order
   *
   * @param id String Order identifier
   * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order updateOrder(
    @NonNull String id,
    @NonNull UpdateOrderRequest updateOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse().updateOrder(id, updateOrderRequest, requestConfig).getData();
  }

  /**
   * Replace order
   *
   * @param id String Order identifier
   * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> updateOrderAsync(
    @NonNull String id,
    @NonNull UpdateOrderRequest updateOrderRequest
  ) throws ApiError, ValidationException {
    return this.updateOrderAsync(id, updateOrderRequest, null);
  }

  /**
   * Replace order
   *
   * @param id String Order identifier
   * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> updateOrderAsync(
    @NonNull String id,
    @NonNull UpdateOrderRequest updateOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse()
      .updateOrderAsync(id, updateOrderRequest, requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildUpdateOrderRequest(
    @NonNull String id,
    @NonNull UpdateOrderRequest updateOrderRequest,
    RequestConfig resolvedConfig
  ) throws ValidationException {
    new ViolationAggregator()
      .add(
        new UpdateOrderRequestValidator("updateOrderRequest")
          .required()
          .validate(updateOrderRequest)
      )
      .validateAll();
    return new RequestBuilder(
      HttpMethod.PUT,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders/{id}"
    )
      .setPathParameter("id", id)
      .setJsonContent(updateOrderRequest)
      .build();
  }

  /**
   * Partially update order
   *
   * @param id String Order identifier
   * @param patchOrderRequest {@link PatchOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order patchOrder(@NonNull String id, @NonNull PatchOrderRequest patchOrderRequest)
    throws ApiError, ValidationException {
    return this.patchOrder(id, patchOrderRequest, null);
  }

  /**
   * Partially update order
   *
   * @param id String Order identifier
   * @param patchOrderRequest {@link PatchOrderRequest} Request Body
   * @return response of {@code Order}
   */
  public Order patchOrder(
    @NonNull String id,
    @NonNull PatchOrderRequest patchOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse().patchOrder(id, patchOrderRequest, requestConfig).getData();
  }

  /**
   * Partially update order
   *
   * @param id String Order identifier
   * @param patchOrderRequest {@link PatchOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> patchOrderAsync(
    @NonNull String id,
    @NonNull PatchOrderRequest patchOrderRequest
  ) throws ApiError, ValidationException {
    return this.patchOrderAsync(id, patchOrderRequest, null);
  }

  /**
   * Partially update order
   *
   * @param id String Order identifier
   * @param patchOrderRequest {@link PatchOrderRequest} Request Body
   * @return response of {@code CompletableFuture<Order>}
   */
  public CompletableFuture<Order> patchOrderAsync(
    @NonNull String id,
    @NonNull PatchOrderRequest patchOrderRequest,
    RequestConfig requestConfig
  ) throws ApiError, ValidationException {
    return withRawResponse()
      .patchOrderAsync(id, patchOrderRequest, requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildPatchOrderRequest(
    @NonNull String id,
    @NonNull PatchOrderRequest patchOrderRequest,
    RequestConfig resolvedConfig
  ) throws ValidationException {
    new ViolationAggregator()
      .add(
        new PatchOrderRequestValidator("patchOrderRequest").required().validate(patchOrderRequest)
      )
      .validateAll();
    return new RequestBuilder(
      HttpMethod.PATCH,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders/{id}"
    )
      .setPathParameter("id", id)
      .setJsonContent(patchOrderRequest)
      .build();
  }

  /**
   * Delete order
   *
   * @param id String Order identifier
   * @return response of {@code void}
   */
  public void deleteOrder(@NonNull String id) throws ApiError {
    this.deleteOrder(id, null);
  }

  /**
   * Delete order
   *
   * @param id String Order identifier
   * @return response of {@code void}
   */
  public void deleteOrder(@NonNull String id, RequestConfig requestConfig) throws ApiError {
    withRawResponse().deleteOrder(id, requestConfig);
  }

  /**
   * Delete order
   *
   * @param id String Order identifier
   * @return response of {@code CompletableFuture<Void>}
   */
  public CompletableFuture<Void> deleteOrderAsync(@NonNull String id) throws ApiError {
    return this.deleteOrderAsync(id, null);
  }

  /**
   * Delete order
   *
   * @param id String Order identifier
   * @return response of {@code CompletableFuture<Void>}
   */
  public CompletableFuture<Void> deleteOrderAsync(@NonNull String id, RequestConfig requestConfig)
    throws ApiError {
    return withRawResponse()
      .deleteOrderAsync(id, requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildDeleteOrderRequest(@NonNull String id, RequestConfig resolvedConfig) {
    return new RequestBuilder(
      HttpMethod.DELETE,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "orders/{id}"
    )
      .setPathParameter("id", id)
      .build();
  }

  /**
   * Returns an accessor whose methods mirror this service but return the full HTTP response
   * (status code, headers, and raw body) wrapped alongside the parsed data.
   *
   * @return An accessor exposing raw-response variants of this service's methods
   */
  public WithRawResponse withRawResponse() {
    return new WithRawResponse();
  }

  /**
   * Per-call accessor exposing raw-response variants of {@link OrdersService}'s methods.
   * Reuses the enclosing service's request builders and configuration.
   */
  public class WithRawResponse {

    /**
     * List orders
     *
     * @return response of {@code OrdersSdkResponse<OrderListResponse>}
     */
    public OrdersSdkResponse<OrderListResponse> listOrders() throws ApiError {
      return this.listOrders(null);
    }

    /**
     * List orders
     *
     * @return response of {@code OrdersSdkResponse<OrderListResponse>}
     */
    public OrdersSdkResponse<OrderListResponse> listOrders(RequestConfig requestConfig)
      throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(listOrdersConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildListOrdersRequest(resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<OrderListResponse>() {})
      );
    }

    /**
     * List orders
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<OrderListResponse>>}
     */
    public CompletableFuture<OrdersSdkResponse<OrderListResponse>> listOrdersAsync()
      throws ApiError {
      return this.listOrdersAsync(null);
    }

    /**
     * List orders
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<OrderListResponse>>}
     */
    public CompletableFuture<OrdersSdkResponse<OrderListResponse>> listOrdersAsync(
      RequestConfig requestConfig
    ) throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(listOrdersConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildListOrdersRequest(resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<OrderListResponse>() {})
        );
      });
    }

    /**
     * Create order
     *
     * @param createOrderRequest {@link CreateOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> createOrder(@NonNull CreateOrderRequest createOrderRequest)
      throws ApiError, ValidationException {
      return this.createOrder(createOrderRequest, null);
    }

    /**
     * Create order
     *
     * @param createOrderRequest {@link CreateOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> createOrder(
      @NonNull CreateOrderRequest createOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(createOrderConfig, requestConfig);
      addErrorMapping(400, ErrorResponse.class, (message, code, body, headers) ->
        new BadRequestError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildCreateOrderRequest(createOrderRequest, resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
      );
    }

    /**
     * Create order
     *
     * @param createOrderRequest {@link CreateOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> createOrderAsync(
      @NonNull CreateOrderRequest createOrderRequest
    ) throws ApiError, ValidationException {
      return this.createOrderAsync(createOrderRequest, null);
    }

    /**
     * Create order
     *
     * @param createOrderRequest {@link CreateOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> createOrderAsync(
      @NonNull CreateOrderRequest createOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(createOrderConfig, requestConfig);
      addErrorMapping(400, ErrorResponse.class, (message, code, body, headers) ->
        new BadRequestError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildCreateOrderRequest(createOrderRequest, resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
        );
      });
    }

    /**
     * Get order by ID
     *
     * @param id String Order identifier
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> getOrder(@NonNull String id) throws ApiError {
      return this.getOrder(id, null);
    }

    /**
     * Get order by ID
     *
     * @param id String Order identifier
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> getOrder(@NonNull String id, RequestConfig requestConfig)
      throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetOrderRequest(id, resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
      );
    }

    /**
     * Get order by ID
     *
     * @param id String Order identifier
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> getOrderAsync(@NonNull String id)
      throws ApiError {
      return this.getOrderAsync(id, null);
    }

    /**
     * Get order by ID
     *
     * @param id String Order identifier
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> getOrderAsync(
      @NonNull String id,
      RequestConfig requestConfig
    ) throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetOrderRequest(id, resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
        );
      });
    }

    /**
     * Replace order
     *
     * @param id String Order identifier
     * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> updateOrder(
      @NonNull String id,
      @NonNull UpdateOrderRequest updateOrderRequest
    ) throws ApiError, ValidationException {
      return this.updateOrder(id, updateOrderRequest, null);
    }

    /**
     * Replace order
     *
     * @param id String Order identifier
     * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> updateOrder(
      @NonNull String id,
      @NonNull UpdateOrderRequest updateOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(updateOrderConfig, requestConfig);
      addErrorMapping(400, ErrorResponse.class, (message, code, body, headers) ->
        new BadRequestError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildUpdateOrderRequest(id, updateOrderRequest, resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
      );
    }

    /**
     * Replace order
     *
     * @param id String Order identifier
     * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> updateOrderAsync(
      @NonNull String id,
      @NonNull UpdateOrderRequest updateOrderRequest
    ) throws ApiError, ValidationException {
      return this.updateOrderAsync(id, updateOrderRequest, null);
    }

    /**
     * Replace order
     *
     * @param id String Order identifier
     * @param updateOrderRequest {@link UpdateOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> updateOrderAsync(
      @NonNull String id,
      @NonNull UpdateOrderRequest updateOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(updateOrderConfig, requestConfig);
      addErrorMapping(400, ErrorResponse.class, (message, code, body, headers) ->
        new BadRequestError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildUpdateOrderRequest(id, updateOrderRequest, resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
        );
      });
    }

    /**
     * Partially update order
     *
     * @param id String Order identifier
     * @param patchOrderRequest {@link PatchOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> patchOrder(
      @NonNull String id,
      @NonNull PatchOrderRequest patchOrderRequest
    ) throws ApiError, ValidationException {
      return this.patchOrder(id, patchOrderRequest, null);
    }

    /**
     * Partially update order
     *
     * @param id String Order identifier
     * @param patchOrderRequest {@link PatchOrderRequest} Request Body
     * @return response of {@code OrdersSdkResponse<Order>}
     */
    public OrdersSdkResponse<Order> patchOrder(
      @NonNull String id,
      @NonNull PatchOrderRequest patchOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(patchOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildPatchOrderRequest(id, patchOrderRequest, resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
      );
    }

    /**
     * Partially update order
     *
     * @param id String Order identifier
     * @param patchOrderRequest {@link PatchOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> patchOrderAsync(
      @NonNull String id,
      @NonNull PatchOrderRequest patchOrderRequest
    ) throws ApiError, ValidationException {
      return this.patchOrderAsync(id, patchOrderRequest, null);
    }

    /**
     * Partially update order
     *
     * @param id String Order identifier
     * @param patchOrderRequest {@link PatchOrderRequest} Request Body
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Order>>}
     */
    public CompletableFuture<OrdersSdkResponse<Order>> patchOrderAsync(
      @NonNull String id,
      @NonNull PatchOrderRequest patchOrderRequest,
      RequestConfig requestConfig
    ) throws ApiError, ValidationException {
      RequestConfig resolvedConfig = getResolvedConfig(patchOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildPatchOrderRequest(id, patchOrderRequest, resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<Order>() {})
        );
      });
    }

    /**
     * Delete order
     *
     * @param id String Order identifier
     * @return response of {@code OrdersSdkResponse<Void>}
     */
    public OrdersSdkResponse<Void> deleteOrder(@NonNull String id) throws ApiError {
      return this.deleteOrder(id, null);
    }

    /**
     * Delete order
     *
     * @param id String Order identifier
     * @return response of {@code OrdersSdkResponse<Void>}
     */
    public OrdersSdkResponse<Void> deleteOrder(@NonNull String id, RequestConfig requestConfig)
      throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(deleteOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildDeleteOrderRequest(id, resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<Void>(response, bodyBytes, null);
    }

    /**
     * Delete order
     *
     * @param id String Order identifier
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Void>>}
     */
    public CompletableFuture<OrdersSdkResponse<Void>> deleteOrderAsync(@NonNull String id)
      throws ApiError {
      return this.deleteOrderAsync(id, null);
    }

    /**
     * Delete order
     *
     * @param id String Order identifier
     * @return response of {@code CompletableFuture<OrdersSdkResponse<Void>>}
     */
    public CompletableFuture<OrdersSdkResponse<Void>> deleteOrderAsync(
      @NonNull String id,
      RequestConfig requestConfig
    ) throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(deleteOrderConfig, requestConfig);
      addErrorMapping(404, ErrorResponse.class, (message, code, body, headers) ->
        new NotFoundError(message, (ErrorResponse) body, headers)
      );
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildDeleteOrderRequest(id, resolvedConfig);
      return executeAsync(request, resolvedConfig).thenApplyAsync(response -> null);
    }
  }
}
