package com.eferraz.usecases

import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.entities.HoldingHistoryResult
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
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<MergeHistoryUseCase.Param, List<HoldingHistoryResult>>(context) {

    public data class Param(
        val referenceDate: YearMonth
    )

    override suspend fun execute(param: Param): List<HoldingHistoryResult> {

        val holdings = assetHoldingRepository.getAll()
        val previousDate = param.referenceDate.minusMonth()

        val previous = holdingHistoryRepository.getByReferenceDate(previousDate).associateBy { it.holding.id }
        val current = holdingHistoryRepository.getByReferenceDate(param.referenceDate).associateBy { it.holding.id }

        return holdings.map { holding ->

            val currentEntry = current[holding.id] ?: defaultEntry(holding, param.referenceDate)
            val previousEntry = previous[holding.id] ?: defaultEntry(holding, previousDate)

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

    private fun defaultEntry(holding: AssetHolding, referenceDate: YearMonth): HoldingHistoryEntry =
        HoldingHistoryEntry(holding = holding, referenceDate = referenceDate)
}
