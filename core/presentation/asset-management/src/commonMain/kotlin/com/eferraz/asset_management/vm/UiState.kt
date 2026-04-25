package com.eferraz.asset_management.vm

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.transactions.AssetTransaction

@Immutable
internal data class UiState(
    val editingHoldingId: Long? = null,
    val editingAssetId: Long? = null,
    val issuers: List<Issuer> = emptyList(),
    val brokerages: List<Brokerage> = emptyList(),
    val isSaving: Boolean = false,
    val navigateAway: Boolean = false,
    val category: InvestmentCategory = InvestmentCategory.FIXED_INCOME,
    val issuer: Issuer? = null,
    val brokerage: Brokerage? = null,
    val transactions: List<AssetTransaction> = emptyList(),
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
) {

    /**
     * Todos os erros de campo a `null` (ex. antes de `runUpsert` ou ao trocar categoria).
     */
    internal fun withClearedFieldErrors() = copy(
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

    internal fun hasAnyFieldError(): Boolean =
        issuerError != null || brokerageError != null ||
                fixedTypeError != null || fixedSubTypeError != null || fixedExpirationError != null ||
                fixedYieldError != null || fixedCdiError != null || fixedLiquidityError != null ||
                variableTypeError != null || variableTickerError != null || cnpjError != null ||
                fundNameError != null || fundTypeError != null || fundLiquidityError != null ||
                fundLiquidityDaysError != null || fundExpirationError != null
}