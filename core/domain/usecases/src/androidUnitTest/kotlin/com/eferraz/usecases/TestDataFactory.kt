package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.Brokerage
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Issuer
import com.eferraz.entities.Liquidity
import com.eferraz.entities.Owner
import com.eferraz.entities.StockQuoteHistory
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

object TestDataFactory {

    fun createOwner(id: Long = 1, name: String = "Owner Name") = Owner(id, name)

    fun createBrokerage(id: Long = 1, name: String = "Brokerage Name") = Brokerage(id, name)

    fun createIssuer(id: Long = 1, name: String = "Issuer Name") = Issuer(id, name)

    fun createVariableIncomeAsset(
        id: Long = 1,
        name: String = "Variable Asset",
        issuer: Issuer = createIssuer(),
        type: VariableIncomeAssetType = VariableIncomeAssetType.NATIONAL_STOCK,
        ticker: String = "TICKER",
    ) = VariableIncomeAsset(id, name, issuer, type, ticker)

    fun createFixedIncomeAsset(
        id: Long = 2,
        issuer: Issuer = createIssuer(),
        type: FixedIncomeAssetType = FixedIncomeAssetType.PRE_FIXED,
        subType: FixedIncomeSubType = FixedIncomeSubType.CDB,
        expirationDate: LocalDate = LocalDate(2025, Month.JANUARY, 1),
        contractedYield: Double = 10.0,
    ) = FixedIncomeAsset(id, issuer, type, subType, expirationDate, contractedYield, liquidity = Liquidity.D_PLUS_DAYS)

    fun createInvestmentFundAsset(
        id: Long = 3,
        name: String = "Investment Fund",
        issuer: Issuer = createIssuer(),
        type: InvestmentFundAssetType = InvestmentFundAssetType.MULTIMARKET_FUND,
        liquidity: Liquidity = Liquidity.D_PLUS_DAYS,
        liquidityDays: Int = 1,
        expirationDate: LocalDate? = null,
    ) = InvestmentFundAsset(id, name, issuer, type, liquidity, liquidityDays, expirationDate)

    fun createAssetHolding(
        id: Long = 1,
        asset: Asset = createVariableIncomeAsset(),
        owner: Owner = createOwner(),
        brokerage: Brokerage = createBrokerage(),
    ) = AssetHolding(id, asset, owner, brokerage)

    fun createHoldingHistoryEntry(
        id: Long? = null,
        holding: AssetHolding = createAssetHolding(),
        referenceDate: YearMonth = YearMonth(2024, Month.JANUARY),
        endOfMonthValue: Double = 100.0,
        endOfMonthQuantity: Double = 10.0,
        endOfMonthAverageCost: Double = 10.0,
        totalInvested: Double = 100.0,
    ) = HoldingHistoryEntry(id, holding, referenceDate, endOfMonthValue, endOfMonthQuantity, endOfMonthAverageCost, totalInvested)

    fun createStockQuoteHistory(
        id: Long = 1,
        ticker: String = "TICKER",
        date: LocalDate = LocalDate(2024, Month.JANUARY, 31),
        open: Double? = 50.0,
        high: Double? = 55.0,
        low: Double? = 48.0,
        close: Double? = 52.0,
        volume: Long? = 1000000,
        adjustedClose: Double? = 52.0,
    ) = StockQuoteHistory(id, ticker, date, open, high, low, close, volume, adjustedClose)
}