package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetTransactionsByHoldingUseCase(
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetTransactionsByHoldingUseCase.Param, List<AssetTransaction>>(context) {

    public data class Param(val holding: AssetHolding)

    override suspend fun execute(param: Param): List<AssetTransaction> {
        return assetTransactionRepository.getAllByHolding(param.holding)
    }
}
