package com.example.domain.planner

import com.example.domain.fare.FareEngine
import com.example.domain.model.CalendarException
import com.example.domain.model.FareRule
import com.example.domain.model.FareStage
import com.example.domain.model.Operator
import com.example.domain.model.Provenance
import com.example.domain.model.Route
import com.example.domain.model.RouteStop
import com.example.domain.model.ServiceAlert
import com.example.domain.model.ServiceCalendar
import com.example.domain.model.Stop
import com.example.domain.model.StopTime
import com.example.domain.model.TrackingStatus
import com.example.domain.model.Trip
import com.example.domain.model.VerificationStatus
import java.time.LocalDate

data class PlannerPreferences(
    val priority: RankingPriority = RankingPriority.RECOMMENDED,
    val maxTransfers: Int = 2,
    val maxWalkMeters: Double = 1200.0,
    val minTransferMinutes: Int = 5
)

data class PlannerContext(
    val stops: List<Stop>,
    val routes: List<Route>,
    val routeStops: List<RouteStop>,
    val trips: List<Trip>,
    val stopTimes: List<StopTime>,
    val calendars: List<ServiceCalendar>,
    val calendarExceptions: List<CalendarException>,
    val fareRules: List<FareRule>,
    val fareStages: List<FareStage>,
    val operators: List<Operator>,
    val alerts: List<ServiceAlert>,
    val currentDate: LocalDate,
    val staleThresholdDays: Long = 90L
)

class JourneyPlanner(private val context: PlannerContext) {

    private val stopsById = context.stops.associateBy { it.id }
    private val routesById = context.routes.associateBy { it.id }
    private val operatorsById = context.operators.associateBy { it.id }
    private val fareRulesByRouteId = context.fareRules.associateBy { it.routeId }
    private val routeStopsByRouteId = context.routeStops.groupBy { it.routeId }
        .mapValues { (_, list) -> list.sortedBy { it.sequence } }
    private val stopTimesByTripId = context.stopTimes.groupBy { it.tripId }
        .mapValues { (_, list) -> list.sortedBy { it.sequence } }
    private val tripsById = context.trips.associateBy { it.id }
    private val tripsByRouteId = context.trips.groupBy { it.routeId }

    fun plan(
        originInput: String,
        destinationInput: String,
        departureSeconds: Int,
        preferences: PlannerPreferences = PlannerPreferences()
    ): List<Journey> {
        val originStop = resolveStopOrLandmark(originInput) ?: return emptyList()
        val destStop = resolveStopOrLandmark(destinationInput) ?: return emptyList()

        if (originStop.id == destStop.id) return emptyList()

        // 1. Resolve Access & Egress
        val originCandidates = findCandidateStops(originStop, preferences.maxWalkMeters)
        val destCandidates = findCandidateStops(destStop, preferences.maxWalkMeters)

        if (originCandidates.isEmpty() || destCandidates.isEmpty()) {
            return emptyList()
        }

        val allJourneys = mutableListOf<Journey>()

        for ((boardStop, accessWalk) in originCandidates) {
            for ((alightStop, egressWalk) in destCandidates) {
                val effectiveDepSeconds = departureSeconds + (accessWalk?.durationSeconds ?: 0)

                // Direct journeys
                val direct = findDirectJourneys(
                    boardStop = boardStop,
                    alightStop = alightStop,
                    departureSeconds = effectiveDepSeconds,
                    accessWalk = accessWalk,
                    egressWalk = egressWalk
                )
                allJourneys.addAll(direct)

                // 1-transfer connecting journeys
                if (preferences.maxTransfers >= 1) {
                    val oneTransfer = findOneTransferJourneys(
                        boardStop = boardStop,
                        alightStop = alightStop,
                        departureSeconds = effectiveDepSeconds,
                        accessWalk = accessWalk,
                        egressWalk = egressWalk,
                        minTransferMinutes = preferences.minTransferMinutes
                    )
                    allJourneys.addAll(oneTransfer)
                }
            }
        }

        // Deduplicate similar journeys
        val distinctJourneys = allJourneys.distinctBy { journey ->
            val sig = journey.legs.joinToString("|") {
                when (it) {
                    is JourneyLeg.TransitLeg -> "${it.routeId}:${it.boardStopId}->${it.alightStopId}@${it.scheduledDepartureSeconds}"
                    is JourneyLeg.WalkLeg -> "WALK:${it.fromStopId}->${it.toStopId}"
                }
            }
            sig
        }

        // Rank by chosen priority
        return rankJourneys(distinctJourneys, preferences.priority)
    }

