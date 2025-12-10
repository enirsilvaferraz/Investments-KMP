package com.eferraz.usecases.repositories

import com.eferraz.entities.StockQuoteHistory

public interface StockQuoteHistoryRepository {

    public suspend fun getQuote(ticker: String): StockQuoteHistory
}

