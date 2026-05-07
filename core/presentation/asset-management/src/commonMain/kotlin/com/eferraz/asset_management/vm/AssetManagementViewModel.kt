package com.eferraz.asset_management.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.asset_management.helpers.buildAsset
import com.eferraz.asset_management.helpers.buildHolding
import com.eferraz.asset_management.helpers.checkErros
import com.eferraz.asset_management.helpers.localDateFromIsoDateDigits
import com.eferraz.asset_management.helpers.remoteFieldErrorsOn
import com.eferraz.asset_management.helpers.toUiState
import com.eferraz.design_system.input.date.dateToDigits
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.VariableIncomeTransaction
import com.eferraz.usecases.GetTransactionsByHoldingUseCase
import com.eferraz.usecases.SaveTransactionUseCase
import com.eferraz.usecases.cruds.GetAssetHoldingUseCase
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@KoinViewModel
internal class AssetManagementViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getOwnerUseCase: GetOwnerUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,
) : ViewModel() {

    internal val state: StateFlow<UiState> field = MutableStateFlow(UiState())

    internal fun dispatch(event: VMEvents) {
        when (event) {
            is VMEvents.ScreenEntered -> resetState(event.holdingId)
            is VMEvents.CategoryChanged -> onCategoryChanged(event.category)
            is VMEvents.IssuerChanged -> state.update { it.copy(issuer = event.issuer, issuerError = null) }
            is VMEvents.ObservationsChanged -> state.update { it.copy(observations = event.value) }
            is VMEvents.BrokerageChanged -> state.update { it.copy(brokerage = event.brokerage, brokerageError = null) }
            is VMEvents.FixedTypeChanged -> state.update { it.copy(fixedType = event.type, fixedTypeError = null) }
            is VMEvents.FixedSubTypeChanged -> state.update { it.copy(fixedSubType = event.subType, fixedSubTypeError = null) }
            is VMEvents.FixedExpirationChanged -> state.update { it.copy(fixedExpiration = dateToDigits(event.raw), fixedExpirationError = null) }
            is VMEvents.FixedYieldChanged -> state.update { it.copy(fixedYield = event.value, fixedYieldError = null) }
            is VMEvents.FixedCdiChanged -> state.update { it.copy(fixedCdi = event.value, fixedCdiError = null) }
            is VMEvents.FixedLiquidityChanged -> state.update { it.copy(fixedLiquidity = event.liquidity, fixedLiquidityError = null) }
            is VMEvents.VariableTypeChanged -> state.update { it.copy(variableType = event.type, variableTypeError = null) }
            is VMEvents.VariableTickerChanged -> state.update { it.copy(variableTicker = event.value, variableTickerError = null) }
            is VMEvents.VariableCnpjChanged -> state.update { it.copy(variableCnpj = event.value, cnpjError = null) }
            is VMEvents.FundNameChanged -> state.update { it.copy(fundName = event.value, fundNameError = null) }
            is VMEvents.FundTypeChanged -> state.update { it.copy(fundType = event.type, fundTypeError = null) }
            is VMEvents.FundLiquidityDaysChanged -> state.update { it.copy(fundLiquidityDays = event.value, fundLiquidityDaysError = null) }
            is VMEvents.FundExpirationChanged -> state.update { it.copy(fundExpiration = dateToDigits(event.raw), fundExpirationError = null) }

            VMEvents.AddTransactionDraft -> addTransactionDraft()
            VMEvents.TransactionDraftErrorDismissed -> state.update {
                it.copy(transactionDraftError = null)
            }
            is VMEvents.DraftTransactionDateChanged -> updateDraft(event.index) { draft -> draft.copy(dateDigits = dateToDigits(event.raw)) }
            is VMEvents.DraftTransactionTypeChanged -> updateDraft(event.index) { it.copy(type = event.type) }
            is VMEvents.DraftTransactionQuantityChanged -> updateDraft(event.index) { it.copy(quantity = event.value) }
            is VMEvents.DraftTransactionUnitPriceChanged -> updateDraft(event.index) { it.copy(unitPrice = event.value) }
            is VMEvents.DraftTransactionTotalValueChanged -> updateDraft(event.index) { it.copy(totalValue = event.value) }
            is VMEvents.DraftTransactionObservationChanged -> updateDraft(event.index) { it.copy(observations = event.value) }

            VMEvents.RequestDismiss -> state.update { it.copy(navigateAway = true) }
            VMEvents.NavigationConsumed -> state.update { it.copy(navigateAway = !it.navigateAway) }
            VMEvents.Save -> onSave()
        }
    }

    private fun onCategoryChanged(category: InvestmentCategory) {
        state.update { current ->
            val hasDiscard = current.transactionDrafts.any { it.quantity.isNotBlank() || it.unitPrice.isNotBlank() }
            current.copy(
                category = category,
                transactionDrafts = current.transactionDrafts.map { it.normalizeByCategory(category) }.map { draft ->
                    validateDraft(draft, category)
                },
                transactionDraftError = if (hasDiscard && category != InvestmentCategory.VARIABLE_INCOME) {
                    "Alguns campos incompatíveis foram removidos e precisam de revisão."
                } else {
                    null
                },
                focusedInvalidRowIndex = null,
            )
        }
    }

    private fun resetState(holdingId: Long?) = viewModelScope.launch {
        val issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrNull().orEmpty()
        val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
        val editableHolding = holdingId?.let { getAssetHoldingUseCase(GetAssetHoldingUseCase.ById(it)).getOrNull() }
        val transactions = editableHolding
            ?.let { getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(it)).getOrNull() }
            .orEmpty()

        state.update {
            val base = (editableHolding?.toUiState() ?: UiState()).copy(
                issuers = issuers,
                brokerages = brokerages,
                transactions = transactions,
            )
            base.copy(
                transactionDrafts = transactions
                    .sortedBy { tx -> tx.date }
                    .map(TransactionDraftUi::fromDomain)
                    .map { draft -> validateDraft(draft, base.category) }
                    .sortDraftsByOldest(),
            )
        }
    }

    private fun addTransactionDraft() {
        state.update { current ->
            val invalidIndex = current.transactionDrafts.indexOfFirst { it.inlineError != null }
            if (invalidIndex >= 0) {
                return@update current.copy(
                    transactionDraftError = "Corrija a linha inválida antes de adicionar outra.",
                    focusedInvalidRowIndex = invalidIndex,
                )
            }

            val blank = validateDraft(
                TransactionDraftUi(
                    isNew = true,
                    dateDigits = todayDateDigits(),
                ),
                current.category,
            )
            val updated = (current.transactionDrafts + blank).sortDraftsByOldest()
            val newIndex = updated.indexOf(blank).takeIf { it >= 0 } ?: 0
            current.copy(
                transactionDrafts = updated,
                transactionDraftError = null,
                focusedInvalidRowIndex = newIndex,
            )
        }
    }

    private fun updateDraft(index: Int, update: (TransactionDraftUi) -> TransactionDraftUi) {
        state.update { current ->
            if (index !in current.transactionDrafts.indices) return@update current
            val draft = update(current.transactionDrafts[index]).normalizeByCategory(current.category)
            val validated = validateDraft(draft, current.category)
            val mutable = current.transactionDrafts.toMutableList()
            mutable[index] = validated
            val sorted = mutable.sortDraftsByOldest()
            val focusedIndex = sorted.indexOf(validated).takeIf { it >= 0 } ?: index
            current.copy(
                transactionDrafts = sorted,
                transactionDraftError = null,
                focusedInvalidRowIndex = if (validated.inlineError != null) focusedIndex else current.focusedInvalidRowIndex,
            )
        }
    }

    private fun onSave() {
        val current = state.value
        if (current.isSaving || state.checkErros()) return

        val firstInvalid = current.transactionDrafts.indexOfFirst { validateDraft(it, current.category).inlineError != null }
        if (firstInvalid >= 0) {
            state.update {
                it.copy(
                    transactionDraftError = "Existem linhas inválidas na tabela. Corrija antes de salvar.",
                    focusedInvalidRowIndex = firstInvalid,
                    transactionDrafts = it.transactionDrafts.mapIndexed { index, draft ->
                        if (index == firstInvalid) validateDraft(draft, it.category) else draft
                    },
                )
            }
            return
        }

        val asset = current.buildAsset()

        viewModelScope.launch {
            state.update { it.copy(isSaving = true) }

            upsertAssetUseCase(UpsertAssetUseCase.Param(asset))
                .fold(
                    onSuccess = { upsertedAssetId ->
                        val assetId = upsertedAssetId.resolveUpsertId(state.value.editingAssetId)
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
                            .onSuccess { upsertedHoldingId ->
                                val savedHoldingId = upsertedHoldingId.resolveUpsertId(state.value.editingHoldingId)
                                saveDraftTransactions(assetHolding.copy(id = savedHoldingId), state.value.category)
                            }
                    },
                    onFailure = { Result.failure(it) },
                )
                .fold(
                    onSuccess = { state.update { it.copy(isSaving = false, navigateAway = true) } },
                    onFailure = { e ->
                        when (e) {
                            is ValidateException -> state.update { e.messages.remoteFieldErrorsOn(it).copy(isSaving = false) }
                            else -> state.update {
                                it.copy(
                                    isSaving = false,
                                    transactionDraftError = "Falha ao salvar transações. Tente novamente.",
                                )
                            }
                        }
                    },
                )
        }
    }

    private suspend fun saveDraftTransactions(holding: AssetHolding, category: InvestmentCategory) {
        val transactions = state.value.transactionDrafts
            .map { validateDraft(it, category) }
            .mapNotNull { draft -> draft.toDomainTransaction(holding, category) }

        transactions.forEach { transaction ->
            saveTransactionUseCase(SaveTransactionUseCase.Param(transaction)).getOrThrow().let { _ -> }
        }
    }
}

