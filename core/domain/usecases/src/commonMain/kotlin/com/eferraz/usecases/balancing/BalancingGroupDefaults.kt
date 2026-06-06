package com.eferraz.usecases.balancing

internal fun List<BalancingGroup>.withDefaultOtherInvestments(): List<BalancingGroup> =
    map { it.withDefaultOtherInvestments() }

internal fun BalancingGroup.withDefaultOtherInvestments(): BalancingGroup {
    if (components.any { it.id == BalancingGroupId.OTHER_INVESTMENTS }) {
        return this
    }
    return copy(
        components = components + BalancingComponent(
            id = BalancingGroupId.OTHER_INVESTMENTS,
            displayName = "<<Demais investimentos>>",
            targetWeight = otherInvestmentsWeight,
            parentId = if (id == BalancingGroupId.PORTFOLIO_TOTAL) null else id,
            matches = BalancingMatchers::always,
        ),
    )
}
