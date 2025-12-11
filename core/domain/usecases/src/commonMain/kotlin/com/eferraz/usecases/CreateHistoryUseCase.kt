package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.repositories.StockQuoteHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

@Factory
public class CreateHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val quoteHistoryRepository: StockQuoteHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CreateHistoryUseCase.Param, HoldingHistoryEntry>(context) {

    public data class Param(val referenceDate: YearMonth, val holding: AssetHolding)

    override suspend fun execute(param: Param): HoldingHistoryEntry {

        return when (param.holding.asset) {
            is FixedIncomeAsset, is InvestmentFundAsset -> createFixedIncomeHistory(param.referenceDate, param.holding)
            is VariableIncomeAsset -> createVariableIncomeHistory(param.referenceDate, param.holding)
        } ?: HoldingHistoryEntry(holding = param.holding, referenceDate = param.referenceDate)
    }

    private suspend fun createFixedIncomeHistory(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {
        return holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)
    }

    private suspend fun createVariableIncomeHistory(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {

        val rv = holding.asset as? VariableIncomeAsset ?: return null
        val previous = holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding) ?: return null

        val quoteHistory = quoteHistoryRepository.getQuote(rv.ticker)
        val endOfMonthValue = quoteHistory.close ?: quoteHistory.adjustedClose ?: return null
        val endOfMonthQuantity = previous.endOfMonthQuantity

        return HoldingHistoryEntry(
            holding = holding,
            referenceDate = referenceDate,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity
        )
    }
}

