package de.riversroses.world.business;

import java.util.Optional;
import java.util.UUID;
import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.ship.model.Ship;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.team.model.Team;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Station;
import de.riversroses.world.model.Vector2;
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

    public void autoSellIfNearPlanet(Ship ship) {
        Optional<Station> station = findNearbyStation(ship);
        if (station.isEmpty()) {
            return;
        }
        if (ship.getCargo().isEmpty()) {
            return;
        }
        long totalValue = 0;
        int totalItems = 0;
        for (var entry : ship.getCargo().entrySet()) {
            String oreId = entry.getKey();
            int count = entry.getValue();
            int basePrice = props.getWorld().getOres().getOrDefault(oreId, new GameProperties.Ore(0, 10)).getValue();
            totalValue += (long) basePrice * count;
            totalItems += count;
        }
        if (totalItems > 0) {
            teamRepo.addCredits(ship.getTeamId(), totalValue);
            ship.getCargo().clear();
            Team team = teamRepo.findById(ship.getTeamId()).orElse(null);
            long teamCredits = team == null ? 0 : team.getCredits();
            log.info("Ship {} sold {} items for {} credits at planet {}. Team total: {}", ship.getShipId(), totalItems,
                    totalValue, station.get().name(), teamCredits);
        }
    }

    public Station createHomePlanetForTeam(String token, Team team, String planetName) {
        Vector2 pos = worldRepo.assignPlanetPositionForToken(token, props.getWorld().getWidth(),
                props.getWorld().getHeight());
        String id = "planet-" + UUID.randomUUID();
        Station station = new Station(
                id,
                planetName,
                pos,
                props.getHomeBase().getRefillRadius(),
                token);
        worldRepo.registerStation(station);
        log.info("Created home planet {} for team {} at {},{}", planetName, team.getDisplayName(), pos.x(), pos.y());
        return station;
    }

    private Optional<Station> findNearbyStation(Ship ship) {
        return worldRepo.getStations().stream()
                .filter(s -> ship.getPosition().distanceTo(s.position()) <= s.interactionRadius())
                .findFirst();
    }

    @Introspected
    @Serdeable
    public record UnloadResult(int itemsSold, long earned, long teamCredits) {
    }
}
