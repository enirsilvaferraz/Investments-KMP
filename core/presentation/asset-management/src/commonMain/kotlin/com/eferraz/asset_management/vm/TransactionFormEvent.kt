package com.eferraz.asset_management.vm

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.TransactionType

/**
 * Ações da tabela de transações — ponto de entrada único [TransactionFormViewModel.dispatch].
 */
internal sealed class TransactionFormEvent {

    internal data class LoadData(val holding: AssetHolding) : TransactionFormEvent()
    internal data class ChangeCategory(val category: InvestmentCategory) : TransactionFormEvent()
    internal data object AddTransactionDraft : TransactionFormEvent()

    internal data class DraftTransactionDateChanged(val index: Int, val raw: String) : TransactionFormEvent()
    internal data class DraftTransactionTypeChanged(val index: Int, val type: TransactionType) : TransactionFormEvent()
    internal data class DraftTransactionQuantityChanged(val index: Int, val value: String) : TransactionFormEvent()
    internal data class DraftTransactionUnitPriceChanged(val index: Int, val value: String) : TransactionFormEvent()
    internal data class DraftTransactionTotalValueChanged(val index: Int, val value: String) : TransactionFormEvent()
    internal data class DraftTransactionObservationChanged(val index: Int, val value: String) : TransactionFormEvent()
    internal data class DraftTransactionDeleteClicked(val index: Int) : TransactionFormEvent()
}
