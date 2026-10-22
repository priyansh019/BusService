# MASTER BUILD PROMPT — Unified Bus Discovery & Journey Planner (Android APK, in-person event prototype)

> Paste this whole file into your coding agent (Claude Code, Cursor, etc.) in an empty project folder on a machine that has JDK 17 and the Android SDK (or Android Studio). Attach or place `Unified_Bus_Discovery_Platform_Report.docx` in the folder as the product spec of record.

---

## 0. YOUR ROLE AND THE RULES OF ENGAGEMENT

You are a senior Android engineer and product-minded technical lead. Build a **complete, working, installable Android app (APK)** for the product described in the attached report "Unified Bus Discovery & Journey Planner" (India, pilot-first). Read the report fully before writing code; it is the source of truth for scope, data model, planner rules, honesty rules and acceptance criteria. Where this prompt is more specific than the report, this prompt wins.

**Context that drives every decision:** this APK will be demonstrated **in person at an event** — installed on several attendees' phones and on a presenter phone mirrored to a projector, often on poor or no Wi-Fi, handled by strangers who tap unpredictably. Therefore the app must be **fully functional offline, impossible to get stuck in, impossible to crash from normal use, and honest about its data**. A reviewer must be able to complete every flow in the report end to end on the phone, with no server, no login service and no internet.

**Strict rules (violating any of these is a failure):**
1. **No placeholders.** No TODOs, no lorem ipsum, no "coming soon" screens, no dead buttons, no mock screens that don't do the thing. If a control is visible, it works. If a feature can't be done fully, it is not shown.
2. **No fabricated facts presented as real.** All bundled data is a *Demo dataset* and is labelled as such in the UI (persistent banner in About and on first launch). Use **fictional operator names** (see §6). Never invent a fare/schedule when none exists — show "Fare not available". Never claim the app is "the first" of anything.
3. **Do not claim anything works unless you executed it.** Build it, run the tests, install it on an emulator or device if available, and report real command results. If you cannot run something (e.g., no emulator), say exactly that in the final report.
4. **Work autonomously.** Do not stop to ask questions. Make the most reasonable decision, record it in `docs/DECISIONS.md`, and continue.
5. **Quality gates every phase:** the project must compile (`./gradlew assembleDebug`) and unit tests must pass before you start the next phase. Commit to git after each phase with a clear message.
6. **Business rules live in the domain layer, not the UI.** Role permissions, verification rules, boarding rules and fare rules must be enforced in code that tests can call directly — hiding a button is not security.
7. **No scope creep into non-goals** (report §3.3): no payments, no ticket selling, no fleet operation, no nationwide data, no "guaranteed live GPS".

---

## 1. WHAT TO DELIVER

1. Full Android Studio project (Gradle wrapper included, builds from a clean clone with `./gradlew assembleDebug`).
2. `dist/BusDiscovery-demo-debug.apk` (installable debug-signed) **and** `dist/BusDiscovery-demo-release.apk` (minified, signed with a keystore you generate; document the keystore details in `README.md` as demo-only).
3. Docs: `README.md` (build, install via `adb install` and via "install unknown apps", architecture), `docs/DEMO_SCRIPT.md` (a timed 5-minute and a 15-minute presenter walkthrough), `docs/DECISIONS.md`, `docs/ACCEPTANCE_MATRIX.md` (every row of report §16 mapped to the test(s) that prove it, with pass/fail from your actual run), `docs/API_MAPPING.md` (every endpoint in report §10 mapped to the repository method that implements it locally).
4. A final report listing: what was built, what was verified and how (commands + results), what could not be verified, known limitations.

---

## 2. TECH STACK (fixed — do not substitute)

