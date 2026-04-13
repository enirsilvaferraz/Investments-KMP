package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.usecases.UpsertInvestmentAssetUseCase

internal fun buildUpsertParam(d: AssetDraft): UpsertInvestmentAssetUseCase.Param? {
    val issuerId = d.issuerId ?: return null
    val brokerageId = d.brokerageId ?: return null
    return when (d.category) {
        InvestmentCategory.FIXED_INCOME -> buildFixedIncomeParam(d, issuerId, brokerageId)
        InvestmentCategory.VARIABLE_INCOME -> buildVariableIncomeParam(d, issuerId, brokerageId)
        InvestmentCategory.INVESTMENT_FUND -> buildFundParam(d, issuerId, brokerageId)
    }
}

private fun hasCompleteFixedIncomeInputs(d: AssetDraft): Boolean {
    if (d.fixedType == null || d.fixedSubType == null || d.fixedLiquidity == null) {
        return false
    }
    val exp = localDateFromIsoDateDigits(d.fixedExpiration.orEmpty())
    return exp != null && d.fixedYield.orEmpty().toDoubleOrNull() != null
}

private fun buildFixedIncomeParam(d: AssetDraft, issuerId: Long, brokerageId: Long): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFixedIncomeInputs(d)) return null
    val type = d.fixedType!!
    val sub = d.fixedSubType!!
    val liq = d.fixedLiquidity!!
    val exp = localDateFromIsoDateDigits(d.fixedExpiration.orEmpty())!!
    val y = d.fixedYield.orEmpty().toDoubleOrNull()!!
    val cdi = d.fixedCdi.takeIf { it.isNullOrBlank().not() }?.toDoubleOrNull()
    return UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration(
        assetId = 0L,
        issuerId = issuerId,
        observations = d.observations.takeIf { it.isNullOrBlank().not() },
        brokerageId = brokerageId,
        type = type,
        subType = sub,
        expirationDate = exp,
        contractedYield = y,
        cdiRelativeYield = cdi,
        liquidity = liq,
    )
}

private fun buildVariableIncomeParam(d: AssetDraft, issuerId: Long, brokerageId: Long): UpsertInvestmentAssetUseCase.Param? {
    val t = d.variableType
    return if (t != null) {
        UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
            assetId = 0L,
            issuerId = issuerId,
            observations = d.observations.takeIf { !it.isNullOrBlank() },
            brokerageId = brokerageId,
            assetName = d.variableName.orEmpty(),
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

private fun buildFundParam(d: AssetDraft, issuerId: Long, brokerageId: Long): UpsertInvestmentAssetUseCase.Param? {
    if (!hasCompleteFundInputs(d)) return null
    val ft = d.fundType!!
    val fl = d.fundLiquidity!!
    val days = d.fundLiquidityDays.orEmpty().toIntOrNull()!!
    val exp = d.fundExpiration.takeIf { !it.isNullOrBlank() }?.let { localDateFromIsoDateDigits(it) }
    return UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
        assetId = 0L,
        issuerId = issuerId,
        observations = d.observations.takeIf { !it.isNullOrBlank() },
        brokerageId = brokerageId,
        name = d.fundName.orEmpty(),
        type = ft,
        liquidity = fl,
        liquidityDays = days,
        expirationDate = exp,
    )
}
