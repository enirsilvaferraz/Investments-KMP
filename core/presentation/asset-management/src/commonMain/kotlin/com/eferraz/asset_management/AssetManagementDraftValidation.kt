package com.eferraz.asset_management

import com.eferraz.entities.assets.InvestmentCategory

/**
 * Combina erros de emissor/corretora com os de categoria: cada [UiState] traz um subconjunto de `*Error`;
 * valores não nulos em [category] prevalecem face a [withIssuerAndBrokerageErrors] quando o mesmo campo existe nos dois.
 */
private fun mergeFieldErrors(withIssuerAndBrokerageErrors: UiState, category: UiState) = withIssuerAndBrokerageErrors.copy(
    issuerError = category.issuerError ?: withIssuerAndBrokerageErrors.issuerError,
    brokerageError = category.brokerageError ?: withIssuerAndBrokerageErrors.brokerageError,
    fixedTypeError = category.fixedTypeError ?: withIssuerAndBrokerageErrors.fixedTypeError,
    fixedSubTypeError = category.fixedSubTypeError ?: withIssuerAndBrokerageErrors.fixedSubTypeError,
    fixedExpirationError = category.fixedExpirationError ?: withIssuerAndBrokerageErrors.fixedExpirationError,
    fixedYieldError = category.fixedYieldError ?: withIssuerAndBrokerageErrors.fixedYieldError,
    fixedCdiError = category.fixedCdiError ?: withIssuerAndBrokerageErrors.fixedCdiError,
    fixedLiquidityError = category.fixedLiquidityError ?: withIssuerAndBrokerageErrors.fixedLiquidityError,
    variableTypeError = category.variableTypeError ?: withIssuerAndBrokerageErrors.variableTypeError,
    variableTickerError = category.variableTickerError ?: withIssuerAndBrokerageErrors.variableTickerError,
    cnpjError = category.cnpjError ?: withIssuerAndBrokerageErrors.cnpjError,
    fundNameError = category.fundNameError ?: withIssuerAndBrokerageErrors.fundNameError,
    fundTypeError = category.fundTypeError ?: withIssuerAndBrokerageErrors.fundTypeError,
    fundLiquidityError = category.fundLiquidityError ?: withIssuerAndBrokerageErrors.fundLiquidityError,
    fundLiquidityDaysError = category.fundLiquidityDaysError ?: withIssuerAndBrokerageErrors.fundLiquidityDaysError,
    fundExpirationError = category.fundExpirationError ?: withIssuerAndBrokerageErrors.fundExpirationError,
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