private fun List<TransactionDraftUi>.sortDraftsByOldest(): List<TransactionDraftUi> =
    sortedWith(
        compareBy<TransactionDraftUi> { localDateFromIsoDateDigits(it.dateDigits) == null }
            .thenBy { localDateFromIsoDateDigits(it.dateDigits) }
    )

@OptIn(ExperimentalTime::class)
private fun todayDateDigits(): String =
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toString()
        .replace("-", "")

private fun validateDraft(
    draft: TransactionDraftUi,
    category: InvestmentCategory,
): TransactionDraftUi {
    val hasDate = localDateFromIsoDateDigits(draft.dateDigits) != null
    val error = when {
        !hasDate -> "Data inválida"
        category == InvestmentCategory.VARIABLE_INCOME && draft.quantity.toDoubleOrNull() == null -> "Quantidade inválida"
        category == InvestmentCategory.VARIABLE_INCOME && draft.unitPrice.toDoubleOrNull() == null -> "Preço unitário inválido"
        category != InvestmentCategory.VARIABLE_INCOME && draft.totalValue.toDoubleOrNull() == null -> "Valor total inválido"
        else -> null
    }
    val normalized = if (category == InvestmentCategory.VARIABLE_INCOME) {
        val quantity = draft.quantity.toDoubleOrNull()
        val unitPrice = draft.unitPrice.toDoubleOrNull()
        if (quantity != null && unitPrice != null) {
            draft.copy(totalValue = (quantity * unitPrice).toString())
        } else {
            draft
        }
    } else {
        draft.copy(quantity = "", unitPrice = "")
    }
    return normalized.copy(inlineError = error)
}

