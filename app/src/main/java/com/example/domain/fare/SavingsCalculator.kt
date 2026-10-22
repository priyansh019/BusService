package com.example.domain.fare

data class SavingsResult(
    val oneWayBusFare: Double,
    val roundTripBusFare: Double,
    val oneWayAlternative: Double,
    val roundTripAlternative: Double,
    val dailySavings: Double,
    val monthlySavings: Double,
    val workingDays: Int,
    val assumptionText: String
)

object SavingsCalculator {
    const val DEFAULT_WORKING_DAYS = 22
    const val ASSUMPTION_TEXT = "Assumes 2 trips per working day across user-specified working days per month. Excludes surge pricing, parking and toll charges."

    fun calculate(
        busFare: Double?,
        alternativeCost: Double?,
        workingDays: Int = DEFAULT_WORKING_DAYS
    ): SavingsResult? {
        // Rule: refuses to compute when fare is unknown or alternative cost is blank/null
        if (busFare == null || alternativeCost == null || alternativeCost <= 0) {
            return null
        }

        val oneWayBus = busFare
        val roundTripBus = busFare * 2.0
        val oneWayAlt = alternativeCost
        val roundTripAlt = alternativeCost * 2.0
        val daily = (alternativeCost - busFare) * 2.0
        val monthly = daily * workingDays

        return SavingsResult(
            oneWayBusFare = oneWayBus,
            roundTripBusFare = roundTripBus,
            oneWayAlternative = oneWayAlt,
            roundTripAlternative = roundTripAlt,
            dailySavings = daily,
            monthlySavings = monthly,
            workingDays = workingDays,
            assumptionText = ASSUMPTION_TEXT
        )
    }
}
