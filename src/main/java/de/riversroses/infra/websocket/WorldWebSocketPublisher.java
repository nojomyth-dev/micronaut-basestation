package de.riversroses.infra.websocket;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.riversroses.kernel.event.DomainEvent;
import de.riversroses.kernel.event.EventBus;
import de.riversroses.world.event.WorldDeltaEvent;
import de.riversroses.world.event.WorldSnapshotEvent;
import de.riversroses.world.ws.WsMessage;
import io.micronaut.context.annotation.Context;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.Data;

@Context
@Singleton
@Data
public class WorldWebSocketPublisher {

  private final EventBus eventBus;
  private final WebSocketBroadcaster broadcaster;
  private final ExecutorService ioExecutor;

  private static final Predicate<WebSocketSession> ALL_SESSIONS = s -> true;

  private Consumer<DomainEvent> handler;

  public WorldWebSocketPublisher(
      EventBus eventBus,
      WebSocketBroadcaster broadcaster,
      @Named(TaskExecutors.IO) ExecutorService ioExecutor) {
    this.eventBus = eventBus;
    this.broadcaster = broadcaster;
    this.ioExecutor = ioExecutor;
  }

  @PostConstruct
  public void init() {
    handler = event -> {
      if (event instanceof WorldDeltaEvent de) {
        WsMessage msg = WsMessage.delta(de.delta());
        ioExecutor.execute(() -> broadcaster.broadcastSync(msg, ALL_SESSIONS));
      } else if (event instanceof WorldSnapshotEvent se) {
        WsMessage msg = WsMessage.snapshot(se.snapshot());
        ioExecutor.execute(() -> broadcaster.broadcastSync(msg, ALL_SESSIONS));
      }
    };
    eventBus.subscribe(handler);
  }
}
