package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public sealed interface TargetWeight {
    public data class Fixed(val percent: Double) : TargetWeight
    public data object Zero : TargetWeight
    public data object Dynamic : TargetWeight
}

public data class BalancingComponent(
    val id: String,
    val displayName: String,
    val targetWeight: TargetWeight,
    val matches: (HoldingHistoryEntry) -> Boolean,
    val parentId: String? = null,
)

public data class BalancingGroup(
    val id: String,
    val displayName: String,
    val components: List<BalancingComponent>,
    /** Subconjunto de posições elegíveis para classificação neste grupo. */
    val universeFilter: (HoldingHistoryEntry) -> Boolean = { true },
)

public data class PortfolioBalancingReportLine(
    val groupId: String,
    val groupName: String,
    val componentName: String,
    val actualValue: Double,
    val actualWeightDisplay: String,
    val actualWeightPercent: Double,
    val configuredWeightDisplay: String,
    val configuredWeightPercent: Double?,
    val normalizedWeightDisplay: String,
    val normalizedWeightPercent: Double,
    /** Total do grupo × peso configurado (ou normalizado quando há previdência); previdência = actual. */
    val idealValue: Double,
    /** `idealValue - actualValue`: negativo = retirar valor; positivo = aportar; zero = em meta. */
    val deviation: Double,
)

public data class PortfolioBalancingHoldingLine(
    val displayName: String,
    val value: Double,
)

public data class PortfolioBalancingGroupHoldings(
    val groupId: String,
    /** Posições classificadas em «Demais investimentos» do grupo. */
    val holdings: List<PortfolioBalancingHoldingLine>,
)

public data class PortfolioBalancingReport(
    val referenceDate: YearMonth,
    val totalPortfolioValue: Double,
    val lines: List<PortfolioBalancingReportLine>,
    val groupHoldings: List<PortfolioBalancingGroupHoldings>,
    val hasDynamicWeight: Boolean,
    /** Ordem de apresentação dos grupos no relatório formatado. */
    val orderedGroupIds: List<String>,
)
