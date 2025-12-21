package com.eferraz.repositories

import com.eferraz.entities.StockQuoteHistory
import com.eferraz.network.datasources.BrApiQuoteDataSource
import com.eferraz.usecases.repositories.StockQuoteHistoryRepository
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory(binds = [StockQuoteHistoryRepository::class])
internal class StockQuoteHistoryRepositoryImpl(
    private val dataSource: BrApiQuoteDataSource,
) : StockQuoteHistoryRepository {

    override suspend fun getQuote(ticker: String): StockQuoteHistory {
        return dataSource.getQuote(ticker)
    }

    override suspend fun getQuote(ticker: String, referenceDate: YearMonth): StockQuoteHistory {
        return dataSource.getQuote(ticker, referenceDate)
    }
}