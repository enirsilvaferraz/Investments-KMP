package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Issuer
import com.eferraz.usecases.GetAssetsUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import com.eferraz.usecases.SaveAssetUseCase2.Params
import kotlinx.coroutines.Job
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

    internal fun onIntent(intent: AssetsIntent): Job = viewModelScope.launch {
        when (intent) {
            is UpdateAsset -> saveUseCase(Params(intent.asset))
        }
    }

    internal fun loadAssets(category: InvestmentCategory) {
        viewModelScope.launch {
            // Load assets
            getUseCase(GetAssetsUseCase.ByCategory(category))
                .onSuccess { assets ->
                    // Load issuers
                    val issuers = getIssuersUseCase()
                    state.value = AssetsState(assets, issuers)
                }
                .onFailure { println("Error: $it") }
        }
    }

    internal data class AssetsState(
        val list: List<Asset>,
        val issuers: List<Issuer>,
    )

    internal sealed interface AssetsIntent
    internal data class UpdateAsset(val asset: Asset) : AssetsIntent
}