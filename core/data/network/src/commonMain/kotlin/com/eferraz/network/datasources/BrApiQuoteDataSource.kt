package com.eferraz.network.datasources

import com.eferraz.entities.holdings.StockQuoteHistory
import kotlinx.datetime.YearMonth

public interface BrApiQuoteDataSource {

    /**
     * Busca cotações atuais (sem histórico) para os tickers fornecidos.
     *
     * @param ticker Ticker (ex: ["PETR4", "MGLU3"])
     * @return Resposta da API com as cotações atuais
     */
    public suspend fun getQuote(ticker: String): StockQuoteHistory

    /**
     * Busca a última cotação disponível do mês de referência para o ticker fornecido.
     *
     * @param ticker Ticker (ex: "PETR4", "MGLU3")
     * @param referenceDate Mês de referência para buscar a última cotação
     * @param range Período do histórico (ex: "5d", "1mo", "1y")
     * @param interval Intervalo dos dados (ex: "1d", "1h")
     * @return Última cotação disponível do mês de referência
     * @throws IllegalStateException se não houver dados históricos disponíveis para o mês de referência
     */
    public suspend fun getQuote(ticker: String, referenceDate: YearMonth, range: String = "3mo", interval: String = "1d"): StockQuoteHistory
}

