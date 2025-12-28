package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetAssetsUseCase(
    private val assetRepository: AssetRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetAssetsUseCase.Param, List<Asset>>(context) {

    public sealed interface Param
    public object All : Param
    public data class ByCategory(val category: InvestmentCategory) : Param

    override suspend fun execute(param: Param): List<Asset> = when (param) {
        is All -> assetRepository.getAll()
        is ByCategory -> assetRepository.getByType(param.category)
    }
}