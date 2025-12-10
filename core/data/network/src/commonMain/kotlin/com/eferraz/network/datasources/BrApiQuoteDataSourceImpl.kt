package com.eferraz.network.datasources

import com.eferraz.entities.StockQuoteHistory
import com.eferraz.network.TokenConfig
import com.eferraz.network.core.ClientConfig
import com.eferraz.network.responses.BrApiQuoteResponse
import com.eferraz.network.responses.QuoteResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory

@Factory(binds = [BrApiQuoteDataSource::class])
internal class BrApiQuoteDataSourceImpl(
    private val clientConfig: ClientConfig,
) : BrApiQuoteDataSource {

    override suspend fun getQuote(ticker: String): StockQuoteHistory {

        val url = "https://brapi.dev/api/quote/$ticker"

        return clientConfig.client.get(url){
            header(HttpHeaders.Authorization, "Bearer ${TokenConfig.BRAPI_TOKEN}")
        }.body<BrApiQuoteResponse>().results.map { result -> result.toModel() }.first()
    }

    override suspend fun getQuotesWithHistory(
        ticker: String,
        range: String,
        interval: String,
    ): List<StockQuoteHistory> {

        val url = "https://brapi.dev/api/quote/$ticker?range=$range&interval=$interval"

        return clientConfig.client.get(url) {
            header(HttpHeaders.Authorization, "Bearer ${TokenConfig.BRAPI_TOKEN}")
        }.body<BrApiQuoteResponse>().results.map { result -> result.toModel() }
    }

    private fun QuoteResult.toModel(): StockQuoteHistory = StockQuoteHistory(
        id = 0,
        ticker = symbol,
        date = regularMarketTime?.let { 
            Instant.parse(it).toLocalDateTime(TimeZone.UTC).date
        } ?: LocalDate(1970, 1, 1), // Data padrão caso regularMarketTime seja null
        open = regularMarketOpen,
        high = regularMarketDayHigh,
        low = regularMarketDayLow,
        close = regularMarketPrice,
        volume = regularMarketVolume,
        adjustedClose = regularMarketPrice
    )
}
