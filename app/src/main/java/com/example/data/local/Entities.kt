package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AlertSeverity
import com.example.domain.model.FareRuleType
import com.example.domain.model.StopType
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus

@Entity(
    tableName = "stops",
    indices = [
        Index(value = ["lat", "lng"]),
        Index(value = ["name"])
    ]
)
data class StopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hindiName: String,
    val lat: Double,
    val lng: Double,
    val type: StopType,
    val isLandmark: Boolean,
    val approximate: Boolean,
    val locality: String,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(
    tableName = "stop_aliases",
    indices = [Index(value = ["stopId"])]
)
data class StopAliasEntity(
    @PrimaryKey val id: String,
    val stopId: String,
    val alias: String
)

@Entity(
    tableName = "routes",
    indices = [Index(value = ["operatorId"])]
)
data class RouteEntity(
    @PrimaryKey val id: String,
    val operatorId: String,
    val shortName: String,
    val longName: String,
    val direction: Int,
    val variantOf: String?,
    val frequencyMinutes: Int?,
    val firstDepartureSeconds: Int?,
    val lastDepartureSeconds: Int?,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(
    tableName = "route_stops",
    primaryKeys = ["routeId", "stopId", "sequence"],
    indices = [
        Index(value = ["routeId", "stopId"]),
        Index(value = ["stopId"])
    ]
)
data class RouteStopEntity(
    val routeId: String,
    val stopId: String,
    val sequence: Int,
    val boardingAllowed: Boolean,
    val alightingAllowed: Boolean,
    val distanceTraveledMeters: Double,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["routeId"]),
        Index(value = ["calendarId"])
    ]
)
data class TripEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val calendarId: String,
    val tripHeadsign: String,
    val isCancelled: Boolean,
    val delayMinutes: Int,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(
    tableName = "stop_times",
    primaryKeys = ["tripId", "stopId", "sequence"],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["stopId"])
    ]
)
data class StopTimeEntity(
    val tripId: String,
    val stopId: String,
    val sequence: Int,
    val arrivalSeconds: Int,
    val departureSeconds: Int,
    val boardingAllowed: Boolean,
    val alightingAllowed: Boolean
)

@Entity(tableName = "service_calendars")
data class ServiceCalendarEntity(
    @PrimaryKey val id: String,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,
    val startDate: String,
    val endDate: String
)

@Entity(
    tableName = "calendar_exceptions",
    indices = [Index(value = ["calendarId"])]
)
data class CalendarExceptionEntity(
    @PrimaryKey val id: String,
    val calendarId: String,
    val date: String,
    val exceptionType: Int
)

@Entity(
    tableName = "fare_rules",
    indices = [Index(value = ["routeId"])]
)
data class FareRuleEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val ruleType: FareRuleType,
    val flatAmount: Double?,
    val ratePerKm: Double?,
    val minFare: Double?,
    val roundUpUnit: Double?,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(
    tableName = "fare_stages",
    indices = [Index(value = ["fareRuleId"])]
)
data class FareStageEntity(
    @PrimaryKey val id: String,
    val fareRuleId: String,
    val fromStopId: String,
    val toStopId: String,
    val amount: Double
)

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val type: String,
    val isVerified: Boolean,
    val contactPhone: String,
    val source: String,
    val effectiveDate: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: UserRole,
    val operatorId: String?,
    val assignedTripId: String?,
    val pin: String,
    val reputationPoints: Int
)

@Entity(
    tableName = "vehicle_positions",
    indices = [Index(value = ["tripId"])]
)
data class VehiclePositionEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val vehicleId: String,
    val lat: Double,
    val lng: Double,
    val timestampSeconds: Long,
    val speedKmh: Double,
    val heading: Double,
    val status: TrackingStatus
)

@Entity(
    tableName = "service_alerts",
    indices = [
        Index(value = ["routeId"]),
        Index(value = ["tripId"])
    ]
)
data class ServiceAlertEntity(
    @PrimaryKey val id: String,
    val routeId: String?,
    val tripId: String?,
    val stopId: String?,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val delayMinutes: Int,
    val isCancelled: Boolean,
    val verified: Boolean,
    val effectiveDate: String,
    val source: String,
    val effectiveDateProvenance: String,
    val lastVerifiedAt: String,
    val verificationStatus: VerificationStatus,
    val reviewer: String?
)

@Entity(tableName = "route_submissions")
data class RouteSubmissionEntity(
    @PrimaryKey val id: String,
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
    val photoUri: String?,
    val status: SubmissionStatus,
    val duplicateFlag: Boolean,
    val duplicateOfId: String?,
    val moderatorReviewerId: String?,
    val moderatorNotes: String?,
    val secondReviewerId: String?,
    val idempotencyKey: String,
    val submittedAt: Long
)

@Entity(tableName = "verifications")
data class VerificationEntity(
    @PrimaryKey val id: String,
    val targetType: String,
    val targetId: String,
    val reviewerId: String,
    val reviewerRole: UserRole,
    val decision: String,
    val notes: String,
    val verifiedAt: String
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: String,
    val userId: String,
    val userRole: UserRole,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val status: String
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val timestamp: Long,
    val payloadJson: String
)

@Entity(tableName = "saved_journeys")
data class SavedJourneyEntity(
    @PrimaryKey val id: String,
    val originStopId: String,
    val destinationStopId: String,
    val departureTimeFormatted: String,
    val routeSummary: String,
    val fareFormatted: String,
    val notifyAlerts: Boolean,
    val savedAt: Long
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val gpsFreshnessThresholdSeconds: Long,
    val staleDataThresholdDays: Long,
    val requiredApprovalsForModeration: Int,
    val submissionRateLimitPerHour: Int,
    val locationRetentionHours: Long,
    val isMapSimulatedUnavailable: Boolean,
    val isFeedSimulatedUnavailable: Boolean,
    val isLiveSimulatorRunning: Boolean,
    val hasCompletedOnboarding: Boolean,
    val selectedLanguage: String
)
