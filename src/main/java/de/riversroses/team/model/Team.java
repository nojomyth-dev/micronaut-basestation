package de.riversroses.team.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Team {

  private final String id;
  private final String displayName;
  private final AtomicLong credits = new AtomicLong(0);
  private volatile Instant lastSeenAt = Instant.now();

  public Team(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public long getCredits() {
    return credits.get();
  }

  public void addCredits(long delta) {
    credits.addAndGet(delta);
    lastSeenAt = Instant.now();
  }

  public Instant getLastSeenAt() {
    return lastSeenAt;
  }
}
