package com.eferraz.asset_management.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.checkErros
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.usecases.SaveAssetWithTransactionsUseCase
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetCurrentDateUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.cruds.GetOwnerUseCase
import com.eferraz.usecases.exceptions.ValidateException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    private val saveAssetWithTransactionsUseCase: SaveAssetWithTransactionsUseCase,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
) : ViewModel() {

    internal val state: StateFlow<AssetManagementUiState> field = MutableStateFlow(AssetManagementUiState())

    internal val dismissAfterSave: SharedFlow<Unit> field = MutableSharedFlow<Unit>(extraBufferCapacity = 0)

    private var existingHolding: AssetHolding? = null

    internal fun dispatch(event: AssetManagementEvents) = when (event) {

        is AssetManagementEvents.ScreenEntered -> {
            state.update { it.copy(isSaving = false, saveError = null) }
            loadInitialState(event.holdingId)
        }

        is AssetManagementEvents.AssetClassChanged -> state.update { it.partialResetForAssetClass(event.assetClass) }
        is AssetManagementEvents.TypeChanged -> state.update { it.copy(type = event.type, typeError = null) }
        is AssetManagementEvents.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null) }

        is AssetManagementEvents.ObservationsChanged -> state.update { it.copy(observations = event.value) }
        is AssetManagementEvents.B3IdentifierChanged -> state.update { it.copy(b3Identifier = event.value) }

        is AssetManagementEvents.YieldIndexerChanged -> state.update { it.copy(yieldIndexer = event.indexer, yieldIndexerError = null) }
        is AssetManagementEvents.FixedExpirationChanged -> state.update {
            it.copy(
                fixedExpiration = dateToDigits(event.raw),
                fixedExpirationError = null
            )
        }

        is AssetManagementEvents.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }
        is AssetManagementEvents.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }
        is AssetManagementEvents.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }
        is AssetManagementEvents.IncomeTaxExemptChanged -> state.update {
            it.copy(incomeTaxExempt = event.exempt, incomeTaxSelected = incomeTaxSelectedFor(event.exempt))
        }

        is AssetManagementEvents.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null) }
        is AssetManagementEvents.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null) }

        is AssetManagementEvents.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null) }

        is AssetManagementEvents.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }

        is AssetManagementEvents.TransactionAdded -> addTransactionDraft(event.assetClass)
        is AssetManagementEvents.TransactionRemoved -> removeTransactionDraft(event.index)
        is AssetManagementEvents.TransactionDateChanged -> updateTransactionDraft(event.index) {
            it.copy(dateDigits = dateToDigits(event.digits))
        }

        is AssetManagementEvents.TransactionTypeChanged -> updateTransactionDraft(event.index) {
            it.copy(type = event.type)
        }

        is AssetManagementEvents.TransactionQuantityChanged -> updateTransactionDraft(event.index) {
            it.copy(quantity = event.value)
        }

        is AssetManagementEvents.TransactionUnitPriceChanged -> updateTransactionDraft(event.index) {
            it.copy(unitPrice = event.value)
        }

        AssetManagementEvents.Save -> onSave()
    }

    private fun loadInitialState(holdingId: Long?) = viewModelScope.launch {

        val issuers = async { getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty() }
        val brokerages = async { getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty() }

        if (holdingId != null) {
            val holding = getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(holdingId)).getOrNull()
            existingHolding = holding

            val sortedTransactions = holding?.transactions
                ?.sortedBy { it.date }
                ?.map { TransactionDraftUi.fromDomain(it, holding.asset.assetClass) }
                .orEmpty()

            val base = holding?.asset?.toUiState()?.withDerivedFields() ?: AssetManagementUiState()
            state.update {
                base.copy(
                    issuers = issuers.await(),
                    brokerages = brokerages.await(),
                    brokerage = holding?.brokerage,
                    owner = holding?.owner,
                    holdingId = holding?.id,
                    transactions = sortedTransactions,
                )
            }
        } else {
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

    private fun addTransactionDraft(assetClass: AssetClass) = viewModelScope.launch {
        val currentDate = getCurrentDateUseCase(Unit).getOrThrow().toString().replace("-", "")
        val defaultQuantity = if (assetClass == AssetClass.VARIABLE_INCOME) "" else "1"
        val blank = TransactionDraftUi(
            isNew = true,
            dateDigits = currentDate,
            assetClass = assetClass,
            quantity = defaultQuantity,
        )
        state.update { it.copy(transactions = it.transactions + blank) }
    }

    private fun removeTransactionDraft(index: Int) =
        state.update {
            it.copy(transactions = it.transactions.filterIndexed { rowIndex, _ -> rowIndex != index })
        }

    private fun updateTransactionDraft(index: Int, update: (TransactionDraftUi) -> TransactionDraftUi) =
        state.update { current ->
            val draft = update(current.transactions[index]).syncTotal()
            current.copy(transactions = current.transactions.toMutableList().apply { this[index] = draft })
        }

    private fun onSave() = viewModelScope.launch {

        if (state.value.isSaving) return@launch
        if (state.checkErros()) return@launch

        state.update { it.copy(isSaving = true, saveError = null) }

        val current = state.value
        val domainTransactions = current.transactions.mapNotNull { it.toDomainTransaction() }
        val holding = current.buildHolding(existingHolding).copy(transactions = domainTransactions)

        saveAssetWithTransactionsUseCase(SaveAssetWithTransactionsUseCase.Param(holding))
            .onSuccess {
                state.update { it.copy(isSaving = false) }
                dismissAfterSave.emit(Unit)
            }
            .onFailure { error ->
                when (error) {
                    is ValidateException -> error.messages.remoteFieldErrorsOn(current).let { mapped ->
                        state.update { mapped.copy(isSaving = false, saveError = error.message) }
                    }

                    else -> state.update { it.copy(isSaving = false, saveError = error.message) }
                }
            }
    }
}