- **Kotlin** (latest stable), **Jetpack Compose + Material 3**, single-activity, Navigation Compose.
- **Room** (SQLite) with KSP, migrations configured; **Kotlinx Serialization** for seed/import JSON; **DataStore** for preferences; **Coroutines + Flow**; **WorkManager** for retention purge and alert checks.
- **Architecture:** clean layering — `ui` (Compose + ViewModels) → `domain` (pure Kotlin: models, use-cases, planner, fare engine, permission policy, validators) → `data` (Room, repositories, importers, simulators). Repository **interfaces** in `domain`, local implementations in `data`, so a FastAPI/PostgreSQL backend (report §7, §14) can replace them later without touching UI/domain. Manual DI or Koin (no Hilt — keep build risk low).
- **Maps:** `osmdroid` (OpenStreetMap tiles) with a proper User-Agent, tile caching and visible attribution "© OpenStreetMap contributors". **Mandatory fallback:** a self-drawn **schematic route map** (Compose Canvas: stops, route line, boarding/alighting markers, current vehicle) that is shown automatically when tiles can't load, and is selectable manually. The app must be fully usable on the schematic map alone.
- **minSdk 26**, targetSdk/compileSdk = latest stable installed. JDK 17. Use a version catalog (`libs.versions.toml`); **verify versions are mutually compatible by actually building** — do not rely on remembered version numbers; check the Google/Maven repositories.
- `applicationId = app.busdiscovery.unified`, launcher name "Bus Discovery". No cleartext traffic (network security config), no hard-coded secrets, R8 on for release, ProGuard rules verified by running the release build.
- Permissions: `INTERNET` (tiles only), `ACCESS_FINE/COARSE_LOCATION` (runtime, optional — app works without), `POST_NOTIFICATIONS` (runtime, optional). Nothing else.
- If the Android SDK/JDK is missing in your environment, install command-line tools and accept licences yourself. If the environment cannot build Android at all, still deliver the complete project plus exact build steps and say plainly that no APK was produced.

---

## 3. NON-NEGOTIABLE PRODUCT HONESTY RULES (from the report — implement as code, test as code)

1. **Provenance on every important fact** (stop location, route, stop sequence, fare, schedule): `source`, `effectiveDate`, `lastVerifiedAt`, `verificationStatus` ∈ `OFFICIAL_VERIFIED` (agency/operator), `COMMUNITY_APPROVED` (moderator-approved report), `UNVERIFIED`, `APPROXIMATE` (location), plus a **derived** `STALE` when `lastVerifiedAt` is older than the admin-configured threshold (default 90 days). Every screen that shows such a fact shows its badge **as icon + text** (never colour alone) and a tap/long-press reveals source, effective date, last verified.
2. **Five tracking/time statuses**, always distinguishable in the UI: `LIVE` (GPS fix ≤ freshness threshold, default 90 s), `SCHEDULED`, `ESTIMATED`, `COMMUNITY_REPORTED`, `UNAVAILABLE`. Scheduled departure ≠ live/predicted arrival; never blend them in one number.
3. **Never fabricate fares.** Unknown fare → "Fare not available", journey total becomes "₹X + unknown" and the journey is flagged `fare_complete=false`.
4. **Validity ≠ proximity.** A service is usable only if its ordered stop sequence serves the board and alight stops **and** the per-stop boarding/alighting permissions allow the movement **and** its calendar runs that day/time. A route line passing near a stop is never evidence.
5. **Community reports stay separate from official data** (own tables, own screens) until a moderator approves with the configured number of approvals. Approved data is published with provenance + timestamp + reviewer; every decision is audit-logged.
6. **Stale positions are hidden from live views** and never shown as live. Stale schedules are labelled, not hidden.
7. **Every journey result explains itself:** assumptions (walking speed, min transfer time, calendar used), data freshness, uncertainty, and missing data.
8. **Privacy:** driver location only for an assigned, active trip, with explicit consent, auto-stop at trip end/consent withdrawal, limited retention (default 24 h, configurable), a "Delete my location data" action, and **driver identity is never exposed to passengers**.

---

## 4. ROLES AND DEMO ACCESS (report §4)

Roles: Passenger (default, no login), Community contributor, Verified operator, Driver, Moderator, Administrator.

