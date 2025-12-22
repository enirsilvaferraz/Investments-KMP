package com.eferraz.presentation.features.transactions

import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.FixedIncomeTransaction
import com.eferraz.entities.FundsTransaction
import com.eferraz.entities.TransactionType
import com.eferraz.entities.VariableIncomeTransaction
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import kotlinx.datetime.LocalDate

internal data class TransactionRow(
    val viewData: ViewData,
    val formatted: Formatted,
)

internal data class ViewData(
    val type: String,
    val date: LocalDate,
    val quantity: Double?,
    val unitPrice: Double?,
    val totalValue: Double,
)

internal data class Formatted(
    val type: String,
    val date: String,
    val quantity: String?,
    val unitPrice: String?,
    val totalValue: String,
)

internal fun AssetTransaction.toTransactionRow(): TransactionRow {
    val typeString = when (this.type) {
        TransactionType.PURCHASE -> "Compra"
        TransactionType.SALE -> "Venda"
    }

    val dateString = formatDate(this.date)

    return when (this) {
        is VariableIncomeTransaction -> {
            TransactionRow(
                viewData = ViewData(
                    type = typeString,
                    date = this.date,
                    quantity = this.quantity,
                    unitPrice = this.unitPrice,
                    totalValue = this.totalValue
                ),
                formatted = Formatted(
                    type = typeString,
                    date = dateString,
                    quantity = formatQuantity(this.quantity),
                    unitPrice = this.unitPrice.currencyFormat(),
                    totalValue = this.totalValue.currencyFormat()
                )
            )
        }

        is FixedIncomeTransaction -> {
            TransactionRow(
                viewData = ViewData(
                    type = typeString,
                    date = this.date,
                    quantity = null,
                    unitPrice = null,
                    totalValue = this.totalValue
                ),
                formatted = Formatted(
                    type = typeString,
                    date = dateString,
                    quantity = null,
                    unitPrice = null,
                    totalValue = this.totalValue.currencyFormat()
                )
            )
        }

        is FundsTransaction -> {
            TransactionRow(
                viewData = ViewData(
                    type = typeString,
                    date = this.date,
                    quantity = null,
                    unitPrice = null,
                    totalValue = this.totalValue
                ),
                formatted = Formatted(
                    type = typeString,
                    date = dateString,
                    quantity = null,
                    unitPrice = null,
                    totalValue = this.totalValue.currencyFormat()
                )
            )
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val year = date.year.toString()
    return "$day/$month/$year"
}

private fun formatQuantity(quantity: Double): String {
    return quantity.toInt().toString()
}

