package com.eferraz.usecases

import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class UpdateFixedIncomeAndFundsHistoryValueUseCase(
    private val repository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpdateFixedIncomeAndFundsHistoryValueUseCase.Params, Long>(context) {

    public data class Params(
        val entry: HoldingHistoryEntry,
        val endOfMonthValue: Double = 0.0,
    )

    override suspend fun execute(param: Params): Long {

        if (param.entry.holding.asset is VariableIncomeAsset) throw IllegalArgumentException("Asset must not be a VariableIncomeAsset")

        return repository.upsert(
            param.entry.copy(
                endOfMonthValue = param.endOfMonthValue,
                endOfMonthQuantity = 1.0 // TODO Verificar mais tarde
            )
        )
    }
}