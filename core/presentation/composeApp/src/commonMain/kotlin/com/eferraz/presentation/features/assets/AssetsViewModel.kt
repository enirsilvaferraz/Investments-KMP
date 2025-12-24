package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class AssetsViewModel(
    private val repository: AssetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsState(emptyList()))
    val state = _state.asStateFlow()

//    init {
//        loadAssets()
//    }

    fun loadAssets(category: InvestmentCategory) {
        viewModelScope.launch {
            val assets = repository.getByType(category)
            _state.update { AssetsState(assets) }
        }
    }

    data class AssetsState(
        val list: List<Asset>,
    )
}