package com.eferraz.presentation.features.transactions

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.TransactionType

internal sealed class TransactionIntent {

    data class LoadTransactions(val holding: AssetHolding) : TransactionIntent()

    data class UpdateTransactionType(val type: TransactionType?) : TransactionIntent()

    data class UpdateDate(val date: String) : TransactionIntent()

    data class UpdateQuantity(val quantity: String) : TransactionIntent()

    data class UpdateUnitPrice(val unitPrice: String) : TransactionIntent()

    data class UpdateTotalValue(val totalValue: String) : TransactionIntent()

    data object SaveTransaction : TransactionIntent()

    data object ClearForm : TransactionIntent()

    data object ClearSelection : TransactionIntent()
}