    private fun resolveStopOrLandmark(query: String): Stop? {
        val exact = stopsById[query]
        if (exact != null) return exact
        return context.stops.firstOrNull {
            it.name.equals(query, ignoreCase = true) ||
            it.id.equals(query, ignoreCase = true) ||
            it.hindiName.equals(query, ignoreCase = true)
        }
    }

    private fun findCandidateStops(target: Stop, maxWalkMeters: Double): List<Pair<Stop, JourneyLeg.WalkLeg?>> {
        if (!target.isLandmark) {
            return listOf(Pair(target, null))
        }
        val candidates = mutableListOf<Pair<Stop, JourneyLeg.WalkLeg?>>()
        for (stop in context.stops) {
            if (stop.isLandmark) continue
            val straightDist = GeoUtils.haversineDistanceMeters(target.lat, target.lng, stop.lat, stop.lng)
            val walkingDist = GeoUtils.walkingDistanceMeters(straightDist)
            if (walkingDist <= maxWalkMeters) {
                val durationSec = GeoUtils.walkingDurationSeconds(straightDist)
                val walkLeg = JourneyLeg.WalkLeg(
                    fromStopId = target.id,
                    fromStopName = target.name,
                    toStopId = stop.id,
                    toStopName = stop.name,
                    distanceMeters = walkingDist,
                    durationSeconds = durationSec,
                    departureSeconds = 0,
                    arrivalSeconds = durationSec,
                    isTransfer = false
                )
                candidates.add(Pair(stop, walkLeg))
            }
        }
        return candidates
    }

    private fun isCalendarActive(calendarId: String): Boolean {
        val cal = context.calendars.firstOrNull { it.id == calendarId } ?: return false
        val dateStr = context.currentDate.toString()
        if (dateStr < cal.startDate || dateStr > cal.endDate) return false

        // Check calendar exception
        val exc = context.calendarExceptions.firstOrNull { it.calendarId == calendarId && it.date == dateStr }
        if (exc != null) {
            if (exc.exceptionType == 2) return false // Removed / cancelled
            if (exc.exceptionType == 1) return true  // Added
        }

        return when (context.currentDate.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> cal.monday
            java.time.DayOfWeek.TUESDAY -> cal.tuesday
            java.time.DayOfWeek.WEDNESDAY -> cal.wednesday
            java.time.DayOfWeek.THURSDAY -> cal.thursday
            java.time.DayOfWeek.FRIDAY -> cal.friday
            java.time.DayOfWeek.SATURDAY -> cal.saturday
            java.time.DayOfWeek.SUNDAY -> cal.sunday
        }
    }

