package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory

/**
 * Aplica o resultado de um ramo categoria (apenas [UiState] com `*Error` preenchidos nesse conjunto) sobre o estado
 * alinhado, preferindo a mensagem mais específica quando a mesma célula for definida nos dois sítios.
 */
private fun mergeFieldErrors(base: UiState, branch: UiState) = base.copy(
    issuerError = branch.issuerError ?: base.issuerError,
    brokerageError = branch.brokerageError ?: base.brokerageError,
    fixedTypeError = branch.fixedTypeError ?: base.fixedTypeError,
    fixedSubTypeError = branch.fixedSubTypeError ?: base.fixedSubTypeError,
    fixedExpirationError = branch.fixedExpirationError ?: base.fixedExpirationError,
    fixedYieldError = branch.fixedYieldError ?: base.fixedYieldError,
    fixedCdiError = branch.fixedCdiError ?: base.fixedCdiError,
    fixedLiquidityError = branch.fixedLiquidityError ?: base.fixedLiquidityError,
    variableTypeError = branch.variableTypeError ?: base.variableTypeError,
    variableTickerError = branch.variableTickerError ?: base.variableTickerError,
    cnpjError = branch.cnpjError ?: base.cnpjError,
    fundNameError = branch.fundNameError ?: base.fundNameError,
    fundTypeError = branch.fundTypeError ?: base.fundTypeError,
    fundLiquidityError = branch.fundLiquidityError ?: base.fundLiquidityError,
    fundLiquidityDaysError = branch.fundLiquidityDaysError ?: base.fundLiquidityDaysError,
    fundExpirationError = branch.fundExpirationError ?: base.fundExpirationError,
)

internal fun validateUiState(s: UiState): UiState {
    val withCommon = s.withClearedFieldErrors().copy(
        issuerError = if (s.issuer == null) "Selecione um emissor" else null,
        brokerageError = if (s.brokerage == null) "Selecione uma corretora" else null,
    )
    return when (s.category) {
        InvestmentCategory.FIXED_INCOME -> mergeFieldErrors(withCommon, validateFixedIncome(s))
        InvestmentCategory.VARIABLE_INCOME -> mergeFieldErrors(withCommon, validateVariableIncome(s))
        InvestmentCategory.INVESTMENT_FUND -> mergeFieldErrors(withCommon, validateFund(s))
    }
}

private fun validateFixedIncome(s: UiState): UiState {
    val expRaw = s.fixedExpiration.orEmpty()
    val yieldRaw = s.fixedYield.orEmpty()
    val cdiRaw = s.fixedCdi.orEmpty()
    var fixedExpirationError: String? = if (expRaw.isBlank()) "Obrigatório" else null
    if (expRaw.isNotBlank() && localDateFromIsoDateDigits(expRaw) == null) fixedExpirationError = "Data inválida"
    var fixedYieldError: String? = if (yieldRaw.isBlank()) "Obrigatório" else null
    if (yieldRaw.isNotBlank() && yieldRaw.toDoubleOrNull() == null) fixedYieldError = "Número inválido"
    val fixedCdiError =
        if (cdiRaw.isNotBlank() && cdiRaw.toDoubleOrNull() == null) "Número inválido" else null
    return s.withClearedFieldErrors().copy(
        fixedTypeError = if (s.fixedType == null) "Obrigatório" else null,
        fixedSubTypeError = if (s.fixedSubType == null) "Obrigatório" else null,
        fixedExpirationError = fixedExpirationError,
        fixedYieldError = fixedYieldError,
        fixedCdiError = fixedCdiError,
        fixedLiquidityError = if (s.fixedLiquidity == null) "Obrigatório" else null,
    )
}

private fun validateVariableIncome(s: UiState) = s.withClearedFieldErrors().copy(
    variableTypeError = if (s.variableType == null) "Obrigatório" else null,
    variableTickerError = if (s.variableTicker.orEmpty().isBlank()) "Obrigatório" else null,
)

private fun validateFund(s: UiState): UiState {
    val fundExp = s.fundExpiration.orEmpty()
    val fundExpirationError = if (fundExp.isNotBlank() && localDateFromIsoDateDigits(fundExp) == null) {
        "Data inválida"
    } else {
        null
    }
    val days = s.fundLiquidityDays.orEmpty().toIntOrNull()
    val fundLiquidityDaysError = if (days == null || days <= 0) "Valor inválido" else null
    return s.withClearedFieldErrors().copy(
        fundNameError = if (s.fundName.orEmpty().isBlank()) "Obrigatório" else null,
        fundTypeError = if (s.fundType == null) "Obrigatório" else null,
        fundLiquidityError = if (s.fundLiquidity == null) "Obrigatório" else null,
        fundLiquidityDaysError = fundLiquidityDaysError,
        fundExpirationError = fundExpirationError,
    )
}
