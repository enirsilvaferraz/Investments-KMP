package com.eferraz.asset_management.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.usecases.DeleteTransactionUseCase
import com.eferraz.usecases.GetTransactionsByHoldingUseCase
import com.eferraz.usecases.SaveTransactionUseCase
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
import com.eferraz.usecases.cruds.GetCurrentDateUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TransactionManagementViewModel(
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    internal val state: StateFlow<TransactionManagementUiState> field = MutableStateFlow(TransactionManagementUiState())

    internal fun dispatch(event: TransactionManagementEvents) = when (event) {

        is TransactionManagementEvents.ScreenEntered -> loadInitialState(event.holdingId)

        TransactionManagementEvents.Save -> onSave()
        TransactionManagementEvents.AddTransactionDraft -> addTransactionDraft()

        is TransactionManagementEvents.DraftTransactionDateChanged ->
            updateDraft(event.index) { it.copy(dateDigits = dateToDigits(event.raw)) }

        is TransactionManagementEvents.DraftTransactionTypeChanged ->
            updateDraft(event.index) { it.copy(type = event.type) }

        is TransactionManagementEvents.DraftTransactionQuantityChanged ->
            updateDraft(event.index) { it.copy(quantity = event.value) }

        is TransactionManagementEvents.DraftTransactionUnitPriceChanged ->
            updateDraft(event.index) { it.copy(unitPrice = event.value) }

        is TransactionManagementEvents.DraftTransactionTotalValueChanged ->
            updateDraft(event.index) { it.copy(totalValue = event.value) }

        is TransactionManagementEvents.DraftTransactionObservationChanged ->
            updateDraft(event.index) { it.copy(observations = event.value) }

        is TransactionManagementEvents.DraftTransactionDeleteClicked ->
            deleteDraft(event.index)
    }

    private fun loadInitialState(holdingId: Long?) = viewModelScope.launch {

        val holding = async { holdingId?.let { getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(it)).getOrNull() } }
        val resolved = holding.await() ?: return@launch

        getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(resolved))
            .onSuccess { transactions ->
                state.update {
                    TransactionManagementUiState(
                        holding = resolved,
                        transactions = transactions.map(TransactionDraftUi::fromDomain),
                    )
                }
            }
    }

    private fun onSave() = viewModelScope.launch {

        val holding = state.value.holding ?: return@launch
        if (state.value.isSaving) return@launch

        state.update { it.copy(isSaving = true) }

        val transactions = state.value.transactions
            .mapNotNull { draft -> draft.toDomainTransaction(holding, state.value.category) }

        runCatching {
            transactions.forEach { transaction ->
                saveTransactionUseCase(SaveTransactionUseCase.Param(transaction)).getOrThrow()
            }
        }.onSuccess {
            state.update { it.copy(isSaving = false, isCompleted = true) }
        }.onFailure {
            state.update { it.copy(isSaving = false) }
        }
    }

    private fun addTransactionDraft() = viewModelScope.launch {

        val currentDate = getCurrentDateUseCase(Unit).getOrThrow().toString().replace("-", "")
        val blank = TransactionDraftUi(isNew = true, dateDigits = currentDate, category = state.value.category)

        state.update { it.copy(transactions = it.transactions + blank) }
    }

    private fun updateDraft(index: Int, update: (TransactionDraftUi) -> TransactionDraftUi) =
        state.update { current ->
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
