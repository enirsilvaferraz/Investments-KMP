package com.eferraz.usecases.balancing

import kotlin.math.abs

internal object PortfolioBalancingCatalogValidator {

    private const val TOLERANCE_PERCENT: Double = 0.01

    fun validate(root: BalancingTreeNode = PortfolioBalancingCatalog.root) {
        validateNode(root, inBalanceableSubtree = false)
    }

    private fun validateNode(node: BalancingTreeNode, inBalanceableSubtree: Boolean) {
        val inBalanceable = inBalanceableSubtree || node.id == BalancingGroupId.BALANCEABLE

        if (inBalanceable && node.id != BalancingGroupId.BALANCEABLE) {
            require(node.targetWeight !is TargetWeight.Dynamic) {
                "Node ${node.id} in balanceable subtree must not use Dynamic weight (FR-006)"
            }
        }

        if (node.children.isNotEmpty()) {
            validateWeightSum(node)
            validatePartition(node)
            node.children.forEach { child ->
                validateNode(child, inBalanceable)
            }
        }
    }

    private fun validateWeightSum(node: BalancingTreeNode) {
        val hasDynamicChild = node.children.any { it.targetWeight is TargetWeight.Dynamic }
        if (hasDynamicChild) return
        if (node.targetWeight is TargetWeight.Zero) return

        val configuredSum = node.children.sumOf { child ->
            when (val weight = child.targetWeight) {
                is TargetWeight.Fixed -> weight.percent
                TargetWeight.Zero -> 0.0
                TargetWeight.Dynamic -> 0.0
            }
        }
        require(abs(configuredSum - 100.0) <= TOLERANCE_PERCENT + 1e-6) {
            "Node ${node.id} configured weights must sum to 100% (±$TOLERANCE_PERCENT), got $configuredSum"
        }
    }

    private fun validatePartition(node: BalancingTreeNode) {
        val terminalChildren = node.children.filter { it.children.isEmpty() }
        if (terminalChildren.size < 2) return

        val overlapPairs = terminalChildren.flatMapIndexed { index, first ->
            terminalChildren.drop(index + 1).map { second -> first to second }
        }.filter { (first, second) ->
            matchersCanOverlap(first, second)
        }

        require(overlapPairs.isEmpty()) {
            "Node ${node.id} terminal children are not mutually exclusive: " +
                overlapPairs.joinToString { (a, b) -> "${a.id} ∩ ${b.id}" }
        }
    }

    private fun matchersCanOverlap(
        first: BalancingTreeNode,
        second: BalancingTreeNode,
    ): Boolean {
        if (BalancingMatchers.isDemaisFallbackNode(first.id) || BalancingMatchers.isDemaisFallbackNode(second.id)) {
            return false
        }
        return first.matches === second.matches
    }
}
