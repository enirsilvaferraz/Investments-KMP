package com.eferraz.usecases.cruds

import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class GetTransactionsUseCase(
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetTransactionsUseCase.Params, List<AssetTransaction>>(context) {

    public sealed interface Params
    public data class ReferenceDate(val date: YearMonth) : Params

    override suspend fun execute(param: Params): List<AssetTransaction> = when (param) {
        is ReferenceDate -> assetTransactionRepository.getByReferenceDate(param.date)
    }
}