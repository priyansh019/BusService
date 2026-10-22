# Bus Discovery & Journey Planner (Offline-First Android App)

A production-grade, 100% offline-first Android application designed for semi-formal and informal bus transit corridors across India, piloted on the **Noida – Ghaziabad – Dasna – Hapur (45 km)** corridor.

Built according to the Master Build Prompt specification (`Bus_Discovery_Android_Build_Prompt_08d8f4ee.md`).

---

## Key Features

1. **100% Offline Capability:**
   - Bundled pre-seeded Room SQLite database containing all 14 pilot corridor stops, 5 baseline routes (`L1`, `L2`, `X1`, `X1N`, `X2`), trips, stop times, fare rules, and community contributions.
   - Zero network dependency. No external map tile servers or backend connectivity required to search, plan journeys, calculate fares, simulate live tracking, or submit crowd contributions.
   - Built-in Compose Canvas Transit Schematic Map rendered dynamically from corridor geometry.

2. **Non-Negotiable Data Honesty:**
   - **No Fabricated Data:** If a fare rule or stage is missing, displays "Fare not available (Stage fare unconfirmed)" rather than guessing or fabricating ₹0 / ₹20.
   - **Provenance & Validity Status Badges:** Every item displays an icon + text badge (`OFFICIAL_VERIFIED`, `COMMUNITY_VERIFIED`, `OPERATOR_DECLARED`, `CROWDSOURCED_PENDING`, `DISPUTED`, `STALE`).
   - **Audit Dialog on Tap:** Tapping any badge opens a full audit dialog detailing the source, timestamp, verification method, and confidence score.
   - **Validity ≠ Proximity:** Freshness and geographical proximity are strictly separated. Vehicle telemetry stale (>90s) displays an amber warning badge with human-readable age.

3. **Multi-Modal Journey Planner (RAPTOR-style Scanning):**
   - Direct and transfer journey search with configurable buffer (min 5 minutes, 10 min default).
   - Real calendar exception support (holiday cancellations, daily/weekday service calendars).
   - Boarding/Alighting restrictions enforced (e.g. Masuri pass-through where `pickup_type = 1` forbids boarding).
   - Ranking profiles: Fastest, Fewest Transfers, Cheapest (ranks complete fares first, incomplete fares clearly marked), Most Reliable.

4. **Interactive Savings Calculator:**
   - Compares bus fare against private alternatives (auto-rickshaw, cab, two-wheeler) for one-way, round-trip, and monthly commutes (default 22 working days).
   - Formula: `(Alternative Fare - Bus Fare) * 2 * Working Days` (e.g., ₹50 vs ₹25 over 22 days = ₹1,100/mo savings).
   - Gracefully refuses with explanation if bus fare is incomplete or unknown.

5. **6 Demo Roles with Visible PINs:**
   - Quick one-tap role switcher in Profile:
     - **Passenger (PIN: 0000):** Discovery, planning, saving journeys.
     - **Contributor (PIN: 1111):** Submit new routes, fare reports, stop edits with client-side rate limiting (5/hr) and idempotency.
     - **Operator (PIN: 2222):** Manage tenant routes (UPSRTC `OP_UPSRTC`), upload schedule CSVs. Cross-tenant tampering strictly rejected.
     - **Driver (PIN: 3333):** Trip telemetry broadcasting, manual departure/delay reports, explicit passenger privacy consent toggle, instant location purge.
     - **Moderator (PIN: 4444):** Review crowd queue (e.g., route `D1`), approve, reject, or request changes. Approvals automatically promote routes to official search tables.
     - **Admin (PIN: 9999):** System health metrics, analytics dashboard (§18.1 report metrics), audit log inspection, one-tap demo reset.

6. **In-App Self-Test Engine:**
   - Interactive runner in the app executing all **P1 to P14** acceptance scenarios, role permission enforcement tests, and telemetry validation tests with live visual pass/fail output.

---

## Architecture & Technology Stack

- **Platform:** Android 7.0+ (API 24 / API 26 tested), Target SDK 36.
- **Language:** Kotlin 2.0 (Strict nullability, coroutines, sealed UI states).
- **UI Framework:** Jetpack Compose with Material Design 3 (Dynamic / Semantic color schemes, WCAG AA contrast).
- **Storage:** Android Room (SQLite) with pre-populated corridor dataset, custom TypeConverters, and transaction safety.
- **Timezone:** Standardized on `Asia/Kolkata` with simulated `DemoClock` (Fixed Weekday 08:00 AM, Real-Time, Custom).
- **Navigation:** Jetpack Navigation Compose with type-safe routing.
- **Testing:** Local JVM JUnit 4 + Robolectric unit test suite covering P1–P14.

---

## Build & Test Instructions

### Running Tests
To run all 24 unit tests:
```bash
gradle :app:testDebugUnitTest
```

### Compiling APK
To build the demo APK:
```bash
gradle :app:assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Verification & Acceptance
All P1–P14 criteria defined in the Master Build Prompt have automated test coverage in `app/src/test/java/com/example/BusDiscoveryPlannerTest.kt` and can also be verified visually inside the app via **Profile > Self-Test Suite**.
