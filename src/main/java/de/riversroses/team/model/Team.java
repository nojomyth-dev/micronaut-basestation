package de.riversroses.team.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Team {

  private final String id;
  private final String displayName;
  private final AtomicLong credits;
  private volatile Instant lastSeenAt = Instant.now();

  public Team(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
    this.credits = new AtomicLong(0);
  }

  public Team(String id, String displayName, long creditsVal) {
    this.id = id;
    this.displayName = displayName;
    this.credits = new AtomicLong(creditsVal);
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("credits")
  public long getCredits() {
    return credits.get();
  }

  public void setCredits(long val) {
    this.credits.set(val);
  }

  public void addCredits(long delta) {
    credits.addAndGet(delta);
    lastSeenAt = Instant.now();
  }

  public Instant getLastSeenAt() {
    return lastSeenAt;
  }
}
