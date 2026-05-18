package com.eferraz.usecases

import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class DeleteTransactionUseCase(
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<DeleteTransactionUseCase.Param, Unit>(context) {

    public data class Param(val id: Long)

    override suspend fun execute(param: Param) {
        assetTransactionRepository.delete(param.id)
    }
}
