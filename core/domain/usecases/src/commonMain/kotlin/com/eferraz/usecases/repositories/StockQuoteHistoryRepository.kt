package com.eferraz.usecases.repositories

import com.eferraz.entities.holdings.StockQuoteHistory
import kotlinx.datetime.YearMonth

public interface StockQuoteHistoryRepository {

    public suspend fun getQuote(ticker: String): StockQuoteHistory

    public suspend fun getQuote(ticker: String, referenceDate: YearMonth): StockQuoteHistory
}