private fun TransactionDraftUi.normalizeByCategory(category: InvestmentCategory): TransactionDraftUi =
    if (category == InvestmentCategory.VARIABLE_INCOME) this else copy(quantity = "", unitPrice = "")

private fun TransactionDraftUi.toDomainTransaction(
    holding: AssetHolding,
    category: InvestmentCategory,
): AssetTransaction? {
    val date = localDateFromIsoDateDigits(dateDigits) ?: return null
    val draftId = id ?: 0L

    return when (category) {
        InvestmentCategory.VARIABLE_INCOME -> {
            val quantity = quantity.toDoubleOrNull() ?: return null
            val unitPrice = unitPrice.toDoubleOrNull() ?: return null
            VariableIncomeTransaction(
                id = draftId,
                holding = holding,
                date = date,
                type = type,
                quantity = quantity,
                unitPrice = unitPrice,
                observations = observations.ifBlank { null },
            )
        }

        InvestmentCategory.FIXED_INCOME -> {
            val total = totalValue.toDoubleOrNull() ?: return null
            FixedIncomeTransaction(
                id = draftId,
                holding = holding,
                date = date,
                type = type,
                totalValue = total,
                observations = observations.ifBlank { null },
            )
        }

        InvestmentCategory.INVESTMENT_FUND -> {
            val total = totalValue.toDoubleOrNull() ?: return null
            FundsTransaction(
                id = draftId,
                holding = holding,
                date = date,
                type = type,
                totalValue = total,
                observations = observations.ifBlank { null },
            )
        }
    }
}

private fun Long.resolveUpsertId(existingId: Long?): Long =
    if (this == -1L) {
        existingId ?: error("Upsert retornou -1 para inserção sem id prévio")
    } else {
        this
    }
