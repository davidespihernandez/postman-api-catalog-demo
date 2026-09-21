import { z } from 'zod';
import { BaseService } from '../base-service';
import { ContentType, HttpResponse, SdkConfig } from '../../http/types';
import { RequestBuilder } from '../../http/transport/request-builder';
import { SerializationStyle } from '../../http/serialization/base-serializer';
import { ThrowableError } from '../../http/errors/throwable-error';
import { Environment } from '../../http/environment';
import { HealthResponse, healthResponseResponse } from './models/health-response';
import { ServerErrorResponse } from '../common/server-error-response';
import { OpenApiDocument, openApiDocumentResponse } from './models/open-api-document';

/**
 * Service class for SystemService operations.
 * Provides methods to interact with SystemService-related API endpoints.
 * All methods return promises and handle request/response serialization automatically.
 */
export class SystemService extends BaseService {
  protected getHealthConfig?: Partial<SdkConfig>;

  protected getOpenApiConfig?: Partial<SdkConfig>;

  /**
   * Sets method-level configuration for getHealth.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setGetHealthConfig(config: Partial<SdkConfig>): this {
    this.getHealthConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for getOpenApi.
   * @param config - Partial configuration to override service-level defaults
   * @returns This service instance for method chaining
   */
  setGetOpenApiConfig(config: Partial<SdkConfig>): this {
    this.getOpenApiConfig = config;
    return this;
  }

  /**
   *
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<HealthResponse>>} - Service is healthy
   */
  async getHealth(requestConfig?: Partial<SdkConfig>): Promise<HealthResponse> {
    const resolvedConfig = this.getResolvedConfig(this.getHealthConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('GET')
      .setPath('/health')
      .setRequestSchema(z.any())
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: healthResponseResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .build();
    return this.client.callDirect<HealthResponse>(request);
  }

  /**
   *
   * @param {Partial<SdkConfig>} [requestConfig] - The request configuration for retry and validation.
   * @returns {Promise<HttpResponse<OpenApiDocument>>} - OpenAPI document
   */
  async getOpenApi(requestConfig?: Partial<SdkConfig>): Promise<OpenApiDocument> {
    const resolvedConfig = this.getResolvedConfig(this.getOpenApiConfig, requestConfig);
    const request = new RequestBuilder()
      .setConfig(resolvedConfig)
      .setBaseUrl(resolvedConfig)
      .setMethod('GET')
      .setPath('/openapi.json')
      .setRequestSchema(z.any())
      .setRequestContentType(ContentType.Json)
      .addResponse({
        schema: openApiDocumentResponse,
        contentType: ContentType.Json,
        status: 200,
      })
      .addError({
        error: ServerErrorResponse,
        contentType: ContentType.Json,
        status: 500,
      })
      .build();
    return this.client.callDirect<OpenApiDocument>(request);
  }
}
