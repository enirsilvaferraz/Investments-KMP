package com.eferraz.usecases

import com.eferraz.entities.StockQuoteHistory
import com.eferraz.usecases.repositories.StockQuoteHistoryRepository
import org.koin.core.annotation.Factory

@Factory
public class GetQuotesUseCase(
    private val repository: StockQuoteHistoryRepository,
) {

    public suspend operator fun invoke(ticker: String): StockQuoteHistory {
        return repository.getQuote(ticker = ticker)
    }
}