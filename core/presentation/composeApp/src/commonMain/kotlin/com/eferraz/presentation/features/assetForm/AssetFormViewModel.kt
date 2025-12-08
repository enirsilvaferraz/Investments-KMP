package com.eferraz.presentation.features.assetForm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity
import com.eferraz.usecases.FixedIncomeFormData
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.SaveFixedIncomeAssetUseCase
import com.eferraz.usecases.ValidateException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

internal sealed class AssetFormIntent {
    data object LoadInitialData : AssetFormIntent()
    data class UpdateCategory(val category: InvestmentCategory) : AssetFormIntent()
    data class UpdateType(val type: FixedIncomeAssetType?) : AssetFormIntent()
    data class UpdateSubType(val subType: FixedIncomeSubType?) : AssetFormIntent()
    data class UpdateExpirationDate(val date: String?) : AssetFormIntent()
    data class UpdateContractedYield(val yield: String) : AssetFormIntent()
    data class UpdateCdiRelativeYield(val yield: String) : AssetFormIntent()
    data class UpdateLiquidity(val liquidity: Liquidity?) : AssetFormIntent()
    data class UpdateIssuerName(val name: String) : AssetFormIntent()
    data class UpdateObservations(val observations: String) : AssetFormIntent()
    data object Save : AssetFormIntent()
    data object ClearForm : AssetFormIntent()
}

@KoinViewModel
internal class AssetFormViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val saveAssetUseCase: SaveFixedIncomeAssetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetFormState())
    val state = _state.asStateFlow()

    init {
        processIntent(AssetFormIntent.LoadInitialData)
    }

    fun processIntent(intent: AssetFormIntent) {
        when (intent) {
            is AssetFormIntent.LoadInitialData -> loadData()
            is AssetFormIntent.UpdateCategory -> _state.update { it.copy(formData = it.formData.copy(category = intent.category)) }
            is AssetFormIntent.UpdateType -> _state.update { it.copy(formData = it.formData.copy(type = intent.type)) }
            is AssetFormIntent.UpdateSubType -> _state.update { it.copy(formData = it.formData.copy(subType = intent.subType)) }
            is AssetFormIntent.UpdateExpirationDate -> _state.update { it.copy(formData = it.formData.copy(expirationDate = intent.date)) }
            is AssetFormIntent.UpdateContractedYield -> _state.update { it.copy(formData = it.formData.copy(contractedYield = intent.yield)) }
            is AssetFormIntent.UpdateCdiRelativeYield -> _state.update { it.copy(formData = it.formData.copy(cdiRelativeYield = intent.yield)) }
            is AssetFormIntent.UpdateLiquidity -> _state.update { it.copy(formData = it.formData.copy(liquidity = intent.liquidity)) }
            is AssetFormIntent.UpdateIssuerName -> _state.update { it.copy(formData = it.formData.copy(issuerName = intent.name)) }
            is AssetFormIntent.UpdateObservations -> _state.update { it.copy(formData = it.formData.copy(observations = intent.observations)) }
            is AssetFormIntent.Save -> save()
            is AssetFormIntent.ClearForm -> clearForm()
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
                saveAssetUseCase(_state.value.formData)

                // Limpar formulário
                clearForm()

                _state.update { it.copy(message = "Ativo salvo com sucesso!") }

            } catch (e: ValidateException) {
                _state.update { it.copy(validationErrors = e.messages) }
            } catch (e: Exception) {
                _state.update { it.copy(message = "Erro ao salvar: ${e.message}") }
            }
        }
    }

    private fun clearForm() {
        _state.update {
            AssetFormState(issuers = it.issuers)
        }
    }

    data class AssetFormState(
        val issuers: List<String> = emptyList(),
        val formData: FixedIncomeFormData = FixedIncomeFormData(),
        val validationErrors: Map<String, String> = emptyMap(),
        val message: String? = null,
    )
}

