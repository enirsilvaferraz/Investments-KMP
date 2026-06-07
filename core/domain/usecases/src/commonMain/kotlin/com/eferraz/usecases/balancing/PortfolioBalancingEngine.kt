package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

internal object PortfolioBalancingEngine {

    fun calculate(
        entries: List<HoldingHistoryEntry>,
        referenceDate: YearMonth,
        groups: List<BalancingGroup> = PortfolioBalancingCatalog.groups,
    ): PortfolioBalancingReport {
        val resolvedGroups = groups.withDefaultOtherInvestments()
        val activeEntries = entries.filter { patrimony(it) > 0.0 }
        val totalPortfolioValue = activeEntries.sumOf { patrimony(it) }

        val portfolioTotalGroup = resolvedGroups.first { it.id == BalancingGroupId.PORTFOLIO_TOTAL }
        val portfolioActuals = classifyAndSum(activeEntries, portfolioTotalGroup)
        val dynamicActuals = portfolioTotalGroup.components
            .filter { it.targetWeight is TargetWeight.Dynamic }
            .sumOf { portfolioActuals.getValue(it.id) }
        val hasDynamicWeight = dynamicActuals > 0.0
        val balanceableBase = totalPortfolioValue - dynamicActuals

        val portfolioContext = BalancingWeightCalculator.PortfolioTotalContext(
            totalPortfolioValue = totalPortfolioValue,
            balanceableBase = balanceableBase,
            hasDynamicWeight = hasDynamicWeight,
        )

        val idealByComponentId = mutableMapOf<String, Double>()
        val lines = resolvedGroups.flatMap { group ->
            buildGroupLines(
                group = group,
                activeEntries = activeEntries,
                portfolioActuals = portfolioActuals,
                totalPortfolioValue = totalPortfolioValue,
                portfolioContext = portfolioContext,
                idealByComponentId = idealByComponentId,
            )
        }

        val groupHoldings = resolvedGroups.map { group ->
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
            orderedGroupIds = resolvedGroups.map { it.id },
        )
    }

    internal fun patrimony(entry: HoldingHistoryEntry): Double =
        entry.endOfMonthValue * entry.endOfMonthQuantity

    internal fun universeForGroup(
        group: BalancingGroup,
        activeEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryEntry> = activeEntries.filter(group.universeFilter)

    internal fun shouldDisplayInReport(component: BalancingComponent, actualValue: Double): Boolean =
        when {
            component.id == BalancingGroupId.OTHER_INVESTMENTS -> actualValue > 0.0
            component.targetWeight is TargetWeight.Zero -> actualValue > 0.0
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
        portfolioActuals: Map<String, Double>,
        totalPortfolioValue: Double,
        portfolioContext: BalancingWeightCalculator.PortfolioTotalContext,
        idealByComponentId: MutableMap<String, Double>,
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

        val parentIdealBase = if (group.id == BalancingGroupId.PORTFOLIO_TOTAL) {
            null
        } else {
            idealByComponentId.getValue(group.id)
        }

        return group.components.mapNotNull { component ->
            val actual = actuals.getValue(component.id)
            val reportLine = toReportLine(
                group = group,
                component = component,
                actualValue = actual,
                groupTotal = groupTotal,
                portfolioContext = portfolioContext,
                parentIdealBase = parentIdealBase,
            )
            idealByComponentId[component.id] = reportLine.idealValue
            if (!shouldDisplayInReport(component, actual)) return@mapNotNull null
            reportLine
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
        parentIdealBase: Double?,
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
                    parentIdealBase = parentIdealBase!!,
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
