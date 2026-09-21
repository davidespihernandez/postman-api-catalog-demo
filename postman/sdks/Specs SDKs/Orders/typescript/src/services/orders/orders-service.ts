import { z } from 'zod';
import { BaseService } from '../base-service';
import { ContentType, HttpResponse, SdkConfig } from '../../http/types';
import { RequestBuilder } from '../../http/transport/request-builder';
import { SerializationStyle } from '../../http/serialization/base-serializer';
import { ThrowableError } from '../../http/errors/throwable-error';
import { Environment } from '../../http/environment';
import { OrderListResponse, orderListResponseResponse } from './models/order-list-response';
import { ServerErrorResponse } from '../common/server-error-response';
import { CreateOrderRequest, createOrderRequestRequest } from './models/create-order-request';
import { Order, orderResponse } from './models/order';
import { ErrorResponse } from './models/error-response';
import { UpdateOrderRequest, updateOrderRequestRequest } from './models/update-order-request';
import { PatchOrderRequest, patchOrderRequestRequest } from './models/patch-order-request';

/**
 * Service class for OrdersService operations.
 * Provides methods to interact with OrdersService-related API endpoints.
 * All methods return promises and handle request/response serialization automatically.
 */
export class OrdersService extends BaseService {
  protected listOrdersConfig?: Partial<SdkConfig>;

  protected createOrderConfig?: Partial<SdkConfig>;

  protected getOrderConfig?: Partial<SdkConfig>;

  protected updateOrderConfig?: Partial<SdkConfig>;

  protected patchOrderConfig?: Partial<SdkConfig>;

  protected deleteOrderConfig?: Partial<SdkConfig>;

  /**
   * Sets method-level configuration for listOrders.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setListOrdersConfig(config: Partial<SdkConfig>): this {
    this.listOrdersConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for createOrder.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setCreateOrderConfig(config: Partial<SdkConfig>): this {
    this.createOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for getOrder.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setGetOrderConfig(config: Partial<SdkConfig>): this {
    this.getOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for updateOrder.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setUpdateOrderConfig(config: Partial<SdkConfig>): this {
    this.updateOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for patchOrder.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setPatchOrderConfig(config: Partial<SdkConfig>): this {
    this.patchOrderConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for deleteOrder.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setDeleteOrderConfig(config: Partial<SdkConfig>): this {
    this.deleteOrderConfig = config;
    return this;
  }

  /**
   *
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<OrderListResponse>>} - Paginated-style order list
   */
  async listOrders(requestConfig?: Partial<SdkConfig>): Promise<OrderListResponse> {
    const resolvedConfig = this.getResolvedConfig(this.listOrdersConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('GET')
      .setPath('/orders')
      .setRequestSchema(z.any())
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: orderListResponseResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .build();
    return this.client.callDirect<OrderListResponse>(request);
  }

  /**
   *
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<Order>>} - Order created
   */
  async createOrder(body: CreateOrderRequest, requestConfig?: Partial<SdkConfig>): Promise<Order> {
    const resolvedConfig = this.getResolvedConfig(this.createOrderConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('POST')
      .setPath('/orders')
      .setRequestSchema(createOrderRequestRequest)
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: orderResponse,
        contentType: ContentType.Json,
        status: 201,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 400,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .addHeaderParam({ key: 'Content-Type', value: 'application/json' })
      .addBody(body)
      .build();
    return this.client.callDirect<Order>(request);
  }

  /**
   *
   * @param {string} id - Order identifier
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<Order>>} - Order found
   */
  async getOrder(id: string, requestConfig?: Partial<SdkConfig>): Promise<Order> {
    const resolvedConfig = this.getResolvedConfig(this.getOrderConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('GET')
      .setPath('/orders/{id}')
      .setRequestSchema(z.any())
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: orderResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 404,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .addPathParam({
        key: 'id',
        value: id,
      })
      .build();
    return this.client.callDirect<Order>(request);
  }

  /**
   *
   * @param {string} id - Order identifier
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<Order>>} - Order updated
   */
  async updateOrder(
    id: string,
    body: UpdateOrderRequest,
    requestConfig?: Partial<SdkConfig>,
  ): Promise<Order> {
    const resolvedConfig = this.getResolvedConfig(this.updateOrderConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('PUT')
      .setPath('/orders/{id}')
      .setRequestSchema(updateOrderRequestRequest)
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: orderResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 400,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 404,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .addPathParam({
        key: 'id',
        value: id,
      })
      .addHeaderParam({ key: 'Content-Type', value: 'application/json' })
      .addBody(body)
      .build();
    return this.client.callDirect<Order>(request);
  }

  /**
   *
   * @param {string} id - Order identifier
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<Order>>} - Order patched
   */
  async patchOrder(
    id: string,
    body: PatchOrderRequest,
    requestConfig?: Partial<SdkConfig>,
  ): Promise<Order> {
    const resolvedConfig = this.getResolvedConfig(this.patchOrderConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('PATCH')
      .setPath('/orders/{id}')
      .setRequestSchema(patchOrderRequestRequest)
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: orderResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 404,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .addPathParam({
        key: 'id',
        value: id,
      })
      .addHeaderParam({ key: 'Content-Type', value: 'application/json' })
      .addBody(body)
      .build();
    return this.client.callDirect<Order>(request);
  }

  /**
   *
   * @param {string} id - Order identifier
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<any>>} - Order deleted
   */
  async deleteOrder(id: string, requestConfig?: Partial<SdkConfig>): Promise<void> {
    const resolvedConfig = this.getResolvedConfig(this.deleteOrderConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('DELETE')
      .setPath('/orders/{id}')
      .setRequestSchema(z.any())
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: z.undefined(),
        contentType: ContentType.NoContent,
        status: 204,
      })
      .addError({
        error: ErrorResponse,
        contentType: ContentType.Json,
        status: 404,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .addPathParam({
        key: 'id',
        value: id,
      })
      .build();
    return this.client.callDirect<void>(request);
  }
}
