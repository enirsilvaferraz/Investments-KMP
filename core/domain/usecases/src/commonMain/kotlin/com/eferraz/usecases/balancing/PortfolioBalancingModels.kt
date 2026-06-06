package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public sealed interface TargetWeight {
    public data class Fixed(val percent: Double) : TargetWeight
    public data object Zero : TargetWeight
    public data object Dynamic : TargetWeight
}

public enum class BalancingComponentId {
    FIXED_INCOME_TOTAL,
    VARIABLE_INCOME_TOTAL,
    PENSION_FUNDS,
    CRYPTO,
    OTHER_INVESTMENTS,
    RF_POST_FIXED,
    RF_PRE_FIXED,
    RF_INFLATION_LINKED,
    RV_NATIONAL_STOCKS,
    RV_INTERNATIONAL,
    RV_REITS,
    FII_REND,
    FII_TAT,
    FII_FOF,
}

public enum class BalancingGroupId {
    PORTFOLIO_TOTAL,
    FIXED_INCOME,
    VARIABLE_INCOME,
    RV_REITS,
}

public data class BalancingComponent(
    val id: BalancingComponentId,
    val displayName: String,
    val targetWeight: TargetWeight,
    val matches: (HoldingHistoryEntry) -> Boolean,
    val parentId: BalancingComponentId? = null,
)

public data class BalancingGroup(
    val id: BalancingGroupId,
    val displayName: String,
    val components: List<BalancingComponent>,
    /** Subconjunto de posições elegíveis para classificação neste grupo. */
    val universeFilter: (HoldingHistoryEntry) -> Boolean = { true },
)

public data class PortfolioBalancingReportLine(
    val groupId: BalancingGroupId,
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
    val groupId: BalancingGroupId,
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
    val orderedGroupIds: List<BalancingGroupId>,
)
