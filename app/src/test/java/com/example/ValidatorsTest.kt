package com.example

import com.example.domain.model.RouteSubmission
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.VehiclePosition
import com.example.domain.validation.PositionValidator
import com.example.domain.validation.SubmissionValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun test_position_validation_india_bounds() {
        val now = 1792656000L
        val active = setOf("TRIP_ACTIVE")

        // Valid Delhi / NCR coordinates
        val validPos = VehiclePosition("p1", "TRIP_ACTIVE", "v1", 28.6015, 77.3685, now - 10, 35.0, 90.0, TrackingStatus.LIVE)
        val (validResult, status) = PositionValidator.validatePosition(validPos, now, active)
        assertTrue(validResult)
        assertTrue(status == TrackingStatus.LIVE)

        // Invalid outside India bounding box (e.g. 0, 0 in Atlantic Ocean)
        val invalidPos = VehiclePosition("p2", "TRIP_ACTIVE", "v1", 0.0, 0.0, now - 10, 35.0, 90.0, TrackingStatus.LIVE)
        val (invalidResult, _) = PositionValidator.validatePosition(invalidPos, now, active)
        assertFalse(invalidResult)

        // Stale timestamp (10 min old > 90s freshness)
        val stalePos = VehiclePosition("p3", "TRIP_ACTIVE", "v1", 28.6015, 77.3685, now - 600, 35.0, 90.0, TrackingStatus.LIVE)
        val (staleAccepted, staleStatus) = PositionValidator.validatePosition(stalePos, now, active, 90L)
        assertTrue(staleAccepted)
        assertTrue("Stale position must be marked UNAVAILABLE in display", staleStatus == TrackingStatus.UNAVAILABLE)

        // Future timestamp (> 60s skew)
        val futurePos = VehiclePosition("p4", "TRIP_ACTIVE", "v1", 28.6015, 77.3685, now + 120, 35.0, 90.0, TrackingStatus.LIVE)
        val (futureResult, _) = PositionValidator.validatePosition(futurePos, now, active)
        assertFalse(futureResult)
    }

    @Test
    fun test_submission_validation_rules() {
        val sub1 = RouteSubmission(
            id = "s1",
            submissionType = SubmissionType.MISSING_ROUTE,
            contributorId = "c1",
            contributorName = "Rahul",
            operatorName = "Pvt",
            fromStopName = "Mamura",
            toStopName = "Dasna",
            stopsListCsv = "Mamura, Sector 62, Dasna",
            proposedFare = 25.0,
            proposedFrequencyMinutes = 40,
            operatingHours = "07:00-19:00",
            observationDate = "2026-10-22",
            evidenceText = "Saw white minibus",
            photoUri = null,
            status = SubmissionStatus.SUBMITTED,
            idempotencyKey = "key_001"
        )

        // Valid submission
        val res1 = SubmissionValidator.validateRouteSubmission(sub1, emptyList(), 1, 5)
        assertTrue(res1.isValid)

        // Rate limit exceeded (5/hour)
        val resRate = SubmissionValidator.validateRouteSubmission(sub1, emptyList(), 5, 5)
        assertFalse(resRate.isValid)

        // Impossible stop sequence (same stop consecutively)
        val subImpossible = sub1.copy(id = "s2", stopsListCsv = "Mamura, Mamura, Dasna", idempotencyKey = "key_002")
        val resImp = SubmissionValidator.validateRouteSubmission(subImpossible, emptyList(), 1, 5)
        assertFalse(resImp.isValid)

        // Duplicate detection
        val resDup = SubmissionValidator.validateRouteSubmission(sub1.copy(id = "s3", idempotencyKey = "key_003"), listOf(sub1), 1, 5)
        assertFalse(resDup.isValid)
    }
}
