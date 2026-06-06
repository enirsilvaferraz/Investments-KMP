package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

public sealed interface TargetWeight {
    public data class Fixed(val percent: Double) : TargetWeight
    public data object Zero : TargetWeight
    public data object DynamicPension : TargetWeight
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
    RV_OTHER,
}

public enum class BalancingGroupId {
    PORTFOLIO_TOTAL,
    FIXED_INCOME,
    VARIABLE_INCOME,
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
)

public data class PortfolioBalancingReportLine(
    val groupId: BalancingGroupId,
    val groupName: String,
    val componentName: String,
    val actualValue: Double,
    val targetWeightDisplay: String,
    val targetWeightPercent: Double?,
    val idealValue: Double,
    val deviation: Double,
)

public data class PortfolioBalancingReport(
    val referenceDate: YearMonth,
    val totalPortfolioValue: Double,
    val lines: List<PortfolioBalancingReportLine>,
)
