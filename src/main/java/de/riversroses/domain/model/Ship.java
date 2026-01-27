package de.riversroses.domain.model;

import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Ship {
  private String token; // Secret key provided by the team (Identity)
  private String shipId; // Public ID used in the game (UUID)

  // Display
  private volatile String teamName;

  // Physics State (Written by Tick Thread, Read by HTTP Threads)
  private volatile Vector2 position;
  private volatile double headingDeg;
  private volatile double speed;
  private volatile double fuel;

  // Inventory
  private final Map<String, Integer> cargo = new ConcurrentHashMap<>();
  private volatile long credits;
  private volatile boolean autoCollect = true;

  // Meta
  private volatile Instant lastSimulatedAt;
  private volatile Instant lastChangedAt;
  private volatile Instant lastCommandAt; // For pruning zombies later
}