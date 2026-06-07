package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry

internal data class BalancingTreeNode(
    val id: String,
    val displayName: String,
    val targetWeight: TargetWeight,
    val matches: (HoldingHistoryEntry) -> Boolean,
    val children: List<BalancingTreeNode> = emptyList(),
)
