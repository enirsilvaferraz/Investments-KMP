package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.GetAssetsUseCase
import com.eferraz.usecases.GetBrokeragesUseCase
import com.eferraz.usecases.GetIssuersUseCase
import com.eferraz.usecases.SaveAssetUseCase2
import com.eferraz.usecases.SaveAssetUseCase2.Params
import com.eferraz.usecases.SetBrokerageToHoldingUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val getAssetsUseCase: GetAssetsUseCase,
    private val saveAssetUseCase: SaveAssetUseCase2,
    private val getIssuersUseCase: GetIssuersUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val setBrokerageToHoldingUseCase: SetBrokerageToHoldingUseCase,
    private val assetHoldingRepository: AssetHoldingRepository,
) : ViewModel() {

    internal val state: StateFlow<AssetsState>
        field = MutableStateFlow(AssetsState())

    internal fun onIntent(intent: AssetsIntent) = viewModelScope.launch {

        when (intent) {

            is AssetsIntent.UpdateAsset -> {
                saveAssetUseCase(Params(intent.asset))
            }

            is AssetsIntent.UpdateBrokerage -> {
                setBrokerageToHoldingUseCase(SetBrokerageToHoldingUseCase.Param(assetId = intent.assetId, brokerage = intent.brokerage))
                state.update { currentState -> currentState.copy(assetBrokerages = reloadMap()) }
            }
        }
    }

    internal fun loadAssets(category: InvestmentCategory) = viewModelScope.launch {

        val issuers = async { getIssuersUseCase(GetIssuersUseCase.Param).getOrNull() ?: emptyList() }
        val assets = async { getAssetsUseCase(GetAssetsUseCase.ByCategory(category)).getOrNull() ?: emptyList() }
        val brokerages = async { getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull() ?: emptyList() }
        val assetBrokeragesMap = async { reloadMap() }

        state.value = AssetsState(
            list = assets.await(),
            issuers = issuers.await(),
            brokerages = brokerages.await(),
            assetBrokerages = assetBrokeragesMap.await()
        )
    }

    private suspend fun reloadMap() =
        assetHoldingRepository.getAll().associate { it.asset.id to it.brokerage }
}