package com.eferraz.asset_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.design_system.input.date.filterDateMaskDigits
import com.eferraz.usecases.UpsertInvestmentAssetUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.exceptions.ValidateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AssetManagementViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val upsertInvestmentAssetUseCase: UpsertInvestmentAssetUseCase,
) : ViewModel() {

    internal val state: StateFlow<UiState> field = MutableStateFlow(initialUiState())

    init {
        loadIssuersAndBrokerages()
    }

    internal fun dispatch(event: AssetManagementEvent) {
        when (event) {
            is AssetManagementEvent.CategoryChanged -> state.update { it.withCategoryPreservingIssuerAndObs(event.category).copy(saveError = null) }
            is AssetManagementEvent.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null, saveError = null) }
            is AssetManagementEvent.ObservationsChanged -> state.update { it.copy(observations = event.value, saveError = null) }
            is AssetManagementEvent.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null, saveError = null) }
            is AssetManagementEvent.FixedTypeChanged -> state.update { it.copy(fixedType = event.type, fixedTypeError = null, saveError = null) }
            is AssetManagementEvent.FixedSubTypeChanged -> state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null, saveError = null) }
            is AssetManagementEvent.FixedExpirationChanged -> state.update { it.copy(fixedExpiration = filterDateMaskDigits(event.raw), fixedExpirationError = null, saveError = null) }
            is AssetManagementEvent.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null, saveError = null) }
            is AssetManagementEvent.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null, saveError = null) }
            is AssetManagementEvent.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null, saveError = null) }
            is AssetManagementEvent.VariableTypeChanged -> state.update { it.copy(variableType = event.type, variableTypeError = null, saveError = null) }
            is AssetManagementEvent.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null, saveError = null) }
            is AssetManagementEvent.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null, saveError = null) }
            is AssetManagementEvent.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null, saveError = null) }
            is AssetManagementEvent.FundTypeChanged -> state.update { it.copy(fundType = event.type, fundTypeError = null, saveError = null) }
            is AssetManagementEvent.FundLiquidityDaysChanged -> state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null, saveError = null) }
            is AssetManagementEvent.FundExpirationChanged -> state.update { it.copy(fundExpiration = filterDateMaskDigits(event.raw), fundExpirationError = null, saveError = null) }
            AssetManagementEvent.Save -> onSave()
            AssetManagementEvent.RequestDismiss -> onRequestDismiss()
            AssetManagementEvent.ConfirmDiscard -> onConfirmDiscard()
            AssetManagementEvent.CancelDiscard -> onCancelDiscard()
            AssetManagementEvent.NavigationConsumed -> onNavigationConsumed()
        }
    }

    private fun loadIssuersAndBrokerages() {
        viewModelScope.launch {
            val issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty()
            val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
            state.update { it.copy(issuers = issuers, brokerages = brokerages) }
        }
    }

    private fun onNavigationConsumed() {
        if (state.value.navigateAway) {
            state.update { it.copy(navigateAway = false) }
        }
    }

    private fun onRequestDismiss() {
        if (state.value.formContentMatchesInitial()) {
            state.update { it.copy(navigateAway = true) }
        } else {
            state.update { it.copy(showDiscardDialog = true) }
        }
    }

    private fun onConfirmDiscard() {
        state.update { s ->
            initialUiState()
                .copy(issuers = s.issuers, brokerages = s.brokerages, showDiscardDialog = false, navigateAway = true)
        }
    }

    private fun onCancelDiscard() {
        state.update { it.copy(showDiscardDialog = false) }
    }

    private fun onSave() {
        val s = state.value
        if (s.isSaving) return

        val validated = validateUiState(s)

        val action: SaveAction = when {
            s.issuers.isEmpty() -> SaveAction.Set(
                s.copy(saveError = "Cadastre um emissor noutro ecrã antes de guardar."),
            )

            s.brokerages.isEmpty() -> SaveAction.Set(
                s.copy(saveError = "Cadastre uma corretora noutro ecrã antes de guardar."),
            )

            validated.hasAnyFieldError() -> SaveAction.Set(
                validated.copy(saveError = null),
            )

            else -> {
                val param = buildUpsertParam(s)
                if (param == null) {
                    SaveAction.Set(s.copy(saveError = "Dados incompletos."))
                } else {
                    SaveAction.RunUpsert(s, param)
                }
            }
        }
        when (action) {
            is SaveAction.Set -> state.update { action.ui }
            is SaveAction.RunUpsert -> runUpsert(action.ui, action.param)
        }
    }

    private fun runUpsert(ui: UiState, param: UpsertInvestmentAssetUseCase.Param) {
        viewModelScope.launch {
            state.update { it.withClearedFieldErrors().copy(isSaving = true, saveError = null) }
            val result = upsertInvestmentAssetUseCase(param)
            result.fold(
                onSuccess = {
                    state.update { it.copy(isSaving = false, navigateAway = true) }
                },
                onFailure = { e ->
                    when (e) {
                        is ValidateException -> {
                            state.update { st ->
                                e.messages.remoteFieldErrorsOn(st).copy(isSaving = false)
                            }
                        }

                        else -> {
                            state.update {
                                it.copy(
                                    isSaving = false,
                                    saveError = "Não foi possível guardar. Tente novamente.",
                                )
                            }
                        }
                    }
                },
            )
        }
    }

    private sealed interface SaveAction {
        data class Set(val ui: UiState) : SaveAction
        data class RunUpsert(val ui: UiState, val param: UpsertInvestmentAssetUseCase.Param) : SaveAction
    }
}
