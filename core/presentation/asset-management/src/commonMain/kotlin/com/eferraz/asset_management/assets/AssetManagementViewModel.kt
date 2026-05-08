package com.eferraz.asset_management.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.checkErros
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.usecases.cruds.GetAssetUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.cruds.UpsertAssetUseCase
import com.eferraz.usecases.exceptions.ValidateException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AssetManagementViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getAssetUseCase: GetAssetUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,
) : ViewModel() {

    internal val state: StateFlow<AssetManagementUiState> field = MutableStateFlow(AssetManagementUiState())

    internal fun dispatch(event: AssetManagementEvents) = when (event) {

        is AssetManagementEvents.ScreenEntered -> loadInitialState(event.assetId)

        is AssetManagementEvents.CategoryChanged -> state.update { it.copy(category = event.category) }
        is AssetManagementEvents.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null) }
        is AssetManagementEvents.ObservationsChanged -> state.update { it.copy(observations = event.value) }

        is AssetManagementEvents.FixedTypeChanged -> state.update { it.copy(fixedType = event.type, fixedTypeError = null) }
        is AssetManagementEvents.FixedSubTypeChanged -> state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null) }
        is AssetManagementEvents.FixedExpirationChanged -> state.update { it.copy(fixedExpiration = dateToDigits(event.raw), fixedExpirationError = null) }

        is AssetManagementEvents.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }
        is AssetManagementEvents.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }
        is AssetManagementEvents.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }

        is AssetManagementEvents.VariableTypeChanged -> state.update { it.copy(variableType = event.type, variableTypeError = null) }
        is AssetManagementEvents.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null) }
        is AssetManagementEvents.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null) }

        is AssetManagementEvents.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null) }
        is AssetManagementEvents.FundTypeChanged -> state.update { it.copy(fundType = event.type, fundTypeError = null) }
        is AssetManagementEvents.FundLiquidityDaysChanged -> state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }
        is AssetManagementEvents.FundExpirationChanged -> state.update { it.copy(fundExpiration = dateToDigits(event.raw), fundExpirationError = null) }

        AssetManagementEvents.Save -> onSave()
    }

    private fun loadInitialState(assetId: Long?) = viewModelScope.launch {

        val issuers = async { getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty() }
        val holding = async { assetId?.let { getAssetUseCase(GetAssetUseCase.ById(it)).getOrNull() } }

        val base = holding.await()?.toUiState() ?: AssetManagementUiState()

        state.update { base.copy(issuers = issuers.await()) }
    }

    private fun onSave() = viewModelScope.launch {

        if (state.value.isSaving || state.checkErros()) return@launch
        state.update { it.copy(isSaving = true) }

        upsertAssetUseCase(UpsertAssetUseCase.Param(state.value.buildAsset()))
            .onSuccess { newAsset ->
                state.update { it.copy(isSaving = false, isCompleted = true, asset = newAsset) }
            }
            .onFailure {
                when (it) {
                    is ValidateException -> it.messages.remoteFieldErrorsOn(state.value)
                }
                state.update { it.copy(isSaving = false) }
            }
    }
}
