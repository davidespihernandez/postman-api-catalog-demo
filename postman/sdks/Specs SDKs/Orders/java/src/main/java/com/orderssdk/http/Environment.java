package com.orderssdk.http;

import lombok.Getter;

/**
 * Predefined environment configurations for the SDK.
 * Each environment represents a different base URL (e.g., production, staging, development).
 */
@Getter
public enum Environment {
  DEFAULT("https://18-157-170-15.nip.io");

  private final String url;

  Environment(String url) {
    this.url = url;
  }
}
