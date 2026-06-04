package com.eferraz.entities.holdings

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Result of regressive income tax on holding profit (rate and amount in BRL).
 *
 * Tax applies only to positive profit; [taxValue] is zero when profit is not positive.
 * [taxRate] is the bracket rate in readable percent (e.g. 22.5 for 22.5%).
 *
 * @property taxRate Bracket rate (percent, not decimal fraction).
 * @property taxValue Tax amount in BRL (raw [Double], no monetary rounding).
 */
public data class IncomeTax private constructor(
    public val taxRate: Double,
    public val taxValue: Double,
) {

    public companion object {

        /**
         * Computes regressive IR from profit and holding period between purchase and reference dates.
         *
         * @param profit Profit in BRL on which tax is assessed.
         * @param purchaseDate Start of the investment period for the bracket table.
         * @param referenceDate End date (e.g. today or simulation date).
         * @throws IllegalArgumentException if [purchaseDate] is after [referenceDate].
         */
        public fun calculate(
            profit: Double,
            purchaseDate: LocalDate,
            referenceDate: LocalDate,
        ): IncomeTax {
            if (purchaseDate > referenceDate) {
                throw IllegalArgumentException(
                    "purchaseDate must not be after referenceDate: $purchaseDate > $referenceDate",
                )
            }

            val daysInvested = purchaseDate.daysUntil(referenceDate)
            val taxRate = taxRateForDays(daysInvested)
            val taxValue = if (profit > 0) profit * taxRate / 100.0 else 0.0

            return IncomeTax(taxRate = taxRate, taxValue = taxValue)
        }

        private fun taxRateForDays(daysInvested: Int): Double =
            when {
                daysInvested <= 180 -> 22.5
                daysInvested <= 360 -> 20.0
                daysInvested <= 720 -> 17.5
                else -> 15.0
            }
    }
}