- No network auth exists, so implement **local demo accounts** (seeded, clearly labelled "Demo account — local only") behind a role switcher in *Profile → Demo roles* with a PIN per role (display the PINs on that screen; this is a demo). Sessions are real in code: a `Session` object carries `userId`, `roles`, `operatorId?`, and every use-case calls `PermissionPolicy.check(session, action, resource)`.
- Permission matrix to implement and unit-test (deny by default, least privilege):
  - Passenger: search, save journeys, navigate, preferences, submit issue reports.
  - Contributor: all Passenger + submit route/stop/fare/frequency submissions; cannot publish.
  - Operator (tenant-scoped): CRUD **only own** routes/variants/trips/stop_times/fare_rules; import CSV/GTFS for own operator; **any attempt on another operator's data is blocked and written to the audit log**.
  - Driver: start/stop sharing for **assigned active trip only**; report service issues; no read access to others' data.
  - Moderator: review queue — approve / reject / request clarification / merge duplicates; cannot manage users/operators or settings.
  - Administrator: users, operator verification, settings, analytics, data-health, audit log, demo controls.
- Strong-auth note: show an honest line in Admin "Production would require strong authentication (MFA); demo uses PINs."

---

## 5. DATA MODEL (Room) — report §8

Implement all 15 entities with proper foreign keys, indices (route_stops by route/stop and by stop; stops by lat/lng bounding-box columns for proximity queries; stop_times by trip and by stop), and migrations:

`users`, `operators`, `stops` (+ `stop_aliases`), `routes` (with explicit `direction` and `variantOf`), `route_stops` (sequence, `boardingAllowed`, `alightingAllowed`, optionally per-trip overrides), `trips`, `service_calendars` (+ `calendar_exceptions`), `stop_times`, `fare_rules`, `fare_reports`, `vehicles`, `vehicle_positions`, `service_alerts`, `route_submissions`, `verifications` (immutable audit rows), `saved_journeys`. Additional: `recent_searches`, `audit_log`, `analytics_events`, `app_settings`, `import_jobs`.

Integrity rules (test each):
- Stable string IDs; names are never keys. Route **≠** trip (route = path, trip = one service instance).
- Stop times stored as **seconds from service-day start**, may exceed 86 400 (trips crossing midnight); all clock handling via `java.time` with `Asia/Kolkata`; UI shows "+1 day".
- Provenance columns on stops, routes, route_stops, fare_rules, stop_times/trips.
- Proximity: bounding-box prefilter by index, then haversine ordering (no PostGIS on device; note equivalence in `API_MAPPING.md`).
- Fare rule types: `FIXED`, `STAGE` (table of stop-pair → amount), `DISTANCE_BASED` (₹/km along route geometry, minimum, rounding), `OPERATOR_PROVIDED` (partial tables allowed).

---

## 6. SEED DATASET — "DEMO CORRIDOR: Noida – Ghaziabad – Dasna – Hapur"

Ship as JSON in `assets/seed/` loaded on first launch (and by "Reset demo data"). Coordinates are **approximate**; mark the stops accordingly (`APPROXIMATE` where noted). The dataset is demo-only and every operator is fictional. Show a persistent, dismissible-per-session but always-in-About banner: "Demo dataset — illustrative routes/fares/times, not real services."

**Operators:** *Demo State Roadways* (Govt, verified) · *Demo City Bus Services* (Local, verified) · *Demo Private Express* (Private, verified, but data last verified 120 days before seed time).

**Stops (id — name — type):** `mamura` Mamura Bus Stop (Sector 66, Noida) roadside · `mamura_market` Mamura Market (~300 m from Mamura) roadside · `sec62` Noida Sector 62 · `sec37` Noida Sector 37 · `sec18` Noida Sector 18 Metro · `botanical` Botanical Garden (terminal) · `anand_vihar` Anand Vihar ISBT (terminal) · `kashmere` Kashmere Gate ISBT (terminal) · `gzb_adda` Ghaziabad New Bus Adda (terminal) · `masuri` Masuri (roadside) · `dasna` Dasna Bus Stop (roadside) · `dasna_chauraha` Dasna Chauraha (~400 m from `dasna`, `APPROXIMATE`) · `dasna_college` Demo Engineering College, Dasna (**landmark, not a bus stop**, placed ~700 m straight-line from `dasna`, i.e. ≈ 910 m walking after the ×1.3 detour factor) · `hapur` Hapur Bus Stand (terminal). Add aliases (e.g., "Sec 62", "ISBT Anand Vihar", "New Bus Stand Ghaziabad", Hindi names) and localities.

