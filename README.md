# Rivers & Roses – Micronaut Workshop Server

This project is a small game backend built with **Micronaut 4**.  
Teams control mining ships on a 2D map, collect ore, trade at planets, and compete on a leaderboard.  
It’s designed as a workshop codebase to explore:

- Building HTTP APIs & WebSocket endpoints with Micronaut
- Simple in‑memory domain model & game loop
- Command bus pattern and domain events
- Frontend talking to the backend via REST + WebSockets

---

## High‑Level Architecture

**Core pieces:**

- `Application` – bootstraps Micronaut, configures OpenAPI title/version.
- `kernel.engine` – game engine:
  - `GameTickScheduler` – the main tick loop (movement, interactions, missions, events).
  - `CommandBus` / `CommandContext` – queuing & executing commands from controllers.
  - `GameProperties` – game configuration (tick, world size, ores, missions, etc.).
  - `GameStatePersistenceService` – periodic save/load of ships, teams, world to a JSON file.
- `kernel.event` – `EventBus` and `DomainEvent` abstraction.
- `ship` – ships, registration, control:
  - `Ship` / `ShipRepository`
  - DTOs: `RegisterShipRequest/Response`, `ShipStatusResponse`, `SetCourseRequest`, `ShipMarkerDto`
  - `ShipsController` – REST API to register teams & ships, move ships, list “my ships”.
- `team` – teams and credits:
  - `Team` / `TeamRepository`
  - `RegisterTeamRequest`
- `world` – static world state, resources, missions, snapshots:
  - `WorldRepository` – stations, resources, missions, indices.
  - Business services: `NavigationService`, `InteractionService`, `WorldPopulationService`, `WorldSnapshotService`,
    `WorldDeltaService`, `ScannerService`, `MissionService`, `CommerceService`, `WorldInitializer`.
  - DTOs: `WorldSnapshotResponse`, `StationDto`, `MissionDto`, `ResourceDto`, `RadarScanResponse`.
  - `LeaderboardController`, `MissionController`, `RadarController`, `WorldController`.
  - `OreRegistry` / `OreDefinition` / `OreBehavior` – ore types & behaviors.
- `infra.websocket` – WebSocket support:
  - `WorldWebSocket` – `/ws/world`, sends initial snapshot and responds to `ping` / `snapshot` messages.
  - `WorldWebSocketPublisher` – listens to `WorldDeltaEvent` / `WorldSnapshotEvent` and broadcasts.
- `infra.error` – error handling:
  - `DomainException`, `ErrorCode`, `ApiErrorResponse`
  - `GlobalExceptionHandler` – normalizes errors into JSON responses.

**Frontend:**

- Plain HTML/JS (`src/main/resources/public/index.html`)
- Canvas map, “Command Center” UI:
  - Login / register team
  - Build ships
  - Select and move ships (right‑click)
  - Scan radar
  - View leaderboard and logs
- Connects to:
  - REST APIs (`/ships/...`, `/scan`, `/world/snapshot`, `/leaderboard`)
  - WebSocket (`/ws/world`) for real‑time world updates

---

## Running the Project

### Prerequisites

- Java 21 (JDK)
- Maven 3.9+ (<4)
- IntelliJ

### Build and Run

From the project root:

```bash
./mvnw clean package
./mvnw mn:run
```

or:

```bash
./mvnw mn:run
```

Micronaut will start on `http://localhost:8080` by default.

### Useful URLs

- Game UI:  
  `http://localhost:8080/`  
  (serves `public/index.html`)
- OpenAPI / Swagger:  
  `http://localhost:8080/swagger-ui`
- World snapshot:  
  `GET /world/snapshot`
- Leaderboard:  
  `GET /leaderboard`
- Missions:  
  `GET /missions`
- WebSocket:  
  `ws://localhost:8080/ws/world`

---

## Gameplay & API Basics

### 1. Register a Team & Planet

Endpoint:

```http
POST /ships/teams/register
Content-Type: application/json
```

Body (`RegisterTeamRequest`):

```json
{
  "token": "cmd-your-token",
  "teamName": "Red Squadron",
  "planetName": "Mars Base"
}
```

Response (`RegisterShipResponse`):

```json
{
  "teamId": "red squadron",
  "planetId": "planet-...",
  "planetX": 500.0,
  "planetY": 500.0,
  "shipIds": []
}
```

Notes:

- `token` is your long‑lived auth token. The frontend stores it in `localStorage`.
- A home planet is created for your team and placed on the world map.

### 2. Build a Ship

Endpoint:

```http
POST /ships/register
X-Token: cmd-your-token
Content-Type: application/json
```

Body (`RegisterShipRequest`):

```json
{
  "shipName": "Explorer One"
}
```

Response (`ShipStatusResponse`), e.g.:

```json
{
  "shipId": "uuid-...",
  "displayName": "Explorer One",
  "teamName": "Red Squadron",
  "teamId": "red squadron",
  "x": 512.0,
  "y": 488.0,
  "headingDeg": 0.0,
  "speed": 0.0,
  "teamCredits": 0,
  "cargo": {}
}
```

The ship spawns at your home planet.

### 3. List Your Ships

Endpoint:

```http
GET /ships/me
X-Token: cmd-your-token
```

Returns a list of `ShipStatusResponse` for your team.

