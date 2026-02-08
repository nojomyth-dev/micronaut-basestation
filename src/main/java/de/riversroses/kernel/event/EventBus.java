package de.riversroses.kernel.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Data
public class EventBus {
  private final List<Consumer<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

  public void publish(DomainEvent event) {
    for (var l : listeners) {
      l.accept(event);
    }
  }

  public void subscribe(Consumer<DomainEvent> listener) {
    listeners.add(listener);
  }

  public void unsubscribe(Consumer<DomainEvent> listener) {
    listeners.remove(listener);
  }
}
