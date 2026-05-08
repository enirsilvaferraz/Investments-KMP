package com.eferraz.asset_management.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.usecases.DeleteTransactionUseCase
import com.eferraz.usecases.GetTransactionsByHoldingUseCase
import com.eferraz.usecases.SaveTransactionUseCase
import com.eferraz.usecases.cruds.GetCurrentDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TransactionFormViewModel(
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    internal val state: StateFlow<TransactionFormUiState> field = MutableStateFlow(TransactionFormUiState(InvestmentCategory.FIXED_INCOME))

    internal fun dispatch(event: TransactionFormEvent) = when (event) {

        is TransactionFormEvent.ChangeCategory ->
            changeCategory(event.category)

        is TransactionFormEvent.LoadData ->
            loadData(event.holding)

        TransactionFormEvent.AddTransactionDraft ->
            addTransactionDraft()

        is TransactionFormEvent.DraftTransactionDateChanged ->
            updateDraft(event.index) { it.copy(dateDigits = dateToDigits(event.raw)) }

        is TransactionFormEvent.DraftTransactionTypeChanged ->
            updateDraft(event.index) { it.copy(type = event.type) }

        is TransactionFormEvent.DraftTransactionQuantityChanged ->
            updateDraft(event.index) { it.copy(quantity = event.value) }

        is TransactionFormEvent.DraftTransactionUnitPriceChanged ->
            updateDraft(event.index) { it.copy(unitPrice = event.value) }

        is TransactionFormEvent.DraftTransactionTotalValueChanged ->
            updateDraft(event.index) { it.copy(totalValue = event.value) }

        is TransactionFormEvent.DraftTransactionObservationChanged ->
            updateDraft(event.index) { it.copy(observations = event.value) }

        is TransactionFormEvent.DraftTransactionDeleteClicked ->
            deleteDraft(event.index)
    }

    private fun loadData(holding: AssetHolding) = viewModelScope.launch {

        getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(holding))
            .onSuccess { transactions ->
                state.update {
                    it.copy(
                        category = holding.asset.category,
                        transactions = transactions.map(TransactionDraftUi::fromDomain)
                    )
                }
            }
    }

    internal fun changeCategory(category: InvestmentCategory) {
        state.update { current ->
            current.copy(category = category, transactions = emptyList())
        }
    }

    internal suspend fun persistAll(holding: AssetHolding, category: InvestmentCategory) {

        val transactions = state.value.transactions
            .mapNotNull { draft -> draft.toDomainTransaction(holding, category) }

        transactions.forEach { transaction ->
            saveTransactionUseCase(SaveTransactionUseCase.Param(transaction)).getOrThrow().let { }
        }
    }

    private fun addTransactionDraft() = viewModelScope.launch {

        val currentDate = getCurrentDateUseCase(Unit).getOrThrow().toString().replace("-", "")
        val blank = TransactionDraftUi(isNew = true, dateDigits = currentDate, category = state.value.category)

        state.update {
            it.copy(transactions = (it.transactions + blank))
        }
    }

    private fun updateDraft(index: Int, update: (TransactionDraftUi) -> TransactionDraftUi) = state.update { current ->
        val draft = update(current.transactions[index])
        current.copy(transactions = current.transactions.toMutableList().apply { this[index] = draft })
    }

    private fun deleteDraft(index: Int) = viewModelScope.launch {

        val draft = state.value.transactions[index]
        if (draft.id != null) deleteTransactionUseCase(DeleteTransactionUseCase.Param(draft.id)).getOrThrow()

        state.update {
            it.copy(transactions = it.transactions.filterIndexed { rowIndex, _ -> rowIndex != index })
        }
    }
}
