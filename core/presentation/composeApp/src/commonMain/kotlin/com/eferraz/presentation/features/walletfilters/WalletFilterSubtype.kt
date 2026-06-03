package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAssetType

internal sealed interface WalletFilterSubtype {

    data class FixedIncome(val value: FixedIncomeAssetType) : WalletFilterSubtype
    data class VariableIncome(val value: VariableIncomeAssetType) : WalletFilterSubtype
    data class InvestmentFund(val value: InvestmentFundAssetType) : WalletFilterSubtype
}

internal fun WalletFilterSubtype.assetClass(): AssetClass =
    when (this) {
        is WalletFilterSubtype.FixedIncome -> AssetClass.FIXED_INCOME
        is WalletFilterSubtype.VariableIncome -> AssetClass.VARIABLE_INCOME
        is WalletFilterSubtype.InvestmentFund -> AssetClass.INVESTMENT_FUND
    }

internal val subtypesByAssetClass: Map<AssetClass, List<WalletFilterSubtype>> =
    mapOf(
        AssetClass.FIXED_INCOME to FixedIncomeAssetType.entries.map { WalletFilterSubtype.FixedIncome(it) },
        AssetClass.VARIABLE_INCOME to VariableIncomeAssetType.entries.map { WalletFilterSubtype.VariableIncome(it) },
        AssetClass.INVESTMENT_FUND to InvestmentFundAssetType.entries.map { WalletFilterSubtype.InvestmentFund(it) },
    )
