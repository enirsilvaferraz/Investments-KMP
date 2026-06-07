package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.HoldingHistoryEntry

internal object BalancingMatchers {

    fun isCrypto(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? VariableIncomeAsset ?: return false
        return asset.ticker.uppercase() == BalancingConstants.HASH11
    }

    fun isPensionFund(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? InvestmentFundAsset ?: return false
        return asset.type == InvestmentFundAssetType.PENSION
    }

    fun isFGTSFund(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? InvestmentFundAsset ?: return false
        return asset.observations == "Fundo atrelado ao FGTS"
    }

    fun isFGTSAccount(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? FixedIncomeAsset ?: return false
        return asset.observations?.contains("FGTS") ?: false
    }

    fun isFixedIncome(entry: HoldingHistoryEntry): Boolean =
        entry.holding.asset is FixedIncomeAsset

    fun isVariableIncomeExcludingCrypto(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? VariableIncomeAsset ?: return false
        return asset.ticker.uppercase() != BalancingConstants.HASH11
    }

    fun isRealEstateFund(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? VariableIncomeAsset ?: return false
        return asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND
    }

    fun isFixedIncomeWithIndexer(indexer: YieldIndexer): (HoldingHistoryEntry) -> Boolean = { entry ->
        val asset = entry.holding.asset as? FixedIncomeAsset
        asset != null && asset.indexer == indexer
    }

    fun isInternationalStock(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? VariableIncomeAsset ?: return false
        return asset.ticker.uppercase() == BalancingConstants.IVVB11
    }

    fun isRealEstateFundWithTickerIn(tickers: Set<String>): (HoldingHistoryEntry) -> Boolean = { entry ->
        val asset = entry.holding.asset as? VariableIncomeAsset
        asset != null &&
            asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND &&
            asset.ticker.uppercase() in tickers
    }

    fun isNationalStock(): (HoldingHistoryEntry) -> Boolean = { entry ->
        val asset = entry.holding.asset as? VariableIncomeAsset
        asset != null && asset.type == VariableIncomeAssetType.NATIONAL_STOCK
    }

    fun isNationalStockWithTickerIn(tickers: Set<String>): (HoldingHistoryEntry) -> Boolean = { entry ->
        val asset = entry.holding.asset as? VariableIncomeAsset
        asset != null &&
            asset.type == VariableIncomeAssetType.NATIONAL_STOCK &&
            asset.ticker.uppercase() in tickers
    }

    fun always(entry: HoldingHistoryEntry): Boolean = true

    fun isNonBalanceable(entry: HoldingHistoryEntry): Boolean =
        listOf(
            BalancingMatchers::isPensionFund,
            BalancingMatchers::isFGTSFund,
            BalancingMatchers::isFGTSAccount,
        ).any { matcher -> matcher(entry) }

    fun isBalanceable(entry: HoldingHistoryEntry): Boolean = !isNonBalanceable(entry)

    fun isDemaisAmong(
        entry: HoldingHistoryEntry,
        inUniverse: (HoldingHistoryEntry) -> Boolean,
        siblingMatchers: List<(HoldingHistoryEntry) -> Boolean>,
    ): Boolean = inUniverse(entry) && siblingMatchers.none { matcher -> matcher(entry) }

    fun isDemaisFallbackNode(nodeId: String): Boolean =
        nodeId.endsWith("_${BalancingGroupId.DEMAIS}")
}
