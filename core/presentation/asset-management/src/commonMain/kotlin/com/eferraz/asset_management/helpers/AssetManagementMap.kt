package com.eferraz.asset_management.helpers

import com.eferraz.asset_management.vm.UiState
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner

internal fun UiState.buildAsset(): Asset {
    return when (category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeAsset()
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeAsset()
        InvestmentCategory.INVESTMENT_FUND -> buildFundAsset()
    }
}

private fun UiState.buildFixedIncomeAsset(): FixedIncomeAsset {
    return FixedIncomeAsset(
        id = editingAssetId ?: 0L,
        issuer = issuer!!,
        type = fixedType!!,
        subType = fixedSubType!!,
        expirationDate = localDateFromIsoDateDigits(fixedExpiration)!!,
        contractedYield = fixedYield?.toDoubleOrNull()!!,
        cdiRelativeYield = fixedCdi?.toDoubleOrNull(),
        liquidity = fixedLiquidity!!,
        observations = observations
    )
}

private fun UiState.buildVariableIncomeAsset(): VariableIncomeAsset {
    return VariableIncomeAsset(
        id = editingAssetId ?: 0L,
        name = variableTicker.orEmpty().trim(),
        issuer = issuer!!,
        type = variableType!!,
        ticker = variableTicker.orEmpty().trim(),
        cnpj = variableCnpj.takeIf { it.isNullOrBlank().not() }?.let { CNPJ(variableCnpj) } ,
        observations = observations,
    )
}

private fun UiState.buildFundAsset(): InvestmentFundAsset {
    return InvestmentFundAsset(
        id = editingAssetId ?: 0L,
        name = fundName.orEmpty().trim(),
        issuer = issuer!!,
        type = fundType!!,
        liquidity = fundLiquidity!!,
        liquidityDays = fundLiquidityDays?.toIntOrNull()!!,
        expirationDate = localDateFromIsoDateDigits(fundExpiration),
        observations = observations,
    )
}

internal fun buildHolding(
    baseAsset: Asset,
    assetId: Long,
    owner: Owner,
    brokerage: Brokerage,
    holdingId: Long?,
): AssetHolding {
    return AssetHolding(
        id = holdingId ?: 0L,
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

internal fun AssetHolding.toUiState(): UiState {

    return when (val currentAsset = asset) {

        is FixedIncomeAsset -> UiState(
            editingHoldingId = id,
            editingAssetId = currentAsset.id,
            category = InvestmentCategory.FIXED_INCOME,
            issuer = currentAsset.issuer,
            brokerage = brokerage,
            observations = currentAsset.observations,
            fixedType = currentAsset.type,
            fixedSubType = currentAsset.subType,
            fixedExpiration = currentAsset.expirationDate.toString().replace("-", ""),
            fixedYield = currentAsset.contractedYield.toString(),
            fixedCdi = currentAsset.cdiRelativeYield?.toString(),
            fixedLiquidity = currentAsset.liquidity,
        )

        is VariableIncomeAsset -> UiState(
            editingHoldingId = id,
            editingAssetId = currentAsset.id,
            category = InvestmentCategory.VARIABLE_INCOME,
            issuer = currentAsset.issuer,
            brokerage = brokerage,
            observations = currentAsset.observations,
            variableName = currentAsset.name,
            variableType = currentAsset.type,
            variableTicker = currentAsset.ticker,
            variableCnpj = currentAsset.cnpj?.get(),
        )

        is InvestmentFundAsset -> UiState(
            editingHoldingId = id,
            editingAssetId = currentAsset.id,
            category = InvestmentCategory.INVESTMENT_FUND,
            issuer = currentAsset.issuer,
            brokerage = brokerage,
            observations = currentAsset.observations,
            fundName = currentAsset.name,
            fundType = currentAsset.type,
            fundLiquidity = currentAsset.liquidity,
            fundLiquidityDays = currentAsset.liquidityDays.toString(),
            fundExpiration = currentAsset.expirationDate?.toString()?.replace("-", ""),
        )
    }
}