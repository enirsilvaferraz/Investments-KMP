package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry

internal object BalancingTreeNodeFactory {

    fun demaisInvestimentosNode(
        parentId: String,
        parentUniverse: (HoldingHistoryEntry) -> Boolean,
        siblings: List<BalancingTreeNode>,
        weight: TargetWeight = TargetWeight.Zero,
    ): BalancingTreeNode = BalancingTreeNode(
        id = demaisId(parentId),
        displayName = "Demais investimentos",
        targetWeight = weight,
        matches = { entry ->
            BalancingMatchers.isDemaisAmong(
                entry = entry,
                inUniverse = parentUniverse,
                siblingMatchers = siblings.map { it.matches },
            )
        },
    )

    fun withDemaisInvestimentos(
        node: BalancingTreeNode,
        parentUniverse: (HoldingHistoryEntry) -> Boolean = node.matches,
    ): BalancingTreeNode {
        if (node.children.isEmpty()) return node
        return node.copy(
            children = node.children + demaisInvestimentosNode(
                parentId = node.id,
                parentUniverse = parentUniverse,
                siblings = node.children,
            ),
        )
    }

    fun demaisId(parentId: String): String = "$parentId${BalancingGroupId.DEMAIS_SUFFIX}"
}
