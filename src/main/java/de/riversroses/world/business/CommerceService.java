package de.riversroses.world.business;

import java.util.Optional;

import de.riversroses.infra.error.DomainException;
import de.riversroses.infra.error.ErrorCode;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor
@Data
public class CommerceService {

  private final WorldRepository worldRepo;
  private final TeamRepository teamRepo;
  private final GameProperties props;

  public void attemptRefill(Ship ship) {
    Optional<Station> station = findNearbyStation(ship, Station::allowsRefill);
    if (station.isPresent()) {
      ship.setFuel(props.getPhysics().getMaxFuel());
      log.info("Ship {} refueled at {}", ship.getShipId(), station.get().name());
    }
  }

  public UnloadResult unloadCargo(Ship ship) {
    Optional<Station> stationOpt = findNearbyStation(ship, Station::allowsUnload);

    if (stationOpt.isEmpty()) {
      throw new DomainException(ErrorCode.BAD_REQUEST, "Not in range of a trading station");
    }
    Station station = stationOpt.get();

    long totalValue = 0;
    int totalItems = 0;

    for (var entry : ship.getCargo().entrySet()) {
      String oreId = entry.getKey();
      int count = entry.getValue();

      int basePrice = props.getWorld().getOres().getOrDefault(oreId, new GameProperties.Ore(0, 10)).getValue();
      long finalPrice = (long) (basePrice * station.cargoPriceMultiplier());

      totalValue += (finalPrice * count);
      totalItems += count;
    }

    if (totalItems > 0) {
      teamRepo.addCredits(ship.getTeamId(), totalValue);
      ship.getCargo().clear();
      log.info("Ship {} sold {} items at {}", ship.getShipId(), totalItems, station.name());
    }

    Team team = teamRepo.findById(ship.getTeamId()).orElse(null);
    long teamCredits = team == null ? 0 : team.getCredits();

    return new UnloadResult(totalItems, totalValue, teamCredits);
  }

  private Optional<Station> findNearbyStation(Ship ship, java.util.function.Predicate<Station> filter) {
    return worldRepo.getStations().stream()
        .filter(filter)
        .filter(s -> ship.getPosition().distanceTo(s.position()) <= s.interactionRadius())
        .findFirst();
  }

  @Introspected
  @Serdeable
  public record UnloadResult(int itemsSold, long earned, long teamCredits) {
  }
}