**Routes (each also has an explicit reverse-direction variant):**
| id | Operator | Stops in order | Boarding / alighting rules | Fare rule |
|---|---|---|---|---|
| `L1` | City Bus | mamura → sec62 → sec37 → sec18 → botanical | all stops both | `DISTANCE_BASED` ₹2/km along geometry, min ₹10, round up to ₹1 |
| `L2` | City Bus (feeder) | mamura → sec62 → gzb_adda | all stops both | `FIXED` ₹15 |
| `X1` | State Roadways (express) | anand_vihar → gzb_adda → masuri → dasna → hapur | **Masuri: neither board nor alight (pass-through)**; board at anand_vihar/gzb_adda/dasna; alight at gzb_adda/dasna/hapur | `STAGE`: AV→Adda 15, AV→Dasna 35, AV→Hapur 55, Adda→Dasna 20, Adda→Hapur 40, Dasna→Hapur 20 |
| `X1N` | State Roadways (night) | same as X1, one trip 23:30 from anand_vihar, reaching hapur at 01:00 next day | as X1 | as X1 |
| `X2` | Private Express (ordinary) | kashmere → anand_vihar → gzb_adda → masuri → dasna → hapur | all stops both | `OPERATOR_PROVIDED` **partial**: Kashmere→AV 10, AV→Adda 20; **all other pairs missing** |

**Timetables (daily unless noted; all times IST):**
- `L2`: from Mamura every 30 min 06:30–20:30 at :00/:30; Mamura→Sec62 +15 min; →Adda +40 min.
- `X1`: from Anand Vihar every 30 min 06:00–21:00 at :00/:30; Adda +25; Masuri +45 (no stop); Dasna +60; Hapur +90. (So Adda departures are at :25/:55; Adda→Dasna is 35 min.)
- `X2`: from Kashmere Gate hourly; at Adda departs at **:50**, Masuri +20, Dasna **:25** (35 min from Adda), Hapur +25 more. **Does not run on the demo holiday exception date 2026-11-08** (calendar exception).
- `L1`: every 30 min 06:00–21:00.
- Frequency is shown only because schedules exist; for community-only data show "frequency unknown" or an observation count with confidence.

**Service alerts:** (a) delay of ~20 min on the next `X1` Adda departure (operator-verified, severity warning) — shown as an *Estimated* arrival beside the retained *Scheduled* one; (b) cancellation of one `X2` trip (verified).

**Live feed (local simulator, labelled "Simulated authorised feed"):** one `X1` trip moves along the route geometry updating every 3 s → `LIVE`; one `X2` trip has a last fix 10 minutes old → hidden from live views, status `UNAVAILABLE` ("last signal 10 min ago"); `L1/L2` → `SCHEDULED` only.

