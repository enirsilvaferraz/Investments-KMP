package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

internal object PortfolioBalancingEngine {

    fun calculate(
        entries: List<HoldingHistoryEntry>,
        referenceDate: YearMonth,
    ): PortfolioBalancingReport {
        val activeEntries = entries.filter { patrimony(it) > 0.0 }
        val totalPortfolioValue = activeEntries.sumOf { patrimony(it) }

        val lines = mutableListOf<PortfolioBalancingReportLine>()

        val group1Actuals = classifyAndSum(activeEntries, PortfolioBalancingCatalog.portfolioTotalGroup)
        val pensionActual = group1Actuals.getValue(BalancingComponentId.PENSION_FUNDS)
        val hasDynamicWeight = pensionActual > 0.0
        val balanceableBase = totalPortfolioValue - pensionActual

        PortfolioBalancingCatalog.groups.forEach { group ->
            when (group.id) {
                BalancingGroupId.PORTFOLIO_TOTAL -> group.components.forEach { component ->
                    val actual = group1Actuals.getValue(component.id)
                    if (!shouldDisplayInReport(component, actual)) return@forEach
                    lines += toGroup1ReportLine(
                        component = component,
                        actualValue = actual,
                        totalPortfolioValue = totalPortfolioValue,
                        balanceableBase = balanceableBase,
                        hasDynamicWeight = hasDynamicWeight,
                    )
                }

                else -> lines += nestedGroupLines(
                    group = group,
                    universe = universeForGroup(group.id, activeEntries),
                    totalPortfolioValue = totalPortfolioValue,
                )
            }
        }

        val groupHoldings = PortfolioBalancingCatalog.groups.map { group ->
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
            hasDynamicWeight = pensionActual > 0.0,
        )
    }

    internal fun patrimony(entry: HoldingHistoryEntry): Double =
        entry.endOfMonthValue * entry.endOfMonthQuantity

