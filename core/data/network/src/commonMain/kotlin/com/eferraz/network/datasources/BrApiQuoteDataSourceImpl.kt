package com.eferraz.network.datasources

import com.eferraz.entities.holdings.StockQuoteHistory
import com.eferraz.network.TokenConfig
import com.eferraz.network.core.ClientConfig
import com.eferraz.network.responses.BrApiQuoteResponse
import com.eferraz.network.responses.QuoteResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory

@Factory(binds = [BrApiQuoteDataSource::class])
internal class BrApiQuoteDataSourceImpl(
    private val clientConfig: ClientConfig,
) : BrApiQuoteDataSource {

    override suspend fun getQuote(ticker: String): StockQuoteHistory {

        val url = "https://brapi.dev/api/quote/$ticker"

        return clientConfig.client.get(url) {
            header(HttpHeaders.Authorization, "Bearer ${TokenConfig.BRAPI_TOKEN}")
        }.body<BrApiQuoteResponse>().results.first().toModel()
    }

    override suspend fun getQuote(
        ticker: String,
        referenceDate: YearMonth,
        range: String,
        interval: String,
    ): StockQuoteHistory {

        val url = "https://brapi.dev/api/quote/$ticker?range=$range&interval=$interval"

        return clientConfig.client.get(url) {
            header(HttpHeaders.Authorization, "Bearer ${TokenConfig.BRAPI_TOKEN}")
        }.body<BrApiQuoteResponse>().results.first().toModel(referenceDate)
    }

    private fun QuoteResult.toModel(): StockQuoteHistory = StockQuoteHistory(
        id = 0,
        ticker = symbol,
        date = (regularMarketTime ?: "1970-01-01T00:00:00Z").toDate(),
        open = regularMarketOpen,
        high = regularMarketDayHigh,
        low = regularMarketDayLow,
        close = regularMarketPrice,
        volume = regularMarketVolume,
        adjustedClose = regularMarketPrice,
        companyName = longName
    )

    private fun QuoteResult.toModel(yearMonth: YearMonth): StockQuoteHistory {

        val quotesInMonth = this.historicalDataPrice
            ?.map { it.date.toDate() to it }
            ?.filter { (date, _) -> YearMonth(date.year, date.month) == yearMonth }
            ?: throw IllegalStateException("Nenhum dado histórico disponível para o mês $yearMonth")

        if (quotesInMonth.isEmpty())
            throw IllegalStateException("Nenhuma cotação encontrada para o mês $yearMonth")

        val lastQuote = quotesInMonth
            .maxByOrNull { (date, _) -> date }
            ?: throw IllegalStateException("Erro ao encontrar a última cotação do mês $yearMonth")

        val (quoteDate, quoteData) = lastQuote

        return StockQuoteHistory(
            id = 0,
            ticker = symbol,
            date = quoteDate,
            open = quoteData.open,
            high = quoteData.high,
            low = quoteData.low,
            close = quoteData.close,
            volume = quoteData.volume,
            adjustedClose = quoteData.adjustedClose,
            companyName = longName
        )
    }

    private fun String.toDate() =
        Instant.parse(this).toLocalDateTime(TimeZone.UTC).date

    private fun Long.toDate() =
        Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.UTC).date
}
