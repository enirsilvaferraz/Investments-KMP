package com.eferraz.asset_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Issuer
import com.eferraz.usecases.UpsertInvestmentAssetUseCase
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
    private val upsertInvestmentAssetUseCase: UpsertInvestmentAssetUseCase,
) : ViewModel() {

    internal val state: StateFlow<UiState.Form> field = MutableStateFlow<UiState.Form>(UiState.Form())

    init {
        dispatch(Intent.LoadIssuers)
    }

    internal fun dispatch(intent: Intent) {
        when (intent) {
            Intent.LoadIssuers -> loadIssuers()
            is Intent.DraftChanged -> onDraftChanged(intent.draft)
            is Intent.CategoryChanged -> onCategoryChanged(intent.category)
            Intent.Save -> onSave()
            Intent.RequestDismiss -> onRequestDismiss()
            Intent.ConfirmDiscard -> onConfirmDiscard()
            Intent.CancelDiscard -> onCancelDiscard()
            Intent.NavigationConsumed -> onNavigationConsumed()
        }
    }

    private fun loadIssuers() {
        viewModelScope.launch {
            state.update {
                it.copy(issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty())
            }
        }
    }

    private fun onNavigationConsumed() {
        if (state.value.navigateAway) {
            state.update {
                it.copy(navigateAway = false)
            }
        }
    }

    private fun onDraftChanged(draft: AssetDraft) {
        state.update {
            it.copy(
                draft = draft,
                fieldErrors = emptyMap(),
                saveError = null,
            )
        }
    }

    private fun onCategoryChanged(category: InvestmentCategory) {
        val next = state.value.draft.withCategoryPreservingIssuerAndObs(category)
        state.value = state.value.copy(draft = next, fieldErrors = emptyMap(), saveError = null)
    }

    private fun onRequestDismiss() {
        if (state.value.draft == initialAssetDraft()) {
            state.value = state.value.copy(navigateAway = true)
        } else {
            state.value = state.value.copy(showDiscardDialog = true)
        }
    }

    private fun onConfirmDiscard() {
        state.value = state.value.copy(
            showDiscardDialog = false,
            navigateAway = true,
            draft = initialAssetDraft(),
            fieldErrors = emptyMap(),
            saveError = null,
        )
    }

    private fun onCancelDiscard() {
        state.value = state.value.copy(showDiscardDialog = false)
    }

    private fun onSave() {

        val s = state.value
        if (s.isSaving) return

        val uiErrors = validateAssetDraft(s.draft)

        val action: SaveAction = when {

            s.issuers.isEmpty() -> SaveAction.SetForm(
                s.copy(saveError = "Cadastre um emissor noutro ecrã antes de guardar."),
            )

            uiErrors.isNotEmpty() -> SaveAction.SetForm(s.copy(fieldErrors = uiErrors))

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
            is SaveAction.SetForm -> state.value = action.form
            is SaveAction.RunUpsert -> runUpsert(action.form, action.param)
        }
    }

    private fun runUpsert(s: UiState.Form, param: UpsertInvestmentAssetUseCase.Param) {
        viewModelScope.launch {
            state.value = s.copy(isSaving = true, saveError = null, fieldErrors = emptyMap())
            val result = upsertInvestmentAssetUseCase(param)
            result.fold(
                onSuccess = {
                    val latest = state.value
                    state.value = latest.copy(isSaving = false, navigateAway = true)
                },
                onFailure = { e ->
                    val latest = state.value
                    when (e) {
                        is ValidateException -> {
                            state.value = latest.copy(isSaving = false, fieldErrors = e.messages)
                        }

                        else -> {
                            state.value = latest.copy(
                                isSaving = false,
                                saveError = "Não foi possível guardar. Tente novamente.",
                            )
                        }
                    }
                },
            )
        }
    }

    internal sealed interface UiState {
        data class Form(
            val issuers: List<Issuer> = emptyList(),
            val draft: AssetDraft = initialAssetDraft(),
            val fieldErrors: Map<String, String> = emptyMap(),
            val saveError: String? = null,
            val isSaving: Boolean = false,
            val showDiscardDialog: Boolean = false,
            val navigateAway: Boolean = false,
        ) : UiState
    }

    internal sealed interface Intent {
        data object LoadIssuers : Intent
        data class DraftChanged(val draft: AssetDraft) : Intent
        data class CategoryChanged(val category: InvestmentCategory) : Intent
        data object Save : Intent
        data object RequestDismiss : Intent
        data object ConfirmDiscard : Intent
        data object CancelDiscard : Intent
        data object NavigationConsumed : Intent
    }

    private sealed interface SaveAction {
        data class SetForm(val form: UiState.Form) : SaveAction
        data class RunUpsert(val form: UiState.Form, val param: UpsertInvestmentAssetUseCase.Param) : SaveAction
    }
}
