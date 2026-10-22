# Live Demo Presentation Scripts

These scripts guide a presenter through demonstrating the **Bus Discovery & Journey Planner** Android app at an in-person showcase or stakeholder review.

---

## 5-Minute Quick Pitch & Demo

### Minute 0:00 – 1:00: Problem & Offline-First Core
1. **Show:** Launch the app. Point out the top demo banner and the offline indicator.
2. **Talking Point:**
   > "Over 70% of bus commuters in semi-formal Indian corridors travel on routes that are neither on Google Maps nor digitized in standard GTFS. Connectivity drops frequently, and low-income commuters can't afford fabricated fares. Bus Discovery is built 100% offline-first for this exact reality."
3. **Action:** On the **Plan** screen, tap the preset badge **"Noida ➔ Ghaziabad"**.

### Minute 1:00 – 2:30: Honest Planning & Schematic Map
1. **Action:** Tap **"Search Journeys"**.
2. **Talking Point:**
   > "Notice the results. We show verified routes like X1 Express and L1 Local. Look at the status badges: each shows provenance (`OFFICIAL_VERIFIED`), not just an arbitrary color.
   > If we look at route fares, when data is verified it shows exact INR. If a stage fare is unverified, our engine explicitly states 'Fare not available'—we never invent fares."
3. **Action:** Tap on the top journey card to open **Journey Detail**.
4. **Action:** Scroll to the **Transit Schematic Map**.
   > "This vector schematic map is rendered completely offline using Compose Canvas. It highlights the passenger's boarding stop, intermediate pass-through stops, and alighting stop, plus real-time simulated bus position."
5. **Action:** Tap the floating **"Savings Calculator"** button. Show the monthly commute comparison (₹25 bus vs ₹50 auto = ₹1,100 saved per month).

### Minute 2:30 – 4:00: The 6-Role Switching & Community Lifecycle
1. **Action:** Go to **Profile** screen. Show the role switcher with visible demo PINs (`0000`, `1111`, `2222`, `3333`, `4444`, `9999`).
2. **Action:** Tap **"Contributor (PIN: 1111)"**. Show the **Contribute** tab where crowdsourced routes, fares, and stop photos can be submitted with 5/hr rate-limiting.
3. **Action:** Switch to **"Moderator (PIN: 4444)"**. Navigate to **Moderation Queue**.
4. **Action:** Tap on pending route **"D1: Dasna to Dhaulana Feeder"**. Tap **"Approve & Publish"**.
   > "With one moderation tap, route D1 is atomically promoted into the official transit tables. Switch back to Passenger, search Dasna to Dhaulana, and it is instantly discoverable."

### Minute 4:00 – 5:00: Data Health, Analytics & Reset
1. **Action:** Switch to **"Admin (PIN: 9999)"**. Show the Report §18.1 analytics dashboard (searches, fallback rate, data health, audit logs).
2. **Action:** Run **Profile > Self-Test Suite**. Show 100% green checkmarks for all P1–P14 tests.
3. **Action:** Tap **"Reset Demo Data"** to restore pristine seed state.
4. **Closing:**
   > "Bus Discovery proves that honest, community-powered, 100% offline transit tools can be delivered on any Android device today."

---

## 15-Minute Comprehensive Deep-Dive

### Segment 1: Corridor Context & Ground Reality (3 mins)
- Explain the pilot corridor: Noida City Centre / Pari Chowk through Ghaziabad, Dasna, to Hapur (45 km).
- Highlight the challenges: mixed operators (UPSRTC, private stage carriages, e-rickshaw feeders), irregular frequencies, frequent mobile dead zones.
- Emphasize the core architectural philosophy: **No internet required after initial install or APK distribution.**

### Segment 2: Multi-Modal Journey Planner & Data Honesty (4 mins)
- **Direct Search:** Search Pari Chowk (S1) to Ghaziabad (S5). Show L1 and X1 options.
- **Transfer Search:** Search Pari Chowk (S1) to Pilkhuwa (S11). Show how the RAPTOR scanner finds transfer journeys at Ghaziabad Old Bus Stand with an enforced ≥5 min buffer.
- **Boarding Restrictions (Masuri Pass-Through):** Demonstrate that searching from Masuri (S8) to Hapur forbids boarding on Express X1 (`pickup_type = 1`), accurately directing passengers to Local X2.
- **Provenance Badges & Tap Audit:** Tap on badges to show the full provenance audit dialog (source, verification method, timestamp).

### Segment 3: Telemetry, Freshness vs Proximity & Driver Consent (3 mins)
- Switch to **Driver (PIN: 3333)**.
- Show assigned trip `TRIP_X1_01`.
- Demonstrate the **Passenger Privacy Consent** toggle. Explain that without explicit consent, GPS telemetry is strictly suppressed.
- Toggle consent ON and start telemetry simulation.
- Explain the **90-second freshness rule**: if telemetry stops updating, the UI immediately marks it `STALE` with an amber warning badge. It never displays an old position as "current".
- Demonstrate **Instant Location Purge** button for driver peace of mind.

### Segment 4: Closed-Loop Community Moderation (3 mins)
- Switch to **Contributor (PIN: 1111)**. Demonstrate the client-side validation: duplicate check, rate limit (5 submissions/hr), and required fields.
- Switch to **Moderator (PIN: 4444)**.
- Review pending submissions: route D1, missing fare report, and stop edit.
- Approve route D1. Show real-time transition to `COMMUNITY_APPROVED`.
- Open **Search** as Passenger and verify route D1 now appears in journey planning.

### Segment 5: Operator Isolation, Admin Governance & Self-Test (2 mins)
- Switch to **Operator (PIN: 2222)**. Tap the button **"Attempt Edit on Route L1 (Cross-Tenant DTC Route)"**.
- Show that `PermissionPolicy` rejects the operation with a clean error message and logs an unauthorized cross-tenant attempt in the audit table.
- Switch to **Admin (PIN: 9999)**. Inspect the audit log to see the recorded security violation.
- Open **Self-Test Suite** and trigger the automated runner for P1–P14.
- Conclude with **Reset Demo Data**.
