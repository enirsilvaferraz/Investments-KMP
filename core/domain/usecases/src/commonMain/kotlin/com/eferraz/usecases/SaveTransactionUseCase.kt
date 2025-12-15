package com.eferraz.usecases

import com.eferraz.entities.AssetTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SaveTransactionUseCase(
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<SaveTransactionUseCase.Param, Long>(context) {

    public data class Param(val transaction: AssetTransaction)

    override suspend fun execute(param: Param): Long {
        return assetTransactionRepository.save(param.transaction).getOrThrow()
    }
}
