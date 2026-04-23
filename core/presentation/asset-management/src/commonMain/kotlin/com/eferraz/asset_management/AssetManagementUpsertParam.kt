package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.UpsertInvestmentAssetUseCase

internal fun buildUpsertParam(s: UiState): UpsertInvestmentAssetUseCase.Param? {
    val issuer = s.issuer ?: return null
    val brokerage = s.brokerage ?: return null
    return when (s.category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeParam(s, issuer, brokerage)
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeParam(s, issuer, brokerage)
        InvestmentCategory.INVESTMENT_FUND -> buildFundParam(s, issuer, brokerage)
    }
}

private fun hasCompleteFixedIncomeInputs(s: UiState): Boolean {
    if (s.fixedType == null || s.fixedSubType == null || s.fixedLiquidity == null) {
        return false
    }
    val exp = localDateFromIsoDateDigits(s.fixedExpiration.orEmpty())
    return exp != null && s.fixedYield.orEmpty().toDoubleOrNull() != null
}

private fun buildFixedIncomeParam(s: UiState, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFixedIncomeInputs(s)) return null
    val type = s.fixedType!!
    val sub = s.fixedSubType!!
    val liq = s.fixedLiquidity!!
    val exp = localDateFromIsoDateDigits(s.fixedExpiration.orEmpty())!!
    val y = s.fixedYield.orEmpty().toDoubleOrNull()!!
    val cdi = s.fixedCdi.takeIf { it.isNullOrBlank().not() }?.toDoubleOrNull()
    return UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration(
        assetId = 0L,
        issuer = issuer,
        observations = s.observations.takeIf { it.isNullOrBlank().not() },
        brokerage = brokerage,
        type = type,
        subType = sub,
        expirationDate = exp,
        contractedYield = y,
        cdiRelativeYield = cdi,
        liquidity = liq,
    )
}

private fun buildVariableIncomeParam(s: UiState, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    val t = s.variableType
    return if (t != null) {
        UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
            assetId = 0L,
            issuer = issuer,
            observations = s.observations.takeIf { !it.isNullOrBlank() },
            brokerage = brokerage,
            assetName = s.variableTicker.orEmpty(),
            type = t,
            ticker = s.variableTicker.orEmpty(),
            cnpjRaw = s.variableCnpj.takeIf { !it.isNullOrBlank() },
        )
    } else {
        null
    }
}

private fun hasCompleteFundInputs(s: UiState): Boolean {
    if (s.fundType == null || s.fundLiquidity == null) {
        return false
    }
    val days = s.fundLiquidityDays.orEmpty().toIntOrNull()
    return days != null && days > 0
}

private fun buildFundParam(s: UiState, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFundInputs(s)) return null
    val ft = s.fundType!!
    val fl = s.fundLiquidity!!
    val days = s.fundLiquidityDays.orEmpty().toIntOrNull()!!
    val exp = s.fundExpiration.takeIf { !it.isNullOrBlank() }?.let { localDateFromIsoDateDigits(it) }
    return UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
        assetId = 0L,
        issuer = issuer,
        observations = s.observations.takeIf { !it.isNullOrBlank() },
        brokerage = brokerage,
        name = s.fundName.orEmpty(),
        type = ft,
        liquidity = fl,
        liquidityDays = days,
        expirationDate = exp,
    )
}
