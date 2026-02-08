package de.riversroses.world.business;

import de.riversroses.kernel.engine.GameProperties;
import de.riversroses.team.db.TeamRepository;
import de.riversroses.world.db.WorldRepository;
import de.riversroses.world.model.Mission;
import de.riversroses.world.model.Vector2;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
@Slf4j
public class MissionService {
  private final GameProperties props;
  private final WorldRepository worldRepo;
  private final TeamRepository teamRepo;

  @Value("${game.missions.reward.min:350}")
  protected int minReward;

  @Value("${game.missions.reward.max:700}")
  protected int maxReward;

  private final Map<String, HttpClient> clientCache = new ConcurrentHashMap<>();

  private volatile Instant lastPoll = Instant.EPOCH;

  public MissionService(GameProperties properties, WorldRepository worldRepository, TeamRepository teamRepository) {
    this.props = properties;
    this.worldRepo = worldRepository;
    this.teamRepo = teamRepository;
  }

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
        return HttpClient.create(new URI(url).toURL());
      } catch (MalformedURLException e) {
        log.error("Invalid mission provider URL: {}", url, e);
        return null;
      } catch (URISyntaxException e) {
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

      String newId = payload.id != null && !payload.id.isBlank() ? payload.id : baseUrl + "-mission";

      Mission existing = worldRepo.getMissionForProvider(baseUrl);
      if (existing != null && existing.id().equals(newId)) {
        return;
      }

      int effectiveMax = Math.max(minReward, maxReward);
      int reward = ThreadLocalRandom.current().nextInt(minReward, effectiveMax + 1);

      Vector2 target = new Vector2(payload.x, payload.y);
      String description = payload.description != null ? payload.description : "External mission";

      Mission m = new Mission(newId, description, target, reward, baseUrl, Instant.now());

      worldRepo.putMissionFromProvider(baseUrl, m);

      log.info("Registered new mission '{}' from provider {} (Reward: {})", newId, baseUrl, reward);

    } catch (Exception e) {
      worldRepo.clearMissionForProvider(baseUrl);
    }
  }

  public void processMissionCompletionsForShip(de.riversroses.ship.model.Ship ship) {

    var missions = worldRepo.getMissions();
    if (props.getScan() == null)
      return;

    Double radius = props.getScan().getMissionCompletionRadius();
    if (radius == null)
      radius = 30.0;

    for (Mission m : missions) {
      if (ship.getPosition().distanceTo(m.target()) <= radius) {

        var pending = worldRepo.drainPendingCompletion(m.id());

        if (pending == null) {
          worldRepo.markPendingMissionCompletion(m, ship);
          pending = worldRepo.drainPendingCompletion(m.id());
        }

        if (pending != null) {
          log.info("Mission completed from {} for {} with reward {}", ship.getTeamName(), pending.teamId(), m.reward());
          notifyCompletion(pending);
          worldRepo.removeMission(m.id());
          teamRepo.addCredits(ship.getTeamId(), m.reward());
        }
      }
    }
  }

  private void notifyCompletion(WorldRepository.PendingCompletion pc) {
    String base = pc.providerUrl();
    HttpClient client = getClient(base);

    if (client == null)
      return;

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
  @Data
  @Introspected
  public static class MissionPayload {
    public String id;
    public String description;
    public Double x;
    public Double y;
    public Integer reward;
  }

  @Serdeable
  @Data
  @Introspected
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
