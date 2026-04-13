package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.UpsertInvestmentAssetUseCase

internal fun buildUpsertParam(d: AssetDraft): UpsertInvestmentAssetUseCase.Param? {
    val issuer = d.issuer ?: return null
    val brokerage = d.brokerage ?: return null
    return when (d.category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeParam(d, issuer, brokerage)
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeParam(d, issuer, brokerage)
        InvestmentCategory.INVESTMENT_FUND -> buildFundParam(d, issuer, brokerage)
    }
}

private fun hasCompleteFixedIncomeInputs(d: AssetDraft): Boolean {
    if (d.fixedType == null || d.fixedSubType == null || d.fixedLiquidity == null) {
        return false
    }
    val exp = localDateFromIsoDateDigits(d.fixedExpiration.orEmpty())
    return exp != null && d.fixedYield.orEmpty().toDoubleOrNull() != null
}

private fun buildFixedIncomeParam(d: AssetDraft, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFixedIncomeInputs(d)) return null
    val type = d.fixedType!!
    val sub = d.fixedSubType!!
    val liq = d.fixedLiquidity!!
    val exp = localDateFromIsoDateDigits(d.fixedExpiration.orEmpty())!!
    val y = d.fixedYield.orEmpty().toDoubleOrNull()!!
    val cdi = d.fixedCdi.takeIf { it.isNullOrBlank().not() }?.toDoubleOrNull()
    return UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration(
        assetId = 0L,
        issuer = issuer,
        observations = d.observations.takeIf { it.isNullOrBlank().not() },
        brokerage = brokerage,
        type = type,
        subType = sub,
        expirationDate = exp,
        contractedYield = y,
        cdiRelativeYield = cdi,
        liquidity = liq,
    )
}

private fun buildVariableIncomeParam(d: AssetDraft, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    val t = d.variableType
    return if (t != null) {
        UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
            assetId = 0L,
            issuer = issuer,
            observations = d.observations.takeIf { !it.isNullOrBlank() },
            brokerage = brokerage,
            assetName = d.variableTicker.orEmpty(),
            type = t,
            ticker = d.variableTicker.orEmpty(),
            cnpjRaw = d.variableCnpj.takeIf { !it.isNullOrBlank() },
        )
    } else {
        null
    }
}

private fun hasCompleteFundInputs(d: AssetDraft): Boolean {
    if (d.fundType == null || d.fundLiquidity == null) {
        return false
    }
    val days = d.fundLiquidityDays.orEmpty().toIntOrNull()
    return days != null && days > 0
}

private fun buildFundParam(d: AssetDraft, issuer: Issuer, brokerage: Brokerage): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFundInputs(d)) return null
    val ft = d.fundType!!
    val fl = d.fundLiquidity!!
    val days = d.fundLiquidityDays.orEmpty().toIntOrNull()!!
    val exp = d.fundExpiration.takeIf { !it.isNullOrBlank() }?.let { localDateFromIsoDateDigits(it) }
    return UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
        assetId = 0L,
        issuer = issuer,
        observations = d.observations.takeIf { !it.isNullOrBlank() },
        brokerage = brokerage,
        name = d.fundName.orEmpty(),
        type = ft,
        liquidity = fl,
        liquidityDays = days,
        expirationDate = exp,
    )
}
