package com.eferraz.presentation.features.assetForm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.entities.AssetFormData
import com.eferraz.usecases.entities.FixedIncomeFormData
import com.eferraz.usecases.GetAssetByIdUseCase
import com.eferraz.usecases.GetBrokeragesUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.entities.InvestmentFundFormData
import com.eferraz.usecases.SaveAssetUseCase
import com.eferraz.usecases.ValidateException
import com.eferraz.usecases.entities.VariableIncomeFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetFormViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val saveAssetUseCase: SaveAssetUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
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
            val issuers = getIssuersUseCase()
            val brokerages = getBrokeragesUseCase()
            _state.update {
                it.copy(
                    issuers = issuers.map { it.name },
                    brokerages = brokerages.map { it.name }
                )
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            try {
                val id: Long? = saveAssetUseCase(_state.value.formData)
                clearForm()
                _state.update { it.copy(message = "Ativo $id salvo com sucesso!", shouldCloseForm = true) }
            } catch (e: ValidateException) {
                _state.update { it.copy(validationErrors = e.messages) }
            } catch (e: Exception) {
                _state.update { it.copy(message = "Erro ao salvar: ${e.message}") }
            }
        }
    }

    private fun loadAssetForEdit(assetId: Long) {
        viewModelScope.launch {
            val asset = getAssetByIdUseCase(assetId) ?: return@launch

            if (_state.value.issuers.isEmpty() || _state.value.brokerages.isEmpty()) {
                loadData()
            }

            val formData = assetToFormDataMapper.toFormData(asset)
            _state.update {
                it.copy(
                    formData = formData,
                    isEditMode = true,
                    validationErrors = emptyMap(),
                    message = null
                )
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
