package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.entities.HoldingHistoryResult
import com.eferraz.usecases.holdings.CreateHistoryUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import org.koin.core.annotation.Factory

@Factory
public class MergeHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val createHistoryUseCase: CreateHistoryUseCase,
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<MergeHistoryUseCase.Param, List<HoldingHistoryResult>>(context) {

    public data class Param(
        val referenceDate: YearMonth,
        val category: InvestmentCategory
    )

    override suspend fun execute(param: Param): List<HoldingHistoryResult> {

        val holdings = assetHoldingRepository.getByCategory(param.category)
//            .filter { holding ->
//                when (val asset = holding.asset) {
//                    is FixedIncomeAsset -> asset.expirationDate < LocalDate(2026, 12, 30) || asset.liquidity == Liquidity.DAILY || asset.issuer.isInLiquidation || asset.observations?.contains("FGTS") ?: false
//                    else -> false
//                }
//            }

        val previos = mapByReferenceDate(param.referenceDate.minusMonth(), holdings)
        val current = mapByReferenceDate(param.referenceDate, holdings)

        return holdings.map { holding -> // TODO melhorar a performance

            val currentEntry = current[holding] ?: create(param.referenceDate, holding)
            val previousEntry = previos[holding] ?: create(param.referenceDate.minusMonth(), holding)

            val startDate = LocalDate(param.referenceDate.year, param.referenceDate.month, 1)
            val endDate = startDate.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
            val transactions = assetTransactionRepository.getAllByHoldingAndDateRange(holding, startDate, endDate)

            val balance = TransactionBalance.calculate(transactions)

            val appreciation = Appreciation.calculate(
                previousValue = previousEntry.endOfMonthValue,
                currentValue = currentEntry.endOfMonthValue,
                contributions = balance.contributions,
                withdrawals = balance.withdrawals
            )

            HoldingHistoryResult(holding, currentEntry, previousEntry, appreciation)
        }.also { println("Enir: ${it.size}") }
    }

    private suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry =
        createHistoryUseCase(CreateHistoryUseCase.Param(referenceDate, holding)).getOrThrow()

    private suspend fun mapByReferenceDate(referenceDate: YearMonth, holdings: List<AssetHolding>): Map<AssetHolding, HoldingHistoryEntry?> {

        val histories: Map<AssetHolding, HoldingHistoryEntry> = holdingHistoryRepository.getByReferenceDate(referenceDate).associateBy(
            keySelector = { historyEntry -> historyEntry.holding },
            valueTransform = { historyEntry -> historyEntry }
        )

        return holdings.associateBy(
            keySelector = { holding -> holding },
            valueTransform = { holding -> histories[holding] }
        )
    }
}