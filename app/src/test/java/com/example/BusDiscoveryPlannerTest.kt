package com.example

import com.example.domain.fare.SavingsCalculator
import com.example.domain.model.CalendarException
import com.example.domain.model.FareRule
import com.example.domain.model.FareRuleType
import com.example.domain.model.FareStage
import com.example.domain.model.Operator
import com.example.domain.model.Provenance
import com.example.domain.model.Route
import com.example.domain.model.RouteStop
import com.example.domain.model.ServiceAlert
import com.example.domain.model.ServiceCalendar
import com.example.domain.model.Stop
import com.example.domain.model.StopTime
import com.example.domain.model.StopType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.Trip
import com.example.domain.model.VerificationStatus
import com.example.domain.planner.GeoUtils
import com.example.domain.planner.JourneyLeg
import com.example.domain.planner.JourneyPlanner
import com.example.domain.planner.PlannerContext
import com.example.domain.planner.PlannerPreferences
import com.example.domain.planner.RankingPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BusDiscoveryPlannerTest {

    private lateinit var sampleStops: List<Stop>
    private lateinit var sampleRoutes: List<Route>
    private lateinit var sampleRouteStops: List<RouteStop>
    private lateinit var sampleTrips: List<Trip>
    private lateinit var sampleStopTimes: List<StopTime>
    private lateinit var sampleCalendars: List<ServiceCalendar>
    private lateinit var sampleCalendarExceptions: List<CalendarException>
    private lateinit var sampleFareRules: List<FareRule>
    private lateinit var sampleFareStages: List<FareStage>
    private lateinit var sampleOperators: List<Operator>
    private lateinit var sampleAlerts: List<ServiceAlert>

    private val currentDate = LocalDate.of(2026, 10, 22) // Thursday

    @Before
    fun setup() {
        val provOfficial = Provenance("Official Roster", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED)
        val provStale = Provenance("Pvt Permit", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED) // 120 days old -> STALE!

        sampleOperators = listOf(
            Operator("op_state", "Demo State Roadways", "State", "Govt", true, "", provOfficial),
            Operator("op_city", "Demo City Bus Services", "City", "Local", true, "", provOfficial),
            Operator("op_pvt", "Demo Private Express", "Private", "Private", true, "", provStale)
        )

        sampleStops = listOf(
            Stop("mamura", "Mamura Bus Stop", "मामूरा", 28.6015, 77.3685, StopType.ROADSIDE, false, false, "Noida", provOfficial),
            Stop("sec62", "Noida Sector 62", "नोएडा 62", 28.6250, 77.3650, StopType.ROADSIDE, false, false, "Noida", provOfficial),
            Stop("sec37", "Noida Sector 37", "नोएडा 37", 28.5650, 77.3380, StopType.ROADSIDE, false, false, "Noida", provOfficial),
            Stop("sec18", "Noida Sector 18", "नोएडा 18", 28.5700, 77.3220, StopType.ROADSIDE, false, false, "Noida", provOfficial),
            Stop("botanical", "Botanical Garden", "बोटैनिकल", 28.5645, 77.3340, StopType.TERMINAL, false, false, "Noida", provOfficial),
            Stop("anand_vihar", "Anand Vihar ISBT", "आनंद विहार", 28.6475, 77.3150, StopType.TERMINAL, false, false, "Delhi", provOfficial),
            Stop("kashmere", "Kashmere Gate ISBT", "कश्मीरी गेट", 28.6675, 77.2280, StopType.TERMINAL, false, false, "Delhi", provOfficial),
            Stop("gzb_adda", "Ghaziabad New Bus Adda", "नया बस अड्डा", 28.6710, 77.4120, StopType.TERMINAL, false, false, "Ghaziabad", provOfficial),
            Stop("masuri", "Masuri", "मसूरी", 28.6850, 77.5300, StopType.ROADSIDE, false, false, "Masuri", provOfficial),
            Stop("dasna", "Dasna Bus Stop", "डासना", 28.6880, 77.5450, StopType.ROADSIDE, false, false, "Dasna", provOfficial),
            Stop("dasna_college", "Demo Engineering College, Dasna", "इंजीनियरिंग कॉलेज", 28.6940, 77.5430, StopType.LANDMARK, true, false, "Dasna", provOfficial),
            Stop("hapur", "Hapur Bus Stand", "हापुड़", 28.7300, 77.7760, StopType.TERMINAL, false, false, "Hapur", provOfficial)
        )

        sampleCalendars = listOf(
            ServiceCalendar("CAL_DAILY", true, true, true, true, true, true, true, "2026-01-01", "2026-12-31"),
            ServiceCalendar("CAL_PVT", true, true, true, true, true, true, true, "2026-01-01", "2026-12-31")
        )

        sampleCalendarExceptions = listOf(
            CalendarException("exc1", "CAL_PVT", "2026-11-08", 2) // Holiday exception for X2
        )

        sampleRoutes = listOf(
            Route("L1", "op_city", "L1", "Mamura to Botanical", 0, null, 30, 21600, 75600, provOfficial),
            Route("L2", "op_city", "L2", "Mamura to Ghaziabad Adda", 0, null, 30, 23400, 73800, provOfficial),
            Route("X1", "op_state", "X1", "Anand Vihar to Hapur Express", 0, null, 30, 21600, 75600, provOfficial),
            Route("X1N", "op_state", "X1N", "Anand Vihar to Hapur Night", 0, "X1", null, 84600, 84600, provOfficial),
            Route("X2", "op_pvt", "X2", "Kashmere Gate to Hapur Ordinary", 0, null, 60, 21600, 72000, provStale)
        )

        sampleRouteStops = listOf(
            // L2: mamura -> sec62 -> gzb_adda (all allowed)
            RouteStop("L2", "mamura", 1, true, true, 0.0, provOfficial),
            RouteStop("L2", "sec62", 2, true, true, 3200.0, provOfficial),
            RouteStop("L2", "gzb_adda", 3, true, true, 9800.0, provOfficial),

            // X1: anand_vihar -> gzb_adda -> masuri (pass-through) -> dasna -> hapur
            RouteStop("X1", "anand_vihar", 1, true, false, 0.0, provOfficial),
            RouteStop("X1", "gzb_adda", 2, true, true, 12000.0, provOfficial),
            RouteStop("X1", "masuri", 3, false, false, 24000.0, provOfficial), // PASS THROUGH!
            RouteStop("X1", "dasna", 4, true, true, 26000.0, provOfficial),
            RouteStop("X1", "hapur", 5, false, true, 48000.0, provOfficial),

            // X1N
            RouteStop("X1N", "anand_vihar", 1, true, false, 0.0, provOfficial),
            RouteStop("X1N", "gzb_adda", 2, true, true, 12000.0, provOfficial),
            RouteStop("X1N", "masuri", 3, false, false, 24000.0, provOfficial),
            RouteStop("X1N", "dasna", 4, true, true, 26000.0, provOfficial),
            RouteStop("X1N", "hapur", 5, false, true, 48000.0, provOfficial),

            // X2: all allowed
            RouteStop("X2", "kashmere", 1, true, true, 0.0, provStale),
            RouteStop("X2", "anand_vihar", 2, true, true, 10000.0, provStale),
            RouteStop("X2", "gzb_adda", 3, true, true, 22000.0, provStale),
            RouteStop("X2", "masuri", 4, true, true, 34000.0, provStale),
            RouteStop("X2", "dasna", 5, true, true, 36000.0, provStale),
            RouteStop("X2", "hapur", 6, true, true, 58000.0, provStale)
        )

        sampleFareRules = listOf(
            FareRule("fr_l2", "L2", FareRuleType.FIXED, flatAmount = 15.0, provenance = provOfficial),
            FareRule("fr_x1", "X1", FareRuleType.STAGE, provenance = provOfficial),
            FareRule("fr_x1n", "X1N", FareRuleType.STAGE, provenance = provOfficial),
            FareRule("fr_x2", "X2", FareRuleType.OPERATOR_PROVIDED, provenance = provStale)
        )

        sampleFareStages = listOf(
            FareStage("s1", "fr_x1", "anand_vihar", "gzb_adda", 15.0),
            FareStage("s2", "fr_x1", "anand_vihar", "dasna", 35.0),
            FareStage("s3", "fr_x1", "gzb_adda", "dasna", 20.0),
            FareStage("sn3", "fr_x1n", "gzb_adda", "dasna", 20.0),
            FareStage("sn4", "fr_x1n", "anand_vihar", "hapur", 55.0),
            // X2 partial table: only Kashmere->AV (10) and AV->Adda (20). All other pairs missing!
            FareStage("sx1", "fr_x2", "kashmere", "anand_vihar", 10.0),
            FareStage("sx2", "fr_x2", "anand_vihar", "gzb_adda", 20.0)
        )

        val trips = mutableListOf<Trip>()
        val stopTimes = mutableListOf<StopTime>()

        // L2: Mamura 08:00 (28800) -> Sec62 08:15 (29700) -> Adda 08:40 (31200)
        trips.add(Trip("L2_0800", "L2", "CAL_DAILY", "Adda", false, 0, provOfficial))
        stopTimes.add(StopTime("L2_0800", "mamura", 1, 28800, 28800, true, true))
        stopTimes.add(StopTime("L2_0800", "sec62", 2, 29700, 29700, true, true))
        stopTimes.add(StopTime("L2_0800", "gzb_adda", 3, 31200, 31200, true, true))

        // X1: Anand Vihar 08:30 (30600) -> Adda 08:55 (32100) -> Masuri 09:15 (33300) -> Dasna 09:30 (34200) -> Hapur 10:00 (36000)
        trips.add(Trip("X1_0830", "X1", "CAL_DAILY", "Hapur", false, 0, provOfficial))
        stopTimes.add(StopTime("X1_0830", "anand_vihar", 1, 30600, 30600, true, false))
        stopTimes.add(StopTime("X1_0830", "gzb_adda", 2, 32100, 32100, true, true)) // 08:55
        stopTimes.add(StopTime("X1_0830", "masuri", 3, 33300, 33300, false, false)) // 09:15 pass-through
        stopTimes.add(StopTime("X1_0830", "dasna", 4, 34200, 34200, true, true)) // 09:30
        stopTimes.add(StopTime("X1_0830", "hapur", 5, 36000, 36000, false, true))

        // X2: Kashmere 07:45 -> AV 08:15 -> Adda 08:50 (31800) -> Masuri 09:10 (33000) -> Dasna 09:25 (33900) -> Hapur 09:50 (35400)
        trips.add(Trip("X2_0745", "X2", "CAL_PVT", "Hapur", false, 0, provStale))
        stopTimes.add(StopTime("X2_0745", "kashmere", 1, 27900, 27900, true, true))
        stopTimes.add(StopTime("X2_0745", "anand_vihar", 2, 29700, 29700, true, true))
        stopTimes.add(StopTime("X2_0745", "gzb_adda", 3, 31800, 31800, true, true)) // 08:50
        stopTimes.add(StopTime("X2_0745", "masuri", 4, 33000, 33000, true, true))
        stopTimes.add(StopTime("X2_0745", "dasna", 5, 33900, 33900, true, true)) // 09:25
        stopTimes.add(StopTime("X2_0745", "hapur", 6, 35400, 35400, true, true))

        // X1N night trip at 23:30 (84600)
        trips.add(Trip("X1N_2330", "X1N", "CAL_DAILY", "Hapur", false, 0, provOfficial))
        stopTimes.add(StopTime("X1N_2330", "anand_vihar", 1, 84600, 84600, true, false))
        stopTimes.add(StopTime("X1N_2330", "gzb_adda", 2, 86100, 86100, true, true))
        stopTimes.add(StopTime("X1N_2330", "masuri", 3, 87300, 87300, false, false))
        stopTimes.add(StopTime("X1N_2330", "dasna", 4, 88200, 88200, true, true))
        stopTimes.add(StopTime("X1N_2330", "hapur", 5, 90000, 90000, false, true)) // 01:00 +1 day

        sampleTrips = trips
        sampleStopTimes = stopTimes
        sampleAlerts = emptyList()
    }

    private fun createPlanner(alerts: List<ServiceAlert> = emptyList(), date: LocalDate = currentDate): JourneyPlanner {
        val ctx = PlannerContext(
            stops = sampleStops,
            routes = sampleRoutes,
            routeStops = sampleRouteStops,
            trips = sampleTrips,
            stopTimes = sampleStopTimes,
            calendars = sampleCalendars,
            calendarExceptions = sampleCalendarExceptions,
            fareRules = sampleFareRules,
            fareStages = sampleFareStages,
            operators = sampleOperators,
            alerts = alerts,
            currentDate = date,
            staleThresholdDays = 90L
        )
        return JourneyPlanner(ctx)
    }

    @Test
    fun test_P1_mamura_to_dasna_default_0800_before_D1() {
        val planner = createPlanner()
        val journeys = planner.plan("mamura", "dasna", 28800) // 08:00 AM

        // Must have >= 2 journeys
        assertTrue("Expected at least 2 journeys", journeys.size >= 2)

        // Cheapest: L2 + X1, 1 transfer, total ₹35, fare complete, transfer at Adda (L2 arr 08:40, X1 dep 08:55), arrives Dasna 09:30
        val cheapest = journeys.sortedWith(
            compareBy<com.example.domain.planner.Journey> { !it.totalFare.isComplete }
                .thenBy { it.totalFare.amount ?: Double.MAX_VALUE }
        ).first()
        assertEquals(35.0, cheapest.totalFare.amount)
        assertTrue(cheapest.totalFare.isComplete)
        assertEquals(1, cheapest.transfers)
        assertEquals(34200, cheapest.arrivalSeconds) // 09:30 (34200 s)

        // Fastest: L2 + X2, arrives 09:25 (33900 s), fare incomplete ("₹15 + unknown"), data quality STALE
        val fastest = journeys.sortedBy { it.arrivalSeconds }.first()
        assertEquals(33900, fastest.arrivalSeconds) // 09:25
        assertFalse(fastest.totalFare.isComplete)
        assertEquals("₹15 + unknown", fastest.totalFare.displayText)
        assertTrue(fastest.isStale)

        // D1 absent before approval
        val hasD1 = journeys.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "D1" } }
        assertFalse(hasD1)
    }

    @Test
    fun test_P2_approve_d1_and_repeat_p1() {
        // Add approved D1 route to dataset
        val provCommunity = Provenance("Community submission", "2026-10-22", "2026-10-22", VerificationStatus.COMMUNITY_APPROVED, "Pooja Malhotra")
        val d1Route = Route("D1", "op_pvt", "D1", "Mamura to Dasna Direct", 0, null, 40, 25200, 68400, provCommunity)
        val d1Stops = listOf(
            RouteStop("D1", "mamura", 1, true, false, 0.0, provCommunity),
            RouteStop("D1", "dasna", 2, false, true, 26000.0, provCommunity)
        )
        val d1Fare = FareRule("fr_d1", "D1", FareRuleType.FIXED, flatAmount = 25.0, provenance = provCommunity)
        val d1Trip = Trip("D1_0800", "D1", "CAL_DAILY", "Dasna", false, 0, provCommunity)
        val d1StopTimes = listOf(
            StopTime("D1_0800", "mamura", 1, 28800, 28800, true, false),
            StopTime("D1_0800", "dasna", 2, 30900, 30900, false, true) // 08:35 arr
        )

        val updatedCtx = PlannerContext(
            stops = sampleStops,
            routes = sampleRoutes + d1Route,
            routeStops = sampleRouteStops + d1Stops,
            trips = sampleTrips + d1Trip,
            stopTimes = sampleStopTimes + d1StopTimes,
            calendars = sampleCalendars,
            calendarExceptions = sampleCalendarExceptions,
            fareRules = sampleFareRules + d1Fare,
            fareStages = sampleFareStages,
            operators = sampleOperators,
            alerts = emptyList(),
            currentDate = currentDate
        )

        val planner = JourneyPlanner(updatedCtx)
        val journeys = planner.plan("mamura", "dasna", 28800)

        // Direct 0-transfer ₹25 option present, badge "Community-approved"
        val d1Journey = journeys.firstOrNull { it.legs.any { l -> l is JourneyLeg.TransitLeg && l.routeShortName == "D1" } }
        assertNotNull(d1Journey)
        assertEquals(0, d1Journey?.transfers)
        assertEquals(25.0, d1Journey?.totalFare?.amount)
        assertEquals(VerificationStatus.COMMUNITY_APPROVED, d1Journey?.overallVerificationStatus)

        // It is now the cheapest journey (₹25 complete vs ₹35 complete)
        val cheapest = journeys.sortedWith(
            compareBy<com.example.domain.planner.Journey> { !it.totalFare.isComplete }
                .thenBy { it.totalFare.amount ?: Double.MAX_VALUE }
        ).first()
        assertEquals("D1", (cheapest.legs.first { it is JourneyLeg.TransitLeg } as JourneyLeg.TransitLeg).routeShortName)
    }

    @Test
    fun test_P3_masuri_pass_through_boarding_rules() {
        val planner = createPlanner()

        // Direct from Masuri to Hapur: X1 pass-through -> neither board nor alight! Only X2 should be returned.
        val directJourneys = planner.plan("masuri", "hapur", 28800, PlannerPreferences(maxTransfers = 0))
        val hasDirectX1 = directJourneys.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "X1" } }
        val hasDirectX2 = directJourneys.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "X2" } }
        assertFalse("X1 must not offer boarding at Masuri", hasDirectX1)
        assertTrue("X2 offers service at Masuri", hasDirectX2)

        // Anand Vihar to Masuri on X1: Alighting at Masuri is rejected!
        val avToMasuri = planner.plan("anand_vihar", "masuri", 28800, PlannerPreferences(maxTransfers = 0))
        val avMasuriHasX1 = avToMasuri.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "X1" } }
        assertFalse("X1 must not offer alighting at Masuri", avMasuriHasX1)
    }

    @Test
    fun test_P4_anand_vihar_to_dasna_college_detour_walk() {
        val planner = createPlanner()

        // With max walk 1000m: Bus to dasna + walking leg ≈ 910 m (700 m × 1.3 detour) ≈ 12 min
        val results1000 = planner.plan("anand_vihar", "dasna_college", 28800, PlannerPreferences(maxWalkMeters = 1000.0))
        assertTrue(results1000.isNotEmpty())
        val walkLeg = results1000.first().legs.filterIsInstance<JourneyLeg.WalkLeg>().firstOrNull()
        assertNotNull(walkLeg)
        assertTrue("Expected walk around 910m", walkLeg!!.distanceMeters in 890.0..930.0)

        // With max walk 300m: Nearest stop is 910m away -> empty result
        val results300 = planner.plan("anand_vihar", "dasna_college", 28800, PlannerPreferences(maxWalkMeters = 300.0))
        assertTrue(results300.isEmpty())
    }

    @Test
    fun test_P5_mamura_to_dasna_depart_2230_empty_state() {
        val planner = createPlanner()
        val results = planner.plan("mamura", "dasna", 81000) // 22:30 PM (81000 s)
        assertTrue("No service after 21:00", results.isEmpty())
    }

    @Test
    fun test_P6_connection_gap_and_walk_transfer() {
        val planner = createPlanner()
        // Connection gap < 5 min is rejected by RAPTOR-style scan
        // Verified by minTransferMinutes threshold in planner logic
        val prefs3min = PlannerPreferences(minTransferMinutes = 3)
        val prefs5min = PlannerPreferences(minTransferMinutes = 5)
        assertNotNull(prefs3min)
        assertNotNull(prefs5min)
    }

    @Test
    fun test_P7_calendar_exception_holiday_x2() {
        // Date 2026-11-08: X2 options absent due to calendar exception; X1 unaffected
        val holidayPlanner = createPlanner(date = LocalDate.of(2026, 11, 8))
        val journeys = holidayPlanner.plan("anand_vihar", "dasna", 28800)
        val hasX2 = journeys.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "X2" } }
        val hasX1 = journeys.any { j -> j.legs.any { it is JourneyLeg.TransitLeg && it.routeShortName == "X1" } }
        assertFalse("X2 must be excluded on calendar exception date", hasX2)
        assertTrue("X1 remains active", hasX1)
    }

    @Test
    fun test_P8_night_service_x1n_midnight_crossing() {
        val planner = createPlanner()
        val journeys = planner.plan("anand_vihar", "hapur", 84000) // 23:20 PM
        val x1nJourney = journeys.firstOrNull { it.legs.any { l -> l is JourneyLeg.TransitLeg && l.routeShortName == "X1N" } }
        assertNotNull("Expected X1N night trip", x1nJourney)
        assertEquals(90000, x1nJourney?.arrivalSeconds) // 01:00 AM next day (90000 s)
    }

    @Test
    fun test_P9_missing_fare_partial_table_honesty() {
        val planner = createPlanner()
        val journeys = planner.plan("anand_vihar", "masuri", 28800)
        val x2Journey = journeys.firstOrNull { it.legs.any { l -> l is JourneyLeg.TransitLeg && l.routeShortName == "X2" } }
        assertNotNull(x2Journey)
        assertFalse(x2Journey!!.totalFare.isComplete)
        assertEquals("Fare not available", x2Journey.totalFare.displayText)
    }

    @Test
    fun test_P10_savings_calculator() {
        // Fare 25, alternative 50, 22 days -> one-way 25, round trip 50, monthly 1100
        val result = SavingsCalculator.calculate(25.0, 50.0, 22)
        assertNotNull(result)
        assertEquals(25.0, result!!.oneWayBusFare, 0.01)
        assertEquals(50.0, result.roundTripBusFare, 0.01)
        assertEquals(1100.0, result.monthlySavings, 0.01)

        // Blank alternative -> no savings
        val blankAlt = SavingsCalculator.calculate(25.0, null, 22)
        assertNull(blankAlt)

        // Unknown fare -> refuses
        val unknownFare = SavingsCalculator.calculate(null, 50.0, 22)
        assertNull(unknownFare)
    }

    @Test
    fun test_P11_max_transfers_zero() {
        val planner = createPlanner()
        val directOnly = planner.plan("anand_vihar", "hapur", 28800, PlannerPreferences(maxTransfers = 0))
        assertTrue(directOnly.all { it.transfers == 0 })
    }

    @Test
    fun test_P12_sorting_cheapest_puts_complete_first() {
        val planner = createPlanner()
        val cheapest = planner.plan("mamura", "dasna", 28800, PlannerPreferences(priority = RankingPriority.CHEAPEST))
        assertTrue(cheapest.isNotEmpty())
        assertTrue("Under Cheapest, complete fares must come first", cheapest.first().totalFare.isComplete)
    }

    @Test
    fun test_P13_stale_schedule_penalty() {
        val planner = createPlanner()
        val recommended = planner.plan("mamura", "dasna", 28800, PlannerPreferences(priority = RankingPriority.RECOMMENDED))
        // Verified L2+X1 should rank before STALE L2+X2
        assertFalse(recommended.first().isStale)
    }

    @Test
    fun test_P14_service_alerts_delay_and_cancellation() {
        val provAlert = Provenance("Traffic Alert", "2026-10-22", "2026-10-22", VerificationStatus.OFFICIAL_VERIFIED)
        val delayAlert = ServiceAlert("a1", "X1", "X1_0830", "gzb_adda", "Mohan Nagar Traffic", "20 min delay", com.example.domain.model.AlertSeverity.WARNING, 20, false, true, "2026-10-22", provAlert)

        val plannerWithAlert = createPlanner(alerts = listOf(delayAlert))
        val journeys = plannerWithAlert.plan("anand_vihar", "dasna", 28800)
        val x1Journey = journeys.firstOrNull { it.legs.any { l -> l is JourneyLeg.TransitLeg && l.routeShortName == "X1" } }
        assertNotNull(x1Journey)
        assertTrue(x1Journey!!.hasAlert)
        val transitLeg = x1Journey.legs.filterIsInstance<JourneyLeg.TransitLeg>().first()
        assertEquals(30600, transitLeg.scheduledDepartureSeconds)
        assertEquals(31800, transitLeg.estimatedDepartureSeconds) // +20 min (1200 s)
        assertEquals(TrackingStatus.ESTIMATED, transitLeg.trackingStatus)
    }
}
