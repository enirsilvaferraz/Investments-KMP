package com.eferraz.usecases.cruds

import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class GetHoldingHistoriesUseCase(
    private val historyRepository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetHoldingHistoriesUseCase.Params, List<HoldingHistoryEntry>>(context) {

    public sealed interface Params
    public data class ByReferenceDate(val referenceDate: YearMonth) : Params

    override suspend fun execute(param: Params): List<HoldingHistoryEntry> = when (param) {
        is ByReferenceDate -> historyRepository.getByReferenceDate(param.referenceDate)
    }
}
