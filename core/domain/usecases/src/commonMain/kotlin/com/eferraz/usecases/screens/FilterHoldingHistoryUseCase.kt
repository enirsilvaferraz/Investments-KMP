package com.eferraz.usecases.screens

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.AppUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class FilterHoldingHistoryUseCase(
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<FilterHoldingHistoryUseCase.Param, List<HoldingHistoryEntry>>(context) {

    public data class Param(
        val entries: List<HoldingHistoryEntry>,
        val criteria: WalletHistoryFilterCriteria,
    )

    override suspend fun execute(param: Param): List<HoldingHistoryEntry> {
        if (param.entries.isEmpty()) return emptyList()
        return param.entries.filter { entry ->
            matchesWalletHistoryFilter(
                entry.toWalletHistoryFilterCandidate(),
                param.criteria,
            )
        }
    }

    private fun matchesWalletHistoryFilter(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (!matchesAssetClass(candidate, criteria)) return false
        if (!matchesSubtype(candidate, criteria)) return false
        if (!matchesLiquidity(candidate, criteria)) return false
        if (!matchesB3Informed(candidate, criteria)) return false
        if (!matchesSettled(candidate, criteria)) return false
        if (!matchesMaturity(candidate, criteria)) return false
        if (!matchesBrokerage(candidate, criteria)) return false
        return true
    }

    private fun matchesBrokerage(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.brokerageIds.isEmpty()) return true
        return candidate.brokerageId in criteria.brokerageIds
    }

    private fun matchesAssetClass(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.assetClasses.isEmpty()) return true
        return candidate.assetClass in criteria.assetClasses
    }

    private fun matchesSubtype(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.subtypes.isEmpty()) return true
        val applicable = criteria.subtypes.filter { it.assetClass() == candidate.assetClass }
        val effective = if (criteria.assetClasses.isEmpty()) {
            applicable
        } else {
            applicable.filter { it.assetClass() in criteria.assetClasses }
        }
        if (effective.isEmpty()) return true
        return candidate.subtype in effective
    }

    private fun matchesLiquidity(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.liquidities.isEmpty()) return true
        if (candidate.assetClass != AssetClass.FIXED_INCOME) return true
        return candidate.liquidity in criteria.liquidities
    }

    private fun matchesB3Informed(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.b3Informed.isEmpty() || criteria.b3Informed.size == 2) return true
        return candidate.b3Informed in criteria.b3Informed
    }

    private fun matchesSettled(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        if (criteria.settled.isEmpty() || criteria.settled.size == 2) return true
        return candidate.settled in criteria.settled
    }

    private fun matchesMaturity(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean {
        val upTo = criteria.maturityUpTo ?: return true
        if (candidate.assetClass != AssetClass.FIXED_INCOME) return true
        if (candidate.liquidity == Liquidity.DAILY) return true
        val expiration = candidate.expirationDate ?: return false
        return expiration <= upTo.lastDayOfMonth()
    }
}
