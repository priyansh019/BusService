# Architectural & Design Decisions

This document details key engineering, data modeling, and UX decisions made while building the Bus Discovery app.

---

## 1. Offline-First Storage: SQLite via Room vs Embedded Mock
- **Decision:** Use a full Android Room SQLite database pre-populated on first launch rather than in-memory mock collections.
- **Rationale:**
  - Real SQL queries, transactions, foreign keys, and indexes reflect production behavior.
  - Allows seamless transition of crowd contributions: when a Moderator approves submission `D1`, an atomic Room transaction inserts it into official `routes`, `route_stops`, `trips`, and `stop_times` tables. It immediately becomes discoverable in search.
  - Resetting demo state simply executes a clean Room re-seeding operation.

## 2. Transit Schematic Map: Compose Canvas vs Map SDK
- **Decision:** Build a vector transit schematic diagram using Jetpack Compose `Canvas` instead of Google Maps / MapLibre.
- **Rationale:**
  - Guarantees 100% offline capability without requiring API keys, map tile caches, or network connectivity.
  - Clear, readable, topological diagram showing transit sequence, intermediate stops, transfer hubs, boarding/alighting status, and live vehicle location.
  - Users can still launch external turn-by-turn navigation (e.g. Google Maps or OpenStreetMap) via an explicit Android Intent (`geo:latitude,longitude`).

## 3. Data Honesty: Refusal to Guess Missing Fares
- **Decision:** If no fare stage or fixed rule matches an origin-destination pair, return `TotalFare(isComplete = false, amount = null, breakdown = "Fare not available (Stage fare unconfirmed)")`.
- **Rationale:**
  - Standard apps often show ₹0 or fabricate a linear price (e.g. ₹20), misleading low-income daily commuters who rely on exact budgeting.
  - The savings calculator explicitly warns the user and disables the comparative calculation when fare is missing, maintaining total honesty.
  - In journey ranking under "Cheapest", journeys with complete verified fares are ranked ahead of incomplete fares, with clear disclaimers.

## 4. Time Handling: Asia/Kolkata DemoClock
- **Decision:** Implement a centralized `DemoClock` supporting three modes:
  1. `FixedWeekdayMorning` (defaults to 2026-10-22 08:00 AM IST) for predictable deterministic testing.
  2. `RealTime` using system time converted to `Asia/Kolkata`.
  3. `Custom` time for testing evening and night services (e.g. `X1N` operating at 23:30).
- **Rationale:** Transit timetables depend strictly on time-of-day and calendar day. Without a controllable demo clock, night buses or morning peak trips could only be tested during actual corresponding hours.

## 5. Strict Role Permission Enforcement & Cross-Tenant Isolation
- **Decision:** Implement `PermissionPolicy` with zero-trust validation before every state change.
- **Rationale:**
  - Operators are scoped to their own `tenantId` (e.g., `OP_UPSRTC`). If an operator attempts to modify a route belonging to another tenant (`OP_DTC`), the action is immediately blocked with an `AuditLogEntry` recording an unauthorized cross-tenant attempt.
  - Drivers cannot broadcast telemetry unless the trip is assigned to them and passenger privacy consent is toggled ON.

## 6. Freshness vs Proximity Disambiguation
- **Decision:** Clearly decouple spatial proximity from data freshness.
- **Rationale:**
  - In GPS-tracked transit, a bus that was 200m away 15 minutes ago is not currently 200m away.
  - Telemetry older than 90 seconds is tagged `TrackingStatus.STALE` with an amber badge showing exact elapsed time (`Updated 4m ago`).
  - Distances are labeled "Straight line distance" with realistic 1.3 walking detour factor and 4.5 km/h walk time calculation.
