package com.eferraz.asset_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage
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

    internal val state: StateFlow<UiState.Form> field = MutableStateFlow(UiState.Form())

    init {
        loadIssuersAndBrokerages()
    }

    internal fun dispatch(event: AssetManagementEvent) {
        when (event) {
            is AssetManagementEvent.CategoryChanged,
            is AssetManagementEvent.IssuerChanged,
            is AssetManagementEvent.ObservationsChanged,
            is AssetManagementEvent.BrokerageChanged,
            is AssetManagementEvent.FixedTypeChanged,
            is AssetManagementEvent.FixedSubTypeChanged,
            is AssetManagementEvent.FixedExpirationChanged,
            is AssetManagementEvent.FixedYieldChanged,
            is AssetManagementEvent.FixedCdiChanged,
            is AssetManagementEvent.FixedLiquidityChanged,
            is AssetManagementEvent.VariableTypeChanged,
            is AssetManagementEvent.VariableTickerChanged,
            is AssetManagementEvent.VariableCnpjChanged,
            is AssetManagementEvent.FundNameChanged,
            is AssetManagementEvent.FundTypeChanged,
            is AssetManagementEvent.FundLiquidityDaysChanged,
            is AssetManagementEvent.FundExpirationChanged,
            -> {
                val next = state.value.draft.applyFormEvent(event) ?: return
                state.update { it.copy(draft = next, saveError = null) }
            }

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
        if (state.value.draft == initialAssetDraft()) {
            state.update { it.copy(navigateAway = true) }
        } else {
            state.update { it.copy(showDiscardDialog = true) }
        }
    }

    private fun onConfirmDiscard() {
        state.update {
            it.copy(
                showDiscardDialog = false,
                navigateAway = true,
                draft = initialAssetDraft(),
                saveError = null,
            )
        }
    }

    private fun onCancelDiscard() {
        state.update { it.copy(showDiscardDialog = false) }
    }

    private fun onSave() {
        val s = state.value
        if (s.isSaving) return

        val uiErrors = validateAssetDraft(s.draft)

        val action: SaveAction = when {
            s.issuers.isEmpty() -> SaveAction.SetForm(
                s.copy(saveError = "Cadastre um emissor noutro ecrã antes de guardar."),
            )

            s.brokerages.isEmpty() -> SaveAction.SetForm(
                s.copy(saveError = "Cadastre uma corretora noutro ecrã antes de guardar."),
            )

            uiErrors.hasAnyError() -> SaveAction.SetForm(
                s.copy(draft = s.draft.copy(errors = uiErrors), saveError = null),
            )

            else -> {
                val param = buildUpsertParam(s.draft)
                if (param == null) {
                    SaveAction.SetForm(s.copy(saveError = "Dados incompletos."))
                } else {
                    SaveAction.RunUpsert(s, param)
                }
            }
        }
        when (action) {
            is SaveAction.SetForm -> state.update { action.form }
            is SaveAction.RunUpsert -> runUpsert(action.form, action.param)
        }
    }

    private fun runUpsert(s: UiState.Form, param: UpsertInvestmentAssetUseCase.Param) {
        viewModelScope.launch {
            state.update { it.copy(isSaving = true, saveError = null, draft = it.draft.copy(errors = AssetFormErrors.Empty)) }
            val result = upsertInvestmentAssetUseCase(param)
            result.fold(
                onSuccess = {
                    state.update { it.copy(isSaving = false, navigateAway = true) }
                },
                onFailure = { e ->
                    when (e) {
                        is ValidateException -> {
                            state.update {
                                it.copy(
                                    isSaving = false,
                                    draft = it.draft.copy(errors = e.messages.toAssetFormErrors()),
                                )
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

    internal sealed interface UiState {
        data class Form(
            val issuers: List<Issuer> = emptyList(),
            val brokerages: List<Brokerage> = emptyList(),
            val draft: AssetDraft = initialAssetDraft(),
            val saveError: String? = null,
            val isSaving: Boolean = false,
            val showDiscardDialog: Boolean = false,
            val navigateAway: Boolean = false,
        ) : UiState
    }

    private sealed interface SaveAction {
        data class SetForm(val form: UiState.Form) : SaveAction
        data class RunUpsert(val form: UiState.Form, val param: UpsertInvestmentAssetUseCase.Param) : SaveAction
    }
}
