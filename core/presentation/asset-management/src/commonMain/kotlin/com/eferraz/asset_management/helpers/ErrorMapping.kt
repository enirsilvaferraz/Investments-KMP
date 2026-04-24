package com.eferraz.asset_management.helpers

import com.eferraz.asset_management.vm.UiState
import com.eferraz.entities.assets.InvestmentCategory

/**
 * Aplica [ValidateException.messages] (chaves do [com.eferraz.usecases.cruds.UpsertAssetUseCase]
 * e de emissor/corretora) nos campos `*Error`.
 * Chave inexistente → `null` nesse alvo; erros de campo de categorias fora de [s.category] ignoram-se.
 */
internal fun Map<String, String>.remoteFieldErrorsOn(s: UiState) = s.withClearedFieldErrors().run {

    val common = copy(
        issuerError = this@remoteFieldErrorsOn["issuer"],
        brokerageError = this@remoteFieldErrorsOn["brokerage"],
    )

    when (s.category) {

        InvestmentCategory.FIXED_INCOME -> common.copy(
            fixedExpirationError = this@remoteFieldErrorsOn["expirationDate"],
            fixedYieldError = this@remoteFieldErrorsOn["contractedYield"],
            fixedCdiError = this@remoteFieldErrorsOn["cdiRelativeYield"],
        )

        InvestmentCategory.VARIABLE_INCOME -> common.copy(
            variableTickerError = this@remoteFieldErrorsOn["ticker"] ?: this@remoteFieldErrorsOn["assetName"],
            cnpjError = this@remoteFieldErrorsOn["cnpj"],
        )

        InvestmentCategory.INVESTMENT_FUND -> common.copy(
            fundNameError = this@remoteFieldErrorsOn["name"],
            fundLiquidityDaysError = this@remoteFieldErrorsOn["liquidityDays"],
            fundExpirationError = this@remoteFieldErrorsOn["expirationDate"],
        )
    }
}
