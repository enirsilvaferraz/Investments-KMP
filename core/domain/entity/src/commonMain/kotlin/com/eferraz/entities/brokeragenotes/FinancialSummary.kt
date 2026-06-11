package com.eferraz.entities.brokeragenotes

/**
 * Financial summary declared by the brokerage on a SINACOR note.
 */
public data class FinancialSummary(
    public val totalVolumeTraded: Double,
    public val totalBuys: Double,
    public val totalSells: Double,
    public val apportionableFees: ApportionableFees,
    public val withheldTaxes: WithheldTaxes,
)
