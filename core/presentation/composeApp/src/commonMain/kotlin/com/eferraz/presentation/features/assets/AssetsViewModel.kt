package com.eferraz.presentation.features.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity
import com.eferraz.presentation.features.assets.SaveAssetUseCase2.*
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.GetAssetsUseCase
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Factory
import kotlin.jvm.JvmInline

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
}

@Factory
public class SaveAssetUseCase2(
    private val repository: AssetRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Params, Unit>(context) {

    public data class Params(val model: Asset)

    override suspend fun execute(param: Params) {
        repository.save(param.model)
    }
}

@JvmInline
public value class MaturityDate(private val value: String) {

    init {
        value.toDate()
    }

    public fun get(): LocalDate =
        value.toDate()

    private fun String.toDate() =
        LocalDate.Format { year(); monthNumber(); day() }.parse(this)
}

@JvmInline
public value class MandatoryText(private val value: String) {

    init {
        if (value.isEmpty()) throw IllegalArgumentException("MandatoryText cannot be empty")
    }

    public fun get(): String = value
}