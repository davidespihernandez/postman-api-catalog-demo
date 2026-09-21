package com.orderssdk.exceptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base type for all API errors thrown by the SDK.
 *
 * <p>This is an unchecked exception (extends {@link RuntimeException}) and is
 * the root of a two-level, status-named exception hierarchy. It exposes the
 * HTTP status code, the deserialized error body, and the response headers
 * without leaking the underlying HTTP transport type.
 */
public class ApiError extends RuntimeException {

  private final int statusCode;
  private final Object body;
  private final Map<String, List<String>> headers;

  /**
   * Initializes a new API error.
   *
   * @param message    The error message
   * @param statusCode The HTTP status code of the response that triggered the error
   * @param body       The deserialized error body, or null if none could be parsed
   * @param headers    The response headers, keyed by name
   */
  public ApiError(String message, int statusCode, Object body, Map<String, List<String>> headers) {
    super(message);
    this.statusCode = statusCode;
    this.body = body;
    this.headers = headers == null ? Collections.emptyMap() : headers;
  }

  /**
   * @return The HTTP status code of the response that triggered this error.
   */
  public int statusCode() {
    return this.statusCode;
  }

  /**
   * @return The deserialized error body, or null when the response body could not be typed.
   */
  public Object body() {
    return this.body;
  }

  /**
   * @return The response headers as an unmodifiable map keyed by header name.
   */
  public Map<String, List<String>> headers() {
    return this.headers;
  }
}
