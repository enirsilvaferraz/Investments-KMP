package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAssetType

/** Subtipo de activo nos filtros da carteira — espelha os enums de `:domain:entity`. */
internal sealed interface WalletFilterSubtype {

    data class FixedIncome(val value: FixedIncomeSubType) : WalletFilterSubtype

    data class VariableIncome(val value: VariableIncomeAssetType) : WalletFilterSubtype

    data class InvestmentFund(val value: InvestmentFundAssetType) : WalletFilterSubtype
}

internal fun WalletFilterSubtype.category(): InvestmentCategory =
    when (this) {
        is WalletFilterSubtype.FixedIncome -> InvestmentCategory.FIXED_INCOME
        is WalletFilterSubtype.VariableIncome -> InvestmentCategory.VARIABLE_INCOME
        is WalletFilterSubtype.InvestmentFund -> InvestmentCategory.INVESTMENT_FUND
    }

internal val subtypesByCategory: Map<InvestmentCategory, List<WalletFilterSubtype>> =
    mapOf(
        InvestmentCategory.FIXED_INCOME to FixedIncomeSubType.entries.map { WalletFilterSubtype.FixedIncome(it) },
        InvestmentCategory.VARIABLE_INCOME to VariableIncomeAssetType.entries.map { WalletFilterSubtype.VariableIncome(it) },
        InvestmentCategory.INVESTMENT_FUND to InvestmentFundAssetType.entries.map { WalletFilterSubtype.InvestmentFund(it) },
    )
