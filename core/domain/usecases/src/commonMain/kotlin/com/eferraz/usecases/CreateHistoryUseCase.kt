package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.strategies.CopyHistoryStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class CreateHistoryUseCase(
    private val strategies: List<CopyHistoryStrategy>,
    private val repository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CreateHistoryUseCase.Param, HoldingHistoryEntry>(context) {

    public data class Param(val referenceDate: YearMonth, val holding: AssetHolding)

    override suspend fun execute(param: Param): HoldingHistoryEntry {

        if (param.referenceDate <= YearMonth(2025, 10))
            return HoldingHistoryEntry(holding = param.holding, referenceDate = param.referenceDate)

        return strategies
            .firstOrNull { it.canHandle(param.holding) }
            ?.create(param.referenceDate, param.holding)
            ?.also { repository.upsert(it) }
            ?: HoldingHistoryEntry(holding = param.holding, referenceDate = param.referenceDate)
    }
}

