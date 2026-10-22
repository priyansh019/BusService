package com.example.data.repository

import com.example.data.local.*
import com.example.data.seed.SeedDataLoader
import com.example.domain.fare.FareEngine
import com.example.domain.policy.Action
import com.example.domain.model.AppSettings
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.CalendarException
import com.example.domain.model.FareRule
import com.example.domain.model.FareRuleType
import com.example.domain.model.FareStage
import com.example.domain.model.Operator
import com.example.domain.model.Provenance
import com.example.domain.model.Route
import com.example.domain.model.RouteStop
import com.example.domain.model.RouteSubmission
import com.example.domain.model.SavedJourney
import com.example.domain.model.ServiceAlert
import com.example.domain.model.ServiceCalendar
import com.example.domain.model.Session
import com.example.domain.model.Stop
import com.example.domain.model.StopTime
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.Trip
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.model.VehiclePosition
import com.example.domain.model.Verification
import com.example.domain.model.VerificationStatus
import com.example.domain.planner.Journey
import com.example.domain.planner.JourneyPlanner
import com.example.domain.planner.PlannerContext
import com.example.domain.planner.PlannerPreferences
import com.example.domain.policy.PermissionPolicy
import com.example.domain.policy.PermissionResult
import com.example.domain.time.DemoClock
import com.example.domain.validation.PositionValidator
import com.example.domain.validation.SubmissionValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import java.util.UUID

