package de.riversroses.world.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.riversroses.world.model.Mission;
import de.riversroses.world.model.SpawnedResource;
import de.riversroses.world.model.Station;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;

@Singleton
@AllArgsConstructor
@Data
public class WorldRepository {

  private final Map<String, Station> stations = new ConcurrentHashMap<>();
  private final Map<String, SpawnedResource> resources = new ConcurrentHashMap<>();
  private final Map<String, Mission> missions = new ConcurrentHashMap<>();

  private final Set<String> changedResourceIds = ConcurrentHashMap.newKeySet();
  private final Set<String> removedResourceIds = ConcurrentHashMap.newKeySet();

  private final Set<String> changedMissionIds = ConcurrentHashMap.newKeySet();
  private final Set<String> removedMissionIds = ConcurrentHashMap.newKeySet();

  // Stations
  public void registerStation(Station s) {
    stations.put(s.id(), s);
  }

  public Collection<Station> getStations() {
    return stations.values();
  }

  // Resources
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

  // Missions
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
}
