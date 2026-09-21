package com.orderssdk.exceptions;

import com.orderssdk.models.ServerErrorResponse;
import java.util.List;
import java.util.Map;

/**
 * Error thrown for HTTP 500 responses.
 *
 * Part of the status-named exception hierarchy; extends {@link ApiError} and
 * exposes a typed {@code body()} for this status.
 */
public class InternalServerError extends ApiError {

  public InternalServerError(
    String message,
    ServerErrorResponse body,
    Map<String, List<String>> headers
  ) {
    super(message, 500, body, headers);
  }

  /**
   * @return The typed error body for this response.
   */
  @java.lang.Override
  public ServerErrorResponse body() {
    return (ServerErrorResponse) super.body();
  }
}
