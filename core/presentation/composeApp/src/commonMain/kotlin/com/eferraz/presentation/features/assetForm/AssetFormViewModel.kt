package com.eferraz.presentation.features.assetForm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.usecases.AssetFormData
import com.eferraz.usecases.FixedIncomeFormData
import com.eferraz.usecases.GetAssetByIdUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.InvestmentFundFormData
import com.eferraz.usecases.SaveAssetUseCase
import com.eferraz.usecases.ValidateException
import com.eferraz.usecases.VariableIncomeFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal sealed class AssetFormIntent {
    
    // Fixed Income
    data class UpdateType(val type: FixedIncomeAssetType?) : AssetFormIntent()
    data class UpdateSubType(val subType: FixedIncomeSubType?) : AssetFormIntent()
    data class UpdateExpirationDate(val date: String?) : AssetFormIntent()
    data class UpdateContractedYield(val yield: String) : AssetFormIntent()
    data class UpdateCdiRelativeYield(val yield: String) : AssetFormIntent()
    data class UpdateLiquidity(val liquidity: Liquidity?) : AssetFormIntent()
    
    // Investment Fund
    data class UpdateFundType(val type: InvestmentFundAssetType?) : AssetFormIntent()
    data class UpdateFundName(val name: String) : AssetFormIntent()
    data class UpdateLiquidityDays(val days: String) : AssetFormIntent()
    data class UpdateFundExpirationDate(val date: String?) : AssetFormIntent()
    
    // Variable Income
    data class UpdateVariableType(val type: VariableIncomeAssetType?) : AssetFormIntent()
    data class UpdateTicker(val ticker: String) : AssetFormIntent()
    
    // Common
    data class UpdateIssuerName(val name: String) : AssetFormIntent()
    data class UpdateObservations(val observations: String) : AssetFormIntent()

    // Actions
    data object LoadInitialData : AssetFormIntent()
    data class UpdateCategory(val category: InvestmentCategory) : AssetFormIntent()
    data class LoadAssetForEdit(val assetId: Long) : AssetFormIntent()
    data object Save : AssetFormIntent()
    data object ClearForm : AssetFormIntent()
    data object ResetCloseFlag : AssetFormIntent()
}

