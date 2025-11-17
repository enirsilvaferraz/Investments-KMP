package com.eferraz.presentation.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
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

    init {
        viewModelScope.launch {
            _state.update { AssetsState(repository.getAll()) }
        }
    }

    data class AssetsState(
        val list: List<Asset>,
    )
}