package com.eferraz.asset_management.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.buildAsset
import com.eferraz.asset_management.helpers.buildHolding
import com.eferraz.asset_management.helpers.remoteFieldErrorsOn
import com.eferraz.asset_management.helpers.validateUiState
import com.eferraz.design_system.input.date.filterDateMaskDigits
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.cruds.GetOwnerUseCase
import com.eferraz.usecases.cruds.UpsertAssetHoldingUseCase
import com.eferraz.usecases.cruds.UpsertAssetUseCase
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
    private val getOwnerUseCase: GetOwnerUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,
) : ViewModel() {

    internal val state: StateFlow<UiState> field = MutableStateFlow(UiState())

    init {
        loadIssuersAndBrokerages()
    }

    internal fun dispatch(event: AssetManagementEvent) = when (event) {

        is AssetManagementEvent.CategoryChanged ->
            state.update { UiState(issuers = it.issuers, brokerages = it.brokerages, category = event.category) }

        is AssetManagementEvent.IssuerChanged ->
            state.update { it.copy(issuer = event.issuer, issuerError = null) }

        is AssetManagementEvent.ObservationsChanged ->
            state.update { it.copy(observations = event.value) }

        is AssetManagementEvent.BrokerageChanged ->
            state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }

        is AssetManagementEvent.FixedTypeChanged ->
            state.update { it.copy(fixedType = event.type, fixedTypeError = null) }

        is AssetManagementEvent.FixedSubTypeChanged ->
            state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null) }

        is AssetManagementEvent.FixedExpirationChanged ->
            state.update { it.copy(fixedExpiration = filterDateMaskDigits(event.raw), fixedExpirationError = null) }

        is AssetManagementEvent.FixedYieldChanged ->
            state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }

        is AssetManagementEvent.FixedCdiChanged ->
            state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }

        is AssetManagementEvent.FixedLiquidityChanged ->
            state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }

        is AssetManagementEvent.VariableTypeChanged ->
            state.update { it.copy(variableType = event.type, variableTypeError = null) }

        is AssetManagementEvent.VariableTickerChanged ->
            state.update { it.copy(variableTicker = event.value, variableTickerError = null) }

        is AssetManagementEvent.VariableCnpjChanged ->
            state.update { it.copy(variableCnpj = event.value, cnpjError = null) }

        is AssetManagementEvent.FundNameChanged ->
            state.update { it.copy(fundName = event.value, fundNameError = null) }

        is AssetManagementEvent.FundTypeChanged ->
            state.update { it.copy(fundType = event.type, fundTypeError = null) }

        is AssetManagementEvent.FundLiquidityDaysChanged ->
            state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }

        is AssetManagementEvent.FundExpirationChanged ->
            state.update { it.copy(fundExpiration = filterDateMaskDigits(event.raw), fundExpirationError = null) }

        AssetManagementEvent.RequestDismiss ->
            state.update { UiState(issuers = it.issuers, brokerages = it.brokerages, navigateAway = true) }

        AssetManagementEvent.NavigationConsumed ->
            state.update { it.copy(navigateAway = it.navigateAway.not()) }

        AssetManagementEvent.Save -> onSave()
    }

    private fun loadIssuersAndBrokerages() = viewModelScope.launch {
        val issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty()
        val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
        state.update { it.copy(issuers = issuers, brokerages = brokerages) }
    }

    private fun onSave() {

        if (state.value.isSaving) return

        val validated = state.value.validateUiState()

        if (validated.hasAnyFieldError()) {
            state.update { validated }
            return
        }

        val asset = state.value.buildAsset()

        viewModelScope.launch {

            state.update { it.copy(isSaving = true) }

            upsertAssetUseCase(UpsertAssetUseCase.Param(asset))
                .fold(
                    onSuccess = { assetId ->
                        val owner = getOwnerUseCase(GetOwnerUseCase.Param).getOrThrow()
                        val brokerage = state.value.brokerage!!
                        upsertAssetHoldingUseCase(UpsertAssetHoldingUseCase.Param(buildHolding(asset, assetId, owner, brokerage)))
                    },
                    onFailure = {
                        Result.failure(it)
                    },
                )
                .fold(
                    onSuccess = {
                        state.update { it.copy(isSaving = false, navigateAway = true) }
                    },
                    onFailure = { e ->
                        when (e) {
                            is ValidateException -> state.update { st -> e.messages.remoteFieldErrorsOn(st).copy(isSaving = false) }
                            else -> state.update { it.copy(isSaving = false) }
                        }
                    }
                )
        }
    }
}