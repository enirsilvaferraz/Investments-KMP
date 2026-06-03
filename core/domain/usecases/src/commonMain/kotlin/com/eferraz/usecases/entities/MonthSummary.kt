package com.eferraz.usecases.entities

/**
 * Consolidated wallet metrics for a single reference month.
 */
public data class MonthSummary(
    public val previousValue: Double = 0.0,
    public val actualValue: Double = 0.0,
    public val contributions: Double = 0.0,
    public val withdrawals: Double = 0.0,
    public val growth: Double = 0.0,
    public val growthPercent: Double = 0.0,
    public val earnings: Double = 0.0,
    public val earningsPercent: Double = 0.0,
)