**Pre-seeded community content (so the moderator queue is never empty):**
1. Pending route submission **D1 "Mamura → Dasna direct mini-bus"**: private operator, ₹25, ~every 40 min, 07:00–19:00, observation date, text evidence by a seeded contributor. *(This mirrors the report's ₹25 vs ₹50-auto story.)*
2. Pending fare report: `L2` fare ₹15 → ₹18.
3. Pending submission that is a **duplicate** of existing `L2` (must be auto-flagged as duplicate).
4. One frequency observation for `X2`.

**Seeded users/contributions:** demo accounts for every role, one operator account tied to *Demo City Bus Services*, one driver account assigned to the live `X1` trip.

**Preset for the demo:** a "College commute: Mamura → Dasna College" preset (origin `mamura`, destination `dasna_college`, alternative cost ₹50, 22 working days).

---

## 7. FEATURES — implement ALL (map to report §5–§6)

### 7.1 Passenger (Plan / Routes / Stops / Saved)
- **Search:** From/To fields with autocomplete over stops, aliases, landmarks, localities, colleges (fuzzy, prefix + substring, diacritic/case-insensitive, Hindi + English); swap button; "Use my location" → nearest stops; **pick on map**; recent searches; nearby stops list (permission optional); departure time picker (Now / date+time) honouring the **demo clock** (§9.3); preferences chips: priority (Cheapest / Fastest / Fewest transfers / Least walking / Recommended), max transfers (0–2), max walk (m), modes (intracity / intercity / feeder).
- **Results:** cards with legs summary, total fare (with completeness), total time split into walk / wait / ride, departure→arrival, transfers, data-quality badge, tracking status, alert warnings. Sorting changes order deterministically. Empty states explain why (e.g., "No service after 21:00 — last service from Mamura 20:30, next 06:30 tomorrow"; "Walking limit too low — nearest valid stop is ≈ 910 m away").
- **Journey detail:** timeline of legs (walk / wait / bus), ordered stops per leg with intermediate stops expandable, boarding & alighting stop with permission note, fare per leg with provenance, scheduled vs estimated vs live times, assumptions panel, map view (tiles or schematic) with board/alight markers, **"Navigate to boarding point"** (opens external maps walking directions via geo/intent; if none installed, shows coordinates + copy), **Share journey** (text), **Save journey** (with notification preference), **Report an issue** after travel (prefills context).
- **Route detail:** direction toggle, variants, ordered stops with per-stop board/alight icons, first/last service, frequency (only if supported), fare rules summary, schedule list, operator, source, last verified, tracking status, map.
- **Stop detail:** name, aliases, locality, coordinates + approximate flag, serving routes, next departures (scheduled; live if available), "Search from here to any downstream stop" (lists valid downstream stops only).
- **Fare & savings calculator:** one-way, return, monthly (default 22 days, editable); compare with user-entered alternative cost; exact required result for ₹25 vs ₹50 alt → ₹25 one-way, ₹50 round trip, **₹1,100/month**; states the assumption text from report §2.2; refuses to compute when fare is unknown.
- **Alerts screen** + optional Android notifications for verified delays/cancellations affecting saved journeys (channel + permission flow handled gracefully when denied).
- **Language:** English + Hindi UI (full string resources, runtime switch persisted); ₹ and Indian digit grouping; 12-hour clock with AM/PM.

### 7.2 Journey planner engine (report §9) — pure Kotlin, heavily unit-tested
1. Resolve text/coordinate/map point/landmark → candidate stops + access walk (walk speed 4.5 km/h, straight-line × 1.3 detour factor when no pedestrian-routing provider; **state this assumption in results**).
2. Direct journeys: ordered-sequence + boarding/alighting-permission + calendar + service-time check.
3. Connecting journeys: **round-based (RAPTOR-style) scan, max 2 transfers**, minimum transfer time 5 min (inclusive), stop-to-stop walking transfers ≤ 300 m (adds walk time), reject impossible connections, handle trips beyond midnight, honour calendar exceptions.
4. Fare engine for all four rule types; incomplete data never fabricated.
5. Rank by chosen priority; **Recommended** = weighted score with data-quality penalty (STALE/UNVERIFIED/APPROXIMATE rank lower, fare-incomplete rank below fare-complete when sorting by cheapest).
6. Output a rich result model mirroring report Appendix A.2 (journey_id, legs[], total_fare{amount,currency,status,complete}, transfers, data_quality) **plus** source, effective_date, freshness, assumptions[], uncertainty[].
7. Performance: ≤ 500 ms for any seeded query on a mid-range device; run off the main thread.

### 7.3 Community contributor (report §5.7, §6.3)
- Forms: **missing route** (endpoints, known stops with ordering, fare, frequency, operator type, observation date), **new/corrected stop** (pick on map, name, aliases), **fare change**, **frequency observation**; evidence = free text + optional photo (system photo picker, stored privately in app storage).
- Client-side + domain validation: required fields, **impossible stop order** (e.g., same stop twice consecutively, stops that contradict a verified route's direction), **duplicate detection** (normalised name + endpoints + proximity), **rate limit** (default 5 submissions/hour/user, configurable), **idempotency key** so a double-tap or retry never creates two submissions.
- "My submissions" with status timeline (Submitted → Under review → Approved/Rejected/Clarification requested), moderator feedback visible, **reputation points** (configurable awards), after-travel "correction" entry from journey detail.
- Submissions never appear in official search results until approved (test it).

### 7.4 Moderator (report §5.8, §6.3)
- Queue with filters (type, status, age, duplicate flag), detail view showing proposal vs existing data side-by-side with diff, evidence, contributor reputation, auto-checks result.
- Actions: approve (publishes to official tables with `COMMUNITY_APPROVED`, provenance, timestamp, reviewer), reject (reason required), request clarification (message), **merge duplicates**. Configurable approvals required (1 default; 2 optional) — with 2, item stays "Awaiting second approval" and a **different** moderator must approve. Every action writes `verifications` + `audit_log`.
- **Live effect:** after approving D1, re-running the Mamura→Dasna search must immediately show the ₹25 direct option labelled "Community-approved".

### 7.5 Operator console (report §5.8)
- Verified-operator-only, tenant-scoped: list/create/edit **routes and variants**, stop-sequence editor (add/remove/reorder, set board/alight flags, validated), trips + stop_times editor, calendars/exceptions, fare-rule editor for all four types, **CSV import** (stops, routes, route_stops, stop_times, fares) and **GTFS Schedule import** from a ZIP/CSV via the file picker (`stops.txt, routes.txt, trips.txt, stop_times.txt, calendar.txt, calendar_dates.txt`), with a validation report (row-level errors, counts) and an `import_jobs` record. Ship **sample import files in assets** and an "Import sample feed" button so the demo needs no file picker.
- Every change audited (who, what, before/after). Editing another operator's data is refused with a clear message and logged.

### 7.6 Driver mode (report §5.6, §13)
- Driver sees only the assigned active trip. Explicit **consent screen** (what's shared, retention, withdraw anytime). Start/stop sharing; auto-stop at trip end or on consent withdrawal; foreground-service-free design (the demo uses either real device GPS if permitted or **"Simulate along route"** so it works indoors). Positions pass the same validation pipeline as the feed (§7.7). Driver can post a **service issue** (delay, breakdown, diversion) that becomes an unverified alert until a moderator/operator verifies it.
- Passenger-facing live views show only vehicle position + freshness, never driver identity.

### 7.7 Live tracking pipeline (report §12)
- Validate every incoming position: coordinate ranges and India bounding box, timestamp not in the future (± 60 s skew) and not older than threshold, **match to an authorised active trip**, snap to route progress, update latest-position store, compute freshness → status, publish via Flow. Reject with a recorded reason otherwise. Hide stale positions; "last signal" text shown on the trip detail.
- ETA = schedule + simple progress estimate; labelled `ESTIMATED`.
- Retention worker purges history older than the configured retention; "Delete my location data" for drivers.

### 7.8 Admin (report §5.8, §18)
- Users & roles (demo), **operator verification** workflow, **Settings** (freshness threshold, stale-data threshold days, approvals required, rate limit, retention), **Data health monitor** (stale schedules/fares, failed imports, stale feeds, user-reported incorrect routes, counts), **Analytics dashboard** computed from local `analytics_events` for report §18.1 metrics (verified route/stop coverage, % of searches returning a usable journey, searches failed, routes discovered, estimated savings for journeys with known comparable fares, accepted contributions, active operators, share of tracked services with fresh data, stale-data rate) — real numbers from real events, with a simple chart each, **Audit log viewer** (filter + export CSV via share sheet).

### 7.9 Event / demo tooling (this is what makes it safe to show live)
- **Demo Controls (Admin):** *Reset demo data* (one tap + confirm, restores seed incl. pending submissions, < 3 s); *Demo clock* (real time / fixed "Weekday 08:00" / custom date-time — all planners, timetables and "next departure" use it); *Simulate map unavailable*; *Simulate transport-feed outage*; *Live simulator on/off*; *Skip onboarding*.
- **Guided demo tour:** optional overlay that walks 6 steps: (1) search Mamura→Dasna College, (2) compare Cheapest vs Fastest, (3) open a leg, see provenance & the Masuri pass-through, (4) submit D1-style report as Contributor, (5) approve it as Moderator, (6) re-search and see the ₹25 direct, then savings ₹1,100/month.
- **Self-Test screen** (Profile → Developer): runs the planner acceptance scenarios (§10), permission matrix, position validation and DB integrity checks in-app and shows PASS/FAIL per check — the presenter runs it before the event starts.
- **Developer JSON view:** show the exact planner request/response in the report's Appendix A shape with copy button.
- Resilience: map or feed unavailable → persistent but non-blocking banner and automatic schematic fallback; app stays usable offline; all DB/network work off main thread; global crash-safe error states (retry buttons), no ANRs; process-death-safe ViewModel state.
- First launch: 3-screen onboarding (what it is, demo-data disclaimer, optional location/notification permissions — all skippable).

---

## 8. UX / UI REQUIREMENTS

- Material 3, light + dark theme, **bottom navigation**: Plan · Routes · Saved · Contribute · Profile. Role consoles (Moderator, Operator, Driver, Admin) appear after switching role in Profile.
- Design for commuters in India on mid-range phones in sunlight: high contrast, large touch targets (≥ 48 dp), clear typographic hierarchy, ₹ prominent, status badges with icon + text, consistent chips/colours (define a semantic status palette that passes WCAG AA).
- Accessibility: content descriptions, TalkBack order, font scaling to 200 % without clipping, no colour-only meaning. Supports portrait + landscape + tablet width (adaptive layouts), edge-to-edge, predictive back.
- Polish: skeleton/loading states, empty states with a next action, snackbars for results of actions, haptic on key confirmations, sensible animations (≤ 300 ms). Splash screen via the SplashScreen API. Adaptive launcher icon (original art — a stylised bus-stop sign + route line; do not copy any real brand).
- Cold start < 2 s on a mid-range device; APK ≤ ~30 MB.

---

## 9. ENGINEERING REQUIREMENTS

1. Module/package structure: `app` (single Gradle module is fine) with packages `ui/*`, `domain/*`, `data/*`, `di`, `sim`, `util`. Keep files focused; no god classes.
2. State: `ViewModel` + `StateFlow` + immutable UI state; one-shot events via channels; no business logic in composables.
3. **Demo clock** abstraction (`Clock` injected everywhere; never call `System.currentTimeMillis()`/`LocalDateTime.now()` in domain code).
4. Logging via a small wrapper (no PII, no precise driver location in logs). Crash-proof JSON seed loading with schema validation and a clear in-app error if seed is corrupt (with a one-tap re-seed).
5. Static analysis: enable lint with warnings-as-errors for new code where practical; resolve all lint **errors**. No unused permissions.
6. Security: no secrets in the repo; `allowBackup=false`; exported components minimal; file URIs through `FileProvider`; user text sanitised before display/storage; parameterised queries only (Room).
7. Git: meaningful commits, `.gitignore` for build outputs and the demo keystore (keep the keystore *file* out of git but documented).

---

## 10. TESTS (write them, run them, report results)

**Unit tests (JUnit, pure Kotlin where possible) — each must exist and pass:**

| # | Scenario | Expected |
|---|---|---|
| P1 | `mamura` → `dasna`, weekday 08:00, default prefs, before D1 approval | ≥ 2 journeys. **Cheapest:** `L2` + `X1`, 1 transfer, total **₹35**, fare complete, transfer at Adda (L2 arrives 08:40, X1 departs 08:55), arrives Dasna 09:30. **Fastest:** `L2` + `X2`, arrives 09:25, fare shown as "₹15 + unknown" (`fare_complete=false`), data quality STALE. **D1 absent.** |
| P2 | Approve D1 as moderator, repeat P1 | Direct 0-transfer ₹25 option present, badge "Community-approved", provenance names the reviewer; it is now the cheapest. Before approval a Passenger search never returned it. |
| P3 | `masuri` → `hapur` | `X1` never offers boarding at Masuri (pass-through); only `X2` is returned. Also `anand_vihar`→`masuri` on `X1` is rejected for alighting. |
| P4 | `anand_vihar` → `dasna_college`, max walk 1000 m | Bus to `dasna` + walking leg ≈ 910 m (700 m × 1.3 detour) ≈ 12 min, shown with its assumption. With max walk 300 m → no result and an explanatory empty state. |
| P5 | `mamura` → `dasna`, depart 22:30 | No regular journey; empty state states last service (20:30 from Mamura) and next (06:30). |
| P6 | Synthetic data: connection gap 3 min | Rejected. Gap 5 min accepted. Stop-to-stop walk 300 m adds ≈ 5 min (300 m × 1.3 ÷ 75 m/min) before the connection is feasible. |
| P7 | Date 2026-11-08 | `X2` options absent (calendar exception); `X1` unaffected. |
| P8 | `anand_vihar` → `hapur`, depart 23:20 | Uses `X1N`; arrival shown as 01:00 **+1 day**. |
| P9 | `anand_vihar`→`masuri` via `X2` | Fare "not available" — never a number. |
| P10 | Savings: fare 25, alternative 50, 22 days | one-way 25, round trip 50, monthly **1100**. Blank alternative → no savings text. Unknown fare → refuses. |
| P11 | max transfers = 0 | Only direct journeys. |
| P12 | Sorting | Cheapest/Fastest/Fewest transfers/Least walking produce deterministic orders; fare-incomplete sorts after fare-complete under Cheapest. |
| P13 | Stale | `X2` schedule marked STALE (120 d > 90 d threshold); Recommended ranks it below equal-time verified options. |
| P14 | Alerts | Delay alert produces an ESTIMATED arrival while SCHEDULED is retained; cancelled trip excluded. |

**Other unit tests:** fare engine (all 4 rule types, rounding, min fare, missing data); route-stop sequence validation; duplicate detection; rate limiting & idempotency; permission matrix (every role × action, including operator-vs-other-operator blocked + audited, driver-only-assigned-trip, moderator cannot change roles, contributor cannot publish); two-approval flow requires a *different* moderator; position validation (bad lat/lng, future timestamp, unassigned trip, stale); freshness thresholds; retention purge; provenance/STALE derivation; GTFS/CSV import validation (good file, malformed rows, wrong operator); seed integrity (all FKs resolve, all route stop sequences monotonic, no orphan fares).

**Instrumented / UI tests (run on emulator/device if available):** Room queries and migrations; Compose tests for **search → journey detail**, **report → moderation → published → re-search**, role switch + PIN, language switch, offline map fallback, Reset demo data.

**Gate:** `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease` must succeed. Install the debug APK on an emulator/device when one is available and run the Self-Test screen; capture the result. Map every report §16 row to evidence in `docs/ACCEPTANCE_MATRIX.md`.

---

## 11. BUILD ORDER (checkpoint after each; keep build green; commit)

0. Environment check, scaffold, version catalog, theme, navigation shell, CI-style script `scripts/verify.sh`.
1. Domain models, Room schema, seed loader + all seed data, provenance/STALE logic, demo clock, tests.
2. Fare engine, planner (direct → RAPTOR-style transfers), ranking, explanations; P1–P14 green.
3. Passenger UI end to end (search → results → journey detail → route/stop → saved → fare calculator → alerts), both map modes, English + Hindi.
4. Community submissions + moderation + audit; verify the D1 live-effect flow.
5. Operator console + CSV/GTFS import + sample feed; Admin (settings, health, analytics, audit, demo controls).
6. Driver mode + live pipeline + simulator + retention + notifications.
7. Event tooling (tour, self-test, developer JSON), polish, accessibility, performance, icon, onboarding.
8. Full verification, release build, APK to `dist/`, docs, final report.

---

## 12. DEFINITION OF DONE (all must be true)

- [ ] Installs and launches from `dist/BusDiscovery-demo-debug.apk` on Android 8+ with **airplane mode on**, and every flow in §7 works.
- [ ] A first-time user can do "Mamura → Dasna College" and see the cheapest option, provenance, and the savings result in under 60 seconds with no help.
- [ ] The D1 story works end to end: contributor submits → moderator approves → passenger re-search shows the community-approved ₹25 direct → savings = ₹1,100/month.
- [ ] No fabricated fares anywhere; unknown fares, stale schedules, approximate stops, stale GPS and unverified data are all visibly and honestly labelled.
- [ ] Permission matrix enforced in domain code and tested; unauthorised operator edits blocked and audited.
- [ ] Map tiles unavailable → schematic map takes over automatically with no error dialog.
- [ ] Reset demo data restores a clean demo in < 3 s; Self-Test shows all PASS.
- [ ] Build, unit tests, lint, release build all pass; results reported truthfully; docs complete.
- [ ] No TODO/FIXME, no dead UI, no debug-only screens reachable without the Developer menu, no crashes in a 10-minute monkey run (`adb shell monkey -p app.busdiscovery.unified --throttle 100 -v 5000` if a device/emulator exists).

**Start now with Phase 0. Do not ask for confirmation. When all phases are done, give the final report described in §1.**
