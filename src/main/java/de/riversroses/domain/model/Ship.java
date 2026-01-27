package de.riversroses.domain.model;

import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Ship {
    private String token;
    private String shipId;

    private volatile String teamName;

    // Position
    private volatile Vector2 position;
    
    // Navigation
    private volatile Double targetX;
    private volatile Double targetY;
    
    // Display / Physics
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
    private volatile Instant lastCommandAt;
}