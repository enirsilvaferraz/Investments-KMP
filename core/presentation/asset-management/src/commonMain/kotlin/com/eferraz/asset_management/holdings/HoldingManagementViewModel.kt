package com.eferraz.asset_management.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.UpsertAssetHoldingUseCase
import com.eferraz.usecases.exceptions.ValidateException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HoldingManagementViewModel(
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,
) : ViewModel() {

    internal val state: StateFlow<HoldingManagementUiState> field = MutableStateFlow(HoldingManagementUiState())

    internal fun dispatch(event: HoldingManagementEvents) = when (event) {
        is HoldingManagementEvents.ScreenEntered -> loadInitialState(event.holdingId)
        is HoldingManagementEvents.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }
        HoldingManagementEvents.Save -> onSave()
    }

    private fun loadInitialState(holdingId: Long?) = viewModelScope.launch {

        val brokerages = async { getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty() }
        val holding = async { holdingId?.let { getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(it)).getOrNull() } }

        val base = holding.await()

        state.update {
            HoldingManagementUiState(
                holding = base,
                brokerage = base?.brokerage,
                brokerages = brokerages.await(),
            )
        }
    }

    private fun onSave() = viewModelScope.launch {

        val holding = state.value.holding ?: return@launch
        if (state.value.isSaving || state.value.hasAnyFieldError()) return@launch

        state.update { it.copy(isSaving = true) }

        val brokerage = state.value.brokerage!!
        val updatedHolding = holding.copy(brokerage = brokerage)

        upsertAssetHoldingUseCase(UpsertAssetHoldingUseCase.Param(updatedHolding))
            .onSuccess {
                state.update { it.copy(isSaving = false, isCompleted = true) }
            }
            .onFailure { error ->
                when (error) {
                    is ValidateException -> state.update { it.copy(brokerageError = error.messages["brokerage"], isSaving = false) }
                    else -> state.update { it.copy(isSaving = false) }
                }
            }
    }
}
