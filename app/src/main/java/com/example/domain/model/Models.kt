package com.example.domain.model

data class Stop(
    val id: String,
    val name: String,
    val hindiName: String = "",
    val lat: Double,
    val lng: Double,
    val type: StopType = StopType.ROADSIDE,
    val isLandmark: Boolean = false,
    val approximate: Boolean = false,
    val locality: String = "",
    val provenance: Provenance
)

data class StopAlias(
    val id: String,
    val stopId: String,
    val alias: String
)

data class Route(
    val id: String,
    val operatorId: String,
    val shortName: String,
    val longName: String,
    val direction: Int = 0, // 0: Outbound, 1: Inbound
    val variantOf: String? = null,
    val frequencyMinutes: Int? = null,
    val firstDepartureSeconds: Int? = null,
    val lastDepartureSeconds: Int? = null,
    val provenance: Provenance
)

data class RouteStop(
    val routeId: String,
    val stopId: String,
    val sequence: Int,
    val boardingAllowed: Boolean = true,
    val alightingAllowed: Boolean = true,
    val distanceTraveledMeters: Double = 0.0,
    val provenance: Provenance
)

data class ServiceCalendar(
    val id: String,
    val monday: Boolean = true,
    val tuesday: Boolean = true,
    val wednesday: Boolean = true,
    val thursday: Boolean = true,
    val friday: Boolean = true,
    val saturday: Boolean = true,
    val sunday: Boolean = true,
    val startDate: String, // YYYY-MM-DD
    val endDate: String    // YYYY-MM-DD
)

data class CalendarException(
    val id: String,
    val calendarId: String,
    val date: String, // YYYY-MM-DD
    val exceptionType: Int // 1 = added, 2 = removed/cancelled
)

data class Trip(
    val id: String,
    val routeId: String,
    val calendarId: String,
    val tripHeadsign: String,
    val isCancelled: Boolean = false,
    val delayMinutes: Int = 0,
    val provenance: Provenance
)

data class StopTime(
    val tripId: String,
    val stopId: String,
    val sequence: Int,
    val arrivalSeconds: Int, // seconds from midnight of service day (can exceed 86400)
    val departureSeconds: Int,
    val boardingAllowed: Boolean = true,
    val alightingAllowed: Boolean = true
)

data class FareRule(
    val id: String,
    val routeId: String,
    val ruleType: FareRuleType,
    val flatAmount: Double? = null,
    val ratePerKm: Double? = null,
    val minFare: Double? = null,
    val roundUpUnit: Double? = null,
    val provenance: Provenance
)

data class FareStage(
    val id: String,
    val fareRuleId: String,
    val fromStopId: String,
    val toStopId: String,
    val amount: Double
)

data class Vehicle(
    val id: String,
    val operatorId: String,
    val registrationNumber: String,
    val label: String
)

data class VehiclePosition(
    val id: String,
    val tripId: String,
    val vehicleId: String,
    val lat: Double,
    val lng: Double,
    val timestampSeconds: Long, // Epoch seconds
    val speedKmh: Double = 0.0,
    val heading: Double = 0.0,
    val status: TrackingStatus = TrackingStatus.LIVE
)

data class ServiceAlert(
    val id: String,
    val routeId: String?,
    val tripId: String?,
    val stopId: String?,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val delayMinutes: Int = 0,
    val isCancelled: Boolean = false,
    val verified: Boolean = true,
    val effectiveDate: String,
    val provenance: Provenance
)

data class RouteSubmission(
    val id: String,
    val submissionType: SubmissionType,
    val contributorId: String,
    val contributorName: String,
    val operatorName: String?,
    val fromStopName: String,
    val toStopName: String,
    val stopsListCsv: String,
    val proposedFare: Double?,
    val proposedFrequencyMinutes: Int?,
    val operatingHours: String?,
    val observationDate: String,
    val evidenceText: String,
    val photoUri: String? = null,
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val duplicateFlag: Boolean = false,
    val duplicateOfId: String? = null,
    val moderatorReviewerId: String? = null,
    val moderatorNotes: String? = null,
    val secondReviewerId: String? = null,
    val idempotencyKey: String,
    val submittedAt: Long = System.currentTimeMillis()
)

data class FareReport(
    val id: String,
    val routeId: String,
    val fromStopId: String,
    val toStopId: String,
    val proposedFare: Double,
    val contributorId: String,
    val evidenceText: String,
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val submittedAt: Long = System.currentTimeMillis()
)

data class Verification(
    val id: String,
    val targetType: String, // ROUTE, STOP, FARE, SUBMISSION
    val targetId: String,
    val reviewerId: String,
    val reviewerRole: UserRole,
    val decision: String, // APPROVED, REJECTED, MERGED
    val notes: String,
    val verifiedAt: String // ISO-8601 or YYYY-MM-DD
)

data class Operator(
    val id: String,
    val name: String,
    val shortName: String,
    val type: String, // Govt, Local, Private
    val isVerified: Boolean = true,
    val contactPhone: String = "",
    val provenance: Provenance
)

data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val operatorId: String? = null,
    val assignedTripId: String? = null,
    val pin: String = "1234",
    val reputationPoints: Int = 10
)

data class Session(
    val user: User
)

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val userId: String,
    val userRole: UserRole,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val status: String // SUCCESS, BLOCKED_SECURITY_VIOLATION, FAILED
)

data class AnalyticsEvent(
    val id: String,
    val eventType: String,
    val timestamp: Long,
    val payloadJson: String = "{}"
)

data class AppSettings(
    val gpsFreshnessThresholdSeconds: Long = 90L,
    val staleDataThresholdDays: Long = 90L,
    val requiredApprovalsForModeration: Int = 1,
    val submissionRateLimitPerHour: Int = 5,
    val locationRetentionHours: Long = 24L,
    val isMapSimulatedUnavailable: Boolean = false,
    val isFeedSimulatedUnavailable: Boolean = false,
    val isLiveSimulatorRunning: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val selectedLanguage: String = "en" // en or hi
)

data class SavedJourney(
    val id: String,
    val originStopId: String,
    val destinationStopId: String,
    val departureTimeFormatted: String,
    val routeSummary: String,
    val fareFormatted: String,
    val notifyAlerts: Boolean = true,
    val savedAt: Long = System.currentTimeMillis()
)