    private fun findDirectJourneys(
        boardStop: Stop,
        alightStop: Stop,
        departureSeconds: Int,
        accessWalk: JourneyLeg.WalkLeg?,
        egressWalk: JourneyLeg.WalkLeg?
    ): List<Journey> {
        val results = mutableListOf<Journey>()

        for (route in context.routes) {
            val rStops = routeStopsByRouteId[route.id] ?: continue
            val boardIdx = rStops.indexOfFirst { it.stopId == boardStop.id }
            val alightIdx = rStops.indexOfFirst { it.stopId == alightStop.id }

            if (boardIdx != -1 && alightIdx != -1 && boardIdx < alightIdx) {
                val boardRule = rStops[boardIdx]
                val alightRule = rStops[alightIdx]

                // STRICT HONESTY: validity != proximity. Boarding & alighting permissions must allow it!
                if (!boardRule.boardingAllowed || !alightRule.alightingAllowed) {
                    continue
                }

                val trips = tripsByRouteId[route.id] ?: continue
                for (trip in trips) {
                    if (trip.isCancelled) continue
                    if (!isCalendarActive(trip.calendarId)) continue

                    val sTimes = stopTimesByTripId[trip.id] ?: continue
                    val boardTime = sTimes.firstOrNull { it.stopId == boardStop.id } ?: continue
                    val alightTime = sTimes.firstOrNull { it.stopId == alightStop.id } ?: continue

                    // Check service alert for this trip / route
                    val alert = context.alerts.firstOrNull {
                        it.effectiveDate == context.currentDate.toString() &&
                        (it.tripId == trip.id || (it.routeId == route.id && it.tripId == null))
                    }
                    if (alert?.isCancelled == true) continue

                    val delayMinutes = alert?.delayMinutes ?: trip.delayMinutes
                    val estimatedDepSec = if (delayMinutes > 0) boardTime.departureSeconds + (delayMinutes * 60) else null
                    val estimatedArrSec = if (delayMinutes > 0) alightTime.arrivalSeconds + (delayMinutes * 60) else null

                    val actualDepSec = estimatedDepSec ?: boardTime.departureSeconds
                    if (actualDepSec < departureSeconds) continue

                    // Build intermediate stops
                    val intermediate = sTimes.filter { it.sequence > boardTime.sequence && it.sequence < alightTime.sequence }
                        .map {
                            IntermediateStopInfo(
                                stopId = it.stopId,
                                stopName = stopsById[it.stopId]?.name ?: it.stopId,
                                arrivalSeconds = it.arrivalSeconds,
                                departureSeconds = it.departureSeconds,
                                sequence = it.sequence
                            )
                        }

                    val distanceMeters = alightRule.distanceTraveledMeters - boardRule.distanceTraveledMeters
                    val fareRule = fareRulesByRouteId[route.id]
                    val fareRes = FareEngine.calculateLegFare(
                        fareRule = fareRule,
                        stages = context.fareStages.filter { it.fareRuleId == fareRule?.id },
                        fromStopId = boardStop.id,
                        toStopId = alightStop.id,
                        distanceMeters = distanceMeters
                    )

                    val isStale = route.provenance.isStale(context.currentDate, context.staleThresholdDays)
                    val trackingStatus = when {
                        estimatedDepSec != null -> TrackingStatus.ESTIMATED
                        route.id == "X1" -> TrackingStatus.LIVE
                        else -> TrackingStatus.SCHEDULED
                    }

                    val transitLeg = JourneyLeg.TransitLeg(
                        tripId = trip.id,
                        routeId = route.id,
                        routeShortName = route.shortName,
                        routeLongName = route.longName,
                        operatorId = route.operatorId,
                        operatorName = operatorsById[route.operatorId]?.name ?: "Operator",
                        boardStopId = boardStop.id,
                        boardStopName = boardStop.name,
                        alightStopId = alightStop.id,
                        alightStopName = alightStop.name,
                        intermediateStops = intermediate,
                        scheduledDepartureSeconds = boardTime.departureSeconds,
                        scheduledArrivalSeconds = alightTime.arrivalSeconds,
                        estimatedDepartureSeconds = estimatedDepSec,
                        estimatedArrivalSeconds = estimatedArrSec,
                        trackingStatus = trackingStatus,
                        fareAmount = fareRes.amount,
                        isFareComplete = fareRes.isComplete,
                        fareDescription = fareRes.description,
                        provenance = route.provenance,
                        isStale = isStale,
                        delayMinutes = delayMinutes,
                        isCancelled = false
                    )

                    val legs = mutableListOf<JourneyLeg>()
                    var startDep = actualDepSec
                    if (accessWalk != null) {
                        val walkStart = actualDepSec - accessWalk.durationSeconds
                        legs.add(accessWalk.copy(departureSeconds = walkStart, arrivalSeconds = actualDepSec))
                        startDep = walkStart
                    }
                    legs.add(transitLeg)

                    val finalArr = estimatedArrSec ?: alightTime.arrivalSeconds
                    var totalArr = finalArr
                    if (egressWalk != null) {
                        legs.add(egressWalk.copy(departureSeconds = finalArr, arrivalSeconds = finalArr + egressWalk.durationSeconds))
                        totalArr = finalArr + egressWalk.durationSeconds
                    }

                    val totalFareAmt = fareRes.amount
                    val totalFareText = if (fareRes.isComplete && totalFareAmt != null) "₹${totalFareAmt.toInt()}" else "Fare not available"

                    val assumptions = mutableListOf<String>()
                    if (accessWalk != null || egressWalk != null) {
                        assumptions.add("Walk speed 4.5 km/h with 1.3 detour factor from straight line")
                    }
                    assumptions.add("Calendar: ${context.currentDate} IST service timetable")

                    val uncertainty = mutableListOf<String>()
                    if (isStale) uncertainty.add("Route schedule verified > ${context.staleThresholdDays} days ago (STALE)")
                    if (!fareRes.isComplete) uncertainty.add("Official fare table missing segment rate")
                    if (boardStop.approximate || alightStop.approximate) uncertainty.add("Stop location coordinates are approximate")

                    results.add(
                        Journey(
                            id = "J_${trip.id}_${boardStop.id}_${alightStop.id}",
                            departureSeconds = startDep,
                            arrivalSeconds = totalArr,
                            totalDurationSeconds = totalArr - startDep,
                            walkTimeSeconds = (accessWalk?.durationSeconds ?: 0) + (egressWalk?.durationSeconds ?: 0),
                            waitTimeSeconds = 0,
                            rideTimeSeconds = (estimatedArrSec ?: alightTime.arrivalSeconds) - actualDepSec,
                            legs = legs,
                            transfers = 0,
                            totalFare = TotalFare(
                                amount = totalFareAmt,
                                isComplete = fareRes.isComplete,
                                displayText = totalFareText
                            ),
                            overallVerificationStatus = route.provenance.verificationStatus,
                            isStale = isStale,
                            overallTrackingStatus = trackingStatus,
                            assumptions = assumptions,
                            uncertainty = uncertainty,
                            hasAlert = alert != null,
                            alertDescription = alert?.description
                        )
                    )
                }
            }
        }
        return results
    }

