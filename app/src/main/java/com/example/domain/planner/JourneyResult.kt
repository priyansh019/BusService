package com.example.domain.planner

import com.example.domain.model.Provenance
import com.example.domain.model.TrackingStatus
import com.example.domain.model.VerificationStatus

enum class RankingPriority {
    RECOMMENDED,
    CHEAPEST,
    FASTEST,
    FEWEST_TRANSFERS,
    LEAST_WALKING
}

sealed class JourneyLeg {
    abstract val departureSeconds: Int
    abstract val arrivalSeconds: Int
    abstract val durationSeconds: Int

    data class WalkLeg(
        val fromStopId: String,
        val fromStopName: String,
        val toStopId: String,
        val toStopName: String,
        val distanceMeters: Double,
        override val durationSeconds: Int,
        override val departureSeconds: Int,
        override val arrivalSeconds: Int,
        val isTransfer: Boolean = false,
        val assumptionText: String = "Walk speed 4.5 km/h, straight-line × 1.3 detour factor"
    ) : JourneyLeg()

    data class TransitLeg(
        val tripId: String,
        val routeId: String,
        val routeShortName: String,
        val routeLongName: String,
        val operatorId: String,
        val operatorName: String,
        val boardStopId: String,
        val boardStopName: String,
        val alightStopId: String,
        val alightStopName: String,
        val intermediateStops: List<IntermediateStopInfo>,
        val scheduledDepartureSeconds: Int,
        val scheduledArrivalSeconds: Int,
        val estimatedDepartureSeconds: Int? = null,
        val estimatedArrivalSeconds: Int? = null,
        val trackingStatus: TrackingStatus = TrackingStatus.SCHEDULED,
        val fareAmount: Double? = null,
        val isFareComplete: Boolean = true,
        val fareDescription: String = "",
        val provenance: Provenance,
        val isStale: Boolean = false,
        val delayMinutes: Int = 0,
        val isCancelled: Boolean = false,
        override val departureSeconds: Int = estimatedDepartureSeconds ?: scheduledDepartureSeconds,
        override val arrivalSeconds: Int = estimatedArrivalSeconds ?: scheduledArrivalSeconds,
        override val durationSeconds: Int = (estimatedArrivalSeconds ?: scheduledArrivalSeconds) - (estimatedDepartureSeconds ?: scheduledDepartureSeconds)
    ) : JourneyLeg()
}

data class IntermediateStopInfo(
    val stopId: String,
    val stopName: String,
    val arrivalSeconds: Int,
    val departureSeconds: Int,
    val sequence: Int
)

data class TotalFare(
    val amount: Double?,
    val isComplete: Boolean,
    val currency: String = "INR",
    val displayText: String
)

data class Journey(
    val id: String,
    val departureSeconds: Int,
    val arrivalSeconds: Int,
    val totalDurationSeconds: Int,
    val walkTimeSeconds: Int,
    val waitTimeSeconds: Int,
    val rideTimeSeconds: Int,
    val legs: List<JourneyLeg>,
    val transfers: Int,
    val totalFare: TotalFare,
    val overallVerificationStatus: VerificationStatus,
    val isStale: Boolean,
    val overallTrackingStatus: TrackingStatus,
    val assumptions: List<String>,
    val uncertainty: List<String>,
    val hasAlert: Boolean = false,
    val alertDescription: String? = null
)
