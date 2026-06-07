package com.eferraz.usecases.balancing

import kotlinx.datetime.YearMonth

public sealed interface TargetWeight {
    public data class Fixed(val percent: Double) : TargetWeight
    public data object Zero : TargetWeight
    public data object Dynamic : TargetWeight
}

public data class PortfolioBalancingHoldingLine(
    val displayName: String,
    val value: Double,
)

public data class PortfolioBalancingReportLine(
    val nodeId: String,
    val displayName: String,
    val actualValue: Double,
    val actualWeightDisplay: String,
    val actualWeightPercent: Double,
    val configuredWeightDisplay: String,
    val configuredWeightPercent: Double?,
    val idealValue: Double,
    /** `idealValue − actualValue`: positive = aportar; negative = acima da meta; zero = em meta. */
    val deviation: Double,
    /** Positions classified in this demais row; empty for non-fallback lines. */
    val holdings: List<PortfolioBalancingHoldingLine> = emptyList(),
)

public data class PortfolioBalancingReportSection(
    val nodeId: String,
    val nodeName: String,
    val rows: List<PortfolioBalancingReportLine>,
    val totalRow: PortfolioBalancingReportLine,
)

public data class PortfolioBalancingReport(
    val referenceDate: YearMonth,
    val totalPortfolioValue: Double,
    /** Pre-order DFS emission order (R6). */
    val sections: List<PortfolioBalancingReportSection>,
    val balanceableBase: Double,
)
