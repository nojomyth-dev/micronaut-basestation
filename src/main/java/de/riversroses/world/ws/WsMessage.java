package de.riversroses.world.ws;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record WsMessage(
    String type,
    Object payload) {
  public static WsMessage snapshot(Object snapshot) {
    return new WsMessage("snapshot", snapshot);
  }

  public static WsMessage delta(Object delta) {
    return new WsMessage("delta", delta);
  }
}
