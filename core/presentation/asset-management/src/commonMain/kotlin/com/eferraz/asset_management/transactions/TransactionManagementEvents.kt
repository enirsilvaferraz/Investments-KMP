package com.eferraz.asset_management.transactions

import com.eferraz.entities.transactions.TransactionType

internal sealed class TransactionManagementEvents {

    data class ScreenEntered(val holdingId: Long?) : TransactionManagementEvents()

    data object Save : TransactionManagementEvents()
    data object AddTransactionDraft : TransactionManagementEvents()

    data class DraftTransactionDateChanged(val index: Int, val raw: String) : TransactionManagementEvents()
    data class DraftTransactionTypeChanged(val index: Int, val type: TransactionType) : TransactionManagementEvents()
    data class DraftTransactionQuantityChanged(val index: Int, val value: String) : TransactionManagementEvents()
    data class DraftTransactionUnitPriceChanged(val index: Int, val value: String) : TransactionManagementEvents()
    data class DraftTransactionTotalValueChanged(val index: Int, val value: String) : TransactionManagementEvents()
    data class DraftTransactionDeleteClicked(val index: Int) : TransactionManagementEvents()
}
