package com.eferraz.usecases

import com.eferraz.entities.assets.Asset
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetAssetUseCase(
    private val assetRepository: AssetRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetAssetUseCase.Param, Asset>(context) {

    public sealed interface Param
    public data class ById(val id: Long) : Param

    override suspend fun execute(param: Param): Asset = when (param) {
        is ById -> assetRepository.getById(param.id) ?: throw Exception("Asset not found")
    }
}

