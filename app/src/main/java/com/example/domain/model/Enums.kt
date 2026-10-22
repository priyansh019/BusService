package com.example.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class VerificationStatus {
    OFFICIAL_VERIFIED,
    COMMUNITY_APPROVED,
    UNVERIFIED,
    APPROXIMATE
}

enum class TrackingStatus {
    LIVE,
    SCHEDULED,
    ESTIMATED,
    COMMUNITY_REPORTED,
    UNAVAILABLE
}

enum class FareRuleType {
    FIXED,
    STAGE,
    DISTANCE_BASED,
    OPERATOR_PROVIDED
}

enum class UserRole {
    PASSENGER,
    CONTRIBUTOR,
    OPERATOR,
    DRIVER,
    MODERATOR,
    ADMIN
}

enum class StopType {
    ROADSIDE,
    TERMINAL,
    LANDMARK
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

enum class SubmissionType {
    MISSING_ROUTE,
    NEW_OR_CORRECTED_STOP,
    FARE_CHANGE,
    FREQUENCY_OBSERVATION
}

enum class SubmissionStatus {
    SUBMITTED,
    UNDER_REVIEW,
    AWAITING_SECOND_APPROVAL,
    APPROVED,
    REJECTED,
    CLARIFICATION_REQUESTED
}

data class Provenance(
    val source: String,
    val effectiveDate: String, // YYYY-MM-DD
    val lastVerifiedAt: String, // YYYY-MM-DD or ISO-8601
    val verificationStatus: VerificationStatus,
    val reviewer: String? = null
) {
    fun isStale(currentDate: LocalDate, thresholdDays: Long = 90): Boolean {
        return try {
            val dateStr = if (lastVerifiedAt.length > 10) lastVerifiedAt.substring(0, 10) else lastVerifiedAt
            val verifiedDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            java.time.temporal.ChronoUnit.DAYS.between(verifiedDate, currentDate) > thresholdDays
        } catch (e: Exception) {
            false
        }
    }
}
