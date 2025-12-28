package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.GetAssetsUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import com.eferraz.usecases.SaveAssetUseCase2.Params
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val getUseCase: GetAssetsUseCase,
    private val saveUseCase: SaveAssetUseCase2,
    private val getIssuersUseCase: GetIssuersUseCase,
) : ViewModel() {

    internal val state: StateFlow<AssetsState>
        field = MutableStateFlow(AssetsState(emptyList(), emptyList()))

    internal fun onIntent(intent: AssetsIntent) = viewModelScope.launch {
        when (intent) {
            is AssetsIntent.UpdateAsset -> saveUseCase(Params(intent.asset))
        }
    }

    internal fun loadAssets(category: InvestmentCategory) = viewModelScope.launch {

        val issuers = async { getIssuersUseCase(GetIssuersUseCase.Param).getOrNull() ?: emptyList() }
        val assets = async { getUseCase(GetAssetsUseCase.ByCategory(category)).getOrNull() ?: emptyList() }

        state.value = AssetsState(assets.await(), issuers.await())
    }
}