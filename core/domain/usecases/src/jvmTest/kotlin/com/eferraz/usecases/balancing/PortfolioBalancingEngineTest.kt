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
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class PortfolioBalancingEngineTest {

    private val referenceDate = YearMonth(2024, Month.JANUARY)
    private val issuer = Issuer(id = 1, name = "Issuer")
    private val owner = Owner(id = 1, name = "Owner")
    private val brokerage = Brokerage(id = 1, name = "Broker")

    /**
     * All fixed income 100k with no pension → RF ideal is 60% of portfolio (100k).
     */
    @Test
    public fun `GIVEN portfolio of 100k all fixed income WHEN calculate THEN fixed income ideal is 60k and deviation is minus 40k`() {

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
        assertEquals(60_000.0, fixedIncomeLine.idealValue, 0.01)
        assertEquals(-40_000.0, fixedIncomeLine.deviation, 0.01)
        assertEquals(60.0, fixedIncomeLine.configuredWeightPercent!!, 0.01)
        assertEquals(60.0, fixedIncomeLine.normalizedWeightPercent, 0.01)
        assertEquals(100.0, fixedIncomeLine.actualWeightPercent, 0.01)
        assertEquals(100_000.0, report.totalPortfolioValue, 0.01)
        assertTrue(report.lines.none { it.componentName == "Demais investimentos" })
        assertTrue(report.groupHoldings.all { it.holdings.isEmpty() })
    }

    /**
     * Canonical 1000 total with 100 pension and 900 RF → balanceable base drives RF ideal 450.
     */
    @Test
    public fun `GIVEN total 1000 with pension 100 and RF 900 WHEN calculate THEN weights and ideals match balanceable base`() {

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
            entry(holdingId = 1, asset = pensionAsset, value = 100.0, quantity = 1.0),
            entry(holdingId = 2, asset = rfAsset, value = 900.0, quantity = 1.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val pensionLine = report.lines.first { it.componentName == "Fundos de Previdência" }
        val rfLine = report.lines.first { it.componentName == "Renda Fixa" }
        assertEquals(540.0, rfLine.idealValue, 0.01)
        assertEquals(60.0, rfLine.configuredWeightPercent!!, 0.01)
        assertEquals(54.0, rfLine.normalizedWeightPercent, 0.01)
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)
        assertNull(pensionLine.configuredWeightPercent)
        assertEquals(10.0, pensionLine.normalizedWeightPercent, 0.01)
        assertEquals(0.0, pensionLine.deviation, 0.01)

        val group1Lines = report.lines.filter { it.groupId == BalancingGroupId.PORTFOLIO_TOTAL }
        val normalizedSum = group1Lines.sumOf { it.normalizedWeightPercent }
        assertEquals(100.0, normalizedSum, 0.01)
    }

    /**
     * Residual pension → ideal equals actual, configured is dynamic, deviation is zero.
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
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)
        assertEquals(10.0, pensionLine.normalizedWeightPercent, 0.01)

        val rfLine = report.lines.first { it.componentName == "Renda Fixa" }
        assertEquals(54_000.0, rfLine.idealValue, 0.01)
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
        assertEquals(-5_000.0, otherLine.deviation, 0.01)
        assertEquals(0.0, otherLine.configuredWeightPercent!!, 0.01)
        assertEquals(100.0, otherLine.actualWeightPercent, 0.01)

        val g1Holdings = report.groupHoldings.first { it.groupId == BalancingGroupId.PORTFOLIO_TOTAL }.holdings
        assertEquals(1, g1Holdings.size)
        assertTrue(g1Holdings.first().displayName.contains("Fundo de Ação"))
        assertEquals(5_000.0, g1Holdings.first().value, 0.01)
        assertTrue(report.groupHoldings.first { it.groupId == BalancingGroupId.FIXED_INCOME }.holdings.isEmpty())
        assertTrue(report.groupHoldings.first { it.groupId == BalancingGroupId.VARIABLE_INCOME }.holdings.isEmpty())
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
     * Zero RF actual → nested RF group omitted; RV lines still calculated.
     */
    @Test
    public fun `GIVEN zero RF actual and 100k total WHEN calculate THEN fixed income nested group is omitted`() {

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
        assertTrue(report.lines.none { it.groupId == BalancingGroupId.FIXED_INCOME })
        val nationalLine = report.lines.first { it.componentName == "Ações Nacionais" }
        assertEquals(100_000.0, nationalLine.actualValue, 0.01)
        assertEquals(100.0, nationalLine.actualWeightPercent, 0.01)
    }

    /**
     * Nested ideal = parent component ideal × configured internal weight.
     */
    @Test
    public fun `GIVEN mixed RF and RV WHEN calculate THEN nested ideal uses parent ideal times weight`() {

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
        assertEquals(19_998.0, postFixedLine.idealValue, 1.0)
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
        assertEquals(1.0, cryptoLine.configuredWeightPercent!!, 0.01)
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
        assertTrue(report.groupHoldings.first { it.groupId == BalancingGroupId.VARIABLE_INCOME }.holdings.isEmpty())
    }

    /**
     * RV holdings list lists only assets classified as Demais investimentos in group 3.
     */
    @Test
    public fun `GIVEN ETF not mapped to RV subcomponents WHEN calculate THEN it appears only in group three holdings`() {

        // GIVEN
        val etfAsset = VariableIncomeAsset(
            id = 1,
            name = "BOVA11",
            issuer = issuer,
            type = VariableIncomeAssetType.ETF,
            ticker = "BOVA11",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = etfAsset, value = 100.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val rvHoldings = report.groupHoldings.first { it.groupId == BalancingGroupId.VARIABLE_INCOME }.holdings
        assertEquals(1, rvHoldings.size)
        assertTrue(rvHoldings.first().displayName.contains("BOVA11"))
        assertTrue(report.groupHoldings.first { it.groupId == BalancingGroupId.PORTFOLIO_TOTAL }.holdings.isEmpty())
        assertTrue(report.groupHoldings.first { it.groupId == BalancingGroupId.FIXED_INCOME }.holdings.isEmpty())
    }

    /**
     * fundsGroup in catalog.groups produces a FIIs section in the report when holdings match.
     */
    @Test
    public fun `GIVEN FII in REND tickers WHEN calculate THEN funds group line appears in report and log`() {

        // GIVEN
        val fiiAsset = VariableIncomeAsset(
            id = 1,
            name = "KNCR11",
            issuer = issuer,
            type = VariableIncomeAssetType.REAL_ESTATE_FUND,
            ticker = "KNCR11",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = fiiAsset, value = 100.0, quantity = 10.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)
        val formatted = formatPortfolioBalancingReport(report)

        // THEN
        val rendLine = report.lines.first { it.componentName == "FII - Renda%" }
        assertEquals(BalancingGroupId.RV_REITS, rendLine.groupId)
        assertEquals(1_000.0, rendLine.actualValue, 0.01)
        assertTrue(formatted.contains("=== FIIs ==="))
        assertTrue(formatted.contains("FII - Renda%"))
    }

    /**
     * Empty portfolio → all values zero; configured weights remain visible.
     */
    @Test
    public fun `GIVEN empty portfolio WHEN calculate THEN all lines have zero values`() {

        // GIVEN
        val entries = emptyList<HoldingHistoryEntry>()

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        assertEquals(0.0, report.totalPortfolioValue, 0.01)
        assertEquals(4, report.lines.size)
        assertEquals(PortfolioBalancingCatalog.groups.size, report.groupHoldings.size)
        assertTrue(report.lines.none { it.componentName == "Demais investimentos" })
        report.lines.forEach { line ->
            assertEquals(0.0, line.actualValue, 0.01)
            assertEquals(0.0, line.idealValue, 0.01)
            assertEquals(0.0, line.deviation, 0.01)
            assertEquals(0.0, line.normalizedWeightPercent, 0.01)
            assertEquals(0.0, line.actualWeightPercent, 0.01)
        }
        val rfLine = report.lines.first { it.componentName == "Renda Fixa" }
        assertEquals(60.0, rfLine.configuredWeightPercent!!, 0.01)
        val pensionLine = report.lines.first { it.componentName == "Fundos de Previdência" }
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)
    }

    /**
     * Normalized weight column appears only in groups that contain a dynamic-weight component.
     */
    @Test
    public fun `GIVEN report with pension WHEN format THEN normalized weight column only in portfolio total group`() {

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
            entry(holdingId = 1, asset = pensionAsset, value = 100.0, quantity = 1.0),
            entry(holdingId = 2, asset = rfAsset, value = 900.0, quantity = 1.0),
        )
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // WHEN
        val formatted = formatPortfolioBalancingReport(report)

        // THEN
        val portfolioSection = formatted.sectionAfter("=== Carteira Total ===")
        val fixedIncomeSection = formatted.sectionAfter("=== Renda Fixa ===")
        assertTrue(portfolioSection.contains("Peso normalizado"))
        assertTrue(!fixedIncomeSection.contains("Peso normalizado"))
        assertTrue(formatted.contains("Total"))
        assertTrue(formatted.contains("100,00%"))
        assertTrue(!formatted.contains("Investimentos:"))
        assertSeparatorBeforeEachTotalRow(formatted)
        assertTrue(formatted.startsWith("\n"))
        assertTrue(formatted.endsWith("\n"))
    }

    /**
     * Without pension (no dynamic weight), normalized weight column is omitted from the log.
     */
    @Test
    public fun `GIVEN report without pension WHEN format THEN output omits normalized weight column`() {

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
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // WHEN
        val formatted = formatPortfolioBalancingReport(report)

        // THEN
        assertEquals(false, report.hasDynamicWeight)
        assertTrue(formatted.contains("Peso configurado"))
        assertTrue(formatted.contains("Percentual actual"))
        assertTrue(!formatted.contains("Peso normalizado"))
        assertTrue(!formatted.contains("Investimentos:"))
    }

    /**
     * Holdings section is rendered only when the group has other investments to list.
     */
    @Test
    public fun `GIVEN other investments in portfolio total WHEN format THEN output includes holdings section`() {

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
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // WHEN
        val formatted = formatPortfolioBalancingReport(report)

        // THEN
        val portfolioSection = formatted.sectionAfter("=== Carteira Total ===")
        assertTrue(portfolioSection.contains("Investimentos:"))
        assertTrue(!portfolioSection.contains("(nenhum)"))
        assertTrue(portfolioSection.contains("Fundo de Ação"))
    }

    private fun String.sectionAfter(header: String): String =
        substringAfter(header).substringBefore("\n===")

    private fun assertSeparatorBeforeEachTotalRow(formatted: String) {
        val lines = formatted.lines()
        lines.forEachIndexed { index, line ->
            if (line.contains("Total") && line.contains(" | ")) {
                assertTrue(index > 0, "Total row should not be first line")
                assertTrue(
                    lines[index - 1].contains("-+-"),
                    "Expected separator before Total row",
                )
            }
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
