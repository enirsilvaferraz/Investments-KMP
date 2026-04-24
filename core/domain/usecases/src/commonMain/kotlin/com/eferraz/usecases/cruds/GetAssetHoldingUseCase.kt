package com.eferraz.usecases.cruds

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetAssetHoldingUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetAssetHoldingUseCase.Param, AssetHolding?>(context) {

    public sealed interface Param
    public data class ById(val id: Long) : Param

    override suspend fun execute(param: Param): AssetHolding? =
        when (param) {
            is ById -> assetHoldingRepository.getById(param.id)
        }
}
