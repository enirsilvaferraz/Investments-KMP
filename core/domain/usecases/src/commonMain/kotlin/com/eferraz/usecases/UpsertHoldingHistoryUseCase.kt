package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class UpsertHoldingHistoryUseCase(
    private val repository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpsertHoldingHistoryUseCase.Params, Long>(context) {

    public data class Params(
        val entryId: Long?,
        val holding: AssetHolding,
        val referenceDate: YearMonth,
        val endOfMonthValue: Double,
        val endOfMonthQuantity: Double = 0.0,
    )

    override suspend fun execute(param: Params): Long {

        if (param.holding.asset is VariableIncomeAsset) throw IllegalArgumentException("Asset must not be a VariableIncomeAsset")

        val entry = HoldingHistoryEntry(
            id = param.entryId,
            holding = param.holding,
            referenceDate = param.referenceDate,
            endOfMonthValue = param.endOfMonthValue,
            endOfMonthQuantity = param.endOfMonthQuantity
        )

        return repository.upsert(entry)
    }
}