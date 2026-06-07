package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry

internal data class BalancingUniverseIndex(
    val byNodeId: Map<String, List<HoldingHistoryEntry>>,
    val totalPortfolioValue: Double,
) {

    companion object {

        fun build(root: BalancingTreeNode, activeEntries: List<HoldingHistoryEntry>): BalancingUniverseIndex {
            val byNodeId = mutableMapOf<String, List<HoldingHistoryEntry>>()
            buildUniverse(root, parentId = null, activeEntries, byNodeId)
            return BalancingUniverseIndex(
                byNodeId = byNodeId,
                totalPortfolioValue = activeEntries.sumOf { PortfolioBalancingEngine.patrimony(it) },
            )
        }

        private fun buildUniverse(
            node: BalancingTreeNode,
            parentId: String?,
            activeEntries: List<HoldingHistoryEntry>,
            byNodeId: MutableMap<String, List<HoldingHistoryEntry>>,
        ) {
            byNodeId[node.id] = when (parentId) {
                null -> activeEntries
                else -> byNodeId.getValue(parentId).filter(node.matches)
            }
            node.children.forEach { child ->
                buildUniverse(child, node.id, activeEntries, byNodeId)
            }
        }
    }
}
