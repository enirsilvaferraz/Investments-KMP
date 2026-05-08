package com.eferraz.asset_management.vm

import androidx.compose.runtime.Immutable
import com.eferraz.asset_management.helpers.localDateFromIsoDateDigits
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.entities.transactions.VariableIncomeTransaction

@Immutable
internal data class TransactionFormUiState(
    val category: InvestmentCategory,
    val transactions: List<TransactionDraftUi> = emptyList(),
)

@Immutable
internal data class TransactionDraftUi(
    val id: Long? = null,
    val category: InvestmentCategory,
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
        get() = category == InvestmentCategory.VARIABLE_INCOME && quantity.toDoubleOrNull() == null

    val unitPriceError: Boolean
        get() = category == InvestmentCategory.VARIABLE_INCOME && unitPrice.toDoubleOrNull() == null

    val totalValueError: Boolean
        get() = category != InvestmentCategory.VARIABLE_INCOME && totalValue.toDoubleOrNull() == null

    internal companion object {

        internal fun fromDomain(value: AssetTransaction): TransactionDraftUi {

            return TransactionDraftUi(
                id = value.id,
                isNew = false,
                dateDigits = value.date.toString().replace("-", ""),
                type = value.type,
                quantity = if (value is VariableIncomeTransaction) value.quantity.toString() else "",
                unitPrice = if (value is VariableIncomeTransaction) value.unitPrice.toString() else "",
                totalValue = value.totalValue.toString(),
                observations = value.observations.orEmpty(),
                category = value.holding.asset.category
            )
        }
    }

    fun toDomainTransaction(
        holding: AssetHolding,
        category: InvestmentCategory,
    ): AssetTransaction? {

        val date = localDateFromIsoDateDigits(dateDigits) ?: return null
        val draftId = id ?: 0L

        return when (category) {

            InvestmentCategory.VARIABLE_INCOME -> {
                VariableIncomeTransaction(
                    id = draftId,
                    holding = holding,
                    date = date,
                    type = type,
                    quantity = quantity.toDouble(),
                    unitPrice = unitPrice.toDouble(),
                    observations = observations.ifBlank { null },
                )
            }

            InvestmentCategory.FIXED_INCOME -> {
                FixedIncomeTransaction(
                    id = draftId,
                    holding = holding,
                    date = date,
                    type = type,
                    totalValue = totalValue.toDouble(),
                    observations = observations.ifBlank { null },
                )
            }

            InvestmentCategory.INVESTMENT_FUND -> {
                FundsTransaction(
                    id = draftId,
                    holding = holding,
                    date = date,
                    type = type,
                    totalValue = totalValue.toDouble(),
                    observations = observations.ifBlank { null },
                )
            }
        }
    }
}

internal fun TransactionDraftUi.hasAnyFieldError(): Boolean =
    dateError || quantityError || unitPriceError || totalValueError
