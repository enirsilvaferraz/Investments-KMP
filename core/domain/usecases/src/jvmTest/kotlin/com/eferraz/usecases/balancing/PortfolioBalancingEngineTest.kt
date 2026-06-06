package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.holdings.Owner
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class PortfolioBalancingEngineTest {

    private val referenceDate = YearMonth(2024, Month.JANUARY)
    private val issuer = Issuer(id = 1, name = "Issuer")
    private val owner = Owner(id = 1, name = "Owner")
    private val brokerage = Brokerage(id = 1, name = "Broker")

    /**
     * Fixed income at 100% of a 100k portfolio → 50% target yields 50k ideal and 50k deviation.
     */
    @Test
    public fun `GIVEN portfolio of 100k all fixed income WHEN calculate THEN fixed income ideal is 50k and deviation is 50k`() {

        // GIVEN
        val rfAsset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entries = listOf(
            entry(holdingId = 1, asset = rfAsset, value = 10_000.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val fixedIncomeLine = report.lines.first { it.componentName == "Renda Fixa" }
        assertEquals(100_000.0, fixedIncomeLine.actualValue, 0.01)
        assertEquals(50_000.0, fixedIncomeLine.idealValue, 0.01)
        assertEquals(50_000.0, fixedIncomeLine.deviation, 0.01)
        assertEquals(100_000.0, report.totalPortfolioValue, 0.01)
    }

    /**
     * Dynamic pension weight → ideal equals actual and deviation is zero.
     */
    @Test
    public fun `GIVEN pension fund in portfolio WHEN calculate THEN pension ideal equals actual and deviation is zero`() {

        // GIVEN
        val pensionAsset = InvestmentFundAsset(
            id = 1,
            name = "Pension Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.PENSION,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val rfAsset = FixedIncomeAsset(
            id = 2,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entries = listOf(
            entry(holdingId = 1, asset = pensionAsset, value = 1_000.0, quantity = 10.0),
            entry(holdingId = 2, asset = rfAsset, value = 9_000.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val pensionLine = report.lines.first { it.componentName == "Fundos de Previdência" }
        assertEquals(10_000.0, pensionLine.actualValue, 0.01)
        assertEquals(10_000.0, pensionLine.idealValue, 0.01)
        assertEquals(0.0, pensionLine.deviation, 0.01)
        assertTrue(pensionLine.targetWeightDisplay.startsWith("dinâmico"))
    }

    /**
     * Non-pension fund falls into "Demais investimentos" with zero ideal.
     */
    @Test
    public fun `GIVEN non pension fund WHEN calculate THEN other investments ideal is zero`() {

        // GIVEN
        val fundAsset = InvestmentFundAsset(
            id = 1,
            name = "Stock Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.STOCK_FUND,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entries = listOf(
            entry(holdingId = 1, asset = fundAsset, value = 1_000.0, quantity = 5.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val otherLine = report.lines.first { it.componentName == "Demais investimentos" }
        assertEquals(5_000.0, otherLine.actualValue, 0.01)
        assertEquals(0.0, otherLine.idealValue, 0.01)
        assertEquals(5_000.0, otherLine.deviation, 0.01)
    }

    /**
     * Liquidated positions (patrimony <= 0) are excluded from totals.
     */
    @Test
    public fun `GIVEN liquidated position WHEN calculate THEN it is excluded from portfolio total`() {

        // GIVEN
        val rfAsset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val activeEntry = entry(holdingId = 1, asset = rfAsset, value = 1_000.0, quantity = 10.0)
        val liquidatedEntry = entry(holdingId = 2, asset = rfAsset, value = 0.0, quantity = 0.0)

        // WHEN
        val report = PortfolioBalancingEngine.calculate(listOf(activeEntry, liquidatedEntry), referenceDate)

        // THEN
        assertEquals(10_000.0, report.totalPortfolioValue, 0.01)
        val group1Sum = report.lines
            .filter { it.groupId == BalancingGroupId.PORTFOLIO_TOTAL }
            .sumOf { it.actualValue }
        assertEquals(report.totalPortfolioValue, group1Sum, 0.01)
    }

    /**
     * Group 1 actual values sum equals total portfolio value.
     */
    @Test
    public fun `GIVEN mixed portfolio WHEN calculate THEN group one actuals sum equals total`() {

        // GIVEN
        val rfAsset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.POST_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val stockAsset = VariableIncomeAsset(
            id = 2,
            name = "Stock",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = rfAsset, value = 3_000.0, quantity = 10.0),
            entry(holdingId = 2, asset = stockAsset, value = 2_000.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val group1Sum = report.lines
            .filter { it.groupId == BalancingGroupId.PORTFOLIO_TOTAL }
            .sumOf { it.actualValue }
        assertEquals(report.totalPortfolioValue, group1Sum, 0.01)
    }

    /**
     * Zero RF actual with 100k total → nested post-fixed ideal is 33.33% of 50k RF parent ideal.
     */
    @Test
    public fun `GIVEN zero RF actual and 100k total WHEN calculate THEN nested post fixed ideal is 16667`() {

        // GIVEN
        val stockAsset = VariableIncomeAsset(
            id = 1,
            name = "Stock",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = stockAsset, value = 10_000.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val postFixedLine = report.lines.first { it.componentName == "Pós-fixados" }
        assertEquals(0.0, postFixedLine.actualValue, 0.01)
        assertEquals(16_665.0, postFixedLine.idealValue, 1.0)
    }

    /**
     * Over-allocated RF still uses parent ideal from group 1, not actual RF value.
     */
    @Test
    public fun `GIVEN over allocated RF WHEN calculate THEN nested ideal still based on parent ideal`() {

        // GIVEN
        val rfAsset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.POST_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val stockAsset = VariableIncomeAsset(
            id = 2,
            name = "Stock",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = rfAsset, value = 8_000.0, quantity = 10.0),
            entry(holdingId = 2, asset = stockAsset, value = 2_000.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val postFixedLine = report.lines.first { it.componentName == "Pós-fixados" }
        assertEquals(80_000.0, postFixedLine.actualValue, 0.01)
        assertEquals(16_665.0, postFixedLine.idealValue, 1.0)
    }

    /**
     * HASH11 is classified as crypto in group 1 and excluded from group 3.
     */
    @Test
    public fun `GIVEN HASH11 position WHEN calculate THEN crypto line has value and group three excludes it`() {

        // GIVEN
        val cryptoAsset = VariableIncomeAsset(
            id = 1,
            name = "Hash ETF",
            issuer = issuer,
            type = VariableIncomeAssetType.ETF,
            ticker = "HASH11",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = cryptoAsset, value = 100.0, quantity = 100.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val cryptoLine = report.lines.first { it.componentName == "Cripto Ativos" }
        assertEquals(10_000.0, cryptoLine.actualValue, 0.01)
        val group3Sum = report.lines
            .filter { it.groupId == BalancingGroupId.VARIABLE_INCOME }
            .sumOf { it.actualValue }
        assertEquals(0.0, group3Sum, 0.01)
    }

    /**
     * IVVB11 is international stocks; FII is REITs component.
     */
    @Test
    public fun `GIVEN IVVB11 and FII WHEN calculate THEN they map to international and REITs lines`() {

        // GIVEN
        val internationalAsset = VariableIncomeAsset(
            id = 1,
            name = "IVVB11",
            issuer = issuer,
            type = VariableIncomeAssetType.INTERNATIONAL_STOCK,
            ticker = "IVVB11",
        )
        val fiiAsset = VariableIncomeAsset(
            id = 2,
            name = "FII",
            issuer = issuer,
            type = VariableIncomeAssetType.REAL_ESTATE_FUND,
            ticker = "HGLG11",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = internationalAsset, value = 100.0, quantity = 10.0),
            entry(holdingId = 2, asset = fiiAsset, value = 200.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val internationalLine = report.lines.first { it.componentName == "Ações Internacionais" }
        val reitsLine = report.lines.first { it.componentName == "FIIs" }
        assertEquals(1_000.0, internationalLine.actualValue, 0.01)
        assertEquals(2_000.0, reitsLine.actualValue, 0.01)
    }

    /**
     * Empty portfolio → all values zero without exception.
     */
    @Test
    public fun `GIVEN empty portfolio WHEN calculate THEN all lines have zero values`() {

        // GIVEN
        val entries = emptyList<HoldingHistoryEntry>()

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        assertEquals(0.0, report.totalPortfolioValue, 0.01)
        assertEquals(12, report.lines.size)
        report.lines.forEach { line ->
            assertEquals(0.0, line.actualValue, 0.01)
            assertEquals(0.0, line.idealValue, 0.01)
            assertEquals(0.0, line.deviation, 0.01)
        }
    }

    private fun entry(
        holdingId: Long,
        asset: com.eferraz.entities.assets.Asset,
        value: Double,
        quantity: Double,
    ): HoldingHistoryEntry = HoldingHistoryEntry(
        holding = AssetHolding(
            id = holdingId,
            asset = asset,
            owner = owner,
            brokerage = brokerage,
        ),
        referenceDate = referenceDate,
        endOfMonthValue = value,
        endOfMonthQuantity = quantity,
    )
}
