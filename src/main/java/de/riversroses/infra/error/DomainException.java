package de.riversroses.infra.error;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Serdeable
@Introspected
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DomainException extends RuntimeException {
  private final ErrorCode code;

  public DomainException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }
}
