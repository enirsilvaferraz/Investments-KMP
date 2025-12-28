package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SaveAssetUseCase2(
    private val repository: AssetRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<SaveAssetUseCase2.Params, Unit>(context) {

    public data class Params(val model: Asset)

    override suspend fun execute(param: Params) {
        repository.save(param.model)
    }
}