@KoinViewModel
internal class AssetFormViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val saveAssetUseCase: SaveAssetUseCase,
    private val getAssetByIdUseCase: GetAssetByIdUseCase,
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
            is AssetFormIntent.UpdateType -> updateFixedIncomeField { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateSubType -> updateFixedIncomeField { it.copy(subType = intent.subType) }
            is AssetFormIntent.UpdateExpirationDate -> updateFixedIncomeField { it.copy(expirationDate = intent.date) }
            is AssetFormIntent.UpdateContractedYield -> updateFixedIncomeField { it.copy(contractedYield = intent.yield) }
            is AssetFormIntent.UpdateCdiRelativeYield -> updateFixedIncomeField { it.copy(cdiRelativeYield = intent.yield) }
            is AssetFormIntent.UpdateLiquidity -> updateFixedIncomeField { it.copy(liquidity = intent.liquidity) }
            is AssetFormIntent.UpdateFundType -> updateInvestmentFundField { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateFundName -> updateInvestmentFundField { it.copy(name = intent.name) }
            is AssetFormIntent.UpdateLiquidityDays -> updateInvestmentFundField { it.copy(liquidityDays = intent.days) }
            is AssetFormIntent.UpdateFundExpirationDate -> updateInvestmentFundField { it.copy(expirationDate = intent.date) }
            is AssetFormIntent.UpdateVariableType -> updateVariableIncomeField { it.copy(type = intent.type) }
            is AssetFormIntent.UpdateTicker -> updateVariableIncomeField { it.copy(ticker = intent.ticker) }
            is AssetFormIntent.UpdateIssuerName -> updateCommonField { formData ->
                when (formData) {
                    is FixedIncomeFormData -> formData.copy(issuerName = intent.name)
                    is InvestmentFundFormData -> formData.copy(issuerName = intent.name)
                    is VariableIncomeFormData -> formData.copy(issuerName = intent.name)
                }
            }
            is AssetFormIntent.UpdateObservations -> updateCommonField { formData ->
                when (formData) {
                    is FixedIncomeFormData -> formData.copy(observations = intent.observations)
                    is InvestmentFundFormData -> formData.copy(observations = intent.observations)
                    is VariableIncomeFormData -> formData.copy(observations = intent.observations)
                }
            }
            is AssetFormIntent.Save -> save()
            is AssetFormIntent.ClearForm -> clearForm()
            is AssetFormIntent.ResetCloseFlag -> resetCloseFlag()
        }
    }

    private fun updateCategory(category: InvestmentCategory) {
        _state.update { state ->
            val newFormData = when (category) {
                InvestmentCategory.FIXED_INCOME -> FixedIncomeFormData(category = category)
                InvestmentCategory.INVESTMENT_FUND -> InvestmentFundFormData(category = category)
                InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFormData(category = category)
            }
            state.copy(formData = newFormData)
        }
    }

    private fun updateFixedIncomeField(update: (FixedIncomeFormData) -> FixedIncomeFormData) {
        _state.update { state ->
            when (val formData = state.formData) {
                is FixedIncomeFormData -> state.copy(formData = update(formData))
                else -> state
            }
        }
    }

    private fun updateInvestmentFundField(update: (InvestmentFundFormData) -> InvestmentFundFormData) {
        _state.update { state ->
            when (val formData = state.formData) {
                is InvestmentFundFormData -> state.copy(formData = update(formData))
                else -> state
            }
        }
    }

    private fun updateVariableIncomeField(update: (VariableIncomeFormData) -> VariableIncomeFormData) {
        _state.update { state ->
            when (val formData = state.formData) {
                is VariableIncomeFormData -> state.copy(formData = update(formData))
                else -> state
            }
        }
    }

    private fun updateCommonField(update: (AssetFormData) -> AssetFormData) {
        _state.update { state ->
            state.copy(formData = update(state.formData))
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val issuers = getIssuersUseCase()
            _state.update { it.copy(issuers = issuers.map { it.name }) }
        }
    }

    private fun save() {
        viewModelScope.launch {
            try {
                // Salvar usando UseCase
                val id: Long? = saveAssetUseCase(_state.value.formData)

                // Limpar formulário
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
            // Buscar asset atualizado do banco de dados
            val asset = getAssetByIdUseCase(assetId) ?: return@launch
            
            // Garantir que os emissores estão carregados
            if (_state.value.issuers.isEmpty()) {
                loadData()
            }
            
            val formData = when (asset) {
                is FixedIncomeAsset -> FixedIncomeFormData(
                    id = asset.id,
                    category = InvestmentCategory.FIXED_INCOME,
                    type = asset.type,
                    subType = asset.subType,
                    expirationDate = asset.expirationDate.toString(),
                    contractedYield = asset.contractedYield.toString(),
                    cdiRelativeYield = asset.cdiRelativeYield?.toString(),
                    liquidity = asset.liquidity,
                    issuerName = asset.issuer.name,
                    observations = asset.observations
                )
                is InvestmentFundAsset -> InvestmentFundFormData(
                    id = asset.id,
                    category = InvestmentCategory.INVESTMENT_FUND,
                    type = asset.type,
                    name = asset.name,
                    liquidityDays = asset.liquidityDays.toString(),
                    expirationDate = asset.expirationDate?.toString(),
                    issuerName = asset.issuer.name,
                    observations = asset.observations
                )
                is VariableIncomeAsset -> VariableIncomeFormData(
                    id = asset.id,
                    category = InvestmentCategory.VARIABLE_INCOME,
                    type = asset.type,
                    ticker = asset.ticker,
                    issuerName = asset.issuer.name,
                    observations = asset.observations
                )
            }
            
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
                formData = newFormData,
                isEditMode = false,
                shouldCloseForm = false
            )
        }
    }

    private fun resetCloseFlag() {
        _state.update { it.copy(shouldCloseForm = false) }
    }

    data class AssetFormState(
        val issuers: List<String> = emptyList(),
        val formData: AssetFormData = FixedIncomeFormData(), // TODO VERIFICAR
        val validationErrors: Map<String, String> = emptyMap(),
        val message: String? = null,
        val isEditMode: Boolean = false,
        val shouldCloseForm: Boolean = false,
    )
}
