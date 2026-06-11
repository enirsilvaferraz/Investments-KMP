package com.eferraz.entities.brokeragenotes

/**
 * Withheld taxes on a brokerage note (informational; not included in fee allocation).
 */
public data class WithheldTaxes(
    public val irrfOperations: Double,
    public val irrfDayTrade: Double,
)
