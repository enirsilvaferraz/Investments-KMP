package com.eferraz.asset_management.transactions

import androidx.compose.runtime.Immutable
import com.eferraz.asset_management.helpers.localDateFromIsoDateDigits
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.entities.transactions.VariableIncomeTransaction

@Immutable
internal data class TransactionManagementUiState(
    val holding: AssetHolding? = null,
    val initialSnapshot: List<TransactionDraftUi> = emptyList(),
    val transactions: List<TransactionDraftUi> = initialSnapshot,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
) {
    internal val assetClass: AssetClass
        get() = holding?.asset?.assetClass ?: AssetClass.FIXED_INCOME

    internal val isDirty: Boolean
        get() = initialSnapshot != transactions

    internal val hasAnyFieldError: Boolean
        get() = transactions.any { it.hasAnyFieldError() }
}

@Immutable
internal data class TransactionDraftUi(
    val id: Long? = null,
    val assetClass: AssetClass,
    val isNew: Boolean = false,
    val dateDigits: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val quantity: String = "",
    val unitPrice: String = "",
    val totalValue: String = "",
    val observations: String = "",
) {

    val dateError: Boolean
        get() = localDateFromIsoDateDigits(dateDigits) == null

    val quantityError: Boolean
        get() = assetClass == AssetClass.VARIABLE_INCOME && quantity.toDoubleOrNull() == null

    val unitPriceError: Boolean
        get() = assetClass == AssetClass.VARIABLE_INCOME && unitPrice.toDoubleOrNull() == null

    val totalValueError: Boolean
        get() = assetClass != AssetClass.VARIABLE_INCOME && totalValue.toDoubleOrNull() == null

    internal companion object {

        internal fun fromDomain(value: AssetTransaction, assetClass: AssetClass): TransactionDraftUi =
            TransactionDraftUi(
                id = value.id,
                isNew = false,
                dateDigits = value.date.toString().replace("-", ""),
                type = value.type,
                quantity = if (value is VariableIncomeTransaction) value.quantity.toString() else "",
                unitPrice = if (value is VariableIncomeTransaction) value.unitPrice.toString() else "",
                totalValue = value.totalValue.toString(),
                observations = value.observations.orEmpty(),
                assetClass = assetClass,
            )
    }

    internal fun toDomainTransaction(assetClass: AssetClass): AssetTransaction? {

        val date = localDateFromIsoDateDigits(dateDigits) ?: return null
        val draftId = id ?: 0L

        return when (assetClass) {

            AssetClass.VARIABLE_INCOME -> VariableIncomeTransaction(
                id = draftId,
                date = date,
                type = type,
                quantity = quantity.toDouble(),
                unitPrice = unitPrice.toDouble(),
                observations = observations.ifBlank { null },
            )

            AssetClass.FIXED_INCOME -> FixedIncomeTransaction(
                id = draftId,
                date = date,
                type = type,
                totalValue = totalValue.toDouble(),
                observations = observations.ifBlank { null },
            )

            AssetClass.INVESTMENT_FUND -> FundsTransaction(
                id = draftId,
                date = date,
                type = type,
                totalValue = totalValue.toDouble(),
                observations = observations.ifBlank { null },
            )
        }
    }
}

internal fun TransactionDraftUi.hasAnyFieldError(): Boolean =
    dateError || quantityError || unitPriceError || totalValueError

// TODO DT -> Repensar a estrategia de valor unitario e total das categorias de transação
internal fun TransactionDraftUi.syncVariableIncomeTotal(): TransactionDraftUi {
    val qty = quantity.toDoubleOrNull()
    val price = unitPrice.toDoubleOrNull()
    return if (qty != null && price != null) {
        copy(totalValue = (qty * price).toString())
    } else {
        this
    }
}