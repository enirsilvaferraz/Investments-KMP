package com.eferraz.usecases

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.entities.HoldingHistoryResult
import com.eferraz.usecases.holdings.CreateHistoryUseCase
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
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<MergeHistoryUseCase.Param, List<HoldingHistoryResult>>(context) {

    public data class Param(
        val referenceDate: YearMonth,
        val assetClass: AssetClass?
    )

    override suspend fun execute(param: Param): List<HoldingHistoryResult> {

        val holdings = assetHoldingRepository.getAll()

        val previos = mapByReferenceDate(param.referenceDate.minusMonth(), holdings)
        val current = mapByReferenceDate(param.referenceDate, holdings)

        return holdings.map { holding ->
            val currentEntry = current[holding.id] ?: create(param.referenceDate, holding)
            val previousEntry = previos[holding.id] ?: create(param.referenceDate.minusMonth(), holding)

            val monthTransactions = holding.transactions.filter {
                it.date.year == param.referenceDate.year && it.date.month == param.referenceDate.month
            }
            val balance = TransactionBalance.calculate(monthTransactions)

            val appreciation = Appreciation.calculate(
                previousValue = previousEntry.endOfMonthValue,
                currentValue = currentEntry.endOfMonthValue,
                contributions = balance.contributions,
                withdrawals = balance.withdrawals
            )

            HoldingHistoryResult(holding, currentEntry, previousEntry, appreciation)
        }
    }

    private suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry =
        createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

    private suspend fun mapByReferenceDate(
        referenceDate: YearMonth,
        holdings: List<AssetHolding>
    ): Map<Long, HoldingHistoryEntry?> {

        val historiesByHoldingId = holdingHistoryRepository.getByReferenceDate(referenceDate)
            .associateBy { it.holding.id }

        return holdings.associateBy(
            keySelector = { it.id },
            valueTransform = { historiesByHoldingId[it.id] }
        )
    }
}