### 4. Move a Ship

Endpoint:

```http
POST /ships/course
X-Token: cmd-your-token
Content-Type: application/json
```

Body (`SetCourseRequest`):

```json
{
  "shipId": "uuid-ship",
  "targetX": 600.0,
  "targetY": 300.0
}
```

- The command is enqueued via `CommandBus`.
- The game tick (`GameTickScheduler`) updates physics over time:
  - Constant speed (`NavigationService`).
  - Clamped to world bounds.
  - Heading updated towards target.

### 5. Scan (Radar)

Endpoint:

```http
GET /scan?radius=200
X-Token: cmd-your-token
```

Response (`RadarScanResponse`):

```json
{
  "ships": [
    {
      "shipId": "other-ship-id",
      "teamName": "Blue Team",
      "x": 700.0,
      "y": 300.0,
      "speed": 100.0,
      "heading": 45.0
    }
  ],
  "resources": [
    {
      "id": "resource-id",
      "oreId": "iron",
      "value": 10,
      "x": 520.0,
      "y": 500.0
    }
  ]
}
```

When you fly near a resource, `InteractionService` picks it up into your ship’s cargo.

### 6. Selling & Credits

- Resources are automatically sold when your ship is close to a station (home planet or prime base).
- `CommerceService.autoSellIfNearPlanet`:
  - Converts ore in cargo into credits using `GameProperties.world.ores` values.
  - Calls `TeamRepository.addCredits`.
  - Clears ship cargo.

### 7. Leaderboard

Endpoint:

```http
GET /leaderboard
```

Response (top 20 teams by credits):

```json
[
  { "team": "Red Squadron", "score": 500 },
  { "team": "Blue Team", "score": 300 }
]
```

---

## Real‑Time Updates

### WebSocket: `/ws/world`

- **On connect** (`OnOpen`): server sends a `WsMessage` with type `"snapshot"` and payload `WorldSnapshotResponse`.
- **On each tick**:
  - `WorldDeltaService` computes changes (ship positions, removed resources/missions).
  - `WorldDeltaEvent` / `WorldSnapshotEvent` are published.
  - `WorldWebSocketPublisher` broadcasts `WsMessage` with type `"delta"` (and periodic `"snapshot"`).

Message shapes:

```json
// Snapshot
{
  "type": "snapshot",
  "payload": {
    "width": 1000.0,
    "height": 1000.0,
    "homeX": 500.0,
    "homeY": 500.0,
    "refillRadius": 40.0,
    "ships": [ { ...ShipMarkerDto } ],
    "stations": [ { ...StationDto } ],
    "missions": [ { ...MissionDto } ],
    "resources": [ { ...ResourceDto } ]
  }
}

// Delta
{
  "type": "delta",
  "payload": {
    "tick": 42,
    "ops": [
      { "ship": { ... } },              // ShipUpsert
      { "resource": { ... } },          // ResourceUpsert
      { "id": "resource-id" },          // ResourceRemove
      { "mission": { ... } },           // MissionUpsert
      { "id": "mission-id" }            // MissionRemove
    ]
  }
}
```

The frontend interpolates positions between server updates for smooth motion.

---

## Error Handling

All controllers share `GlobalExceptionHandler`:

- Validation errors (`ConstraintViolationException`) → `400` with `code=VALIDATION_ERROR` and `fieldErrors`.
- `DomainException` mapped by `ErrorCode`:
  - `UNAUTHORIZED` → `401`
  - `FORBIDDEN` → `403`
  - `NOT_FOUND` → `404`
  - `BAD_REQUEST` / `VALIDATION_ERROR` → `400`
  - others → `500`
- `IllegalArgumentException` → `400`
- Any other exception → `500` with `code=INTERNAL_ERROR`.

Response shape (`ApiErrorResponse`):

```json
{
  "code": "NOT_FOUND",
  "message": "Ship not found",
  "fieldErrors": null
}
```

---

## Configuration

Game configuration is under `game.*` (`GameProperties`):

Key sections:

```yaml
game:
  tick:
    enabled: true
    periodMs: 500

  physics:
    collectionRadius: 15.0

  home-base:
    x: 500.0
    y: 500.0
    refillRadius: 40.0

  world:
    width: 1000.0
    height: 1000.0
    nodesOnMap: 20       # target number of ore nodes
    missions: 5
    ores:
      iron:
        weight: 5
        value: 10
      diamond:
        weight: 1
        value: 100
      gold:
        weight: 2
        value: 50

  scan:
    baseCost: 0.0
    costPerRadiusUnit: 0.0
    maxRadius: 800.0
    missionCompletionRadius: 20.0

  mission-providers:
    urls: []
    pollIntervalMs: 5000
    timeoutMs: 2000
    completionPath: "/missions/complete"

  savePath: "/tmp/rivers-roses-state.json"
```

- `world.ores` is converted by `OreConfigConverter` into `GameProperties.Ore`.
- `OreRegistry` infers `OreBehavior` from names (`cratecredits`, `cratefuel`, else `CARGO`).

---

## Testing

There is a basic Micronaut test to check that the application starts:

```java
@MicronautTest
class MicronautBasestationTest {
    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }
}
```

---

This project is intentionally simple and in‑memory to keep the focus on Micronaut patterns and game logic rather than infrastructure.