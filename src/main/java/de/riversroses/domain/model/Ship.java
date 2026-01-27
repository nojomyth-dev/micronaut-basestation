package de.riversroses.domain.model;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Ship {
  private String shipId;
  private String teamName;

  private Vector2 position;
  private double headingDeg;
  private double speed;
  private double fuel;

  private Map<String, Integer> cargo = new ConcurrentHashMap<>();
  private long credits;

  private Instant lastSimulatedAt;
  private Instant lastChangedAt;
}
