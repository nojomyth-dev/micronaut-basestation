package de.riversroses.infra.websocket;

import de.riversroses.world.business.WorldSnapshotService;
import de.riversroses.world.ws.WsMessage;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.*;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@ServerWebSocket("/ws/world")
@Singleton
@AllArgsConstructor
@Data
@Slf4j
public class WorldWebSocket {

  private final WorldSnapshotService snapshotService;

  @OnOpen
  public void onOpen(WebSocketSession session) {
    log.info("WS open: {}", session.getId());
    session.sendAsync(WsMessage.snapshot(snapshotService.snapshot()));
  }

  @OnClose
  public void onClose(WebSocketSession session) {
    log.info("WS close: {}", session.getId());
  }

  @OnError
  public void onError(WebSocketSession session, Throwable t) {
    log.warn("WS error: {}", session.getId(), t);
  }

  @OnMessage
  public void onMessage(String msg, WebSocketSession session) {
    if ("ping".equalsIgnoreCase(msg)) {
      session.sendAsync("pong");
    } else if ("snapshot".equalsIgnoreCase(msg)) {
      session.sendAsync(WsMessage.snapshot(snapshotService.snapshot()));
    }
  }
}
