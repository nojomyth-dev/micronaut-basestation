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
  private final Map<String, Ship> shipsById = new ConcurrentHashMap<>();

  public Ship save(Ship ship) {
    shipsById.put(ship.getShipId(), ship);
    return ship;
  }

  public Optional<Ship> findByToken(String token) {
    return shipsById.values().stream()
        .filter(s -> token.equals(s.getToken()))
        .findFirst();
  }

  public Optional<Ship> findById(String id) {
    return Optional.ofNullable(shipsById.get(id));
  }

  public Collection<Ship> getAllShips() {
    return shipsById.values();
  }

  public long countByTeamId(String teamId) {
    return shipsById.values().stream().filter(s -> teamId.equals(s.getTeamId())).count();
  }

  public void removeByToken(String token) {
    shipsById.values().removeIf(s -> token.equals(s.getToken()));
  }
}