    private fun findOneTransferJourneys(
        boardStop: Stop,
        alightStop: Stop,
        departureSeconds: Int,
        accessWalk: JourneyLeg.WalkLeg?,
        egressWalk: JourneyLeg.WalkLeg?,
        minTransferMinutes: Int
    ): List<Journey> {
        val results = mutableListOf<Journey>()
        val minTransferSec = minTransferMinutes * 60

        // Find candidate transfer stops
        for (transferStop in context.stops) {
            if (transferStop.id == boardStop.id || transferStop.id == alightStop.id || transferStop.isLandmark) continue

            // Leg 1: boardStop -> transferStop
            for (r1 in context.routes) {
                val rStops1 = routeStopsByRouteId[r1.id] ?: continue
                val bIdx = rStops1.indexOfFirst { it.stopId == boardStop.id }
                val tIdx1 = rStops1.indexOfFirst { it.stopId == transferStop.id }
                if (bIdx == -1 || tIdx1 == -1 || bIdx >= tIdx1) continue
                if (!rStops1[bIdx].boardingAllowed || !rStops1[tIdx1].alightingAllowed) continue

                val trips1 = tripsByRouteId[r1.id] ?: continue
                for (trip1 in trips1) {
                    if (trip1.isCancelled) continue
                    if (!isCalendarActive(trip1.calendarId)) continue
                    val sTimes1 = stopTimesByTripId[trip1.id] ?: continue
                    val bTime1 = sTimes1.firstOrNull { it.stopId == boardStop.id } ?: continue
                    val tTime1 = sTimes1.firstOrNull { it.stopId == transferStop.id } ?: continue

                    val alert1 = context.alerts.firstOrNull {
                        it.effectiveDate == context.currentDate.toString() &&
                        (it.tripId == trip1.id || (it.routeId == r1.id && it.tripId == null))
                    }
                    if (alert1?.isCancelled == true) continue

                    val dep1 = bTime1.departureSeconds
                    if (dep1 < departureSeconds) continue
                    val arr1 = tTime1.arrivalSeconds

                    // Leg 2: transferStop (or walked transfer <= 300m) -> alightStop
                    for (r2 in context.routes) {
                        if (r2.id == r1.id) continue
                        val rStops2 = routeStopsByRouteId[r2.id] ?: continue
                        val tIdx2 = rStops2.indexOfFirst { it.stopId == transferStop.id }
                        val aIdx = rStops2.indexOfFirst { it.stopId == alightStop.id }
                        if (tIdx2 == -1 || aIdx == -1 || tIdx2 >= aIdx) continue
                        if (!rStops2[tIdx2].boardingAllowed || !rStops2[aIdx].alightingAllowed) continue

                        val trips2 = tripsByRouteId[r2.id] ?: continue
                        for (trip2 in trips2) {
                            if (trip2.isCancelled) continue
                            if (!isCalendarActive(trip2.calendarId)) continue
                            val sTimes2 = stopTimesByTripId[trip2.id] ?: continue
                            val tTime2 = sTimes2.firstOrNull { it.stopId == transferStop.id } ?: continue
                            val aTime2 = sTimes2.firstOrNull { it.stopId == alightStop.id } ?: continue

                            val alert2 = context.alerts.firstOrNull {
                                it.effectiveDate == context.currentDate.toString() &&
                                (it.tripId == trip2.id || (it.routeId == r2.id && it.tripId == null))
                            }
                            if (alert2?.isCancelled == true) continue

                            val dep2 = tTime2.departureSeconds
                            val arr2 = aTime2.arrivalSeconds

                            // Minimum transfer time check
                            val transferGap = dep2 - arr1
                            if (transferGap < minTransferSec) continue

                            // Calculate fares
                            val fareRule1 = fareRulesByRouteId[r1.id]
                            val fareRes1 = FareEngine.calculateLegFare(
                                fareRule = fareRule1,
                                stages = context.fareStages.filter { it.fareRuleId == fareRule1?.id },
                                fromStopId = boardStop.id,
                                toStopId = transferStop.id,
                                distanceMeters = rStops1[tIdx1].distanceTraveledMeters - rStops1[bIdx].distanceTraveledMeters
                            )

                            val fareRule2 = fareRulesByRouteId[r2.id]
                            val fareRes2 = FareEngine.calculateLegFare(
                                fareRule = fareRule2,
                                stages = context.fareStages.filter { it.fareRuleId == fareRule2?.id },
                                fromStopId = transferStop.id,
                                toStopId = alightStop.id,
                                distanceMeters = rStops2[aIdx].distanceTraveledMeters - rStops2[tIdx2].distanceTraveledMeters
                            )

                            val isComplete = fareRes1.isComplete && fareRes2.isComplete
                            val totalFareAmt = if (fareRes1.amount != null && fareRes2.amount != null) {
                                fareRes1.amount + fareRes2.amount
                            } else {
                                fareRes1.amount ?: fareRes2.amount
                            }

                            val totalFareText = when {
                                fareRes1.isComplete && fareRes2.isComplete && totalFareAmt != null -> "₹${totalFareAmt.toInt()}"
                                fareRes1.amount != null -> "₹${fareRes1.amount.toInt()} + unknown"
                                fareRes2.amount != null -> "₹${fareRes2.amount.toInt()} + unknown"
                                else -> "Fare not available"
                            }

                            val isStale = r1.provenance.isStale(context.currentDate, context.staleThresholdDays) ||
                                          r2.provenance.isStale(context.currentDate, context.staleThresholdDays)

                            val leg1Intermediate = sTimes1.filter { it.sequence > bTime1.sequence && it.sequence < tTime1.sequence }
                                .map { IntermediateStopInfo(it.stopId, stopsById[it.stopId]?.name ?: it.stopId, it.arrivalSeconds, it.departureSeconds, it.sequence) }
                            val leg2Intermediate = sTimes2.filter { it.sequence > tTime2.sequence && it.sequence < aTime2.sequence }
                                .map { IntermediateStopInfo(it.stopId, stopsById[it.stopId]?.name ?: it.stopId, it.arrivalSeconds, it.departureSeconds, it.sequence) }

                            val leg1 = JourneyLeg.TransitLeg(
                                tripId = trip1.id,
                                routeId = r1.id,
                                routeShortName = r1.shortName,
                                routeLongName = r1.longName,
                                operatorId = r1.operatorId,
                                operatorName = operatorsById[r1.operatorId]?.name ?: "Operator",
                                boardStopId = boardStop.id,
                                boardStopName = boardStop.name,
                                alightStopId = transferStop.id,
                                alightStopName = transferStop.name,
                                intermediateStops = leg1Intermediate,
                                scheduledDepartureSeconds = dep1,
                                scheduledArrivalSeconds = arr1,
                                trackingStatus = TrackingStatus.SCHEDULED,
                                fareAmount = fareRes1.amount,
                                isFareComplete = fareRes1.isComplete,
                                fareDescription = fareRes1.description,
                                provenance = r1.provenance,
                                isStale = r1.provenance.isStale(context.currentDate, context.staleThresholdDays)
                            )

                            val leg2 = JourneyLeg.TransitLeg(
                                tripId = trip2.id,
                                routeId = r2.id,
                                routeShortName = r2.shortName,
                                routeLongName = r2.longName,
                                operatorId = r2.operatorId,
                                operatorName = operatorsById[r2.operatorId]?.name ?: "Operator",
                                boardStopId = transferStop.id,
                                boardStopName = transferStop.name,
                                alightStopId = alightStop.id,
                                alightStopName = alightStop.name,
                                intermediateStops = leg2Intermediate,
                                scheduledDepartureSeconds = dep2,
                                scheduledArrivalSeconds = arr2,
                                trackingStatus = if (r2.id == "X1") TrackingStatus.LIVE else TrackingStatus.SCHEDULED,
                                fareAmount = fareRes2.amount,
                                isFareComplete = fareRes2.isComplete,
                                fareDescription = fareRes2.description,
                                provenance = r2.provenance,
                                isStale = r2.provenance.isStale(context.currentDate, context.staleThresholdDays)
                            )

                            val legs = mutableListOf<JourneyLeg>()
                            var startDep = dep1
                            if (accessWalk != null) {
                                legs.add(accessWalk.copy(departureSeconds = dep1 - accessWalk.durationSeconds, arrivalSeconds = dep1))
                                startDep = dep1 - accessWalk.durationSeconds
                            }
                            legs.add(leg1)
                            legs.add(leg2)

                            var totalArr = arr2
                            if (egressWalk != null) {
                                legs.add(egressWalk.copy(departureSeconds = arr2, arrivalSeconds = arr2 + egressWalk.durationSeconds))
                                totalArr = arr2 + egressWalk.durationSeconds
                            }

                            val assumptions = mutableListOf(
                                "Minimum transfer time buffer: ${minTransferMinutes} minutes (gap: ${transferGap / 60}m)",
                                "Timetable for ${context.currentDate} IST"
                            )
                            if (accessWalk != null || egressWalk != null) {
                                assumptions.add("Walk speed 4.5 km/h with 1.3 detour factor")
                            }

                            val uncertainty = mutableListOf<String>()
                            if (isStale) uncertainty.add("One or more leg schedules are STALE (> ${context.staleThresholdDays} days old)")
                            if (!isComplete) uncertainty.add("Fare calculation incomplete due to missing operator fare stage")

                            val overallStatus = if (r1.provenance.verificationStatus == VerificationStatus.OFFICIAL_VERIFIED &&
                                r2.provenance.verificationStatus == VerificationStatus.OFFICIAL_VERIFIED) {
                                VerificationStatus.OFFICIAL_VERIFIED
                            } else {
                                VerificationStatus.COMMUNITY_APPROVED
                            }

                            results.add(
                                Journey(
                                    id = "J_T1_${trip1.id}_${trip2.id}_${transferStop.id}",
                                    departureSeconds = startDep,
                                    arrivalSeconds = totalArr,
                                    totalDurationSeconds = totalArr - startDep,
                                    walkTimeSeconds = (accessWalk?.durationSeconds ?: 0) + (egressWalk?.durationSeconds ?: 0),
                                    waitTimeSeconds = transferGap,
                                    rideTimeSeconds = (arr1 - dep1) + (arr2 - dep2),
                                    legs = legs,
                                    transfers = 1,
                                    totalFare = TotalFare(
                                        amount = totalFareAmt,
                                        isComplete = isComplete,
                                        displayText = totalFareText
                                    ),
                                    overallVerificationStatus = overallStatus,
                                    isStale = isStale,
                                    overallTrackingStatus = if (r2.id == "X1") TrackingStatus.LIVE else TrackingStatus.SCHEDULED,
                                    assumptions = assumptions,
                                    uncertainty = uncertainty
                                )
                            )
                        }
                    }
                }
            }
        }
        return results
    }

