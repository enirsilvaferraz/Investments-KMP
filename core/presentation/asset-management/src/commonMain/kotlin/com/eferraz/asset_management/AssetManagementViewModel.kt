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

    internal val state: StateFlow<UiState> field = MutableStateFlow(UiState())

    init {
        loadIssuersAndBrokerages()
    }

    internal fun dispatch(event: AssetManagementEvent) {
        when (event) {
            is AssetManagementEvent.CategoryChanged -> state.update { it.withCategoryPreservingIssuerAndObs(event.category) }
            is AssetManagementEvent.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null) }
            is AssetManagementEvent.ObservationsChanged -> state.update { it.copy(observations = event.value) }
            is AssetManagementEvent.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }
            is AssetManagementEvent.FixedTypeChanged -> state.update { it.copy(fixedType = event.type, fixedTypeError = null) }
            is AssetManagementEvent.FixedSubTypeChanged -> state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null) }
            is AssetManagementEvent.FixedExpirationChanged -> state.update { it.copy(fixedExpiration = filterDateMaskDigits(event.raw), fixedExpirationError = null) }
            is AssetManagementEvent.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }
            is AssetManagementEvent.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }
            is AssetManagementEvent.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }
            is AssetManagementEvent.VariableTypeChanged -> state.update { it.copy(variableType = event.type, variableTypeError = null) }
            is AssetManagementEvent.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null) }
            is AssetManagementEvent.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null) }
            is AssetManagementEvent.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null) }
            is AssetManagementEvent.FundTypeChanged -> state.update { it.copy(fundType = event.type, fundTypeError = null) }
            is AssetManagementEvent.FundLiquidityDaysChanged -> state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }
            is AssetManagementEvent.FundExpirationChanged -> state.update { it.copy(fundExpiration = filterDateMaskDigits(event.raw), fundExpirationError = null) }
            AssetManagementEvent.Save -> onSave()
            AssetManagementEvent.RequestDismiss -> onRequestDismiss()
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
        state.update { s ->
            UiState(issuers = s.issuers, brokerages = s.brokerages, navigateAway = true)
        }
    }

    private fun onSave() {
        val s = state.value
        if (s.isSaving) return
        if (s.issuers.isEmpty() || s.brokerages.isEmpty()) return

        val validated = validateUiState(s)
        if (validated.hasAnyFieldError()) {
            state.update { validated }
            return
        }

        val param = buildUpsertParam(s) ?: return
        runUpsert(s, param)
    }

    private fun runUpsert(ui: UiState, param: UpsertInvestmentAssetUseCase.Param) {
        viewModelScope.launch {
            state.update { it.withClearedFieldErrors().copy(isSaving = true) }
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
                            state.update { it.copy(isSaving = false) }
                        }
                    }
                },
            )
        }
    }
}
