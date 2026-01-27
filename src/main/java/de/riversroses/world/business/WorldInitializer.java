package de.riversroses.world.business;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
import de.riversroses.world.model.Vector2;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
public class WorldInitializer implements ApplicationEventListener<ServerStartupEvent> {

  private final GameProperties props;
  private final WorldRepository worldRepo;

  @Override
  public void onApplicationEvent(ServerStartupEvent event) {
    log.info("Initializing World");

    // Prime Station (Home Base)
    worldRepo.registerStation(new Station(
        "prime-base",
        "Prime Station",
        new Vector2(props.getHomeBase().getX(), props.getHomeBase().getY()),
        props.getHomeBase().getRefillRadius(),
        true,
        true,
        1.0,
        1.0));

    // Depots from config
    if (props.getDepots() != null) {
      props.getDepots().forEach(d -> {
        worldRepo.registerStation(new Station(
            d.getId(),
            d.getName(),
            new Vector2(d.getX(), d.getY()),
            40.0,
            false,
            true,
            1.1,
            1.0));
      });
    }

    log.info("World initialized with {} stations.", worldRepo.getStations().size());
  }
}