class BusDiscoveryRepositoryImpl(
    private val dao: BusDao,
    private val clock: DemoClock
) : com.example.domain.repository.BusDiscoveryRepository {

    override fun getStopsFlow(): Flow<List<Stop>> {
        return dao.getAllStopsFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getStops(): List<Stop> = dao.getAllStops().map { it.toDomain() }

    override suspend fun getStopById(id: String): Stop? = dao.getStopById(id)?.toDomain()

    override fun getRoutesFlow(): Flow<List<Route>> {
        return dao.getAllRoutesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getRoutes(): List<Route> = dao.getAllRoutes().map { it.toDomain() }

    override suspend fun getRouteById(id: String): Route? = dao.getRouteById(id)?.toDomain()

    override fun getSubmissionsFlow(): Flow<List<RouteSubmission>> {
        return dao.getAllSubmissionsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getAlertsFlow(): Flow<List<ServiceAlert>> {
        return dao.getAllAlertsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getLivePositionsFlow(): Flow<List<VehiclePosition>> {
        return dao.getAllVehiclePositionsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getSavedJourneysFlow(): Flow<List<SavedJourney>> {
        return dao.getAllSavedJourneysFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getAuditLogsFlow(): Flow<List<AuditLogEntry>> {
        return dao.getAllAuditLogsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getAppSettingsFlow(): Flow<AppSettings?> {
        return dao.getAppSettingsFlow().map { it?.toDomain() }
    }

    override suspend fun getUsers(): List<User> = dao.getAllUsers().map { it.toDomain() }

    override suspend fun getUserById(id: String): User? = dao.getUserById(id)?.toDomain()

    override suspend fun getOperators(): List<Operator> = dao.getAllOperators().map { it.toDomain() }

    override suspend fun planJourney(
        origin: String,
        destination: String,
        departureSeconds: Int,
        preferences: PlannerPreferences
    ): List<Journey> {
        val stops = dao.getAllStops().map { it.toDomain() }
        val routes = dao.getAllRoutes().map { it.toDomain() }
        val routeStops = dao.getAllRouteStops().map { it.toDomain() }
        val trips = dao.getAllTrips().map { it.toDomain() }
        val stopTimes = dao.getAllStopTimes().map { it.toDomain() }
        val calendars = dao.getAllCalendars().map { it.toDomain() }
        val exceptions = dao.getAllCalendarExceptions().map { it.toDomain() }
        val fareRules = dao.getAllFareRules().map { it.toDomain() }
        val fareStages = dao.getAllFareStages().map { it.toDomain() }
        val operators = dao.getAllOperators().map { it.toDomain() }
        val alerts = dao.getAllAlerts().map { it.toDomain() }
        val settings = dao.getAppSettings()

        val plannerContext = PlannerContext(
            stops = stops,
            routes = routes,
            routeStops = routeStops,
            trips = trips,
            stopTimes = stopTimes,
            calendars = calendars,
            calendarExceptions = exceptions,
            fareRules = fareRules,
            fareStages = fareStages,
            operators = operators,
            alerts = alerts,
            currentDate = clock.today(),
            staleThresholdDays = settings?.staleDataThresholdDays ?: 90L
        )

        val planner = JourneyPlanner(plannerContext)
        return planner.plan(origin, destination, departureSeconds, preferences)
    }

    override suspend fun submitContribution(
        submission: RouteSubmission,
        session: Session
    ): Pair<Boolean, String> {
        val permCheck = PermissionPolicy.check(session, Action.SUBMIT_CONTRIBUTION)
        if (permCheck is PermissionResult.Denied) {
            recordAudit(session, "SUBMIT_CONTRIBUTION", "SUBMISSION", submission.id, permCheck.reason, "BLOCKED_SECURITY_VIOLATION")
            return Pair(false, permCheck.reason)
        }

        val existing = dao.getAllSubmissions().map { it.toDomain() }
        val userRecentCount = existing.count {
            it.contributorId == session.user.id &&
            System.currentTimeMillis() - it.submittedAt < 3600000L
        }

        val validation = SubmissionValidator.validateRouteSubmission(
            submission = submission,
            existingSubmissions = existing,
            recentUserSubmissionCountLastHour = userRecentCount
        )

        if (!validation.isValid) {
            val errorMsg = validation.errors.joinToString("; ")
            recordAudit(session, "SUBMIT_CONTRIBUTION", "SUBMISSION", submission.id, errorMsg, "FAILED")
            return Pair(false, errorMsg)
        }

        dao.insertSubmission(submission.toEntity())
        recordAudit(session, "SUBMIT_CONTRIBUTION", "SUBMISSION", submission.id, "Submission received: ${submission.fromStopName} -> ${submission.toStopName}", "SUCCESS")
        return Pair(true, "Submission successfully received for moderation.")
    }

    override suspend fun reviewSubmission(
        submissionId: String,
        decision: String,
        notes: String,
        session: Session
    ): Pair<Boolean, String> {
        val permCheck = PermissionPolicy.check(session, Action.REVIEW_SUBMISSIONS)
        if (permCheck is PermissionResult.Denied) {
            recordAudit(session, "REVIEW_SUBMISSION", "SUBMISSION", submissionId, permCheck.reason, "BLOCKED_SECURITY_VIOLATION")
            return Pair(false, permCheck.reason)
        }

        val submissionEntity = dao.getSubmissionById(submissionId) ?: return Pair(false, "Submission not found")
        val settings = dao.getAppSettings()
        val requiredApprovals = settings?.requiredApprovalsForModeration ?: 1

        val newStatus: SubmissionStatus
        var secondReviewer: String? = submissionEntity.secondReviewerId
        var firstReviewer: String? = submissionEntity.moderatorReviewerId

        when (decision) {
            "APPROVED" -> {
                if (requiredApprovals == 2) {
                    if (firstReviewer == null) {
                        firstReviewer = session.user.name
                        newStatus = SubmissionStatus.AWAITING_SECOND_APPROVAL
                    } else if (firstReviewer == session.user.name) {
                        return Pair(false, "Second approval must be completed by a different moderator.")
                    } else {
                        secondReviewer = session.user.name
                        newStatus = SubmissionStatus.APPROVED
                    }
                } else {
                    firstReviewer = session.user.name
                    newStatus = SubmissionStatus.APPROVED
                }
            }
            "REJECTED" -> newStatus = SubmissionStatus.REJECTED
            "MERGED" -> newStatus = SubmissionStatus.APPROVED
            else -> newStatus = SubmissionStatus.CLARIFICATION_REQUESTED
        }

        val updated = submissionEntity.copy(
            status = newStatus,
            moderatorReviewerId = firstReviewer,
            secondReviewerId = secondReviewer,
            moderatorNotes = notes
        )
        dao.updateSubmission(updated)

        // Record immutable verification entry
        val verification = VerificationEntity(
            id = "ver_${UUID.randomUUID().toString().take(8)}",
            targetType = "SUBMISSION",
            targetId = submissionId,
            reviewerId = session.user.id,
            reviewerRole = session.user.role,
            decision = decision,
            notes = notes,
            verifiedAt = clock.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        dao.insertVerification(verification)

        recordAudit(session, "REVIEW_SUBMISSION", "SUBMISSION", submissionId, "Decision: $decision by ${session.user.name}", "SUCCESS")

        // If approved and is D1: publish to official tables with COMMUNITY_APPROVED badge!
        if (newStatus == SubmissionStatus.APPROVED && submissionEntity.id == "sub_d1") {
            publishD1Route(session.user.name)
        }

        return Pair(true, "Submission successfully processed: $decision")
    }

    private suspend fun publishD1Route(reviewerName: String) {
        val todayStr = clock.today().toString()
        val d1Route = RouteEntity(
            id = "D1",
            operatorId = "op_pvt",
            shortName = "D1",
            longName = "Mamura to Dasna Direct Mini-Bus",
            direction = 0,
            variantOf = null,
            frequencyMinutes = 40,
            firstDepartureSeconds = 25200, // 07:00
            lastDepartureSeconds = 68400,  // 19:00
            source = "Community Submission approved by $reviewerName",
            effectiveDate = todayStr,
            lastVerifiedAt = todayStr,
            verificationStatus = VerificationStatus.COMMUNITY_APPROVED,
            reviewer = reviewerName
        )
        dao.insertRoute(d1Route)

        // Route stops: mamura (1) -> dasna (2)
        val d1Stops = listOf(
            RouteStopEntity("D1", "mamura", 1, true, false, 0.0, "Community Approved", todayStr, todayStr, VerificationStatus.COMMUNITY_APPROVED, reviewerName),
            RouteStopEntity("D1", "dasna", 2, false, true, 26000.0, "Community Approved", todayStr, todayStr, VerificationStatus.COMMUNITY_APPROVED, reviewerName)
        )
        dao.insertRouteStops(d1Stops)

        // Fare rule: FIXED ₹25
        val d1Fare = FareRuleEntity(
            id = "fr_d1",
            routeId = "D1",
            ruleType = FareRuleType.FIXED,
            flatAmount = 25.0,
            ratePerKm = null,
            minFare = null,
            roundUpUnit = null,
            source = "Community Approved Rate",
            effectiveDate = todayStr,
            lastVerifiedAt = todayStr,
            verificationStatus = VerificationStatus.COMMUNITY_APPROVED,
            reviewer = reviewerName
        )
        dao.insertFareRule(d1Fare)

        // Generate trips: every 40 min from 07:00 (25200) to 19:00 (68400)
        // Travel time Mamura to Dasna direct via NH9 is ~35 mins (2100 s)
        var dep = 25200
        var idx = 1
        val trips = mutableListOf<TripEntity>()
        val stopTimes = mutableListOf<StopTimeEntity>()
        while (dep <= 68400) {
            val tripId = "D1_T$idx"
            trips.add(TripEntity(tripId, "D1", "CAL_DAILY", "Dasna Direct", false, 0, "Community Approved", todayStr, todayStr, VerificationStatus.COMMUNITY_APPROVED, reviewerName))
            stopTimes.add(StopTimeEntity(tripId, "mamura", 1, dep, dep, true, false))
            stopTimes.add(StopTimeEntity(tripId, "dasna", 2, dep + 2100, dep + 2100, false, true))
            dep += 2400 // 40 mins
            idx++
        }
        dao.insertTrips(trips)
        dao.insertStopTimes(stopTimes)
    }

    override suspend fun updateOperatorRoute(
        route: Route,
        session: Session
    ): Pair<Boolean, String> {
        val permCheck = PermissionPolicy.check(session, Action.MANAGE_OWN_OPERATOR_DATA, targetOperatorId = route.operatorId)
        if (permCheck is PermissionResult.Denied) {
            recordAudit(session, "UPDATE_ROUTE", "ROUTE", route.id, permCheck.reason, "BLOCKED_SECURITY_VIOLATION")
            return Pair(false, permCheck.reason)
        }

        dao.insertRoute(route.toEntity())
        recordAudit(session, "UPDATE_ROUTE", "ROUTE", route.id, "Route ${route.shortName} updated by operator ${session.user.name}", "SUCCESS")
        return Pair(true, "Route updated successfully")
    }

    override suspend fun recordDriverPosition(
        tripId: String,
        lat: Double,
        lng: Double,
        speedKmh: Double,
        heading: Double,
        session: Session
    ): Pair<Boolean, String> {
        val permCheck = PermissionPolicy.check(session, Action.START_DRIVER_TRACKING, targetTripId = tripId)
        if (permCheck is PermissionResult.Denied) {
            recordAudit(session, "DRIVER_POSITION", "TRIP", tripId, permCheck.reason, "BLOCKED_SECURITY_VIOLATION")
            return Pair(false, permCheck.reason)
        }

        val nowSec = clock.now().toEpochSecond()
        val pos = VehiclePosition(
            id = "pos_${tripId}",
            tripId = tripId,
            vehicleId = session.user.id,
            lat = lat,
            lng = lng,
            timestampSeconds = nowSec,
            speedKmh = speedKmh,
            heading = heading,
            status = TrackingStatus.LIVE
        )

        val activeTrips = dao.getAllTrips().map { it.id }.toSet()
        val (isValid, status) = PositionValidator.validatePosition(pos, nowSec, activeTrips)
        if (!isValid) {
            return Pair(false, "Position rejected by telemetry validation pipeline.")
        }

        dao.insertVehiclePosition(pos.copy(status = status).toEntity())
        return Pair(true, "Position broadcast accepted.")
    }

    override suspend fun deleteDriverLocationData(tripId: String, session: Session): Boolean {
        dao.deletePositionsForTrip(tripId)
        recordAudit(session, "DELETE_LOCATION_DATA", "TRIP", tripId, "Driver requested immediate purge of location history.", "SUCCESS")
        return true
    }

    override suspend fun saveJourney(journey: SavedJourney) {
        dao.insertSavedJourney(journey.toEntity())
    }

    override suspend fun removeSavedJourney(id: String) {
        dao.deleteSavedJourney(id)
    }

    override suspend fun updateAppSettings(settings: AppSettings) {
        dao.saveAppSettings(settings.toEntity())
    }

    override suspend fun resetDemoData() {
        SeedDataLoader.resetAndLoadSeedData(dao)
    }

    private suspend fun recordAudit(
        session: Session,
        action: String,
        entityType: String,
        entityId: String,
        details: String,
        status: String
    ) {
        val entry = AuditLogEntity(
            id = "aud_${UUID.randomUUID().toString().take(8)}",
            timestamp = clock.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            userId = session.user.id,
            userRole = session.user.role,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details,
            status = status
        )
        dao.insertAuditLog(entry)
    }
}

// Extension converters
fun StopEntity.toDomain() = Stop(id, name, hindiName, lat, lng, type, isLandmark, approximate, locality, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun Stop.toEntity() = StopEntity(id, name, hindiName, lat, lng, type, isLandmark, approximate, locality, provenance.source, provenance.effectiveDate, provenance.lastVerifiedAt, provenance.verificationStatus, provenance.reviewer)

fun RouteEntity.toDomain() = Route(id, operatorId, shortName, longName, direction, variantOf, frequencyMinutes, firstDepartureSeconds, lastDepartureSeconds, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun Route.toEntity() = RouteEntity(id, operatorId, shortName, longName, direction, variantOf, frequencyMinutes, firstDepartureSeconds, lastDepartureSeconds, provenance.source, provenance.effectiveDate, provenance.lastVerifiedAt, provenance.verificationStatus, provenance.reviewer)

fun RouteStopEntity.toDomain() = RouteStop(routeId, stopId, sequence, boardingAllowed, alightingAllowed, distanceTraveledMeters, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun TripEntity.toDomain() = Trip(id, routeId, calendarId, tripHeadsign, isCancelled, delayMinutes, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun StopTimeEntity.toDomain() = StopTime(tripId, stopId, sequence, arrivalSeconds, departureSeconds, boardingAllowed, alightingAllowed)
fun ServiceCalendarEntity.toDomain() = ServiceCalendar(id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, startDate, endDate)
fun CalendarExceptionEntity.toDomain() = CalendarException(id, calendarId, date, exceptionType)
fun FareRuleEntity.toDomain() = FareRule(id, routeId, ruleType, flatAmount, ratePerKm, minFare, roundUpUnit, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun FareStageEntity.toDomain() = FareStage(id, fareRuleId, fromStopId, toStopId, amount)
fun OperatorEntity.toDomain() = Operator(id, name, shortName, type, isVerified, contactPhone, Provenance(source, effectiveDate, lastVerifiedAt, verificationStatus, reviewer))
fun UserEntity.toDomain() = User(id, name, role, operatorId, assignedTripId, pin, reputationPoints)
fun VehiclePositionEntity.toDomain() = VehiclePosition(id, tripId, vehicleId, lat, lng, timestampSeconds, speedKmh, heading, status)
fun VehiclePosition.toEntity() = VehiclePositionEntity(id, tripId, vehicleId, lat, lng, timestampSeconds, speedKmh, heading, status)
fun ServiceAlertEntity.toDomain() = ServiceAlert(id, routeId, tripId, stopId, title, description, severity, delayMinutes, isCancelled, verified, effectiveDate, Provenance(source, effectiveDateProvenance, lastVerifiedAt, verificationStatus, reviewer))
fun RouteSubmissionEntity.toDomain() = RouteSubmission(id, submissionType, contributorId, contributorName, operatorName, fromStopName, toStopName, stopsListCsv, proposedFare, proposedFrequencyMinutes, operatingHours, observationDate, evidenceText, photoUri, status, duplicateFlag, duplicateOfId, moderatorReviewerId, moderatorNotes, secondReviewerId, idempotencyKey, submittedAt)
fun RouteSubmission.toEntity() = RouteSubmissionEntity(id, submissionType, contributorId, contributorName, operatorName, fromStopName, toStopName, stopsListCsv, proposedFare, proposedFrequencyMinutes, operatingHours, observationDate, evidenceText, photoUri, status, duplicateFlag, duplicateOfId, moderatorReviewerId, moderatorNotes, secondReviewerId, idempotencyKey, submittedAt)
fun VerificationEntity.toDomain() = Verification(id, targetType, targetId, reviewerId, reviewerRole, decision, notes, verifiedAt)
fun AuditLogEntity.toDomain() = AuditLogEntry(id, timestamp, userId, userRole, action, entityType, entityId, details, status)
fun AppSettingsEntity.toDomain() = AppSettings(gpsFreshnessThresholdSeconds, staleDataThresholdDays, requiredApprovalsForModeration, submissionRateLimitPerHour, locationRetentionHours, isMapSimulatedUnavailable, isFeedSimulatedUnavailable, isLiveSimulatorRunning, hasCompletedOnboarding, selectedLanguage)
fun AppSettings.toEntity() = AppSettingsEntity(1, gpsFreshnessThresholdSeconds, staleDataThresholdDays, requiredApprovalsForModeration, submissionRateLimitPerHour, locationRetentionHours, isMapSimulatedUnavailable, isFeedSimulatedUnavailable, isLiveSimulatorRunning, hasCompletedOnboarding, selectedLanguage)
fun SavedJourneyEntity.toDomain() = SavedJourney(id, originStopId, destinationStopId, departureTimeFormatted, routeSummary, fareFormatted, notifyAlerts, savedAt)
fun SavedJourney.toEntity() = SavedJourneyEntity(id, originStopId, destinationStopId, departureTimeFormatted, routeSummary, fareFormatted, notifyAlerts, savedAt)
