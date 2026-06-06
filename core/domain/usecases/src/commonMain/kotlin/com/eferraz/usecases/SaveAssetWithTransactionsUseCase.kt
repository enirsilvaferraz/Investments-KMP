package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.usecases.cruds.UpsertAssetUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SaveAssetWithTransactionsUseCase(
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<SaveAssetWithTransactionsUseCase.Param, Unit>(context) {

    public data class Param(val holding: AssetHolding)

    override suspend fun execute(param: Param) {
        val savedAsset = upsertAssetUseCase(UpsertAssetUseCase.Param(param.holding.asset)).getOrThrow()
        assetHoldingRepository.upsertWithTransactions(
            param.holding.copy(asset = savedAsset),
        )
    }
}
