package com.eferraz.asset_management.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.buildAsset
import com.eferraz.asset_management.helpers.buildHolding
import com.eferraz.asset_management.helpers.remoteFieldErrorsOn
import com.eferraz.asset_management.helpers.checkErros
import com.eferraz.asset_management.helpers.toUiState
import com.eferraz.design_system.input.date.filterDateMaskDigits
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
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
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val getOwnerUseCase: GetOwnerUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,
) : ViewModel() {

    internal val state: StateFlow<UiState> field = MutableStateFlow(UiState())

    internal fun dispatch(event: VMEvents) = when (event) {

        is VMEvents.ScreenEntered ->
            resetState(holdingId = event.holdingId)

        is VMEvents.CategoryChanged ->
            state.update { UiState(issuers = it.issuers, brokerages = it.brokerages, category = event.category) }

        is VMEvents.IssuerChanged ->
            state.update { it.copy(issuer = event.issuer, issuerError = null) }

        is VMEvents.ObservationsChanged ->
            state.update { it.copy(observations = event.value) }

        is VMEvents.BrokerageChanged ->
            state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }

        is VMEvents.FixedTypeChanged ->
            state.update { it.copy(fixedType = event.type, fixedTypeError = null) }

        is VMEvents.FixedSubTypeChanged ->
            state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null) }

        is VMEvents.FixedExpirationChanged ->
            state.update { it.copy(fixedExpiration = filterDateMaskDigits(event.raw), fixedExpirationError = null) }

        is VMEvents.FixedYieldChanged ->
            state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }

        is VMEvents.FixedCdiChanged ->
            state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }

        is VMEvents.FixedLiquidityChanged ->
            state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }

        is VMEvents.VariableTypeChanged ->
            state.update { it.copy(variableType = event.type, variableTypeError = null) }

        is VMEvents.VariableTickerChanged ->
            state.update { it.copy(variableTicker = event.value, variableTickerError = null) }

        is VMEvents.VariableCnpjChanged ->
            state.update { it.copy(variableCnpj = event.value, cnpjError = null) }

        is VMEvents.FundNameChanged ->
            state.update { it.copy(fundName = event.value, fundNameError = null) }

        is VMEvents.FundTypeChanged ->
            state.update { it.copy(fundType = event.type, fundTypeError = null) }

        is VMEvents.FundLiquidityDaysChanged ->
            state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }

        is VMEvents.FundExpirationChanged ->
            state.update { it.copy(fundExpiration = filterDateMaskDigits(event.raw), fundExpirationError = null) }

        VMEvents.RequestDismiss ->
            state.update { it.copy(navigateAway = true) }

        VMEvents.NavigationConsumed ->
            state.update { it.copy(navigateAway = it.navigateAway.not()) }

        VMEvents.Save -> onSave()
    }

    private fun resetState(holdingId: Long?) = viewModelScope.launch {
        val issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty()
        val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
        val editable = holdingId?.let {
            getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(it)).getOrNull()
        }
        state.update {
            editable?.toUiState(issuers = issuers, brokerages = brokerages)
                ?: UiState(issuers = issuers, brokerages = brokerages)
        }
    }

    private fun onSave() {

        if (state.value.isSaving || state.checkErros()) return

        val asset = state.value.buildAsset()

        viewModelScope.launch {

            state.update { it.copy(isSaving = true) }

            upsertAssetUseCase(UpsertAssetUseCase.Param(asset))
                .fold(
                    onSuccess = { assetId ->

                        val owner = getOwnerUseCase(GetOwnerUseCase.Param).getOrThrow()
                        val brokerage = state.value.brokerage!!
                        val assetHolding = buildHolding(
                            baseAsset = asset,
                            assetId = assetId,
                            owner = owner,
                            brokerage = brokerage,
                            holdingId = state.value.editingHoldingId,
                        )

                        upsertAssetHoldingUseCase(UpsertAssetHoldingUseCase.Param(assetHolding))
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
                            is ValidateException -> state.update { e.messages.remoteFieldErrorsOn(it).copy(isSaving = false) }
                            else -> state.update { UiState(issuers = it.issuers, brokerages = it.brokerages, isSaving = false) }
                        }
                    }
                )
        }
    }
}