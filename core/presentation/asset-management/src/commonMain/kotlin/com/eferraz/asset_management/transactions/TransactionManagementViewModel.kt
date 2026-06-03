package com.eferraz.asset_management.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.entities.assets.AssetClass
import com.eferraz.usecases.DeleteTransactionUseCase
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
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
) : ViewModel() {

    internal val state: StateFlow<TransactionManagementUiState> field = MutableStateFlow(TransactionManagementUiState())

    internal fun dispatch(event: TransactionManagementEvents) = when (event) {

        is TransactionManagementEvents.ScreenEntered ->
            loadInitialState(event.holdingId)

        is TransactionManagementEvents.AddTransactionDraft ->
            addTransactionDraft()

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

        is TransactionManagementEvents.DraftTransactionDeleteClicked ->
            deleteDraft(event.index)

        is TransactionManagementEvents.Save ->
            onSave()
    }

    private fun loadInitialState(holdingId: Long?) = viewModelScope.launch {

        val holding = async { holdingId?.let { getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(it)).getOrNull() } }
        val resolved = holding.await() ?: return@launch

        val sorted = resolved.transactions.sortedBy { it.date }
        state.update {
            TransactionManagementUiState(
                holding = resolved,

                // TODO DT -> Mover o sorte para o UseCase?
                initialSnapshot = sorted.map { TransactionDraftUi.fromDomain(it, resolved.asset.assetClass) },
            )
        }
    }

    private fun onSave() = viewModelScope.launch {

        val current = state.value

        val holding = current.holding ?: return@launch
        if (current.isSaving || !current.isDirty || current.hasAnyFieldError) return@launch

        state.update { it.copy(isSaving = true) }

        // TODO fazer upsert em lote -> consultar no banco de dados e fazer essa operação no usecase
        val removeIds = current.initialSnapshot.mapNotNull { it.id }.toSet() - current.transactions.mapNotNull { it.id }.toSet()
        val upserts = current.transactions.mapNotNull { it.toDomainTransaction(current.assetClass) }

        runCatching {

            // TODO Debito tecnico -> Fazer a persistencia dos dados de forma transacional

            removeIds.forEach { id ->
                deleteTransactionUseCase(DeleteTransactionUseCase.Param(holding, id)).getOrThrow()
            }

            upserts.forEach { transaction ->
                saveTransactionUseCase(SaveTransactionUseCase.Param(holding, transaction)).getOrThrow()
            }

        }.onSuccess {
            state.update { it.copy(isSaving = false, isCompleted = true) }
        }.onFailure {
            state.update { it.copy(isSaving = false) }
        }
    }

    private fun addTransactionDraft() = viewModelScope.launch {

        // TODO Débito Tecnico -> Nao usar o throw
        val currentDate = getCurrentDateUseCase(Unit).getOrThrow().toString().replace("-", "")

        // TODO Débito Técnico -> Nao trabalhar com dateDigits e sim com LocalDate
        val blank = TransactionDraftUi(isNew = true, dateDigits = currentDate, assetClass = state.value.assetClass)

        state.update { it.copy(transactions = it.transactions + blank) }
    }

    private fun updateDraft(index: Int, update: (TransactionDraftUi) -> TransactionDraftUi) =
        state.update { current ->
            var draft = update(current.transactions[index])
            if (current.assetClass == AssetClass.VARIABLE_INCOME) {
                draft = draft.syncVariableIncomeTotal()
            }
            current.copy(transactions = current.transactions.toMutableList().apply { this[index] = draft })
        }

    private fun deleteDraft(index: Int) =
        state.update {
            it.copy(transactions = it.transactions.filterIndexed { rowIndex, _ -> rowIndex != index })
        }
}
