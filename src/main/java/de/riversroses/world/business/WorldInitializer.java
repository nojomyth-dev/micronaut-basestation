package de.riversroses.world.business;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
import de.riversroses.world.model.Vector2;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Singleton
@Slf4j
@AllArgsConstructor
@Introspected
public class WorldInitializer implements ApplicationEventListener<ServerStartupEvent> {
  private final GameProperties props;
  private final WorldRepository worldRepo;

  @Override
  public void onApplicationEvent(ServerStartupEvent event) {
    log.info("Initializing World");
    worldRepo.registerStation(new Station(
        "prime-base",
        "Prime Station",
        new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY()),
        props.getHomeBase().getRefillRadius(),
        "prime-base"));
    log.info("World initialized with {} stations.", worldRepo.getStations().size());
  }
}
