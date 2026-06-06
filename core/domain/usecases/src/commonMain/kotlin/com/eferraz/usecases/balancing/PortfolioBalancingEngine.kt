package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

internal object PortfolioBalancingEngine {

    fun calculate(
        entries: List<HoldingHistoryEntry>,
        referenceDate: YearMonth,
        groups: List<BalancingGroup> = PortfolioBalancingCatalog.groups,
    ): PortfolioBalancingReport {
        val activeEntries = entries.filter { patrimony(it) > 0.0 }
        val totalPortfolioValue = activeEntries.sumOf { patrimony(it) }

        val portfolioTotalGroup = groups.first { it.id == BalancingGroupId.PORTFOLIO_TOTAL }
        val portfolioActuals = classifyAndSum(activeEntries, portfolioTotalGroup)
        val pensionActual = portfolioActuals.getValue(BalancingGroupId.PENSION_FUNDS)
        val hasDynamicWeight = pensionActual > 0.0
        val balanceableBase = totalPortfolioValue - pensionActual

        val portfolioContext = BalancingWeightCalculator.PortfolioTotalContext(
            totalPortfolioValue = totalPortfolioValue,
            balanceableBase = balanceableBase,
            hasDynamicWeight = hasDynamicWeight,
        )

        val lines = groups.flatMap { group ->
            buildGroupLines(
                group = group,
                activeEntries = activeEntries,
                portfolioActuals = portfolioActuals,
                totalPortfolioValue = totalPortfolioValue,
                portfolioContext = portfolioContext,
            )
        }

        val groupHoldings = groups.map { group ->
            PortfolioBalancingGroupHoldings(
                groupId = group.id,
                holdings = otherInvestmentsEntries(group, activeEntries)
                    .map { entry ->
                        PortfolioBalancingHoldingLine(
                            displayName = formatBalancingAssetDisplayName(entry.holding.asset),
                            value = patrimony(entry),
                        )
                    }
                    .sortedBy { it.displayName },
            )
        }

        return PortfolioBalancingReport(
            referenceDate = referenceDate,
            totalPortfolioValue = totalPortfolioValue,
            lines = lines,
            groupHoldings = groupHoldings,
            hasDynamicWeight = hasDynamicWeight,
            orderedGroupIds = groups.map { it.id },
        )
    }

    internal fun patrimony(entry: HoldingHistoryEntry): Double =
        entry.endOfMonthValue * entry.endOfMonthQuantity

    internal fun universeForGroup(
        group: BalancingGroup,
        activeEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryEntry> = activeEntries.filter(group.universeFilter)

    internal fun shouldDisplayInReport(component: BalancingComponent, actualValue: Double): Boolean =
        when (component.targetWeight) {
            TargetWeight.Zero -> actualValue > 0.0
            else -> true
        }

    internal fun classifyComponent(
        group: BalancingGroup,
        entry: HoldingHistoryEntry,
    ): BalancingComponent = group.components.first { it.matches(entry) }

    internal fun classifyAndSum(
        universe: List<HoldingHistoryEntry>,
        group: BalancingGroup,
    ): Map<String, Double> {
        val sums = group.components.associate { it.id to 0.0 }.toMutableMap()
        for (entry in universe) {
            val component = classifyComponent(group, entry)
            sums[component.id] = sums.getValue(component.id) + patrimony(entry)
        }
        return sums
    }

    private fun buildGroupLines(
        group: BalancingGroup,
        activeEntries: List<HoldingHistoryEntry>,
        portfolioActuals: Map<BalancingGroupId, Double>,
        totalPortfolioValue: Double,
        portfolioContext: BalancingWeightCalculator.PortfolioTotalContext,
    ): List<PortfolioBalancingReportLine> {
        val universe = universeForGroup(group, activeEntries)
        val actuals = if (group.id == BalancingGroupId.PORTFOLIO_TOTAL) {
            portfolioActuals
        } else {
            classifyAndSum(universe, group)
        }
        val groupTotal = if (group.id == BalancingGroupId.PORTFOLIO_TOTAL) {
            totalPortfolioValue
        } else {
            actuals.values.sum()
        }
        if (group.id != BalancingGroupId.PORTFOLIO_TOTAL && groupTotal == 0.0) {
            return emptyList()
        }

        return group.components.mapNotNull { component ->
            val actual = actuals.getValue(component.id)
            if (!shouldDisplayInReport(component, actual)) return@mapNotNull null
            toReportLine(
                group = group,
                component = component,
                actualValue = actual,
                groupTotal = groupTotal,
                portfolioContext = portfolioContext,
            )
        }
    }

    private fun otherInvestmentsEntries(
        group: BalancingGroup,
        activeEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryEntry> {
        val otherId = BalancingGroupId.OTHER_INVESTMENTS
        return universeForGroup(group, activeEntries)
            .filter { classifyComponent(group, it).id == otherId }
    }

    private fun toReportLine(
        group: BalancingGroup,
        component: BalancingComponent,
        actualValue: Double,
        groupTotal: Double,
        portfolioContext: BalancingWeightCalculator.PortfolioTotalContext,
    ): PortfolioBalancingReportLine {
        val configured = BalancingWeightCalculator.configuredWeight(component.targetWeight)
        val computed = if (group.id == BalancingGroupId.PORTFOLIO_TOTAL) {
            BalancingWeightCalculator.computePortfolioTotalWeights(
                targetWeight = component.targetWeight,
                actualValue = actualValue,
                configuredPercent = configured.percent,
                context = portfolioContext,
            )
        } else {
            BalancingWeightCalculator.computeNestedWeights(
                targetWeight = component.targetWeight,
                context = BalancingWeightCalculator.NestedContext(
                    groupTotal = groupTotal,
                    totalPortfolioValue = portfolioContext.totalPortfolioValue,
                ),
            )
        }
        val actualWeightPercent = BalancingWeightCalculator.actualWeightPercent(
            actualValue = actualValue,
            groupTotal = groupTotal,
        )
        return PortfolioBalancingReportLine(
            groupId = group.id,
            groupName = group.displayName,
            componentName = component.displayName,
            actualValue = actualValue,
            actualWeightDisplay = BalancingFormatters.formatPercent(actualWeightPercent),
            actualWeightPercent = actualWeightPercent,
            configuredWeightDisplay = configured.display,
            configuredWeightPercent = configured.percent,
            normalizedWeightDisplay = BalancingFormatters.formatPercent(computed.normalizedPercent),
            normalizedWeightPercent = computed.normalizedPercent,
            idealValue = computed.idealValue,
            deviation = computed.idealValue - actualValue,
        )
    }
}
