package de.riversroses.infra.error;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
@Introspected
@Serdeable
public class ApiErrorResponse {
  ErrorCode code;
  String message;
  List<FieldError> fieldErrors;

  @Value
  @Builder
  @Introspected
  @Serdeable
  public static class FieldError {
    String field;
    String message;
  }
}
