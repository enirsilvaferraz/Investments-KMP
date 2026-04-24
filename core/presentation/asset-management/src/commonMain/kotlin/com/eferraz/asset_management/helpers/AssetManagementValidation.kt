package com.eferraz.asset_management.helpers

import com.eferraz.asset_management.vm.UiState
import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.InvestmentCategory

internal fun UiState.validateUiState(): UiState {

    val withCommon = withClearedFieldErrors().copy(
        issuerError = if (issuer == null) "Obrigatório" else null,
        brokerageError = if (brokerage == null) "Obrigatório" else null,
    )

    return when (category) {
        InvestmentCategory.FIXED_INCOME -> withCommon.validateFixedIncome()
        InvestmentCategory.VARIABLE_INCOME -> withCommon.validateVariableIncome()
        InvestmentCategory.INVESTMENT_FUND -> withCommon.validateFund()
    }
}

private fun UiState.validateFixedIncome(): UiState = copy(
    fixedTypeError = if (fixedType == null) "Obrigatório" else null,
    fixedSubTypeError = if (fixedSubType == null) "Obrigatório" else null,
    fixedExpirationError = if (fixedExpiration.isNullOrBlank()) "Obrigatório" else if (localDateFromIsoDateDigits(fixedExpiration) == null) "Data inválida" else null,
    fixedYieldError = if (fixedYield.isNullOrBlank()) "Obrigatório" else if (fixedYield.toDoubleOrNull() == null) "Número inválido" else null,
    fixedCdiError = if (fixedCdi.isNullOrBlank().not() && fixedCdi.toDoubleOrNull() == null) "Número inválido" else null,
    fixedLiquidityError = if (fixedLiquidity == null) "Obrigatório" else null,
)

private fun UiState.validateVariableIncome(): UiState = copy(
    variableTypeError = if (variableType == null) "Obrigatório" else null,
    variableTickerError = if (variableTicker.orEmpty().isBlank()) "Obrigatório" else null,
    cnpjError = if (variableCnpj.isNullOrBlank().not() && runCatching { CNPJ(this@validateVariableIncome.variableCnpj) }.isFailure) "CNPJ inválido" else null,
)

private fun UiState.validateFund(): UiState = copy(
    fundNameError = if (fundName.orEmpty().isBlank()) "Obrigatório" else null,
    fundTypeError = if (fundType == null) "Obrigatório" else null,
    fundLiquidityError = if (fundLiquidity == null) "Obrigatório" else null,
    fundLiquidityDaysError = if ((fundLiquidityDays?.toIntOrNull() ?: -1) <= 0) "Valor inválido" else null,
    fundExpirationError = if (localDateFromIsoDateDigits(fundExpiration) == null) "Data inválida" else null
)
