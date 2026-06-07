package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

internal object PortfolioBalancingEngine {

    fun calculate(
        entries: List<HoldingHistoryEntry>,
        referenceDate: YearMonth,
        root: BalancingTreeNode = PortfolioBalancingCatalog.root,
    ): PortfolioBalancingReport {

        val activeEntries = entries.filter { patrimony(it) > 0.0 }
        val index = BalancingUniverseIndex.build(root, activeEntries)
        val state = ComputeState()

        state.idealByNodeId[root.id] = index.totalPortfolioValue
        computeAndEmitNode(node = root, index = index, state = state)

        return PortfolioBalancingReport(
            referenceDate = referenceDate,
            totalPortfolioValue = index.totalPortfolioValue,
            sections = state.sections,
            balanceableBase = state.actualByNodeId[BalancingGroupId.BALANCEABLE] ?: 0.0,
        )
    }

    internal fun patrimony(entry: HoldingHistoryEntry): Double =
        entry.endOfMonthValue * entry.endOfMonthQuantity

    private data class ComputeState(
        val idealByNodeId: MutableMap<String, Double> = mutableMapOf(),
        val actualByNodeId: MutableMap<String, Double> = mutableMapOf(),
        val sections: MutableList<PortfolioBalancingReportSection> = mutableListOf(),
    )

    private fun computeAndEmitNode(
        node: BalancingTreeNode,
        index: BalancingUniverseIndex,
        state: ComputeState,
    ) {
        if (node.children.isEmpty()) return

        val referenceBase = state.idealByNodeId.getValue(node.id)
        val childLines = node.children.map { child ->
            val actual = computeActual(child, index)
            state.actualByNodeId[child.id] = actual
            val ideal = BalancingIdealCalculator.computeIdeal(child.targetWeight, actual, referenceBase)
            state.idealByNodeId[child.id] = ideal
            Triple(child, actual, ideal)
        }
        val sectionActualTotal = childLines.sumOf { it.second }
        val rows = childLines.mapNotNull { (child, actual, ideal) ->
            val line = toReportLine(child, actual, ideal, sectionActualTotal, index)
            if (shouldDisplayInReport(child.id, actual)) line else null
        }
        val totalRow = buildTotalRow(node.id, rows)
        state.sections += PortfolioBalancingReportSection(
            nodeId = node.id,
            nodeName = node.displayName,
            rows = rows,
            totalRow = totalRow,
        )
        node.children.forEach { child ->
            computeAndEmitNode(child, index, state)
        }
    }

    private fun computeActual(node: BalancingTreeNode, index: BalancingUniverseIndex): Double =
        if (node.children.isEmpty()) {
            index.byNodeId.getValue(node.id).sumOf { patrimony(it) }
        } else {
            node.children.sumOf { computeActual(it, index) }
        }

    private fun toReportLine(
        node: BalancingTreeNode,
        actualValue: Double,
        idealValue: Double,
        sectionActualTotal: Double,
        index: BalancingUniverseIndex,
    ): PortfolioBalancingReportLine {
        val actualWeightPercent = actualWeightPercent(actualValue, sectionActualTotal)
        return PortfolioBalancingReportLine(
            nodeId = node.id,
            displayName = node.displayName,
            actualValue = actualValue,
            actualWeightDisplay = BalancingFormatters.formatPercent(actualWeightPercent),
            actualWeightPercent = actualWeightPercent,
            configuredWeightDisplay = BalancingIdealCalculator.configuredWeightDisplay(node.targetWeight),
            configuredWeightPercent = BalancingIdealCalculator.configuredWeightPercent(node.targetWeight),
            idealValue = idealValue,
            deviation = idealValue - actualValue,
            holdings = demaisHoldings(node, actualValue, index),
        )
    }

    private fun actualWeightPercent(actualValue: Double, sectionActualTotal: Double): Double =
        if (sectionActualTotal > 0.0) actualValue / sectionActualTotal * 100.0 else 0.0

    private fun demaisHoldings(
        node: BalancingTreeNode,
        actualValue: Double,
        index: BalancingUniverseIndex,
    ): List<PortfolioBalancingHoldingLine> {
        if (!BalancingMatchers.isDemaisFallbackNode(node.id) || actualValue <= 0.0) {
            return emptyList()
        }
        return index.byNodeId.getValue(node.id)
            .map { entry ->
                PortfolioBalancingHoldingLine(
                    displayName = formatBalancingAssetDisplayName(entry.holding.asset),
                    value = patrimony(entry),
                )
            }
            .sortedBy { it.displayName }
    }

    private fun shouldDisplayInReport(nodeId: String, actualValue: Double): Boolean =
        !BalancingMatchers.isDemaisFallbackNode(nodeId) || actualValue > 0.0

    private fun buildTotalRow(
        sectionNodeId: String,
        rows: List<PortfolioBalancingReportLine>,
    ): PortfolioBalancingReportLine = PortfolioBalancingReportLine(
        nodeId = sectionNodeId,
        displayName = "Total",
        actualValue = rows.sumOf { it.actualValue },
        actualWeightDisplay = "100,00%",
        actualWeightPercent = 100.0,
        configuredWeightDisplay = "100,00%",
        configuredWeightPercent = 100.0,
        idealValue = rows.sumOf { it.idealValue },
        deviation = rows.sumOf { it.deviation },
    )
}
