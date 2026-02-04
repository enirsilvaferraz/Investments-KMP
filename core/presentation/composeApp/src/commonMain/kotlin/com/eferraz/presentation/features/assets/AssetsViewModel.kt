package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.cruds.GetAssetUseCase
import com.eferraz.usecases.screens.GetAssetsTableDataUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetFinancialGoalsUseCase
import com.eferraz.usecases.cruds.GetIssuersUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import com.eferraz.usecases.SaveAssetUseCase2.Params
import com.eferraz.usecases.SetBrokerageToHoldingUseCase
import com.eferraz.usecases.SetGoalToHoldingUseCase
import com.eferraz.usecases.entities.AssetsTableData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
internal class AssetsViewModel(
    private val getAssetsTableDataUseCase: GetAssetsTableDataUseCase,
    private val getAssetUseCase: GetAssetUseCase,
    private val saveAssetUseCase: SaveAssetUseCase2,
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getFinancialGoalsUseCase: GetFinancialGoalsUseCase,
    private val setBrokerageToHoldingUseCase: SetBrokerageToHoldingUseCase,
    private val setGoalToHoldingUseCase: SetGoalToHoldingUseCase,
   @Provided private val category: InvestmentCategory
) : ViewModel() {

    private val _state = MutableStateFlow<AssetsState?>(null)

    internal val state: StateFlow<AssetsState?>
        get() = _state.asStateFlow()

    init {
        loadAssets(category)
    }

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

            is AssetsIntent.UpdateGoal -> {
                setGoalToHoldingUseCase(SetGoalToHoldingUseCase.Param(assetId = intent.assetId, goal = intent.goal))
                // Recarregar dados após atualização
                loadAssets(intent.category)
            }
        }
    }

    internal fun loadAssets(category: InvestmentCategory) = viewModelScope.launch {

        val tableData =
            getAssetsTableDataUseCase(GetAssetsTableDataUseCase.Param(category)).getOrNull() ?: emptyList() 

        val issuers =
            getIssuersUseCase(GetIssuersUseCase.Param).getOrNull() ?: emptyList() 

        val brokerages =
            getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull() ?: emptyList() 

        val goals =
            getFinancialGoalsUseCase(GetFinancialGoalsUseCase.All).getOrNull() ?: emptyList()


//        val tableData1 = tableData.await()
//        val issuers1 = issuers.await()
//        val brokerages1 = brokerages.await()
//        val goals1 = goals.await()
        
        _state.update {
            AssetsState(
                tableData = tableData,
                issuers = issuers,
                brokerages = brokerages,
                goals = goals,
            )
        }
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
        data class UpdateGoal(val assetId: Long, val goal: FinancialGoal?, val category: InvestmentCategory) : AssetsIntent
    }

    internal data class AssetsState(
        val tableData: List<AssetsTableData> = emptyList(),
        val issuers: List<Issuer> = emptyList(),
        val brokerages: List<Brokerage> = emptyList(),
        val goals: List<FinancialGoal> = emptyList(),
    )
}