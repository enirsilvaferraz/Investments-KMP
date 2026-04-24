package com.eferraz.asset_management

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner

internal fun buildAsset(s: UiState): Asset {
    return when (s.category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeAsset(s)
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeAsset(s)
        InvestmentCategory.INVESTMENT_FUND -> buildFundAsset(s)
    }
}

private fun buildFixedIncomeAsset(s: UiState): FixedIncomeAsset {
    return FixedIncomeAsset(
        id = 0L,
        issuer = s.issuer!!,
        type = s.fixedType!!,
        subType = s.fixedSubType!!,
        expirationDate = localDateFromIsoDateDigits(s.fixedExpiration)!!,
        contractedYield = s.fixedYield?.toDoubleOrNull()!!,
        cdiRelativeYield = s.fixedCdi?.toDoubleOrNull(),
        liquidity = s.fixedLiquidity!!,
        observations = s.observations
    )
}

private fun buildVariableIncomeAsset(s: UiState): VariableIncomeAsset {
    return VariableIncomeAsset(
        id = 0L,
        name = s.variableTicker.orEmpty().trim(),
        issuer = s.issuer!!,
        type = s.variableType!!,
        ticker = s.variableTicker.orEmpty().trim(),
        cnpj = CNPJ(s.variableCnpj),
        observations = s.observations,
    )
}

private fun buildFundAsset(s: UiState): InvestmentFundAsset {
    return InvestmentFundAsset(
        id = 0L,
        name = s.fundName.orEmpty().trim(),
        issuer = s.issuer!!,
        type = s.fundType!!,
        liquidity = s.fundLiquidity!!,
        liquidityDays = s.fundLiquidityDays?.toIntOrNull()!!,
        expirationDate = localDateFromIsoDateDigits(s.fundExpiration),
        observations = s.observations,
    )
}

internal fun buildHolding(
    baseAsset: Asset,
    assetId: Long,
    owner: Owner,
    brokerage: Brokerage,
): AssetHolding {
    return AssetHolding(
        id = 0L,
        asset = baseAsset.withSavedId(assetId),
        owner = owner,
        brokerage = brokerage,
        goal = null
    )
}

private fun Asset.withSavedId(newId: Long): Asset =
    when (this) {
        is FixedIncomeAsset -> copy(id = newId)
        is VariableIncomeAsset -> copy(id = newId)
        is InvestmentFundAsset -> copy(id = newId)
    }