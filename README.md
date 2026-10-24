# Route Optimiser

A REST API that takes a start point and a list of destinations and returns a quick order for visiting all of them by road. Driving times come from [OSRM](https://project-osrm.org/) (Open Source Routing Machine), and the ordering is computed by this project's own solvers.

Built with Java, Spring Boot (embedded Tomcat) and Docker.

## How it works

1. A client sends a start point and a list of destinations.
2. The service asks OSRM's `/table` endpoint for the driving time between every pair of points.
3. A solver finds a visiting order: nearest neighbour builds an initial path, then 2-opt improves it.
4. The API returns the ordered stops, the duration of each leg and the total.

The result is an *open path*: it begins at the start point and ends at the last stop, without returning to the start.

```
Client -> RouteController -> RouteOptimisationService -> RoutingClient -> OSRM /table
                                      |
                                      +-> RouteSolver (nearest neighbour + 2-opt)
```

## Quick start

**Prerequisites:**

- Docker with Compose v2 (on Windows, Docker Desktop must be running)
- A bash shell: any Linux or macOS terminal, WSL, or [Git Bash](https://git-scm.com/downloads) on Windows

```bash
git clone https://github.com/Lmatt05/route-optimiser.git
cd route-optimiser

# 1. Download and process a map (needs approximately 3 GB of RAM available to Docker. Default map requires approx 400MB - Greater London UK)
./scripts/prepare-osrm.sh

# 2. Start OSRM and the API
docker compose up --build
```

On Windows, run both commands from Git Bash rather than PowerShell or Command Prompt. If the script isn't executable, run `bash scripts/prepare-osrm.sh` instead.

Check it is running:

```bash
curl http://localhost:8080/actuator/health
```

The default map is Greater London. To use another region, pass its [Geofabrik](https://download.geofabrik.de/) path, for example `./scripts/prepare-osrm.sh europe/germany/berlin`, and use coordinates inside that region.

## API

### `POST /api/v1/routes/optimise`

Request:

```json
{
  "start": { "label": "Depot", "latitude": 51.5074, "longitude": -0.1278 },
  "destinations": [
    { "label": "A", "latitude": 51.5155, "longitude": -0.0922 },
    { "label": "B", "latitude": 51.5033, "longitude": -0.1195 }
  ]
}
```

| Field | Required | Notes |
|---|---|---|
| `start` | yes | where the route begins |
| `destinations` | yes | at least one stop |
| `latitude` | yes | -90 to 90 |
| `longitude` | yes | -180 to 180 |
| `label` | no | up to 100 characters, echoed back in the response |

Example call:

```bash
curl -s -X POST http://localhost:8080/api/v1/routes/optimise \
  -H "Content-Type: application/json" \
  -d '{"start":{"label":"Depot","latitude":51.5074,"longitude":-0.1278},"destinations":[{"label":"A","latitude":51.5155,"longitude":-0.0922},{"label":"B","latitude":51.5033,"longitude":-0.1195}]}'
```

(In IntelliJ you can also run `requests/optimise.http` directly.)

Response:

```json
{
  "stops": [
    {
      "label": "Depot",
      "latitude": 51.5074,
      "longitude": -0.1278
    },
    {
      "label": "B",
      "latitude": 51.5033,
      "longitude": -0.1195
    },
    {
      "label": "A",
      "latitude": 51.5155,
      "longitude": -0.0922
    }
  ],
  "legs": [
    {
      "fromIndex": 0,
      "toIndex": 1,
      "durationSeconds": 304.1
    },
    {
      "fromIndex": 1,
      "toIndex": 2,
      "durationSeconds": 449.0
    }
  ],
  "totalDurationSeconds": 753.1
}
```

`stops` is in visiting order, starting with the start point. Each leg's `fromIndex` and `toIndex` are positions in that array, and durations are in seconds.

### Errors

| Status | When |
|---|---|
| 400 | invalid or missing fields, malformed JSON, no destinations, too many stops |
| 422 | the points cannot be connected by road |
| 502 | the routing service (OSRM) is unavailable or returned an unusable response |
| 500 | unexpected internal error |

Error bodies look like:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "details": [
    "start.latitude: must be greater than or equal to -90.0"
  ]
}
```

## Configuration

| Setting | Environment variable | Default | Purpose |
|---|---|---|---|
| `osrm.base-url` | `OSRM_BASE_URL` | `http://localhost:5000` | where OSRM is reachable |
| `osrm.timeout` | `OSRM_TIMEOUT` | `5s` | connect and read timeout for OSRM |
| `route.max-stops` | `ROUTE_MAXSTOPS` | `50` | maximum stops including the start (keep at or below OSRM's table limit of 100) |

## Design decisions

- **`/table` plus my own solver, rather than OSRM's `/trip`.** OSRM can solve a similar problem server-side. Doing the ordering here keeps the algorithm visible, testable and swappable behind the `RouteSolver` interface.
- **Heuristic, not exact.** Finding the optimal order is NP-hard. Nearest neighbour followed by 2-opt gives good routes quickly, but doesn't guarantee the optimal route.
- **Open path and asymmetric times.** The route doesn't return to the start, and driving time from A to B can differ from B to A (one-way streets). The 2-opt implementation handles both.
- **Ports and adapters.** `domain` has no framework imports. `RoutingClient` and `RouteSolver` are interfaces, so the service is tested with stubs and no network.
- **Validation in layers.** The API layer checks request shape and coordinate ranges. The domain objects enforce their own invariants. The service enforces the stop limit.

## Limitations

- Driving profile only, with no live traffic or time-of-day effects.
- OSRM snaps each point to the nearest road, so a point in a park or river can be routed from somewhere slightly different.
- Results are heuristic and not guaranteed to be the shortest possible.
- Coordinates must fall inside the processed map region.

## Development

```bash
mvn test                      # run the unit tests

docker compose up osrm        # run only OSRM...
mvn spring-boot:run           # ...and the API from your machine (default base URL is localhost:5000)
```

On Windows, run these from Git Bash too, so the commands behave the same as on Linux and macOS.

### Project structure

| Package | Responsibility |
|---|---|
| `api` | controller, request and response DTOs, error handling |
| `domain` | `Coordinate`, `Stop`, `DurationMatrix`, `Route`, `RouteLeg`, domain exceptions |
| `service` | `RouteOptimisationService` orchestrates matrix, solver and route building |
| `solver` | `RouteSolver`, `NearestNeighbourSolver`, `TwoOptSolver` |
| `routing` | `RoutingClient` interface and the OSRM implementation |
| `config` | configuration properties and bean wiring |

## Possible improvements

- Or-opt moves to complement 2-opt
- Route geometry from OSRM's `/route` endpoint for the final order
- Caching repeated matrix requests
- GitHub Actions CI

## Data

Map data is from OpenStreetMap, © OpenStreetMap contributors, available under the [ODbL](https://www.openstreetmap.org/copyright), and downloaded via [Geofabrik](https://download.geofabrik.de/).
