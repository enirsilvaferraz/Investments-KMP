package com.eferraz.asset_management.assets

import androidx.compose.runtime.Immutable
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner

@Immutable
internal data class AssetManagementUiState(

    val asset: Asset? = null,
    val issuers: List<Issuer> = emptyList(),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,

    val brokerage: Brokerage? = null,
    val brokerages: List<Brokerage> = emptyList(),
    val brokerageError: String? = null,
    val holdingId: Long? = null,
    val owner: Owner? = null,

    val assetClass: AssetClass = AssetClass.FIXED_INCOME,

    val observations: String? = null,
    val b3Identifier: String? = null,
    val issuer: Issuer? = null,
    val yieldIndexer: YieldIndexer? = null,
    val fixedType: FixedIncomeAssetType? = null,
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

    val issuerError: String? = null,
    val yieldIndexerError: String? = null,
    val fixedTypeError: String? = null,
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
) {

    /**
     * Todos os erros de campo a `null` (ex. antes de `runUpsert` ou ao trocar classe).
     */
    internal fun withClearedFieldErrors() = copy(
        issuerError = null,
        brokerageError = null,
        yieldIndexerError = null,
        fixedTypeError = null,
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
                yieldIndexerError != null || fixedTypeError != null || fixedExpirationError != null ||
                fixedYieldError != null || fixedCdiError != null || fixedLiquidityError != null ||
                variableTypeError != null || variableTickerError != null || cnpjError != null ||
                fundNameError != null || fundTypeError != null || fundLiquidityError != null ||
                fundLiquidityDaysError != null || fundExpirationError != null
}
