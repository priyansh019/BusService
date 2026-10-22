# Report §10 Endpoint to Local Repository Mapping

In this offline-first architecture, all operations specified as REST endpoints in Report §10 are mapped directly to Kotlin Flow and suspend functions in `BusDiscoveryRepository` backed by Room DAOs and local policy validators.

| Report §10 HTTP Endpoint | Equivalent Repository Method | Room DAO / Domain Component | Notes / Offline Implementation |
|---|---|---|---|
| `GET /api/v1/stops` | `getStops(): Flow<List<Stop>>` | `busDao.getAllStops()` | Emits full list of corridor stops with coordinates and metadata. |
| `GET /api/v1/stops/{id}` | `getStopById(stopId: String): Stop?` | `busDao.getStopById(id)` | Returns stop entity or null. |
| `GET /api/v1/routes` | `getRoutes(): Flow<List<Route>>` | `busDao.getAllRoutes()` | Returns active official and community-approved routes. |
| `GET /api/v1/routes/{id}` | `getRouteById(routeId: String): Route?` | `busDao.getRouteById(id)` | Returns route details with operator ID. |
| `GET /api/v1/routes/{id}/stops` | `getRouteStops(routeId: String): List<RouteStop>` | `busDao.getRouteStops(routeId)` | Returns ordered sequence of stops with `pickup_type` and `drop_off_type`. |
| `POST /api/v1/journeys/plan` | `planJourney(origin, dest, time, config): List<Journey>` | `JourneyPlanner.planJourney()` | Executes in-memory RAPTOR scanning using pre-fetched corridor graph. |
| `GET /api/v1/fares/calculate` | `calculateFare(routeId, fromStop, toStop): TotalFare` | `FareEngine.calculateFare()` | Stage and distance lookups via `busDao.getFareRulesForRoute()`. |
| `POST /api/v1/submissions` | `submitCrowdContribution(submission): String` | `busDao.insertSubmission()`, `SubmissionValidator` | Rate-limited (5/hr), checked for duplicates and idempotency. |
| `GET /api/v1/moderation/queue` | `getPendingSubmissions(): Flow<List<RouteSubmission>>` | `busDao.getPendingSubmissions()` | Filtered by `status = PENDING`. |
| `POST /api/v1/moderation/{id}/review` | `reviewSubmission(id, action, reviewerId, note)` | `BusDiscoveryRepositoryImpl.reviewSubmission()` | Transactionally promotes approved routes (`D1`) to official search tables. |
| `POST /api/v1/telemetry/position` | `updateVehiclePosition(position): Boolean` | `PositionValidator`, `LiveVehicleSimulator` | Validates bounding box, future skew, and suppresses when driver consent is OFF. |
| `GET /api/v1/telemetry/{routeId}` | `getVehiclePositionsForRoute(routeId): Flow<List<VehiclePosition>>` | `busDao.getVehiclePositionsForRoute()` | Emits latest positions with `STALE` status calculation. |
| `POST /api/v1/telemetry/purge` | `purgeVehiclePositions(vehicleId)` | `busDao.deleteVehiclePositions()` | Instant location purge for driver privacy. |
| `GET /api/v1/alerts` | `getActiveAlerts(): Flow<List<ServiceAlert>>` | `busDao.getActiveAlerts()` | Emits active corridor disruptions with severity. |
| `POST /api/v1/operator/schedule/import` | `importOperatorCsv(tenantId, csvData): Int` | `GtfsCsvImporter.importScheduleCsv()` | Validates tenant ownership, rejects cross-tenant edits. |
| `GET /api/v1/admin/analytics` | `getAnalyticsSummary(): AnalyticsSummary` | `busDao.getAnalyticsSummary()` | Aggregates searches, success rate, fallback rate, and audit counts. |
| `POST /api/v1/admin/reset` | `resetDemoData()` | `AppDatabase.resetToSeedData()` | Clears Room tables and re-executes `SeedDataLoader`. |
