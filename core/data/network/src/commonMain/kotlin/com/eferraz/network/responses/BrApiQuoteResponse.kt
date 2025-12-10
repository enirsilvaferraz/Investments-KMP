package com.eferraz.network.responses

import kotlinx.serialization.Serializable

@Serializable
internal data class BrApiQuoteResponse(
    val results: List<QuoteResult>,
    val requestedAt: String,
    val took: Long,
)

@Serializable
internal data class QuoteResult(
    val currency: String,
    val marketCap: Long? = null,
    val shortName: String,
    val longName: String,
    val regularMarketChange: Double? = null,
    val regularMarketChangePercent: Double? = null,
    val regularMarketTime: String? = null,
    val regularMarketPrice: Double,
    val regularMarketDayHigh: Double? = null,
    val regularMarketDayRange: String? = null,
    val regularMarketDayLow: Double? = null,
    val regularMarketVolume: Long? = null,
    val regularMarketPreviousClose: Double? = null,
    val regularMarketOpen: Double? = null,
    val fiftyTwoWeekRange: String? = null,
    val fiftyTwoWeekLow: Double? = null,
    val fiftyTwoWeekHigh: Double? = null,
    val symbol: String,
    val logourl: String? = null,
    val priceEarnings: Double? = null,
    val earningsPerShare: Double? = null,
    val usedInterval: String? = null,
    val usedRange: String? = null,
    val historicalDataPrice: List<HistoricalDataPrice>? = null,
    val validRanges: List<String>? = null,
    val validIntervals: List<String>? = null,
)

@Serializable
internal data class HistoricalDataPrice(
    val date: Long,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val volume: Long? = null,
    val adjustedClose: Double? = null,
)