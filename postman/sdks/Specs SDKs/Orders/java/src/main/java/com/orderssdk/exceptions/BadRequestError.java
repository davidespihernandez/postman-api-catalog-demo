package com.orderssdk.exceptions;

import com.orderssdk.models.ErrorResponse;
import java.util.List;
import java.util.Map;

/**
 * Error thrown for HTTP 400 responses.
 *
 * Part of the status-named exception hierarchy; extends {@link ApiError} and
 * exposes a typed {@code body()} for this status.
 */
public class BadRequestError extends ApiError {

  public BadRequestError(String message, ErrorResponse body, Map<String, List<String>> headers) {
    super(message, 400, body, headers);
  }

  /**
   * @return The typed error body for this response.
   */
  @java.lang.Override
  public ErrorResponse body() {
    return (ErrorResponse) super.body();
  }
}
