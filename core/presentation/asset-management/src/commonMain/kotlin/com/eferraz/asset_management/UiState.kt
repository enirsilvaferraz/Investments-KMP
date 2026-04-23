package com.eferraz.asset_management

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage

/**
 * Estado do ecrã de gestão de activos: listas, fluxo (guardar, navegação) e campos de formulário unificados
 * (incl. mensagens de validação em campos, [validateUiState]).
 */
@Immutable
internal data class UiState(
    val issuers: List<Issuer> = emptyList(),
    val brokerages: List<Brokerage> = emptyList(),
    val isSaving: Boolean = false,
    val navigateAway: Boolean = false,
    val category: InvestmentCategory = InvestmentCategory.FIXED_INCOME,
    val issuer: Issuer? = null,
    val brokerage: Brokerage? = null,
    val observations: String? = null,
    val issuerError: String? = null,
    val brokerageError: String? = null,
    val fixedTypeError: String? = null,
    val fixedSubTypeError: String? = null,
    val fixedExpirationError: String? = null,
    val fixedYieldError: String? = null,
    val fixedCdiError: String? = null,
    val fixedLiquidityError: String? = null,
    val variableTypeError: String? = null,
    val variableTickerError: String? = null,
    val cnpjError: String? = null,
    val fundNameError: String? = null,
    val fundTypeError: String? = null,
    val fundLiquidityError: String? = null,
    val fundLiquidityDaysError: String? = null,
    val fundExpirationError: String? = null,
    val fixedType: FixedIncomeAssetType? = null,
    val fixedSubType: FixedIncomeSubType? = null,
    val fixedExpiration: String? = null,
    val fixedYield: String? = null,
    val fixedCdi: String? = null,
    val fixedLiquidity: Liquidity? = null,
    val variableName: String? = null,
    val variableType: VariableIncomeAssetType? = null,
    val variableTicker: String? = null,
    val variableCnpj: String? = null,
    val fundName: String? = null,
    val fundType: InvestmentFundAssetType? = null,
    val fundLiquidity: Liquidity? = null,
    val fundLiquidityDays: String? = null,
    val fundExpiration: String? = null,
)

internal fun UiState.hasAnyFieldError(): Boolean =
    issuerError != null || brokerageError != null ||
        fixedTypeError != null || fixedSubTypeError != null || fixedExpirationError != null ||
        fixedYieldError != null || fixedCdiError != null || fixedLiquidityError != null ||
        variableTypeError != null || variableTickerError != null || cnpjError != null ||
        fundNameError != null || fundTypeError != null || fundLiquidityError != null ||
        fundLiquidityDaysError != null || fundExpirationError != null

/**
 * Todos os erros de campo a `null` (ex. antes de `runUpsert` ou ao trocar categoria).
 */
internal fun UiState.withClearedFieldErrors() = copy(
    issuerError = null,
    brokerageError = null,
    fixedTypeError = null,
    fixedSubTypeError = null,
    fixedExpirationError = null,
    fixedYieldError = null,
    fixedCdiError = null,
    fixedLiquidityError = null,
    variableTypeError = null,
    variableTickerError = null,
    cnpjError = null,
    fundNameError = null,
    fundTypeError = null,
    fundLiquidityError = null,
    fundLiquidityDaysError = null,
    fundExpirationError = null,
)

/**
 * Aplica [ValidateException.messages] (chaves do [com.eferraz.usecases.UpsertInvestmentAssetUseCase]
 * e de emissor/corretora) nos campos `*Error`.
 * Chave inexistente → `null` nesse alvo; erros de campo de categorias fora de [s.category] ignoram-se.
 */
internal fun Map<String, String>.remoteFieldErrorsOn(s: UiState) =
    s.withClearedFieldErrors().run {
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

            InvestmentCategory.VARIABLE_INCOME -> {
                val tn = this@remoteFieldErrorsOn["ticker"] ?: this@remoteFieldErrorsOn["assetName"]
                common.copy(
                    variableTickerError = tn,
                    cnpjError = this@remoteFieldErrorsOn["cnpj"],
                )
            }

            InvestmentCategory.INVESTMENT_FUND -> common.copy(
                fundNameError = this@remoteFieldErrorsOn["name"],
                fundLiquidityDaysError = this@remoteFieldErrorsOn["liquidityDays"],
                fundExpirationError = this@remoteFieldErrorsOn["expirationDate"],
            )
        }
    }

internal fun UiState.withCategoryPreservingIssuerAndObs(category: InvestmentCategory) = withClearedFieldErrors().copy(
    category = category,
    issuer = issuer,
    brokerage = brokerage,
    observations = observations,
    fixedType = null,
    fixedSubType = null,
    fixedExpiration = null,
    fixedYield = null,
    fixedCdi = null,
    fixedLiquidity = null,
    variableName = null,
    variableType = null,
    variableTicker = null,
    variableCnpj = null,
    fundName = null,
    fundType = null,
    fundLiquidity = null,
    fundLiquidityDays = null,
    fundExpiration = null,
)
