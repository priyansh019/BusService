package com.example.data.seed

import com.example.domain.model.AlertSeverity
import com.example.data.local.AnalyticsEventEntity
import com.example.data.local.AppSettingsEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.BusDao
import com.example.data.local.CalendarExceptionEntity
import com.example.data.local.FareRuleEntity
import com.example.data.local.FareStageEntity
import com.example.data.local.OperatorEntity
import com.example.data.local.RouteEntity
import com.example.data.local.RouteStopEntity
import com.example.data.local.RouteSubmissionEntity
import com.example.data.local.ServiceAlertEntity
import com.example.data.local.ServiceCalendarEntity
import com.example.data.local.StopAliasEntity
import com.example.data.local.StopEntity
import com.example.data.local.StopTimeEntity
import com.example.data.local.TripEntity
import com.example.data.local.UserEntity
import com.example.data.local.VehiclePositionEntity
import com.example.data.local.VerificationEntity
import com.example.domain.model.FareRuleType
import com.example.domain.model.StopType
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus

object SeedDataLoader {

    suspend fun resetAndLoadSeedData(dao: BusDao) {
        // Clear all tables
        dao.clearStops()
        dao.clearStopAliases()
        dao.clearRoutes()
        dao.clearRouteStops()
        dao.clearTrips()
        dao.clearStopTimes()
        dao.clearCalendars()
        dao.clearCalendarExceptions()
        dao.clearFareRules()
        dao.clearFareStages()
        dao.clearOperators()
        dao.clearUsers()
        dao.clearVehiclePositions()
        dao.clearAlerts()
        dao.clearSubmissions()
        dao.clearVerifications()
        dao.clearAuditLog()
        dao.clearAnalytics()
        dao.clearSavedJourneys()

        // 1. Seed Operators
        val operators = listOf(
            OperatorEntity(
                id = "op_state",
                name = "Demo State Roadways",
                shortName = "UPSRTC Demo",
                type = "Govt",
                isVerified = true,
                contactPhone = "1800-180-2877",
                source = "State Transport Authority Gazette 2026",
                effectiveDate = "2026-08-01",
                lastVerifiedAt = "2026-10-15",
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                reviewer = "Govt Transport Inspector"
            ),
            OperatorEntity(
                id = "op_city",
                name = "Demo City Bus Services",
                shortName = "City Bus Demo",
                type = "Local",
                isVerified = true,
                contactPhone = "0120-2444555",
                source = "Urban Mobility Directorate 2026",
                effectiveDate = "2026-08-01",
                lastVerifiedAt = "2026-10-15",
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                reviewer = "Municipal Transport Officer"
            ),
            OperatorEntity(
                id = "op_pvt",
                name = "Demo Private Express",
                shortName = "Pvt Express",
                type = "Private",
                isVerified = true,
                contactPhone = "0120-4321987",
                source = "Regional Transport Office Permit Registry",
                effectiveDate = "2026-05-01",
                lastVerifiedAt = "2026-06-24", // 120 days ago -> STALE!
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                reviewer = "RTO Field Surveyor"
            )
        )
        dao.insertOperators(operators)

        // 2. Seed Stops
        val stops = listOf(
            StopEntity("mamura", "Mamura Bus Stop (Sector 66, Noida)", "मामूरा बस स्टॉप", 28.6015, 77.3685, StopType.ROADSIDE, false, false, "Sector 66, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("mamura_market", "Mamura Market", "मामूरा मार्केट", 28.5990, 77.3705, StopType.ROADSIDE, false, false, "Sector 66, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("sec62", "Noida Sector 62", "नोएडा सेक्टर 62", 28.6250, 77.3650, StopType.ROADSIDE, false, false, "Sector 62, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("sec37", "Noida Sector 37", "नोएडा सेक्टर 37", 28.5650, 77.3380, StopType.ROADSIDE, false, false, "Sector 37, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("sec18", "Noida Sector 18 Metro", "नोएडा सेक्टर 18", 28.5700, 77.3220, StopType.ROADSIDE, false, false, "Sector 18, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("botanical", "Botanical Garden", "बोटैनिकल गार्डन", 28.5645, 77.3340, StopType.TERMINAL, false, false, "Botanical Garden, Noida", "Survey 2026", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Transport Officer"),
            StopEntity("anand_vihar", "Anand Vihar ISBT", "आनंद विहार आईएसबीटी", 28.6475, 77.3150, StopType.TERMINAL, false, false, "East Delhi / Anand Vihar", "ISBT Registry", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Terminal Manager"),
            StopEntity("kashmere", "Kashmere Gate ISBT", "कश्मीरी गेट आईएसबीटी", 28.6675, 77.2280, StopType.TERMINAL, false, false, "North Delhi / Kashmere Gate", "ISBT Registry", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Terminal Manager"),
            StopEntity("gzb_adda", "Ghaziabad New Bus Adda", "नया बस अड्डा गाजियाबाद", 28.6710, 77.4120, StopType.TERMINAL, false, false, "New Bus Stand, Ghaziabad", "City Transport Map", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Terminal Manager"),
            StopEntity("masuri", "Masuri", "मसूरी", 28.6850, 77.5300, StopType.ROADSIDE, false, false, "Masuri, NH9", "Highway Authority", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Highway Inspector"),
            StopEntity("dasna", "Dasna Bus Stop", "डासना बस स्टॉप", 28.6880, 77.5450, StopType.ROADSIDE, false, false, "Dasna, Ghaziabad", "RTO Registry", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Field Officer"),
            StopEntity("dasna_chauraha", "Dasna Chauraha", "डासना चौराहा", 28.6865, 77.5410, StopType.ROADSIDE, false, true, "Dasna Chauraha", "Community Report", "2026-08-01", "2026-10-15", VerificationStatus.APPROXIMATE, "Contributor 42"),
            StopEntity("dasna_college", "Demo Engineering College, Dasna", "इंजीनियरिंग कॉलेज डासना", 28.6940, 77.5430, StopType.LANDMARK, true, false, "Dasna Institutional Area", "Campus Directory", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Admin"),
            StopEntity("hapur", "Hapur Bus Stand", "हापुड़ बस स्टैंड", 28.7300, 77.7760, StopType.TERMINAL, false, false, "Hapur Bypass", "State Roadways Roster", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, "Depot Manager")
        )
        dao.insertStops(stops)

        // Stop Aliases
        val aliases = listOf(
            StopAliasEntity("a1", "mamura", "Mamura Chowk"),
            StopAliasEntity("a2", "mamura", "Sector 66"),
            StopAliasEntity("a3", "sec62", "Sec 62"),
            StopAliasEntity("a4", "sec62", "Noida 62"),
            StopAliasEntity("a5", "anand_vihar", "ISBT Anand Vihar"),
            StopAliasEntity("a6", "gzb_adda", "New Bus Stand Ghaziabad"),
            StopAliasEntity("a7", "gzb_adda", "Gzb Adda"),
            StopAliasEntity("a8", "dasna", "Dasna Flyover"),
            StopAliasEntity("a9", "dasna_college", "Dasna College"),
            StopAliasEntity("a10", "dasna_college", "Engineering College")
        )
        dao.insertStopAliases(aliases)

        // 3. Calendars & Exceptions
        val calendars = listOf(
            ServiceCalendarEntity(
                id = "CAL_DAILY",
                monday = true, tuesday = true, wednesday = true, thursday = true, friday = true, saturday = true, sunday = true,
                startDate = "2026-01-01", endDate = "2026-12-31"
            ),
            ServiceCalendarEntity(
                id = "CAL_PVT",
                monday = true, tuesday = true, wednesday = true, thursday = true, friday = true, saturday = true, sunday = true,
                startDate = "2026-01-01", endDate = "2026-12-31"
            )
        )
        dao.insertCalendars(calendars)

        val exceptions = listOf(
            // Calendar exception: X2 does NOT run on 2026-11-08 (demo holiday)
            CalendarExceptionEntity("exc_x2_holiday", "CAL_PVT", "2026-11-08", 2)
        )
        dao.insertCalendarExceptions(exceptions)

        // 4. Routes
        val routes = listOf(
            RouteEntity(
                id = "L1", operatorId = "op_city", shortName = "L1", longName = "Mamura to Botanical Garden",
                direction = 0, variantOf = null, frequencyMinutes = 30, firstDepartureSeconds = 21600, lastDepartureSeconds = 75600,
                source = "Urban Mobility Roster", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "City Transport"
            ),
            RouteEntity(
                id = "L2", operatorId = "op_city", shortName = "L2", longName = "Mamura to Ghaziabad New Bus Adda (Feeder)",
                direction = 0, variantOf = null, frequencyMinutes = 30, firstDepartureSeconds = 23400, lastDepartureSeconds = 73800, // 06:30 to 20:30
                source = "Urban Mobility Roster", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "City Transport"
            ),
            RouteEntity(
                id = "X1", operatorId = "op_state", shortName = "X1", longName = "Anand Vihar to Hapur Express",
                direction = 0, variantOf = null, frequencyMinutes = 30, firstDepartureSeconds = 21600, lastDepartureSeconds = 75600, // 06:00 to 21:00
                source = "State Roadways Gazetted Timetable", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "State Inspector"
            ),
            RouteEntity(
                id = "X1N", operatorId = "op_state", shortName = "X1N", longName = "Anand Vihar to Hapur Night Express",
                direction = 0, variantOf = "X1", frequencyMinutes = null, firstDepartureSeconds = 84600, lastDepartureSeconds = 84600, // 23:30
                source = "State Roadways Night Service", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "State Inspector"
            ),
            RouteEntity(
                id = "X2", operatorId = "op_pvt", shortName = "X2", longName = "Kashmere Gate to Hapur Ordinary",
                direction = 0, variantOf = null, frequencyMinutes = 60, firstDepartureSeconds = 21600, lastDepartureSeconds = 72000,
                source = "RTO Permit Schedule", effectiveDate = "2026-05-01", lastVerifiedAt = "2026-06-24", // 120 days ago -> STALE!
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "Field Surveyor"
            )
        )
        dao.insertRoutes(routes)

        // 5. Route Stops
        val routeStops = listOf(
            // L1: mamura -> sec62 -> sec37 -> sec18 -> botanical (all stops both allowed)
            RouteStopEntity("L1", "mamura", 1, true, true, 0.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L1", "sec62", 2, true, true, 3200.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L1", "sec37", 3, true, true, 7800.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L1", "sec18", 4, true, true, 9500.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L1", "botanical", 5, true, true, 11200.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),

            // L2: mamura -> sec62 -> gzb_adda (all stops both allowed)
            RouteStopEntity("L2", "mamura", 1, true, true, 0.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L2", "sec62", 2, true, true, 3200.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("L2", "gzb_adda", 3, true, true, 9800.0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),

            // X1: anand_vihar -> gzb_adda -> masuri -> dasna -> hapur
            // Masuri: neither board nor alight! Board at AV, Adda, Dasna; Alight at Adda, Dasna, Hapur
            RouteStopEntity("X1", "anand_vihar", 1, true, false, 0.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1", "gzb_adda", 2, true, true, 12000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1", "masuri", 3, false, false, 24000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null), // Pass-through
            RouteStopEntity("X1", "dasna", 4, true, true, 26000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1", "hapur", 5, false, true, 48000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),

            // X1N: same as X1
            RouteStopEntity("X1N", "anand_vihar", 1, true, false, 0.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1N", "gzb_adda", 2, true, true, 12000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1N", "masuri", 3, false, false, 24000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1N", "dasna", 4, true, true, 26000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X1N", "hapur", 5, false, true, 48000.0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null),

            // X2: kashmere -> anand_vihar -> gzb_adda -> masuri -> dasna -> hapur (all stops both)
            RouteStopEntity("X2", "kashmere", 1, true, true, 0.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X2", "anand_vihar", 2, true, true, 10000.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X2", "gzb_adda", 3, true, true, 22000.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X2", "masuri", 4, true, true, 34000.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X2", "dasna", 5, true, true, 36000.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null),
            RouteStopEntity("X2", "hapur", 6, true, true, 58000.0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null)
        )
        dao.insertRouteStops(routeStops)

        // 6. Fare Rules & Fare Stages
        val fareRules = listOf(
            FareRuleEntity("fr_l1", "L1", FareRuleType.DISTANCE_BASED, flatAmount = null, ratePerKm = 2.0, minFare = 10.0, roundUpUnit = 1.0, source = "Municipal Fare Tariff 2026", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "Tariff Board"),
            FareRuleEntity("fr_l2", "L2", FareRuleType.FIXED, flatAmount = 15.0, ratePerKm = null, minFare = null, roundUpUnit = null, source = "Feeder Flat Tariff 2026", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "Tariff Board"),
            FareRuleEntity("fr_x1", "X1", FareRuleType.STAGE, flatAmount = null, ratePerKm = null, minFare = null, roundUpUnit = null, source = "State Roadways Gazetted Stages", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "Tariff Board"),
            FareRuleEntity("fr_x1n", "X1N", FareRuleType.STAGE, flatAmount = null, ratePerKm = null, minFare = null, roundUpUnit = null, source = "State Roadways Gazetted Stages", effectiveDate = "2026-08-01", lastVerifiedAt = "2026-10-15", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "Tariff Board"),
            FareRuleEntity("fr_x2", "X2", FareRuleType.OPERATOR_PROVIDED, flatAmount = null, ratePerKm = null, minFare = null, roundUpUnit = null, source = "Private Operator Self-Declaration", effectiveDate = "2026-05-01", lastVerifiedAt = "2026-06-24", verificationStatus = VerificationStatus.OFFICIAL_VERIFIED, reviewer = "RTO Officer")
        )
        dao.insertFareRules(fareRules)

        // Fare Stages:
        // X1: AV->Adda 15, AV->Dasna 35, AV->Hapur 55, Adda->Dasna 20, Adda->Hapur 40, Dasna->Hapur 20
        val stages = listOf(
            FareStageEntity("s1", "fr_x1", "anand_vihar", "gzb_adda", 15.0),
            FareStageEntity("s2", "fr_x1", "anand_vihar", "dasna", 35.0),
            FareStageEntity("s3", "fr_x1", "anand_vihar", "hapur", 55.0),
            FareStageEntity("s4", "fr_x1", "gzb_adda", "dasna", 20.0),
            FareStageEntity("s5", "fr_x1", "gzb_adda", "hapur", 40.0),
            FareStageEntity("s6", "fr_x1", "dasna", "hapur", 20.0),

            // For X1N as well
            FareStageEntity("sn1", "fr_x1n", "anand_vihar", "gzb_adda", 15.0),
            FareStageEntity("sn2", "fr_x1n", "anand_vihar", "dasna", 35.0),
            FareStageEntity("sn3", "fr_x1n", "anand_vihar", "hapur", 55.0),
            FareStageEntity("sn4", "fr_x1n", "gzb_adda", "dasna", 20.0),
            FareStageEntity("sn5", "fr_x1n", "gzb_adda", "hapur", 40.0),
            FareStageEntity("sn6", "fr_x1n", "dasna", "hapur", 20.0),

            // X2: Partial table only! Kashmere->AV 10, AV->Adda 20; all other pairs missing!
            FareStageEntity("sx1", "fr_x2", "kashmere", "anand_vihar", 10.0),
            FareStageEntity("sx2", "fr_x2", "anand_vihar", "gzb_adda", 20.0)
        )
        dao.insertFareStages(stages)

        // 7. Trips & Stop Times
        val tripsList = mutableListOf<TripEntity>()
        val stopTimesList = mutableListOf<StopTimeEntity>()

        // L2: from Mamura every 30 min 06:30–20:30 at :00/:30; Mamura->Sec62 +15 min; ->Adda +40 min.
        // 06:30 (23400) to 20:30 (73800)
        var l2Dep = 23400
        var l2Idx = 1
        while (l2Dep <= 73800) {
            val tripId = "L2_T$l2Idx"
            tripsList.add(TripEntity(tripId, "L2", "CAL_DAILY", "Ghaziabad New Bus Adda", false, 0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null))
            stopTimesList.add(StopTimeEntity(tripId, "mamura", 1, l2Dep, l2Dep, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "sec62", 2, l2Dep + 900, l2Dep + 900, true, true)) // +15 min
            stopTimesList.add(StopTimeEntity(tripId, "gzb_adda", 3, l2Dep + 2400, l2Dep + 2400, true, true)) // +40 min
            l2Dep += 1800 // 30 min
            l2Idx++
        }

        // X1: from Anand Vihar every 30 min 06:00–21:00 at :00/:30; Adda +25; Masuri +45 (no stop); Dasna +60; Hapur +90.
        // (So Adda departures are at :25/:55; Adda->Dasna is 35 min.)
        var x1Dep = 21600 // 06:00
        var x1Idx = 1
        while (x1Dep <= 75600) {
            val tripId = if (x1Dep == 30600) "X1_0830" else "X1_T$x1Idx" // 08:30 is assigned to driver
            tripsList.add(TripEntity(tripId, "X1", "CAL_DAILY", "Hapur Bus Stand", false, 0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null))
            stopTimesList.add(StopTimeEntity(tripId, "anand_vihar", 1, x1Dep, x1Dep, true, false))
            stopTimesList.add(StopTimeEntity(tripId, "gzb_adda", 2, x1Dep + 1500, x1Dep + 1500, true, true)) // +25 min
            stopTimesList.add(StopTimeEntity(tripId, "masuri", 3, x1Dep + 2700, x1Dep + 2700, false, false)) // +45 min (pass-through)
            stopTimesList.add(StopTimeEntity(tripId, "dasna", 4, x1Dep + 3600, x1Dep + 3600, true, true)) // +60 min (35 min from Adda)
            stopTimesList.add(StopTimeEntity(tripId, "hapur", 5, x1Dep + 5400, x1Dep + 5400, false, true)) // +90 min
            x1Dep += 1800
            x1Idx++
        }

        // X1N: night trip at 23:30 (84600) reaching Hapur at 01:00 next day (90000)
        val x1nTripId = "X1N_NIGHT"
        tripsList.add(TripEntity(x1nTripId, "X1N", "CAL_DAILY", "Hapur Night Express", false, 0, "State Roadways", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null))
        stopTimesList.add(StopTimeEntity(x1nTripId, "anand_vihar", 1, 84600, 84600, true, false))
        stopTimesList.add(StopTimeEntity(x1nTripId, "gzb_adda", 2, 86100, 86100, true, true))
        stopTimesList.add(StopTimeEntity(x1nTripId, "masuri", 3, 87300, 87300, false, false))
        stopTimesList.add(StopTimeEntity(x1nTripId, "dasna", 4, 88200, 88200, true, true))
        stopTimesList.add(StopTimeEntity(x1nTripId, "hapur", 5, 90000, 90000, false, true)) // 01:00 +1 day

        // X2: from Kashmere Gate hourly; at Adda departs at :50, Masuri +20, Dasna :25 (35 min from Adda), Hapur +25 more.
        // For instance trip: departs Kashmere Gate 07:45 (27900), AV 08:15 (29700), Adda 08:50 (31800), Masuri 09:10 (33000), Dasna 09:25 (33900), Hapur 09:50 (35400).
        var x2Adda = 24600 // 06:50 Adda
        var x2Idx = 1
        while (x2Adda <= 71400) {
            val tripId = "X2_T$x2Idx"
            val kgDep = x2Adda - 3900 // Kashmere Gate 65 min before Adda
            val avDep = x2Adda - 2100 // AV 35 min before Adda
            tripsList.add(TripEntity(tripId, "X2", "CAL_PVT", "Hapur Stand", false, 0, "Private Express", "2026-05-01", "2026-06-24", VerificationStatus.OFFICIAL_VERIFIED, null))
            stopTimesList.add(StopTimeEntity(tripId, "kashmere", 1, kgDep, kgDep, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "anand_vihar", 2, avDep, avDep, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "gzb_adda", 3, x2Adda, x2Adda, true, true)) // :50
            stopTimesList.add(StopTimeEntity(tripId, "masuri", 4, x2Adda + 1200, x2Adda + 1200, true, true)) // +20 min
            stopTimesList.add(StopTimeEntity(tripId, "dasna", 5, x2Adda + 2100, x2Adda + 2100, true, true)) // :25 (35 min from Adda)
            stopTimesList.add(StopTimeEntity(tripId, "hapur", 6, x2Adda + 3600, x2Adda + 3600, true, true)) // +25 min
            x2Adda += 3600 // hourly
            x2Idx++
        }

        // L1: every 30 min 06:00–21:00
        var l1Dep = 21600
        var l1Idx = 1
        while (l1Dep <= 75600) {
            val tripId = "L1_T$l1Idx"
            tripsList.add(TripEntity(tripId, "L1", "CAL_DAILY", "Botanical Garden", false, 0, "City Bus", "2026-08-01", "2026-10-15", VerificationStatus.OFFICIAL_VERIFIED, null))
            stopTimesList.add(StopTimeEntity(tripId, "mamura", 1, l1Dep, l1Dep, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "sec62", 2, l1Dep + 600, l1Dep + 600, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "sec37", 3, l1Dep + 1500, l1Dep + 1500, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "sec18", 4, l1Dep + 2100, l1Dep + 2100, true, true))
            stopTimesList.add(StopTimeEntity(tripId, "botanical", 5, l1Dep + 2700, l1Dep + 2700, true, true))
            l1Dep += 1800
            l1Idx++
        }

        dao.insertTrips(tripsList)
        dao.insertStopTimes(stopTimesList)

        // 8. Service Alerts
        val alerts = listOf(
            ServiceAlertEntity(
                id = "alert_x1_delay",
                routeId = "X1",
                tripId = "X1_0830",
                stopId = "gzb_adda",
                title = "Heavy Traffic Delay at Mohan Nagar",
                description = "UPSRTC Express departing Anand Vihar 08:30 is delayed by ~20 min reaching Ghaziabad Adda.",
                severity = AlertSeverity.WARNING,
                delayMinutes = 20,
                isCancelled = false,
                verified = true,
                effectiveDate = "2026-10-22",
                source = "UPSRTC Control Room Dispatch",
                effectiveDateProvenance = "2026-10-22",
                lastVerifiedAt = "2026-10-22 08:10",
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                reviewer = "Traffic Controller"
            ),
            ServiceAlertEntity(
                id = "alert_x2_cancel",
                routeId = "X2",
                tripId = "X2_T4",
                stopId = null,
                title = "Maintenance Cancellation",
                description = "X2 service 10:50 from Ghaziabad Adda cancelled due to vehicle inspection.",
                severity = AlertSeverity.CRITICAL,
                delayMinutes = 0,
                isCancelled = true,
                verified = true,
                effectiveDate = "2026-10-22",
                source = "Private Depot Maintenance Log",
                effectiveDateProvenance = "2026-10-22",
                lastVerifiedAt = "2026-10-22 07:30",
                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                reviewer = "Depot Manager"
            )
        )
        dao.insertAlerts(alerts)

        // 9. Live Vehicle Positions
        val livePositions = listOf(
            // X1 live position near Gzb Adda (fresh timestamp ~ now)
            VehiclePositionEntity(
                id = "pos_x1",
                tripId = "X1_0830",
                vehicleId = "UP-14-BT-1024",
                lat = 28.6690,
                lng = 77.4050,
                timestampSeconds = 1792656000L - 25, // fresh ≤ 90s -> LIVE
                speedKmh = 38.0,
                heading = 78.0,
                status = TrackingStatus.LIVE
            ),
            // X2 stale position: 10 minutes (600s) old -> UNAVAILABLE
            VehiclePositionEntity(
                id = "pos_x2",
                tripId = "X2_T2",
                vehicleId = "UP-16-P-8899",
                lat = 28.6750,
                lng = 77.4450,
                timestampSeconds = 1792656000L - 600, // 10 min old -> UNAVAILABLE
                speedKmh = 0.0,
                heading = 0.0,
                status = TrackingStatus.UNAVAILABLE
            )
        )
        livePositions.forEach { dao.insertVehiclePosition(it) }

        // 10. Pre-seeded Community Submissions
        val submissions = listOf(
            RouteSubmissionEntity(
                id = "sub_d1",
                submissionType = SubmissionType.MISSING_ROUTE,
                contributorId = "user_contributor",
                contributorName = "Rahul Verma",
                operatorName = "Local Mini-Bus Syndicate",
                fromStopName = "Mamura Bus Stop (Sector 66, Noida)",
                toStopName = "Dasna Bus Stop",
                stopsListCsv = "Mamura Bus Stop (Sector 66, Noida), Noida Sector 62, Dasna Bus Stop",
                proposedFare = 25.0,
                proposedFrequencyMinutes = 40,
                operatingHours = "07:00–19:00",
                observationDate = "2026-10-20",
                evidenceText = "Direct 16-seater white minibus runs continuously between Mamura Chowk and Dasna Flyover via NH9. Fare is flat ₹25 collected on board by the conductor. Departure roughly every 35-40 min.",
                photoUri = null,
                status = SubmissionStatus.SUBMITTED,
                duplicateFlag = false,
                duplicateOfId = null,
                moderatorReviewerId = null,
                moderatorNotes = null,
                secondReviewerId = null,
                idempotencyKey = "sub_d1_seed_001",
                submittedAt = System.currentTimeMillis() - 86400000L
            ),
            RouteSubmissionEntity(
                id = "sub_l2_fare",
                submissionType = SubmissionType.FARE_CHANGE,
                contributorId = "user_contributor",
                contributorName = "Rahul Verma",
                operatorName = "Demo City Bus Services",
                fromStopName = "Mamura Bus Stop (Sector 66, Noida)",
                toStopName = "Ghaziabad New Bus Adda",
                stopsListCsv = "Mamura Bus Stop (Sector 66, Noida), Ghaziabad New Bus Adda",
                proposedFare = 18.0,
                proposedFrequencyMinutes = null,
                operatingHours = null,
                observationDate = "2026-10-21",
                evidenceText = "L2 conductor issued paper ticket with revised ₹18 stamp starting this Monday.",
                photoUri = null,
                status = SubmissionStatus.SUBMITTED,
                duplicateFlag = false,
                duplicateOfId = null,
                moderatorReviewerId = null,
                moderatorNotes = null,
                secondReviewerId = null,
                idempotencyKey = "sub_l2_fare_002",
                submittedAt = System.currentTimeMillis() - 43200000L
            ),
            RouteSubmissionEntity(
                id = "sub_l2_dup",
                submissionType = SubmissionType.MISSING_ROUTE,
                contributorId = "user_contributor",
                contributorName = "Rahul Verma",
                operatorName = "City Bus Demo",
                fromStopName = "Mamura Bus Stop (Sector 66, Noida)",
                toStopName = "Ghaziabad New Bus Adda",
                stopsListCsv = "Mamura Bus Stop (Sector 66, Noida), Noida Sector 62, Ghaziabad New Bus Adda",
                proposedFare = 15.0,
                proposedFrequencyMinutes = 30,
                operatingHours = "06:30-20:30",
                observationDate = "2026-10-21",
                evidenceText = "Submitting feeder route from Mamura to Adda.",
                photoUri = null,
                status = SubmissionStatus.SUBMITTED,
                duplicateFlag = true,
                duplicateOfId = "L2",
                moderatorReviewerId = null,
                moderatorNotes = "Auto-flagged duplicate of route L2",
                secondReviewerId = null,
                idempotencyKey = "sub_l2_dup_003",
                submittedAt = System.currentTimeMillis() - 21600000L
            ),
            RouteSubmissionEntity(
                id = "sub_x2_freq",
                submissionType = SubmissionType.FREQUENCY_OBSERVATION,
                contributorId = "user_contributor",
                contributorName = "Rahul Verma",
                operatorName = "Demo Private Express",
                fromStopName = "Ghaziabad New Bus Adda",
                toStopName = "Hapur Bus Stand",
                stopsListCsv = "Ghaziabad New Bus Adda, Dasna Bus Stop, Hapur Bus Stand",
                proposedFare = null,
                proposedFrequencyMinutes = 75,
                operatingHours = "08:00-18:00",
                observationDate = "2026-10-21",
                evidenceText = "Observed intervals averaging 75 mins instead of scheduled 60 mins due to depot shortages.",
                photoUri = null,
                status = SubmissionStatus.SUBMITTED,
                duplicateFlag = false,
                duplicateOfId = null,
                moderatorReviewerId = null,
                moderatorNotes = null,
                secondReviewerId = null,
                idempotencyKey = "sub_x2_freq_004",
                submittedAt = System.currentTimeMillis() - 10800000L
            )
        )
        submissions.forEach { dao.insertSubmission(it) }

        // 11. Seed Users for Demo Roles
        val users = listOf(
            UserEntity("user_passenger", "Anita Sharma (Passenger)", UserRole.PASSENGER, null, null, "0000", 10),
            UserEntity("user_contributor", "Rahul Verma (Contributor)", UserRole.CONTRIBUTOR, null, null, "1111", 45),
            UserEntity("user_operator", "Suresh Gupta (City Bus)", UserRole.OPERATOR, "op_city", null, "2222", 50),
            UserEntity("user_driver", "Vikram Singh (X1 Driver)", UserRole.DRIVER, "op_state", "X1_0830", "3333", 30),
            UserEntity("user_moderator", "Pooja Malhotra (Moderator)", UserRole.MODERATOR, null, null, "4444", 120),
            UserEntity("user_moderator_2", "Arun Patel (Moderator 2)", UserRole.MODERATOR, null, null, "5555", 110),
            UserEntity("user_admin", "Admin System", UserRole.ADMIN, null, null, "9999", 500)
        )
        dao.insertUsers(users)

        // 12. App Settings
        val settings = AppSettingsEntity(
            id = 1,
            gpsFreshnessThresholdSeconds = 90L,
            staleDataThresholdDays = 90L,
            requiredApprovalsForModeration = 1,
            submissionRateLimitPerHour = 5,
            locationRetentionHours = 24L,
            isMapSimulatedUnavailable = false,
            isFeedSimulatedUnavailable = false,
            isLiveSimulatorRunning = true,
            hasCompletedOnboarding = false,
            selectedLanguage = "en"
        )
        dao.saveAppSettings(settings)

        // 13. Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = "log_seed_init",
                timestamp = "2026-10-22 08:00:00",
                userId = "system",
                userRole = UserRole.ADMIN,
                action = "SYSTEM_SEED_INITIALIZED",
                entityType = "CORRIDOR_DATASET",
                entityId = "noida_ghaziabad_dasna_hapur",
                details = "Seeded 14 stops, 5 routes, 150+ trips, 2 alerts, 4 community reports.",
                status = "SUCCESS"
            )
        )

        // 14. Analytics Event
        dao.insertAnalyticsEvent(
            AnalyticsEventEntity(
                id = "evt_seed_ready",
                eventType = "SEED_CORRIDOR_INITIALIZED",
                timestamp = System.currentTimeMillis(),
                payloadJson = "{\"corridor\":\"Noida-Ghaziabad-Dasna-Hapur\",\"stops\":14,\"routes\":5}"
            )
        )
    }
}
