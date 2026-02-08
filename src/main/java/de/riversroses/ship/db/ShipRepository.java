package de.riversroses.ship.db;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import de.riversroses.ship.model.Ship;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Data
@Introspected
public class ShipRepository {
  private final Map<String, Ship> shipsByToken = new ConcurrentHashMap<>();

  public Ship save(Ship ship) {
    shipsByToken.put(ship.getToken(), ship);
    return ship;
  }

  public Optional<Ship> findByToken(String token) {
    return Optional.ofNullable(shipsByToken.get(token));
  }

  public Optional<Ship> findById(String id) {
    return shipsByToken.values().stream().filter(s -> s.getShipId().equals(id)).findFirst();
  }

  public Collection<Ship> getAllShips() {
    return shipsByToken.values();
  }

  public long countByTeamId(String teamId) {
    return shipsByToken.values().stream().filter(s -> teamId.equals(s.getTeamId())).count();
  }

  public void removeByToken(String token) {
    shipsByToken.remove(token);
  }
}
