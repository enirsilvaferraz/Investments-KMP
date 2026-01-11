package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.Brokerage
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.Issuer
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.GetAssetUseCase
import com.eferraz.usecases.GetAssetsTableDataUseCase
import com.eferraz.usecases.GetBrokeragesUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import com.eferraz.usecases.SaveAssetUseCase2.Params
import com.eferraz.usecases.SetBrokerageToHoldingUseCase
import com.eferraz.usecases.entities.AssetsTableData
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val getAssetsTableDataUseCase: GetAssetsTableDataUseCase,
    private val getAssetUseCase: GetAssetUseCase,
    private val saveAssetUseCase: SaveAssetUseCase2,
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val setBrokerageToHoldingUseCase: SetBrokerageToHoldingUseCase,
) : ViewModel() {

    internal val state: StateFlow<AssetsState>
        get() = _state

    private val _state = MutableStateFlow(AssetsState())

    internal fun onIntent(intent: AssetsIntent) = viewModelScope.launch {

        when (intent) {

            is AssetsIntent.UpdateAsset -> {
                saveAssetUseCase(Params(intent.asset))
                // Recarregar dados após atualização
                loadAssets(intent.category)
            }

            is AssetsIntent.UpdateBrokerage -> {
                setBrokerageToHoldingUseCase(SetBrokerageToHoldingUseCase.Param(assetId = intent.assetId, brokerage = intent.brokerage))
                // Recarregar dados após atualização
                loadAssets(intent.category)
            }
        }
    }

    internal fun loadAssets(category: InvestmentCategory) = viewModelScope.launch {

        val tableData = async { 
            getAssetsTableDataUseCase(GetAssetsTableDataUseCase.Param(category)).getOrNull() ?: emptyList() 
        }
        val issuers = async { 
            getIssuersUseCase(GetIssuersUseCase.Param).getOrNull() ?: emptyList() 
        }
        val brokerages = async { 
            getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull() ?: emptyList() 
        }

        _state.value = AssetsState(
            tableData = tableData.await(),
            issuers = issuers.await(),
            brokerages = brokerages.await(),
        )
    }

    internal fun updateFixedIncomeAsset(
        assetId: Long,
        category: InvestmentCategory,
        update: (FixedIncomeAsset) -> FixedIncomeAsset
    ) = viewModelScope.launch {
        val asset = getAssetUseCase(GetAssetUseCase.ById(assetId)).getOrNull() as? FixedIncomeAsset
        asset?.let { updatedAsset ->
            onIntent(AssetsIntent.UpdateAsset(update(updatedAsset), category))
        }
    }

    internal fun updateVariableIncomeAsset(
        assetId: Long,
        category: InvestmentCategory,
        update: (VariableIncomeAsset) -> VariableIncomeAsset
    ) = viewModelScope.launch {
        val asset = getAssetUseCase(GetAssetUseCase.ById(assetId)).getOrNull() as? VariableIncomeAsset
        asset?.let { updatedAsset ->
            onIntent(AssetsIntent.UpdateAsset(update(updatedAsset), category))
        }
    }

    internal fun updateInvestmentFundAsset(
        assetId: Long,
        category: InvestmentCategory,
        update: (InvestmentFundAsset) -> InvestmentFundAsset
    ) = viewModelScope.launch {
        val asset = getAssetUseCase(GetAssetUseCase.ById(assetId)).getOrNull() as? InvestmentFundAsset
        asset?.let { updatedAsset ->
            onIntent(AssetsIntent.UpdateAsset(update(updatedAsset), category))
        }
    }

    internal sealed interface AssetsIntent {
        data class UpdateAsset(val asset: Asset, val category: InvestmentCategory) : AssetsIntent
        data class UpdateBrokerage(val assetId: Long, val brokerage: Brokerage?, val category: InvestmentCategory) : AssetsIntent
    }

    internal data class AssetsState(
        val tableData: List<AssetsTableData> = emptyList(),
        val issuers: List<Issuer> = emptyList(),
        val brokerages: List<Brokerage> = emptyList(),
    )
}