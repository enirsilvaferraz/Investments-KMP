package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Representa um registro de histórico diário de cotação de ação (OHLCV).
 * Armazena os dados de abertura, máxima, mínima, fechamento, volume e fechamento ajustado
 * de um ativo de renda variável em uma data específica.
 *
 * @property id O identificador único do registro (chave primária).
 * @property ticker O código do ativo (ex: PETR4, VALE3).
 * @property date A data da cotação (formato ISO 8601: YYYY-MM-DD).
 * @property open O preço de abertura do ativo no dia (opcional).
 * @property high O preço máximo do ativo no dia (opcional).
 * @property low O preço mínimo do ativo no dia (opcional).
 * @property close O preço de fechamento do ativo no dia (opcional).
 * @property volume O volume negociado no dia (opcional).
 * @property adjustedClose O preço de fechamento ajustado (considerando splits, dividendos, etc.) (opcional).
 * @property companyName O nome completo da empresa retornado pela BR API (opcional).
 */
public data class StockQuoteHistory(
    public val id: Long = 0,
    public val ticker: String,
    public val date: LocalDate,
    public val open: Double? = null,
    public val high: Double? = null,
    public val low: Double? = null,
    public val close: Double? = null,
    public val volume: Long? = null,
    public val adjustedClose: Double? = null,
    public val companyName: String? = null
)
