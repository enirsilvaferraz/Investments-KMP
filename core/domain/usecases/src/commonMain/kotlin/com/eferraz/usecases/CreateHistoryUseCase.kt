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
    private val repository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CreateHistoryUseCase.Param, HoldingHistoryEntry>(context) {

    public data class Param(val referenceDate: YearMonth, val holding: AssetHolding)

    override suspend fun execute(param: Param): HoldingHistoryEntry {

        return (when (param.holding.asset) {
            is FixedIncomeAsset, is InvestmentFundAsset -> createFixedIncomeHistory(param.referenceDate, param.holding)
            is VariableIncomeAsset -> createVariableIncomeHistory(param.referenceDate, param.holding)
        } ?: HoldingHistoryEntry(holding = param.holding, referenceDate = param.referenceDate)).also {
            repository.upsert(it)
        }
    }

    private suspend fun createFixedIncomeHistory(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {
        return holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)
    }

    private suspend fun createVariableIncomeHistory(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {

        val rv = holding.asset as? VariableIncomeAsset ?: return null

        val quoteHistory = quoteHistoryRepository.getQuote(rv.ticker) // TODO Passar referenceDate por parametro
        val endOfMonthValue = quoteHistory.close ?: quoteHistory.adjustedClose ?: return null

        holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding)?.let {
            return it.copy(endOfMonthValue = endOfMonthValue).also {
                println(" - Clonando: $it")
            }
        }

        return (holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding))?.let {
            HoldingHistoryEntry(
                holding = holding,
                referenceDate = referenceDate,
                endOfMonthValue = endOfMonthValue,
                endOfMonthQuantity = it.endOfMonthQuantity
            ).also {
                println(" - Criando: $it")
            }
        }
    }
}

