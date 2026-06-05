package com.eferraz.asset_management.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.checkErros
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.cruds.GetOwnerUseCase
import com.eferraz.usecases.cruds.UpsertAssetHoldingUseCase
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
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,
    private val getOwnerUseCase: GetOwnerUseCase,
) : ViewModel() {

    internal val state: StateFlow<AssetManagementUiState> field = MutableStateFlow(AssetManagementUiState())

    private var existingHolding: AssetHolding? = null

    internal fun dispatch(event: AssetManagementEvents) = when (event) {

        is AssetManagementEvents.ScreenEntered -> loadInitialState(event.holdingId)

        is AssetManagementEvents.AssetClassChanged -> state.update { it.partialResetForAssetClass(event.assetClass) }
        is AssetManagementEvents.TypeChanged -> state.update { it.copy(type = event.type, typeError = null) }
        is AssetManagementEvents.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null) }

        is AssetManagementEvents.ObservationsChanged -> state.update { it.copy(observations = event.value) }
        is AssetManagementEvents.B3IdentifierChanged -> state.update { it.copy(b3Identifier = event.value) }

        is AssetManagementEvents.YieldIndexerChanged -> state.update { it.copy(yieldIndexer = event.indexer, yieldIndexerError = null) }
        is AssetManagementEvents.FixedExpirationChanged -> state.update { it.copy(fixedExpiration = dateToDigits(event.raw), fixedExpirationError = null) }
        is AssetManagementEvents.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }
        is AssetManagementEvents.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }
        is AssetManagementEvents.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }
        is AssetManagementEvents.IncomeTaxExemptChanged -> state.update { it.copy(incomeTaxExempt = event.exempt) }

//        is AssetManagementEvents.VariableTypeChanged -> state.update { it.copy(type = event.type, variableTypeError = null) }
        is AssetManagementEvents.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null) }
        is AssetManagementEvents.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null) }

        is AssetManagementEvents.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null) }
//        is AssetManagementEvents.FundLiquidityChanged -> state.update { it.copy(fundLiquidity = event.liquidity, fundLiquidityError = null) }
//        is AssetManagementEvents.FundTypeChanged -> state.update { it.copy(type = event.type, fundTypeError = null) }
//        is AssetManagementEvents.FundLiquidityDaysChanged -> state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }
//        is AssetManagementEvents.FundExpirationChanged -> state.update { it.copy(fundExpiration = dateToDigits(event.raw), fundExpirationError = null) }

        is AssetManagementEvents.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }

        AssetManagementEvents.Save -> onSave()
    }

    private fun loadInitialState(holdingId: Long?) = viewModelScope.launch {

        val issuers = async { getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty() }
        val brokerages = async { getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty() }

        if (holdingId != null) {
            val holding = getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(holdingId)).getOrNull()
            existingHolding = holding

            val base = holding?.asset?.toUiState() ?: AssetManagementUiState()
            state.update {
                base.copy(
                    issuers = issuers.await(),
                    brokerages = brokerages.await(),
                    brokerage = holding?.brokerage,
                    owner = holding?.owner,
                    holdingId = holding?.id,
                )
            }
        } else {
            // Novo investimento: não reutilizar holding de uma edição anterior na mesma instância do VM
            // (evita upsert no mesmo `asset_holding` em vez de inserir linha nova).
            existingHolding = null
            val owner = async { getOwnerUseCase(GetOwnerUseCase.Param).getOrNull() }
            state.update {
                AssetManagementUiState(
                    issuers = issuers.await(),
                    brokerages = brokerages.await(),
                    owner = owner.await(),
                )
            }
        }
    }

    private fun onSave() = viewModelScope.launch {

        if (state.value.isSaving || state.checkErros()) return@launch
        state.update { it.copy(isSaving = true) }

        upsertAssetUseCase(UpsertAssetUseCase.Param(state.value.buildAsset()))
            .onSuccess { newAsset ->

                val brokerage = state.value.brokerage!!
                val holding = existingHolding?.copy(asset = newAsset, brokerage = brokerage)
                    ?: AssetHolding(
                        id = 0L,
                        asset = newAsset,
                        owner = state.value.owner!!,
                        brokerage = brokerage,
                    )

                upsertAssetHoldingUseCase(UpsertAssetHoldingUseCase.Param(holding))
                    .onSuccess {
                        state.update { it.copy(isSaving = false, isCompleted = true, asset = newAsset) }
                    }
                    .onFailure { holdingError ->
                        when (holdingError) {
                            is ValidateException -> state.update { s ->
                                s.copy(brokerageError = holdingError.messages["brokerage"], isSaving = false)
                            }
                            else -> state.update { it.copy(isSaving = false) }
                        }
                    }
            }
            .onFailure { assetError ->
                when (assetError) {
                    is ValidateException -> assetError.messages.remoteFieldErrorsOn(state.value).let { mapped ->
                        state.update { mapped.copy(isSaving = false) }
                    }
                    else -> state.update { it.copy(isSaving = false) }
                }
            }
    }
}
