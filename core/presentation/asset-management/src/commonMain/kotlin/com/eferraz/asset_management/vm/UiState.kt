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
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.entities.transactions.VariableIncomeTransaction

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
    val transactionDrafts: List<TransactionDraftUi> = emptyList(),
    val transactionDraftError: String? = null,
    val focusedInvalidRowIndex: Int? = null,
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

@Immutable
internal data class TransactionDraftUi(
    val id: Long? = null,
    val isNew: Boolean = false,
    val dateDigits: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val quantity: String = "",
    val unitPrice: String = "",
    val totalValue: String = "",
    val observations: String = "",
    val inlineError: String? = null,
) {
    internal companion object {
        internal fun fromDomain(value: AssetTransaction): TransactionDraftUi {
            val quantity = if (value is VariableIncomeTransaction) value.quantity.toString() else ""
            val unitPrice = if (value is VariableIncomeTransaction) value.unitPrice.toString() else ""
            val total = when (value) {
                is VariableIncomeTransaction -> value.totalValue
                is FixedIncomeTransaction -> value.totalValue
                is FundsTransaction -> value.totalValue
            }

            return TransactionDraftUi(
                id = value.id,
                isNew = false,
                dateDigits = value.date.toString().replace("-", ""),
                type = value.type,
                quantity = quantity,
                unitPrice = unitPrice,
                totalValue = total.toString(),
                observations = value.observations.orEmpty(),
            )
        }
    }
}

internal fun UiState.hasAnyInvalidDraft(): Boolean = transactionDrafts.any { it.inlineError != null }