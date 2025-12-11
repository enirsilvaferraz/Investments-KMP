package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

@Factory
public class MergeHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val createHistoryUseCase: CreateHistoryUseCase,
    private val context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<MergeHistoryUseCase.Param, List<HoldingHistoryResult>>(context) {

    public data class Param(val referenceDate: YearMonth)

    override suspend fun execute(param: Param): List<HoldingHistoryResult> {

        val holdings = assetHoldingRepository.getAll()
        val previos = mapByReferenceDate(param.referenceDate.minusMonth(), holdings)
        val current = mapByReferenceDate(param.referenceDate, holdings)

        return holdings.map { holding ->
            val currentEntry = current[holding] ?: createHistoryUseCase(CreateHistoryUseCase.Param(param.referenceDate, holding)).getOrThrow()
            val previousEntry = previos[holding] ?: createHistoryUseCase(CreateHistoryUseCase.Param(param.referenceDate.minusMonth(), holding)).getOrThrow()
            HoldingHistoryResult(holding, currentEntry, previousEntry)
        }
    }

    private suspend fun mapByReferenceDate(
        referenceDate: YearMonth,
        holdings: List<AssetHolding>,
    ): Map<AssetHolding, HoldingHistoryEntry?> {

        val histories = holdingHistoryRepository.getByReferenceDate(referenceDate).associateBy(
            keySelector = { historyEntry -> historyEntry.holding },
            valueTransform = { historyEntry -> historyEntry }
        )

        return holdings.associateBy(
            keySelector = { holding -> holding },
            valueTransform = { holding -> histories[holding] }
        )
    }
}