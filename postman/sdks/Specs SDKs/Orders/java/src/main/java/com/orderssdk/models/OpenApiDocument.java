package com.orderssdk.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * OpenAPI 3.0 document
 */
@Data
@Builder
@With
@ToString
@EqualsAndHashCode
@Jacksonized
public class OpenApiDocument {

  @NonNull
  private String openapi;

  @NonNull
  private Info info;

  @NonNull
  private Object paths;

  @JsonProperty("servers")
  private JsonNullable<List<Servers>> servers;

  @JsonProperty("components")
  private JsonNullable<Object> components;

  // FSM-59: capture unknown JSON fields so they round-trip on re-serialize.
  // @Builder.Default keeps the empty-map default in the Lombok-generated builder; without it the
  // builder would leave the map null and the any-setter would NPE on the first unknown field.
  // Deserialization is wired via the builder's @JsonAnySetter (see the Builder below), NOT here:
  // Lombok @Jacksonized deserializes through the builder and does not copy a field-level
  // @JsonAnySetter across, so unknown fields would be silently dropped if it lived on this field.
  @Builder.Default
  private Map<String, Object> additionalProperties = new HashMap<>();

  // @JsonAnyGetter must sit on the getter (not the field) so Jackson inlines the unknown entries on
  // serialize. On the field it double-registers with the Lombok getter and leaks a literal
  // "additionalProperties" property into every request body and object parameter.
  // Declaring the getter here also stops Lombok @Data from generating its own.
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonIgnore
  public List<Servers> getServers() {
    return servers.orElse(null);
  }

  @JsonIgnore
  public Object getComponents() {
    return components.orElse(null);
  }

  // Overwrite lombok builder methods
  public static class OpenApiDocumentBuilder {

    private JsonNullable<List<Servers>> servers = JsonNullable.undefined();

    @JsonProperty("servers")
    public OpenApiDocumentBuilder servers(List<Servers> value) {
      if (value == null) {
        throw new IllegalStateException("servers cannot be null");
      }
      this.servers = JsonNullable.of(value);
      return this;
    }

    private JsonNullable<Object> components = JsonNullable.undefined();

    @JsonProperty("components")
    public OpenApiDocumentBuilder components(Object value) {
      if (value == null) {
        throw new IllegalStateException("components cannot be null");
      }
      this.components = JsonNullable.of(value);
      return this;
    }

    @JsonAnySetter
    public OpenApiDocumentBuilder additionalProperties(String key, Object value) {
      if (this.additionalProperties$value == null) {
        this.additionalProperties$value = new HashMap<>();
      }
      this.additionalProperties$value.put(key, value);
      this.additionalProperties$set = true;
      return this;
    }
  }
}
