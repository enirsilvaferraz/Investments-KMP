package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.holdings.HoldingHistoryEntry

public fun HoldingHistoryEntry.toWalletHistoryFilterCandidate(): WalletHistoryFilterCandidate {
    val asset = holding.asset
    val patrimony = endOfMonthValue * endOfMonthQuantity
    val settled = patrimony == 0.0
    val brokerageId = holding.brokerage.id
    return when (asset) {
        is FixedIncomeAsset -> WalletHistoryFilterCandidate(
            assetClass = asset.assetClass,
            subtype = WalletHistorySubtype.FixedIncome(asset.type),
            liquidity = asset.liquidity,
            b3Informed = asset.b3Identifier.orEmpty().trim().isNotEmpty(),
            settled = settled,
            expirationDate = asset.expirationDate,
            brokerageId = brokerageId,
        )

        is VariableIncomeAsset -> WalletHistoryFilterCandidate(
            assetClass = asset.assetClass,
            subtype = WalletHistorySubtype.VariableIncome(asset.type),
            liquidity = asset.liquidity,
            b3Informed = true,
            settled = settled,
            expirationDate = null,
            brokerageId = brokerageId,
        )

        is InvestmentFundAsset -> WalletHistoryFilterCandidate(
            assetClass = asset.assetClass,
            subtype = WalletHistorySubtype.InvestmentFund(asset.type),
            liquidity = asset.liquidity,
            b3Informed = false,
            settled = settled,
            expirationDate = asset.expirationDate,
            brokerageId = brokerageId,
        )
    }
}
