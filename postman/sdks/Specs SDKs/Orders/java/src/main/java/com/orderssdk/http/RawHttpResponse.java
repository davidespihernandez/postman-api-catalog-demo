package com.orderssdk.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.Response;

/**
 * Represents a raw HTTP response containing the body bytes and original OkHttp response.
 * Provides access to the unparsed response data for low-level operations.
 */
@AllArgsConstructor
@Getter
public class RawHttpResponse {

  /** The raw response body as bytes */
  private final byte[] bodyBytes;

  /** The original OkHttp response object with all metadata */
  private final Response originalResponse;

  /**
   * Converts the body bytes to a UTF-8 string.
   *
   * @return The response body as a string
   */
  public String getBodyString() {
    return ModelConverter.toBodyString(bodyBytes);
  }
}
