package com.eferraz.asset_management

import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.InvestmentCategory

internal fun validateUiState(s: UiState): UiState {

    val withCommon = s.withClearedFieldErrors().copy(
        issuerError = if (s.issuer == null) "Obrigatório" else null,
        brokerageError = if (s.brokerage == null) "Obrigatório" else null,
    )

    return when (s.category) {
        InvestmentCategory.FIXED_INCOME -> validateFixedIncome(withCommon)
        InvestmentCategory.VARIABLE_INCOME -> validateVariableIncome(withCommon)
        InvestmentCategory.INVESTMENT_FUND -> validateFund(withCommon)
    }
}

private fun validateFixedIncome(s: UiState): UiState = s.copy(
    fixedTypeError = if (s.fixedType == null) "Obrigatório" else null,
    fixedSubTypeError = if (s.fixedSubType == null) "Obrigatório" else null,
    fixedExpirationError = if (s.fixedExpiration.isNullOrBlank()) "Obrigatório" else if (localDateFromIsoDateDigits(s.fixedExpiration) == null) "Data inválida" else null,
    fixedYieldError = if (s.fixedYield.isNullOrBlank()) "Obrigatório" else if (s.fixedYield.toDoubleOrNull() == null) "Número inválido" else null,
    fixedCdiError = if (s.fixedCdi.isNullOrBlank().not() && s.fixedCdi.toDoubleOrNull() == null) "Número inválido" else null,
    fixedLiquidityError = if (s.fixedLiquidity == null) "Obrigatório" else null,
)

private fun validateVariableIncome(s: UiState): UiState = s.copy(
    variableTypeError = if (s.variableType == null) "Obrigatório" else null,
    variableTickerError = if (s.variableTicker.orEmpty().isBlank()) "Obrigatório" else null,
    cnpjError = if (s.variableCnpj.isNullOrBlank().not() && runCatching { CNPJ(s.variableCnpj) }.isFailure) "CNPJ inválido" else null,
)

private fun validateFund(s: UiState): UiState = s.copy(
    fundNameError = if (s.fundName.orEmpty().isBlank()) "Obrigatório" else null,
    fundTypeError = if (s.fundType == null) "Obrigatório" else null,
    fundLiquidityError = if (s.fundLiquidity == null) "Obrigatório" else null,
    fundLiquidityDaysError = if ((s.fundLiquidityDays?.toIntOrNull() ?: -1) <= 0) "Valor inválido" else null,
    fundExpirationError = if (localDateFromIsoDateDigits(s.fundExpiration) == null) "Data inválida" else null
)
