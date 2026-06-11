package com.eferraz.entities.brokeragenotes

/**
 * Apportionable fees charged on a brokerage note (subject to proportional allocation).
 */
public data class ApportionableFees(
    public val settlement: Double,
    public val emoluments: Double,
    public val transfer: Double,
    public val brokerage: Double,
    public val iss: Double,
    public val others: Double,
) {
    public val total: Double get() =
        settlement + emoluments + transfer + brokerage + iss + others
}
