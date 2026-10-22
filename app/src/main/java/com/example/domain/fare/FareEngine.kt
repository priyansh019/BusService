package com.example.domain.fare

import com.example.domain.model.FareRule
import com.example.domain.model.FareRuleType
import com.example.domain.model.FareStage
import kotlin.math.ceil

data class FareCalculationResult(
    val amount: Double?,
    val isComplete: Boolean,
    val currency: String = "INR",
    val description: String,
    val provenanceText: String
)

object FareEngine {
    fun calculateLegFare(
        fareRule: FareRule?,
        stages: List<FareStage>,
        fromStopId: String,
        toStopId: String,
        distanceMeters: Double
    ): FareCalculationResult {
        if (fareRule == null) {
            return FareCalculationResult(
                amount = null,
                isComplete = false,
                description = "Fare not available",
                provenanceText = "No fare rule configured"
            )
        }

        return when (fareRule.ruleType) {
            FareRuleType.FIXED -> {
                val amt = fareRule.flatAmount
                if (amt != null) {
                    FareCalculationResult(
                        amount = amt,
                        isComplete = true,
                        description = "Flat fare: ₹${amt.toInt()}",
                        provenanceText = fareRule.provenance.source
                    )
                } else {
                    FareCalculationResult(
                        amount = null,
                        isComplete = false,
                        description = "Fare not available",
                        provenanceText = "Incomplete fixed fare rule"
                    )
                }
            }

            FareRuleType.STAGE -> {
                val match = stages.firstOrNull {
                    (it.fromStopId == fromStopId && it.toStopId == toStopId) ||
                    (it.fromStopId == toStopId && it.toStopId == fromStopId) // Bi-directional symmetry for stage tables unless specified
                }
                if (match != null) {
                    FareCalculationResult(
                        amount = match.amount,
                        isComplete = true,
                        description = "Stage fare: ₹${match.amount.toInt()}",
                        provenanceText = fareRule.provenance.source
                    )
                } else {
                    FareCalculationResult(
                        amount = null,
                        isComplete = false,
                        description = "Fare not available",
                        provenanceText = "Missing stage fare in official table"
                    )
                }
            }

            FareRuleType.DISTANCE_BASED -> {
                val rate = fareRule.ratePerKm ?: 0.0
                val minFare = fareRule.minFare ?: 0.0
                val distanceKm = distanceMeters / 1000.0
                var calculated = distanceKm * rate
                if (calculated < minFare) {
                    calculated = minFare
                }
                val roundingUnit = fareRule.roundUpUnit ?: 1.0
                val finalFare = ceil(calculated / roundingUnit) * roundingUnit

                FareCalculationResult(
                    amount = finalFare,
                    isComplete = true,
                    description = "Distance-based (₹${rate}/km, min ₹${minFare.toInt()}): ₹${finalFare.toInt()}",
                    provenanceText = fareRule.provenance.source
                )
            }

            FareRuleType.OPERATOR_PROVIDED -> {
                val match = stages.firstOrNull {
                    (it.fromStopId == fromStopId && it.toStopId == toStopId) ||
                    (it.fromStopId == toStopId && it.toStopId == fromStopId)
                }
                if (match != null) {
                    FareCalculationResult(
                        amount = match.amount,
                        isComplete = true,
                        description = "Operator provided: ₹${match.amount.toInt()}",
                        provenanceText = fareRule.provenance.source
                    )
                } else {
                    // Critical honesty rule: never invent missing fare pairs
                    FareCalculationResult(
                        amount = null,
                        isComplete = false,
                        description = "Fare not available",
                        provenanceText = "Missing operator rate for segment"
                    )
                }
            }
        }
    }
}
