package de.riversroses.world.db;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import de.riversroses.ship.model.Ship;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.SpawnedResource;
import de.riversroses.world.model.Station;
import de.riversroses.world.model.Vector2;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor
@Data
@Introspected
public class WorldRepository {
  private final Map<String, Station> stations = new ConcurrentHashMap<>();
  private final Map<String, SpawnedResource> resources = new ConcurrentHashMap<>();
  private final Map<String, Mission> missions = new ConcurrentHashMap<>();
  private final Set<String> changedResourceIds = ConcurrentHashMap.newKeySet();
  private final Set<String> removedResourceIds = ConcurrentHashMap.newKeySet();
  private final Set<String> changedMissionIds = ConcurrentHashMap.newKeySet();
  private final Set<String> removedMissionIds = ConcurrentHashMap.newKeySet();
  private final Map<String, Mission> missionsByProvider = new ConcurrentHashMap<>();
  private final Map<String, PendingCompletion> pendingCompletions = new ConcurrentHashMap<>();
  private final Map<String, Integer> tokenPlanetIndex = new ConcurrentHashMap<>();

  public void registerStation(Station s) {
    stations.put(s.id(), s);
  }

  public Collection<Station> getStations() {
    return stations.values();
  }

  public Optional<Station> findPlanetByToken(String token) {
    return stations.values().stream()
        .filter(s -> token.equals(s.ownerToken()))
        .findFirst();
  }

  public Vector2 assignPlanetPositionForToken(String token, double width, double height) {
    int index = tokenPlanetIndex.computeIfAbsent(token, t -> tokenPlanetIndex.size());

    // FIX: Use a fixed number of slots (e.g. 16) for the circle.
    // Previously it used 'count' which changed every time a user joined, shifting
    // angles.
    double slots = 16.0;

    double cx = width / 2.0;
    double cy = height / 2.0;
    double radius = Math.min(width, height) * 0.35;

    // Distribute evenly based on fixed slots
    double angle = (2 * Math.PI * index) / slots;

    double x = cx + radius * Math.cos(angle);
    double y = cy + radius * Math.sin(angle);
    return new Vector2(x, y);
  }

  // New method to restore internal index state after loading from disk
  public void rebuildIndices() {
    tokenPlanetIndex.clear();
    for (Station s : stations.values()) {
      if (s.ownerToken() != null && !s.ownerToken().equals("prime-base")) {
        // We reserve a slot for existing planets so new registrations don't overlap
        tokenPlanetIndex.putIfAbsent(s.ownerToken(), tokenPlanetIndex.size());
      }
    }
  }

  public void addResource(SpawnedResource res) {
    resources.put(res.id(), res);
    changedResourceIds.add(res.id());
  }

  public void removeResource(String id) {
    resources.remove(id);
    removedResourceIds.add(id);
  }

  public Collection<SpawnedResource> getResources() {
    return resources.values();
  }

  public int getResourceCount() {
    return resources.size();
  }

  public Set<String> drainChangedResourceIds() {
    Set<String> copy = Set.copyOf(changedResourceIds);
    changedResourceIds.clear();
    return copy;
  }

  public Set<String> drainRemovedResourceIds() {
    Set<String> copy = Set.copyOf(removedResourceIds);
    removedResourceIds.clear();
    return copy;
  }

  public SpawnedResource getResource(String id) {
    return resources.get(id);
  }

  public void addMission(Mission m) {
    missions.put(m.id(), m);
    changedMissionIds.add(m.id());
  }

  public Collection<Mission> getMissions() {
    return missions.values();
  }

  public void removeMission(String id) {
    missions.remove(id);
    removedMissionIds.add(id);
  }

  public Mission getMission(String id) {
    return missions.get(id);
  }

  public Set<String> drainChangedMissionIds() {
    Set<String> copy = Set.copyOf(changedMissionIds);
    changedMissionIds.clear();
    return copy;
  }

  public Set<String> drainRemovedMissionIds() {
    Set<String> copy = Set.copyOf(removedMissionIds);
    removedMissionIds.clear();
    return copy;
  }

  public void putMissionFromProvider(String providerUrl, Mission mission) {
    missionsByProvider.put(providerUrl, mission);
    addMission(mission);
  }

  public void clearMissionForProvider(String providerUrl) {
    Mission m = missionsByProvider.remove(providerUrl);
    if (m != null) {
      removeMission(m.id());
    }
  }

  public Mission getMissionForProvider(String providerUrl) {
    return missionsByProvider.get(providerUrl);
  }

  public void markPendingMissionCompletion(Mission mission, Ship ship) {
    pendingCompletions.put(mission.id(),
        new PendingCompletion(mission.id(), mission.providerUrl(), ship.getShipId(), ship.getTeamId()));
  }

  public PendingCompletion drainPendingCompletion(String missionId) {
    return pendingCompletions.remove(missionId);
  }

  public record PendingCompletion(String missionId, String providerUrl, String shipId, String teamId) {
  }
}
