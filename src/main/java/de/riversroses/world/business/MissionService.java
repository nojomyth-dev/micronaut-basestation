package de.riversroses.world.business;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.Vector2;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class MissionService {
  private final GameProperties props;
  private final WorldRepository worldRepo;

  private final Map<String, HttpClient> clientCache = new ConcurrentHashMap<>();

  private volatile Instant lastPoll = Instant.EPOCH;

  public void refreshMissionsIfNeeded(Instant now) {
    if (props.getMissionProviders() == null || props.getMissionProviders().getUrls() == null) {
      return;
    }

    long interval = props.getMissionProviders().getPollIntervalMs() != null
        ? props.getMissionProviders().getPollIntervalMs()
        : 5000L;

    if (Duration.between(lastPoll, now).toMillis() < interval) {
      return;
    }

    lastPoll = now;
    List<String> urls = props.getMissionProviders().getUrls();
    for (String base : urls) {
      fetchMissionFromProvider(base);
    }
  }

  private HttpClient getClient(String baseUrl) {
    return clientCache.computeIfAbsent(baseUrl, url -> {
      try {
        return HttpClient.create(new URL(url));
      } catch (MalformedURLException e) {
        log.error("Invalid mission provider URL: {}", url, e);
        return null;
      }
    });
  }

  private void fetchMissionFromProvider(String baseUrl) {
    HttpClient client = getClient(baseUrl);
    if (client == null)
      return;

    try {
      URI uri = URI.create(baseUrl);
      HttpRequest<?> req = HttpRequest.GET(uri)
          .accept(MediaType.APPLICATION_JSON_TYPE);

      MissionPayload payload = client.toBlocking().retrieve(req, MissionPayload.class);

      if (payload == null || payload.x == null || payload.y == null) {
        worldRepo.clearMissionForProvider(baseUrl);
        return;
      }

      int reward = payload.reward != null ? payload.reward : 100;
      String id = payload.id != null && !payload.id.isBlank() ? payload.id : baseUrl + "-mission";
      Vector2 target = new Vector2(payload.x, payload.y);
      String description = payload.description != null ? payload.description : "External mission";

      Mission m = new Mission(id, description, target, reward, baseUrl, Instant.now());
      worldRepo.putMissionFromProvider(baseUrl, m);
    } catch (Exception e) {
      log.warn("Mission provider {} failed: {}", baseUrl, e.toString());
      worldRepo.clearMissionForProvider(baseUrl);
    }
  }

  public void processMissionCompletionsForShip(de.riversroses.ship.model.Ship ship) {
    var missions = worldRepo.getMissions();
    if (props.getScan() == null)
      return;

    Double radius = props.getScan().getMissionCompletionRadius();
    if (radius == null)
      radius = 100.0;

    for (Mission m : missions) {
      if (ship.getPosition().distanceTo(m.target()) <= radius) {
        var pending = worldRepo.drainPendingCompletion(m.id());
        if (pending == null) {
          worldRepo.markPendingMissionCompletion(m, ship);
          pending = worldRepo.drainPendingCompletion(m.id());
        }
        if (pending != null) {
          notifyCompletion(pending);
          worldRepo.removeMission(m.id());
        }
      }
    }
  }

  private void notifyCompletion(WorldRepository.PendingCompletion pc) {
    String base = pc.providerUrl();
    HttpClient client = getClient(base);
    if (client == null)
      return;

    try {
      String completionPath = props.getMissionProviders().getCompletionPath();
      if (completionPath == null || completionPath.isBlank()) {
        completionPath = "/missions/complete";
      }

      String url;
      if (base.endsWith("/") && completionPath.startsWith("/")) {
        url = base + completionPath.substring(1);
      } else if (!base.endsWith("/") && !completionPath.startsWith("/")) {
        url = base + "/" + completionPath;
      } else {
        url = base + completionPath;
      }

      URI uri = URI.create(url);
      MissionCompletionBody body = new MissionCompletionBody(pc.missionId(), pc.shipId(), pc.teamId());

      HttpRequest<MissionCompletionBody> req = HttpRequest.POST(uri, body)
          .contentType(MediaType.APPLICATION_JSON_TYPE)
          .accept(MediaType.APPLICATION_JSON_TYPE);

      client.toBlocking().exchange(req, Void.class);
    } catch (Exception e) {
      log.warn("Failed to notify mission completion to {}: {}", pc.providerUrl(), e.toString());
    }
  }

  @PreDestroy
  public void cleanup() {
    log.info("Closing {} mission provider clients...", clientCache.size());
    for (HttpClient client : clientCache.values()) {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {
          log.warn("Error closing HTTP client", e);
        }
      }
    }
    clientCache.clear();
  }

  @Serdeable
  public static class MissionPayload {
    public String id;
    public String description;
    public Double x;
    public Double y;
    public Integer reward;
  }

  @Serdeable
  public static class MissionCompletionBody {
    public String missionId;
    public String shipId;
    public String teamId;

    public MissionCompletionBody(String missionId, String shipId, String teamId) {
      this.missionId = missionId;
      this.shipId = shipId;
      this.teamId = teamId;
    }
  }
}
