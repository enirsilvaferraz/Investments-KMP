package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory

internal fun validateAssetDraft(d: AssetDraft): Map<String, String> {
    val e = mutableMapOf<String, String>()
    if (d.issuerId == null) {
        e["issuer"] = "Selecione um emissor"
    }
    when (d.category) {
        InvestmentCategory.FIXED_INCOME -> e.putAll(validateFixedIncomeDraft(d))
        InvestmentCategory.VARIABLE_INCOME -> e.putAll(validateVariableIncomeDraft(d))
        InvestmentCategory.INVESTMENT_FUND -> e.putAll(validateFundDraft(d))
    }
    return e
}

private fun validateFixedIncomeDraft(d: AssetDraft): Map<String, String> {
    val expRaw = d.fixedExpiration.orEmpty()
    val yieldRaw = d.fixedYield.orEmpty()
    val cdiRaw = d.fixedCdi.orEmpty()
    val e = mutableMapOf<String, String>()
    if (d.fixedType == null) e["fixedType"] = "Obrigatório"
    if (d.fixedSubType == null) e["fixedSubType"] = "Obrigatório"
    if (expRaw.isBlank()) e["fixedExpiration"] = "Obrigatório"
    if (yieldRaw.isBlank()) e["fixedYield"] = "Obrigatório"
    if (d.fixedLiquidity == null) e["fixedLiquidity"] = "Obrigatório"
    if (expRaw.isNotBlank()) {
        if (localDateFromIsoDateDigits(expRaw) == null) {
            e["fixedExpiration"] = "Data inválida"
        }
    }
    if (yieldRaw.isNotBlank() && yieldRaw.toDoubleOrNull() == null) {
        e["fixedYield"] = "Número inválido"
    }
    if (cdiRaw.isNotBlank() && cdiRaw.toDoubleOrNull() == null) {
        e["fixedCdi"] = "Número inválido"
    }
    return e
}

private fun validateVariableIncomeDraft(d: AssetDraft): Map<String, String> {
    val e = mutableMapOf<String, String>()
    if (d.variableName.orEmpty().isBlank()) e["variableName"] = "Obrigatório"
    if (d.variableType == null) e["variableType"] = "Obrigatório"
    if (d.variableTicker.orEmpty().isBlank()) e["variableTicker"] = "Obrigatório"
    return e
}

private fun validateFundDraft(d: AssetDraft): Map<String, String> {
    val fundExp = d.fundExpiration.orEmpty()
    val e = mutableMapOf<String, String>()
    if (d.fundName.orEmpty().isBlank()) e["fundName"] = "Obrigatório"
    if (d.fundType == null) e["fundType"] = "Obrigatório"
    if (d.fundLiquidity == null) e["fundLiquidity"] = "Obrigatório"
    val days = d.fundLiquidityDays.orEmpty().toIntOrNull()
    if (days == null || days <= 0) e["fundLiquidityDays"] = "Valor inválido"
    if (fundExp.isNotBlank()) {
        if (localDateFromIsoDateDigits(fundExp) == null) {
            e["fundExpiration"] = "Data inválida"
        }
    }
    return e
}
