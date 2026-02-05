package com.eferraz.presentation.features.assetForm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.usecases.SaveAssetUseCase
import com.eferraz.usecases.cruds.GetAssetUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetFinancialGoalsUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.entities.AssetFormData
import com.eferraz.usecases.entities.FixedIncomeFormData
import com.eferraz.usecases.entities.InvestmentFundFormData
import com.eferraz.usecases.entities.VariableIncomeFormData
import com.eferraz.usecases.exceptions.ValidateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AssetFormViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getFinancialGoalsUseCase: GetFinancialGoalsUseCase,
    private val saveAssetUseCase: SaveAssetUseCase,
    private val getAssetUseCase: GetAssetUseCase,
    private val assetToFormDataMapper: AssetToFormDataMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetFormState())
    val state = _state.asStateFlow()

    init {
        processIntent(AssetFormIntent.LoadInitialData)
    }

    fun processIntent(intent: AssetFormIntent) {
        when (intent) {
            is AssetFormIntent.LoadInitialData -> loadData()
            is AssetFormIntent.UpdateCategory -> updateCategory(intent.category)
            is AssetFormIntent.LoadAssetForEdit -> loadAssetForEdit(intent.assetId)
            is AssetFormIntent.Save -> save()
            is AssetFormIntent.ClearForm -> clearForm()
            is AssetFormIntent.ResetCloseFlag -> resetCloseFlag()

            // Fixed Income
            is AssetFormIntent.UpdateType -> updateFixedIncome { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateSubType -> updateFixedIncome { it.copy(subType = intent.subType) }
            is AssetFormIntent.UpdateExpirationDate -> updateFixedIncome { it.copy(expirationDate = intent.date) }
            is AssetFormIntent.UpdateContractedYield -> updateFixedIncome { it.copy(contractedYield = intent.yield) }
            is AssetFormIntent.UpdateCdiRelativeYield -> updateFixedIncome { it.copy(cdiRelativeYield = intent.yield) }
            is AssetFormIntent.UpdateLiquidity -> updateFixedIncome { it.copy(liquidity = intent.liquidity) }

            // Investment Fund
            is AssetFormIntent.UpdateFundType -> updateInvestmentFund { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateFundName -> updateInvestmentFund { it.copy(name = intent.name) }
            is AssetFormIntent.UpdateLiquidityDays -> updateInvestmentFund { it.copy(liquidityDays = intent.days) }
            is AssetFormIntent.UpdateFundExpirationDate -> updateInvestmentFund { it.copy(expirationDate = intent.date) }

            // Variable Income
            is AssetFormIntent.UpdateVariableType -> updateVariableIncome { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateTicker -> updateVariableIncome { it.copy(ticker = intent.ticker) }

            // Common
            is AssetFormIntent.UpdateIssuerName -> updateCommon {
                when (it) {
                    is FixedIncomeFormData -> it.copy(issuerName = intent.name)
                    is InvestmentFundFormData -> it.copy(issuerName = intent.name)
                    is VariableIncomeFormData -> it.copy(issuerName = intent.name)
                }
            }

            is AssetFormIntent.UpdateObservations -> updateCommon {
                when (it) {
                    is FixedIncomeFormData -> it.copy(observations = intent.observations)
                    is InvestmentFundFormData -> it.copy(observations = intent.observations)
                    is VariableIncomeFormData -> it.copy(observations = intent.observations)
                }
            }

            is AssetFormIntent.UpdateBrokerageName -> updateCommon {
                when (it) {
                    is FixedIncomeFormData -> it.copy(brokerageName = intent.name)
                    is InvestmentFundFormData -> it.copy(brokerageName = intent.name)
                    is VariableIncomeFormData -> it.copy(brokerageName = intent.name)
                }
            }

            is AssetFormIntent.UpdateGoalName -> updateCommon {
                when (it) {
                    is FixedIncomeFormData -> it.copy(goalName = intent.name)
                    is InvestmentFundFormData -> it.copy(goalName = intent.name)
                    is VariableIncomeFormData -> it.copy(goalName = intent.name)
                }
            }
        }
    }

    private fun updateCategory(category: InvestmentCategory) {
        _state.update { state -> state.copy(formData = AssetFormData.build(category)) }
    }

    private inline fun <reified T : AssetFormData> updateFormField(
        crossinline update: (T) -> AssetFormData,
    ) {
        _state.update { state ->
            when (val formData = state.formData) {
                is T -> state.copy(formData = update(formData))
                else -> state
            }
        }
    }

    private fun updateFixedIncome(update: (FixedIncomeFormData) -> AssetFormData) {
        updateFormField<FixedIncomeFormData>(update)
    }

    private fun updateInvestmentFund(update: (InvestmentFundFormData) -> AssetFormData) {
        updateFormField<InvestmentFundFormData>(update)
    }

    private fun updateVariableIncome(update: (VariableIncomeFormData) -> AssetFormData) {
        updateFormField<VariableIncomeFormData>(update)
    }

    private fun updateCommon(update: (AssetFormData) -> AssetFormData) {
        _state.update { state ->
            state.copy(formData = update(state.formData))
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val issuers = getIssuersUseCase(GetIssuersUseCase.Param).getOrElse { emptyList() }
            val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrElse { emptyList() }
            val goals = getFinancialGoalsUseCase(GetFinancialGoalsUseCase.All).getOrElse { emptyList() }
            _state.update {
                it.copy(
                    issuers = issuers.map { it.name },
                    brokerages = brokerages.map { it.name },
                    goals = goals.map { it.name }
                )
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            saveAssetUseCase(SaveAssetUseCase.Param(_state.value.formData))
                .onSuccess { id ->
                    clearForm()
                    _state.update { it.copy(message = "Ativo $id salvo com sucesso!", shouldCloseForm = true) }
                }
                .onFailure { error ->
                    when (error) {
                        is ValidateException -> {
                            _state.update { it.copy(validationErrors = error.messages) }
                        }

                        else -> {
                            _state.update { it.copy(message = "Erro ao salvar: ${error.message}") }
                        }
                    }
                }
        }
    }

    private fun loadAssetForEdit(assetId: Long) {

        viewModelScope.launch {

            if (_state.value.issuers.isEmpty() || _state.value.brokerages.isEmpty() || _state.value.goals.isEmpty()) {
                loadData()
            }

            getAssetUseCase(GetAssetUseCase.ById(assetId))
                .onSuccess { asset ->
                    _state.update {
                        it.copy(
                            formData = assetToFormDataMapper.toFormData(asset),
                            isEditMode = true,
                            validationErrors = emptyMap(),
                            message = null
                        )
                    }
                }
        }
    }

    private fun clearForm() {
        _state.update { state ->
            val newFormData = when (val category = state.formData.category) {
                InvestmentCategory.FIXED_INCOME -> FixedIncomeFormData(category = category)
                InvestmentCategory.INVESTMENT_FUND -> InvestmentFundFormData(category = category)
                InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFormData(category = category)
                null -> FixedIncomeFormData()
            }
            AssetFormState(
                issuers = state.issuers,
                brokerages = state.brokerages,
                goals = state.goals,
                formData = newFormData,
                isEditMode = false,
                shouldCloseForm = false
            )
        }
    }

    private fun resetCloseFlag() {
        _state.update { it.copy(shouldCloseForm = false) }
    }
}
