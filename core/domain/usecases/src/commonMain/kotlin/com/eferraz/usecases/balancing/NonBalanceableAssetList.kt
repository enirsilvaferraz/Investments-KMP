package com.eferraz.usecases.balancing

import com.eferraz.entities.holdings.HoldingHistoryEntry

/**
 * Single explicit configuration point for non-balanceable assets (FR-007).
 * Omission from this list means balanceable.
 */
internal object NonBalanceableAssetList {

    val matchers: List<(HoldingHistoryEntry) -> Boolean> = listOf(
        BalancingMatchers::isPensionFund,
        BalancingMatchers::isFGTSFund,
    )
}