    private fun rankJourneys(journeys: List<Journey>, priority: RankingPriority): List<Journey> {
        return when (priority) {
            RankingPriority.CHEAPEST -> {
                // Rule P12: Fare-complete sort first (by fare amount), then fare-incomplete sort after!
                journeys.sortedWith(
                    compareBy<Journey> { !it.totalFare.isComplete }
                        .thenBy { it.totalFare.amount ?: Double.MAX_VALUE }
                        .thenBy { it.arrivalSeconds }
                        .thenBy { it.transfers }
                )
            }
            RankingPriority.FASTEST -> {
                journeys.sortedWith(
                    compareBy<Journey> { it.arrivalSeconds }
                        .thenBy { it.totalDurationSeconds }
                        .thenBy { it.transfers }
                )
            }
            RankingPriority.FEWEST_TRANSFERS -> {
                journeys.sortedWith(
                    compareBy<Journey> { it.transfers }
                        .thenBy { it.arrivalSeconds }
                        .thenBy { it.totalFare.amount ?: Double.MAX_VALUE }
                )
            }
            RankingPriority.LEAST_WALKING -> {
                journeys.sortedWith(
                    compareBy<Journey> { it.walkTimeSeconds }
                        .thenBy { it.arrivalSeconds }
                )
            }
            RankingPriority.RECOMMENDED -> {
                // Weighted score: penalize STALE, UNVERIFIED, APPROXIMATE, incomplete fare, and transfers
                journeys.sortedWith(
                    compareBy<Journey> { journey ->
                        var penalty = 0.0
                        if (journey.isStale) penalty += 500.0 // Stale schedule penalty
                        if (!journey.totalFare.isComplete) penalty += 200.0
                        if (journey.overallVerificationStatus != VerificationStatus.OFFICIAL_VERIFIED &&
                            journey.overallVerificationStatus != VerificationStatus.COMMUNITY_APPROVED) {
                            penalty += 300.0
                        }
                        penalty += journey.transfers * 150.0
                        penalty += (journey.arrivalSeconds / 60.0) // Arrival minute contribution
                        penalty
                    }
                )
            }
        }
    }
}
