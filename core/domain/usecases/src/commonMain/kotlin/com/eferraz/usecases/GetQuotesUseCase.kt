package com.eferraz.usecases

import com.eferraz.entities.holdings.StockQuoteHistory
import com.eferraz.usecases.repositories.StockQuoteHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class GetQuotesUseCase(
    private val repository: StockQuoteHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetQuotesUseCase.Params, StockQuoteHistory>(context) {

    public data class Params(val ticker: String, val referenceDate: YearMonth? = null)

    override suspend fun execute(param: Params): StockQuoteHistory {
        return if (param.referenceDate != null)
            repository.getQuote(ticker = param.ticker, referenceDate = param.referenceDate)
        else
            repository.getQuote(ticker = param.ticker)
    }
}