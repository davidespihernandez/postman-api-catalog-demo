package com.orderssdk.http;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import okhttp3.Response;

/**
 * Wrapper class for HTTP responses containing parsed data, raw response, and metadata.
 * Provides structured access to response components for application code.
 *
 * @param <T> The type of the parsed response data
 */
@Getter
public class OrdersSdkResponse<T> {

  /** The parsed response data */
  private final T data;
  /** The raw HTTP response including body bytes and response object */
  private final RawHttpResponse raw;
  /** Response metadata including status code and headers */
  private final OrdersSdkResponseMetadata metadata;

  /**
   * Constructs a new response wrapper.
   *
   * @param response The OkHttp response object
   * @param bodyBytes The raw response body as bytes
   * @param data The parsed response data
   */
  public OrdersSdkResponse(Response response, byte[] bodyBytes, T data) {
    this.data = data;
    this.raw = new RawHttpResponse(bodyBytes, response);
    this.metadata = new OrdersSdkResponseMetadata(response.code(), response.headers().toMultimap());
  }

  /**
   * Returns the parsed response data. Alias for {@link #getData()}, provided for
   * compatibility with the Fern-style {@code body()} accessor.
   *
   * @return The parsed response data
   */
  public T body() {
    return this.data;
  }

  /**
   * Returns the response headers. Alias for {@code getMetadata().getHeaders()}, provided for
   * compatibility with the Fern-style {@code headers()} accessor.
   *
   * @return The response headers as a map of header names to lists of values
   */
  public Map<String, List<String>> headers() {
    return this.metadata.getHeaders();
  }
}
