package com.eferraz.asset_management.assets

import com.eferraz.asset_management.helpers.localDateFromIsoDateDigits
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset

internal fun AssetManagementUiState.buildAsset(): Asset {
    return when (category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeAsset()
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeAsset()
        InvestmentCategory.INVESTMENT_FUND -> buildFundAsset()
    }
}

private fun AssetManagementUiState.buildFixedIncomeAsset(): FixedIncomeAsset {

    return FixedIncomeAsset(
        id = asset?.id ?: 0L,
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

private fun AssetManagementUiState.buildVariableIncomeAsset(): VariableIncomeAsset {

    return VariableIncomeAsset(
        id = asset?.id ?: 0L,
        name = variableTicker.orEmpty().trim(),
        issuer = issuer!!,
        type = variableType!!,
        ticker = variableTicker.orEmpty().trim(),
        cnpj = variableCnpj.takeIf { it.isNullOrBlank().not() }?.let { CNPJ(variableCnpj) },
        observations = observations,
    )
}

private fun AssetManagementUiState.buildFundAsset(): InvestmentFundAsset {

    return InvestmentFundAsset(
        id = asset?.id ?: 0L,
        name = fundName.orEmpty().trim(),
        issuer = issuer!!,
        type = fundType!!,
        liquidity = fundLiquidity!!,
        liquidityDays = fundLiquidityDays?.toIntOrNull()!!,
        expirationDate = localDateFromIsoDateDigits(fundExpiration),
        observations = observations,
    )
}

internal fun Asset.toUiState(): AssetManagementUiState {

    return when (val currentAsset = this) {

        is FixedIncomeAsset -> AssetManagementUiState(
            asset = this,
            category = InvestmentCategory.FIXED_INCOME,
            issuer = currentAsset.issuer,
            observations = currentAsset.observations,
            fixedType = currentAsset.type,
            fixedSubType = currentAsset.subType,
            fixedExpiration = currentAsset.expirationDate.toString().replace("-", ""),
            fixedYield = currentAsset.contractedYield.toString(),
            fixedCdi = currentAsset.cdiRelativeYield?.toString(),
            fixedLiquidity = currentAsset.liquidity,
        )

        is VariableIncomeAsset -> AssetManagementUiState(
            asset = this,
            category = InvestmentCategory.VARIABLE_INCOME,
            issuer = currentAsset.issuer,
            observations = currentAsset.observations,
            variableName = currentAsset.name,
            variableType = currentAsset.type,
            variableTicker = currentAsset.ticker,
            variableCnpj = currentAsset.cnpj?.get(),
        )

        is InvestmentFundAsset -> AssetManagementUiState(
            asset = this,
            category = InvestmentCategory.INVESTMENT_FUND,
            issuer = currentAsset.issuer,
            observations = currentAsset.observations,
            fundName = currentAsset.name,
            fundType = currentAsset.type,
            fundLiquidity = currentAsset.liquidity,
            fundLiquidityDays = currentAsset.liquidityDays.toString(),
            fundExpiration = currentAsset.expirationDate?.toString()?.replace("-", ""),
        )
    }
}