    internal fun universeForGroup(
        groupId: BalancingGroupId,
        activeEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryEntry> = when (groupId) {

        BalancingGroupId.PORTFOLIO_TOTAL -> activeEntries

        BalancingGroupId.FIXED_INCOME -> activeEntries.filter { it.holding.asset is FixedIncomeAsset }

        BalancingGroupId.VARIABLE_INCOME -> activeEntries.filter { entry ->
            val asset = entry.holding.asset
            asset is VariableIncomeAsset && asset.ticker.uppercase() != PortfolioBalancingCatalog.HASH11
        }

        BalancingGroupId.RV_REITS -> activeEntries.filter { entry ->
            val asset = entry.holding.asset
            asset is VariableIncomeAsset && asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND
        }
    }

    internal fun shouldDisplayInReport(component: BalancingComponent, actualValue: Double): Boolean =
        when (component.targetWeight) {
            TargetWeight.Zero -> actualValue > 0.0
            else -> true
        }

    internal fun classifyComponent(
        group: BalancingGroup,
        entry: HoldingHistoryEntry,
    ): BalancingComponent = group.components.first { it.matches(entry) }

    private fun otherInvestmentsEntries(
        group: BalancingGroup,
        activeEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryEntry> {
        val otherId = BalancingComponentId.OTHER_INVESTMENTS
        return universeForGroup(group.id, activeEntries)
            .filter { classifyComponent(group, it).id == otherId }
    }

    internal fun classifyAndSum(
        universe: List<HoldingHistoryEntry>,
        group: BalancingGroup,
    ): Map<BalancingComponentId, Double> {
        val sums = group.components.associate { it.id to 0.0 }.toMutableMap()
        for (entry in universe) {
            val component = classifyComponent(group, entry)
            sums[component.id] = sums.getValue(component.id) + patrimony(entry)
        }
        return sums
    }

    private fun toGroup1ReportLine(
        component: BalancingComponent,
        actualValue: Double,
        totalPortfolioValue: Double,
        balanceableBase: Double,
        hasDynamicWeight: Boolean,
    ): PortfolioBalancingReportLine {
        val (configuredDisplay, configuredPercent) = configuredWeight(component.targetWeight)
        val normalizedPercent = when {
            totalPortfolioValue == 0.0 -> 0.0
            component.targetWeight is TargetWeight.Dynamic ->
                actualValue / totalPortfolioValue * 100.0

            component.targetWeight is TargetWeight.Fixed ->
                balanceableBase * configuredPercent!! / totalPortfolioValue

            else -> 0.0
        }
        val idealValue = when {
            totalPortfolioValue == 0.0 -> 0.0
            component.targetWeight is TargetWeight.Dynamic -> actualValue
            component.targetWeight is TargetWeight.Zero -> 0.0
            hasDynamicWeight -> totalPortfolioValue * normalizedPercent / 100.0
            else -> totalPortfolioValue * configuredPercent!! / 100.0
        }
        return toReportLine(
            group = PortfolioBalancingCatalog.portfolioTotalGroup,
            component = component,
            actualValue = actualValue,
            idealValue = idealValue,
            actualWeightPercent = actualWeightPercent(
                actualValue = actualValue,
                groupTotal = totalPortfolioValue,
            ),
            configuredDisplay = configuredDisplay,
            configuredPercent = configuredPercent,
            normalizedPercent = normalizedPercent,
        )
    }

    private fun nestedGroupLines(
        group: BalancingGroup,
        universe: List<HoldingHistoryEntry>,
        totalPortfolioValue: Double,
    ): List<PortfolioBalancingReportLine> {
        val actuals = classifyAndSum(universe, group)
        val groupTotal = actuals.values.sum()
        if (groupTotal == 0.0) return emptyList()
        return group.components.mapNotNull { component ->
            val actual = actuals.getValue(component.id)
            if (!shouldDisplayInReport(component, actual)) return@mapNotNull null
            val (configuredDisplay, configuredPercent) = configuredWeight(component.targetWeight)
            val ideal = when {
                totalPortfolioValue == 0.0 -> 0.0
                component.targetWeight is TargetWeight.Fixed -> groupTotal * component.targetWeight.percent / 100.0
                component.targetWeight is TargetWeight.Zero -> 0.0
                else -> error("Residual component is not supported in nested groups")
            }
            val normalizedPercent = if (totalPortfolioValue > 0.0) {
                ideal / totalPortfolioValue * 100.0
            } else {
                0.0
            }
            toReportLine(
                group = group,
                component = component,
                actualValue = actual,
                idealValue = ideal,
                actualWeightPercent = actualWeightPercent(
                    actualValue = actual,
                    groupTotal = groupTotal,
                ),
                configuredDisplay = configuredDisplay,
                configuredPercent = configuredPercent,
                normalizedPercent = normalizedPercent,
            )
        }
    }

    private fun actualWeightPercent(actualValue: Double, groupTotal: Double): Double =
        if (groupTotal > 0.0) actualValue / groupTotal * 100.0 else 0.0

    private fun toReportLine(
        group: BalancingGroup,
        component: BalancingComponent,
        actualValue: Double,
        idealValue: Double,
        actualWeightPercent: Double,
        configuredDisplay: String,
        configuredPercent: Double?,
        normalizedPercent: Double,
    ): PortfolioBalancingReportLine {
        return PortfolioBalancingReportLine(
            groupId = group.id,
            groupName = group.displayName,
            componentName = component.displayName,
            actualValue = actualValue,
            actualWeightDisplay = formatPercent(actualWeightPercent),
            actualWeightPercent = actualWeightPercent,
            configuredWeightDisplay = configuredDisplay,
            configuredWeightPercent = configuredPercent,
            normalizedWeightDisplay = formatPercent(normalizedPercent),
            normalizedWeightPercent = normalizedPercent,
            idealValue = idealValue,
            deviation = idealValue - actualValue,
        )
    }

    private fun configuredWeight(weight: TargetWeight): Pair<String, Double?> = when (weight) {
        is TargetWeight.Fixed -> formatPercent(weight.percent) to weight.percent
        TargetWeight.Zero -> "0,00%" to 0.0
        TargetWeight.Dynamic -> "dinâmico" to null
    }

    internal fun formatPercent(value: Double): String {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        val parts = rounded.toString().split('.')
        val integer = parts[0]
        val fraction = parts.getOrElse(1) { "0" }.padEnd(2, '0').take(2)
        return "$integer,$fraction%"
    }
}
