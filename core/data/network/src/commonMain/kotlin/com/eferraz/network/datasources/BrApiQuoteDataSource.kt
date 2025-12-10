package com.eferraz.network.datasources

import com.eferraz.entities.StockQuoteHistory

public interface BrApiQuoteDataSource {

    /**
     * Busca cotações atuais (sem histórico) para os tickers fornecidos.
     * 
     * @param ticker Ticker (ex: ["PETR4", "MGLU3"])
     * @return Resposta da API com as cotações atuais
     */
    public suspend fun getQuote(ticker: String): StockQuoteHistory
    
    /**
     * Busca cotações com histórico para os tickers fornecidos.
     * 
     * @param ticker Ticker (ex: ["PETR4", "MGLU3"])
     * @param range Período do histórico (ex: "5d", "1mo", "1y")
     * @param interval Intervalo dos dados (ex: "1d", "1h")
     * @return Resposta da API com as cotações e histórico
     */
    public suspend fun getQuotesWithHistory(ticker: String, range: String, interval: String): List<StockQuoteHistory>
}

