package com.example.domain.validation

import com.example.domain.model.RouteSubmission
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.TrackingStatus
import com.example.domain.model.VehiclePosition

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

object SubmissionValidator {
    fun validateRouteSubmission(
        submission: RouteSubmission,
        existingSubmissions: List<RouteSubmission>,
        recentUserSubmissionCountLastHour: Int,
        maxSubmissionsPerHour: Int = 5
    ): ValidationResult {
        val errors = mutableListOf<String>()

        // 1. Rate limiting
        if (recentUserSubmissionCountLastHour >= maxSubmissionsPerHour) {
            errors.add("Submission rate limit exceeded (maximum $maxSubmissionsPerHour per hour). Please try again later.")
        }

        // 2. Required fields
        if (submission.fromStopName.isBlank()) errors.add("Origin stop name is required.")
        if (submission.toStopName.isBlank()) errors.add("Destination stop name is required.")
        if (submission.evidenceText.isBlank()) errors.add("Observation details or evidence text is required.")

        // 3. Stop sequence sanity
        val stops = submission.stopsListCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (stops.size >= 2) {
            for (i in 0 until stops.size - 1) {
                if (stops[i].equals(stops[i + 1], ignoreCase = true)) {
                    errors.add("Impossible stop order: Stop '${stops[i]}' cannot appear consecutively.")
                }
            }
        }

        // 4. Idempotency & Duplicate detection
        val isDuplicate = existingSubmissions.any { existing ->
            existing.idempotencyKey == submission.idempotencyKey ||
            (existing.status != SubmissionStatus.REJECTED &&
             existing.fromStopName.trim().equals(submission.fromStopName.trim(), ignoreCase = true) &&
             existing.toStopName.trim().equals(submission.toStopName.trim(), ignoreCase = true) &&
             existing.stopsListCsv.trim().equals(submission.stopsListCsv.trim(), ignoreCase = true))
        }
        if (isDuplicate) {
            errors.add("Duplicate submission detected: A submission for this exact corridor already exists or key was reused.")
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }
}

object PositionValidator {
    // India Bounding Box: Lat 6.0 to 38.0, Lng 68.0 to 98.0
    private const val MIN_LAT = 6.0
    private const val MAX_LAT = 38.0
    private const val MIN_LNG = 68.0
    private const val MAX_LNG = 98.0

    fun validatePosition(
        position: VehiclePosition,
        currentTimeSeconds: Long,
        activeTripIds: Set<String>,
        freshnessThresholdSeconds: Long = 90L
    ): Pair<Boolean, TrackingStatus> {
        // 1. Coordinates inside India
        if (position.lat < MIN_LAT || position.lat > MAX_LAT || position.lng < MIN_LNG || position.lng > MAX_LNG) {
            return Pair(false, TrackingStatus.UNAVAILABLE)
        }

        // 2. Timestamp check: not > 60s in the future
        if (position.timestampSeconds > currentTimeSeconds + 60) {
            return Pair(false, TrackingStatus.UNAVAILABLE)
        }

        // 3. Trip authorization
        if (!activeTripIds.contains(position.tripId)) {
            return Pair(false, TrackingStatus.UNAVAILABLE)
        }

        // 4. Freshness check: if older than freshness threshold, hide from live and mark UNAVAILABLE
        val ageSeconds = currentTimeSeconds - position.timestampSeconds
        return if (ageSeconds <= freshnessThresholdSeconds) {
            Pair(true, TrackingStatus.LIVE)
        } else {
            Pair(true, TrackingStatus.UNAVAILABLE) // Accepted record, but stale in display
        }
    }
}
