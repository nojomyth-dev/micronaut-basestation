package de.riversroses.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Serdeable
@Introspected
public class Mission {
  private String id;
  private String description;
  private Vector2 target;
  private int reward;
  private Instant expiresAt;
}
