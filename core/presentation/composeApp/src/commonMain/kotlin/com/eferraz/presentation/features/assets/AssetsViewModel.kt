package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity
import com.eferraz.entities.value.MaturityDate
import com.eferraz.usecases.SaveAssetUseCase2.Params
import com.eferraz.usecases.GetAssetsUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val getUseCase: GetAssetsUseCase,
    private val saveUseCase: SaveAssetUseCase2,
) : ViewModel() {

    internal val state: StateFlow<AssetsState>
        field = MutableStateFlow(AssetsState(emptyList()))

    internal fun onIntent(intent: AssetsIntent): Job = viewModelScope.launch {
        when (intent) {
            is UpdateMaturity -> saveUseCase(Params((intent.asset as FixedIncomeAsset).copy(expirationDate = intent.value.get())))
            is UpdateDescription -> saveUseCase(Params((intent.asset as FixedIncomeAsset).copy(observations = intent.value)))
            is UpdateLiquidity -> saveUseCase(Params((intent.asset as FixedIncomeAsset).copy(liquidity = intent.value)))
            is UpdateSubType -> saveUseCase(Params((intent.asset as FixedIncomeAsset).copy(subType = intent.value)))
        }
    }

    internal fun loadAssets(category: InvestmentCategory) {
        viewModelScope.launch {
            getUseCase(GetAssetsUseCase.ByCategory(category))
                .onSuccess { state.value = AssetsState(it) }
                .onFailure { println("Error: $it") }
        }
    }

    internal data class AssetsState(val list: List<Asset>)

    internal sealed interface AssetsIntent
    internal data class UpdateMaturity(val asset: Asset, val value: MaturityDate) : AssetsIntent
    internal data class UpdateDescription(val asset: Asset, val value: String) : AssetsIntent
    internal data class UpdateLiquidity(val asset: Asset, val value: Liquidity) : AssetsIntent
    internal data class UpdateSubType(val asset: Asset, val value: FixedIncomeSubType) : AssetsIntent
}