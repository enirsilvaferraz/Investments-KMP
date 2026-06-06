package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAsset
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
        val group1Ideals = computeGroup1Ideals(group1Actuals, totalPortfolioValue)

        PortfolioBalancingCatalog.portfolioTotalGroup.components.forEach { component ->
            val actual = group1Actuals.getValue(component.id)
            val ideal = group1Ideals.getValue(component.id)
            lines += toReportLine(
                group = PortfolioBalancingCatalog.portfolioTotalGroup,
                component = component,
                actualValue = actual,
                idealValue = ideal,
                totalPortfolioValue = totalPortfolioValue,
            )
        }

        val rfUniverse = activeEntries.filter { it.holding.asset is FixedIncomeAsset }
        val rfParentIdeal = group1Ideals.getValue(BalancingComponentId.FIXED_INCOME_TOTAL)
        lines += nestedGroupLines(
            group = PortfolioBalancingCatalog.fixedIncomeGroup,
            universe = rfUniverse,
            parentIdeal = rfParentIdeal,
            totalPortfolioValue = totalPortfolioValue,
        )

        val rvUniverse = activeEntries.filter { entry ->
            val asset = entry.holding.asset
            asset is VariableIncomeAsset && asset.ticker.uppercase() != PortfolioBalancingCatalog.HASH11
        }
        val rvParentIdeal = group1Ideals.getValue(BalancingComponentId.VARIABLE_INCOME_TOTAL)
        lines += nestedGroupLines(
            group = PortfolioBalancingCatalog.variableIncomeGroup,
            universe = rvUniverse,
            parentIdeal = rvParentIdeal,
            totalPortfolioValue = totalPortfolioValue,
        )

        return PortfolioBalancingReport(
            referenceDate = referenceDate,
            totalPortfolioValue = totalPortfolioValue,
            lines = lines,
        )
    }

    internal fun patrimony(entry: HoldingHistoryEntry): Double =
        entry.endOfMonthValue * entry.endOfMonthQuantity

    internal fun classifyAndSum(
        universe: List<HoldingHistoryEntry>,
        group: BalancingGroup,
    ): Map<BalancingComponentId, Double> {
        val sums = group.components.associate { it.id to 0.0 }.toMutableMap()
        for (entry in universe) {
            val component = group.components.first { it.matches(entry) }
            sums[component.id] = sums.getValue(component.id) + patrimony(entry)
        }
        return sums
    }

    private fun computeGroup1Ideals(
        actuals: Map<BalancingComponentId, Double>,
        totalPortfolioValue: Double,
    ): Map<BalancingComponentId, Double> {
        if (totalPortfolioValue == 0.0) {
            return PortfolioBalancingCatalog.portfolioTotalGroup.components.associate { it.id to 0.0 }
        }

        return PortfolioBalancingCatalog.portfolioTotalGroup.components.associate { component ->
            val ideal = when (val weight = component.targetWeight) {
                is TargetWeight.Fixed -> totalPortfolioValue * weight.percent / 100.0
                TargetWeight.Zero -> 0.0
                TargetWeight.DynamicPension -> actuals.getValue(component.id)
            }
            component.id to ideal
        }
    }

    private fun nestedGroupLines(
        group: BalancingGroup,
        universe: List<HoldingHistoryEntry>,
        parentIdeal: Double,
        totalPortfolioValue: Double,
    ): List<PortfolioBalancingReportLine> {
        val actuals = classifyAndSum(universe, group)
        return group.components.map { component ->
            val actual = actuals.getValue(component.id)
            val ideal = if (totalPortfolioValue == 0.0) {
                0.0
            } else {
                val weight = component.targetWeight as TargetWeight.Fixed
                parentIdeal * weight.percent / 100.0
            }
            toReportLine(
                group = group,
                component = component,
                actualValue = actual,
                idealValue = ideal,
                totalPortfolioValue = totalPortfolioValue,
            )
        }
    }

    private fun toReportLine(
        group: BalancingGroup,
        component: BalancingComponent,
        actualValue: Double,
        idealValue: Double,
        totalPortfolioValue: Double,
    ): PortfolioBalancingReportLine {
        val (display, percent) = targetWeightDisplay(component.targetWeight, actualValue, totalPortfolioValue)
        return PortfolioBalancingReportLine(
            groupId = group.id,
            groupName = group.displayName,
            componentName = component.displayName,
            actualValue = actualValue,
            targetWeightDisplay = display,
            targetWeightPercent = percent,
            idealValue = idealValue,
            deviation = actualValue - idealValue,
        )
    }

    private fun targetWeightDisplay(
        weight: TargetWeight,
        actualValue: Double,
        totalPortfolioValue: Double,
    ): Pair<String, Double?> = when (weight) {
        is TargetWeight.Fixed -> {
            val display = formatPercent(weight.percent)
            display to weight.percent
        }
        TargetWeight.Zero -> "0,00%" to 0.0
        TargetWeight.DynamicPension -> {
            val percent = if (totalPortfolioValue > 0.0) {
                actualValue / totalPortfolioValue * 100.0
            } else {
                0.0
            }
            "dinâmico (${formatPercent(percent)})" to percent
        }
    }

    private fun formatPercent(value: Double): String {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        val parts = rounded.toString().split('.')
        val integer = parts[0]
        val fraction = parts.getOrElse(1) { "0" }.padEnd(2, '0').take(2)
        return "$integer,$fraction%"
    }
}
