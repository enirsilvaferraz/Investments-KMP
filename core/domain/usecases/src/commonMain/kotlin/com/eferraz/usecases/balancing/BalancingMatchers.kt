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

    fun isNationalStockExcludingSpecialTickers(entry: HoldingHistoryEntry): Boolean {
        val asset = entry.holding.asset as? VariableIncomeAsset ?: return false
        return asset.type == VariableIncomeAssetType.NATIONAL_STOCK &&
            asset.ticker.uppercase() !in setOf(BalancingConstants.HASH11, BalancingConstants.IVVB11)
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

    fun always(entry: HoldingHistoryEntry): Boolean = true
}
