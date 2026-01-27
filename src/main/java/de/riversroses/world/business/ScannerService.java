package de.riversroses.world.business;

import java.util.List;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.db.ShipRepository;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.SpawnedResource;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Data
public class ScannerService {

  private final ShipRepository shipRepo;
  private final WorldRepository worldRepo;
  private final GameProperties props;

  public ScanResult performScan(Ship ship, double requestedRadius) {
    double maxR = props.getScan().getMaxRadius();
    double actualRadius = Math.max(10.0, Math.min(maxR, requestedRadius));

    double cost = props.getScan().getBaseCost() + (actualRadius * props.getScan().getCostPerRadiusUnit());
    if (ship.getFuel() < cost) {
      return ScanResult.empty(); // not enough fuel, punish them for not checking
    }
    ship.setFuel(ship.getFuel() - cost);

    var visibleShips = shipRepo.getAllShips().stream()
        .filter(s -> !s.getShipId().equals(ship.getShipId()))
        .filter(s -> s.getPosition().distanceTo(ship.getPosition()) <= actualRadius)
        .toList();

    var visibleResources = worldRepo.getResources().stream()
        .filter(r -> r.position().distanceTo(ship.getPosition()) <= actualRadius)
        .toList();

    return new ScanResult(visibleShips, visibleResources);
  }

  public record ScanResult(List<Ship> ships, List<SpawnedResource> resources) {
    public static ScanResult empty() {
      return new ScanResult(List.of(), List.of());
    }
  }
}
