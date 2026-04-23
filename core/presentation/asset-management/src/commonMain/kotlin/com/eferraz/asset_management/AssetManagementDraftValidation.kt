package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory

internal fun validateAssetDraft(d: AssetDraft): AssetFormErrors {
    var e = AssetFormErrors()
    if (d.issuer == null) e = e.copy(issuer = "Selecione um emissor")
    if (d.brokerage == null) e = e.copy(brokerage = "Selecione uma corretora")
    when (d.category) {
        InvestmentCategory.FIXED_INCOME -> e = e.merge(validateFixedIncomeDraft(d))
        InvestmentCategory.VARIABLE_INCOME -> e = e.merge(validateVariableIncomeDraft(d))
        InvestmentCategory.INVESTMENT_FUND -> e = e.merge(validateFundDraft(d))
    }
    return e
}

private fun AssetFormErrors.merge(other: AssetFormErrors) = copy(
    issuer = other.issuer ?: issuer,
    brokerage = other.brokerage ?: brokerage,
    fixedType = other.fixedType ?: fixedType,
    fixedSubType = other.fixedSubType ?: fixedSubType,
    fixedExpiration = other.fixedExpiration ?: fixedExpiration,
    fixedYield = other.fixedYield ?: fixedYield,
    fixedCdi = other.fixedCdi ?: fixedCdi,
    fixedLiquidity = other.fixedLiquidity ?: fixedLiquidity,
    variableType = other.variableType ?: variableType,
    variableTicker = other.variableTicker ?: variableTicker,
    cnpj = other.cnpj ?: cnpj,
    fundName = other.fundName ?: fundName,
    fundType = other.fundType ?: fundType,
    fundLiquidity = other.fundLiquidity ?: fundLiquidity,
    fundLiquidityDays = other.fundLiquidityDays ?: fundLiquidityDays,
    fundExpiration = other.fundExpiration ?: fundExpiration,
)

private fun validateFixedIncomeDraft(d: AssetDraft): AssetFormErrors {
    val expRaw = d.fixedExpiration.orEmpty()
    val yieldRaw = d.fixedYield.orEmpty()
    val cdiRaw = d.fixedCdi.orEmpty()
    var e = AssetFormErrors()
    if (d.fixedType == null) e = e.copy(fixedType = "Obrigatório")
    if (d.fixedSubType == null) e = e.copy(fixedSubType = "Obrigatório")
    if (expRaw.isBlank()) e = e.copy(fixedExpiration = "Obrigatório")
    if (yieldRaw.isBlank()) e = e.copy(fixedYield = "Obrigatório")
    if (d.fixedLiquidity == null) e = e.copy(fixedLiquidity = "Obrigatório")
    if (expRaw.isNotBlank() && localDateFromIsoDateDigits(expRaw) == null) e = e.copy(fixedExpiration = "Data inválida")
    if (yieldRaw.isNotBlank() && yieldRaw.toDoubleOrNull() == null) e = e.copy(fixedYield = "Número inválido")
    if (cdiRaw.isNotBlank() && cdiRaw.toDoubleOrNull() == null) e = e.copy(fixedCdi = "Número inválido")
    return e
}

private fun validateVariableIncomeDraft(d: AssetDraft): AssetFormErrors {
    var e = AssetFormErrors()
    if (d.variableType == null) e = e.copy(variableType = "Obrigatório")
    if (d.variableTicker.orEmpty().isBlank()) e = e.copy(variableTicker = "Obrigatório")
    return e
}

private fun validateFundDraft(d: AssetDraft): AssetFormErrors {
    val fundExp = d.fundExpiration.orEmpty()
    var e = AssetFormErrors()
    if (d.fundName.orEmpty().isBlank()) e = e.copy(fundName = "Obrigatório")
    if (d.fundType == null) e = e.copy(fundType = "Obrigatório")
    if (d.fundLiquidity == null) e = e.copy(fundLiquidity = "Obrigatório")
    val days = d.fundLiquidityDays.orEmpty().toIntOrNull()
    if (days == null || days <= 0) e = e.copy(fundLiquidityDays = "Valor inválido")
    if (fundExp.isNotBlank() && localDateFromIsoDateDigits(fundExp) == null) e = e.copy(fundExpiration = "Data inválida")
    return e
}
