package com.eferraz.usecases.balancing

import kotlin.math.abs

internal object PortfolioBalancingCatalogValidator {

    private const val TOLERANCE_PERCENT: Double = 0.01

    fun validate(groups: List<BalancingGroup> = PortfolioBalancingCatalog.groups) {
        groups.forEach(::validateGroup)
    }

    private fun validateGroup(group: BalancingGroup) {

        val dynamicCount = group.components.count { it.targetWeight is TargetWeight.Dynamic }

        require(dynamicCount <= 1) {
            "Group ${group.id} must have at most one Dynamic component, found $dynamicCount"
        }

        val configuredSum = group.components.sumOf { component ->
            when (val weight = component.targetWeight) {
                is TargetWeight.Fixed -> weight.percent
                TargetWeight.Zero -> 0.0
                TargetWeight.Dynamic -> 0.0
            }
        }

        require(abs(configuredSum - 100.0) <= TOLERANCE_PERCENT + 1e-6) {
            "Group ${group.id} configured weights must sum to 100% (±$TOLERANCE_PERCENT), got $configuredSum"
        }
    }
}
