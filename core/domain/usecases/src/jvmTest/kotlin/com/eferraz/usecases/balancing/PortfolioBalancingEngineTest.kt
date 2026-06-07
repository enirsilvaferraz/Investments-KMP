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
     * All fixed income 100k with no pension → RF ideal uses balanceable base × configured RF weight.
     */
    @Test
    public fun `GIVEN portfolio of 100k all fixed income WHEN calculate THEN fixed income ideal uses balanceable base`() {

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
        val rfLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Fixa")
        assertEquals(100_000.0, rfLine.actualValue, 0.01)
        assertEquals(82_500.0, rfLine.idealValue, 0.01)
        assertEquals(17_500.0, rfLine.deviation, 0.01)
        assertEquals(82.5, rfLine.configuredWeightPercent!!, 0.01)
        assertEquals(100_000.0, report.totalPortfolioValue, 0.01)
        assertEquals(100_000.0, report.balanceableBase, 0.01)
    }

    /**
     * Canonical 1000 total with 100 pension and 900 RF → RF ideal uses balanceable base 900.
     */
    @Test
    public fun `GIVEN total 1000 with pension 100 and RF 900 WHEN calculate THEN RF ideal equals balanceable base times weight`() {

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
        val pensionLine = lineInSection(report, BalancingGroupId.NON_BALANCEABLE, "Fundos de Previdência")
        val rfLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Fixa")
        assertEquals(742.5, rfLine.idealValue, 0.01)
        assertEquals(82.5, rfLine.configuredWeightPercent!!, 0.01)
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)
        assertNull(pensionLine.configuredWeightPercent)
        assertEquals(0.0, pensionLine.deviation, 0.01)
        assertEquals(900.0, report.balanceableBase, 0.01)
    }

    /**
     * Pension and FGTS are dynamic → ideal equals actual and deviation zero.
     */
    @Test
    public fun `GIVEN portfolio with pension and FGTS WHEN calculate THEN dynamic components have zero deviation`() {

        // GIVEN
        val pensionAsset = InvestmentFundAsset(
            id = 1,
            name = "Pension Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.PENSION,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val fgtsAsset = InvestmentFundAsset(
            id = 2,
            name = "FGTS Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.MULTIMARKET_FUND,
            liquidity = Liquidity.D_PLUS_DAYS,
            observations = "Fundo atrelado ao FGTS",
        )
        val cryptoAsset = VariableIncomeAsset(
            id = 3,
            name = BalancingConstants.HASH11,
            issuer = issuer,
            type = VariableIncomeAssetType.INTERNATIONAL_STOCK,
            ticker = BalancingConstants.HASH11,
        )
        val rfAsset = FixedIncomeAsset(
            id = 4,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2025, Month.JANUARY, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val rvAsset = VariableIncomeAsset(
            id = 5,
            name = "PETR4",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = pensionAsset, value = 16_890.0, quantity = 1.0),
            entry(holdingId = 2, asset = fgtsAsset, value = 1_120.0, quantity = 1.0),
            entry(holdingId = 3, asset = cryptoAsset, value = 830.0, quantity = 1.0),
            entry(holdingId = 4, asset = rfAsset, value = 55_000.0, quantity = 1.0),
            entry(holdingId = 5, asset = rvAsset, value = 26_160.0, quantity = 1.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val pensionLine = lineInSection(report, BalancingGroupId.NON_BALANCEABLE, "Fundos de Previdência")
        val fgtsLine = lineInSection(report, BalancingGroupId.NON_BALANCEABLE, "Fundos do FGTS")
        assertEquals(0.0, pensionLine.deviation, 0.01)
        assertEquals(0.0, fgtsLine.deviation, 0.01)
        assertEquals(pensionLine.actualValue, pensionLine.idealValue, 0.01)
        assertEquals(fgtsLine.actualValue, fgtsLine.idealValue, 0.01)
    }

    /**
     * Dynamic pension → ideal equals actual and deviation is zero.
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
        val pensionLine = lineInSection(report, BalancingGroupId.NON_BALANCEABLE, "Fundos de Previdência")
        assertEquals(10_000.0, pensionLine.actualValue, 0.01)
        assertEquals(10_000.0, pensionLine.idealValue, 0.01)
        assertEquals(0.0, pensionLine.deviation, 0.01)
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)

        val rfLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Fixa")
        assertEquals(74_250.0, rfLine.idealValue, 0.01)
    }

    /**
     * Balanceable stock fund falls into «Demais investimentos» with zero ideal.
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
        val otherLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Demais investimentos")
        assertEquals(5_000.0, otherLine.actualValue, 0.01)
        assertEquals(0.0, otherLine.idealValue, 0.01)
        assertEquals(5_000.0, otherLine.deviation, 0.01)
        assertEquals(0.0, otherLine.configuredWeightPercent!!, 0.01)
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
        val rootSection = section(report, BalancingGroupId.PORTFOLIO_TOTAL)!!
        assertEquals(report.totalPortfolioValue, rootSection.totalRow.actualValue, 0.01)
    }

    /**
     * Root section child actuals sum equals total portfolio value.
     */
    @Test
    public fun `GIVEN mixed portfolio WHEN calculate THEN root section actuals sum equals total`() {

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
        val rootSection = section(report, BalancingGroupId.PORTFOLIO_TOTAL)!!
        assertEquals(report.totalPortfolioValue, rootSection.totalRow.actualValue, 0.01)
    }

    /**
     * Nested RF ideal = balanceableBase × RF weight × child weight.
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
        val postFixedLine = lineInSection(report, BalancingGroupId.FIXED_INCOME, "Pós-fixados")
        assertEquals(80_000.0, postFixedLine.actualValue, 0.01)
        assertEquals(27_497.25, postFixedLine.idealValue, 1.0)
    }

    /**
     * Balanceable-only portfolio equals legacy group-1 behaviour on balanceable base.
     */
    @Test
    public fun `GIVEN balanceable-only portfolio WHEN calculate THEN RF ideal equals balanceable base times weight`() {

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
        val rfLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Fixa")
        assertEquals(report.balanceableBase * 0.825, rfLine.idealValue, 0.01)
    }

    /**
     * HASH11 is crypto in balanceable subtree and excluded from RV.
     */
    @Test
    public fun `GIVEN HASH11 position WHEN calculate THEN crypto line has value and RV excludes it`() {

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
        val cryptoLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Cripto Ativos")
        assertEquals(10_000.0, cryptoLine.actualValue, 0.01)
        assertEquals(1.0, cryptoLine.configuredWeightPercent!!, 0.01)
        val rvLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Variável")
        assertEquals(0.0, rvLine.actualValue, 0.01)
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
        val internationalLine = lineInSection(report, BalancingGroupId.VARIABLE_INCOME, "Ações Internacionais")
        val reitsLine = lineInSection(report, BalancingGroupId.VARIABLE_INCOME, "FIIs")
        assertEquals(1_000.0, internationalLine.actualValue, 0.01)
        assertEquals(2_000.0, reitsLine.actualValue, 0.01)
    }

    /**
     * ETF is variable income and maps to demais investimentos within RV.
     */
    @Test
    public fun `GIVEN ETF not mapped to RV subcomponents WHEN calculate THEN it appears in demais investimentos`() {

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
        val otherLine = lineInSection(report, BalancingGroupId.VARIABLE_INCOME, "Demais investimentos")
        assertEquals(1_000.0, otherLine.actualValue, 0.01)
    }

    /**
     * FII in REND tickers produces nested FII section in report and log.
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
        val rendLine = lineInSection(report, BalancingGroupId.RV_REITS, "FII - Renda%")
        assertEquals(1_000.0, rendLine.actualValue, 0.01)
        assertTrue(formatted.contains("=== FIIs ==="))
        assertTrue(formatted.contains("FII - Renda%"))
    }

    /**
     * Empty portfolio → structural sections with zero values (FR-012).
     */
    @Test
    public fun `GIVEN empty portfolio WHEN calculate THEN all sections appear with zero values`() {

        // GIVEN
        val entries = emptyList<HoldingHistoryEntry>()

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        assertEquals(0.0, report.totalPortfolioValue, 0.01)
        assertTrue(report.sections.isNotEmpty())
        assertEquals(BalancingGroupId.PORTFOLIO_TOTAL, report.sections.first().nodeId)
        report.sections.forEach { section ->
            section.rows.forEach { row ->
                assertEquals(0.0, row.actualValue, 0.01)
                assertEquals(0.0, row.idealValue, 0.01)
                assertEquals(0.0, row.deviation, 0.01)
            }
        }
        val rfLine = lineInSection(report, BalancingGroupId.BALANCEABLE, "Renda Fixa")
        assertEquals(82.5, rfLine.configuredWeightPercent!!, 0.01)
        val pensionLine = lineInSection(report, BalancingGroupId.NON_BALANCEABLE, "Fundos de Previdência")
        assertEquals("dinâmico", pensionLine.configuredWeightDisplay)
        assertTrue(report.sections.none { section ->
            section.rows.any { it.displayName == "Demais investimentos" }
        })
    }

    /**
     * FIIs not listed in catalog tickers appear under demais with holdings listed in the log.
     */
    @Test
    public fun `GIVEN FII outside catalog tickers WHEN format THEN demais reits lists holdings`() {

        // GIVEN
        val fiiAsset = VariableIncomeAsset(
            id = 1,
            name = "HGLG11",
            issuer = issuer,
            type = VariableIncomeAssetType.REAL_ESTATE_FUND,
            ticker = "HGLG11",
        )
        val entries = listOf(
            entry(holdingId = 1, asset = fiiAsset, value = 100.0, quantity = 171.4688),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)
        val formatted = formatPortfolioBalancingReport(report)

        // THEN
        val demaisLine = lineInSection(report, BalancingGroupId.RV_REITS, "Demais investimentos")
        assertEquals(1, demaisLine.holdings.size)
        assertTrue(demaisLine.holdings.first().displayName.contains("HGLG11"))
        assertTrue(formatted.contains("Investimentos:"))
        assertTrue(formatted.contains("HGLG11"))
        val fiiSection = formatted.substringAfter("=== FIIs ===").substringBefore("\n===")
        assertTrue(fiiSection.indexOf("Total") < fiiSection.indexOf("Investimentos:"))
    }

    /**
     * Demais fallback rows appear in the log only when actual value is greater than zero.
     */
    @Test
    public fun `GIVEN demais with zero actual WHEN calculate THEN demais row is omitted from section`() {

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
            entry(holdingId = 1, asset = rfAsset, value = 10_000.0, quantity = 1.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val balanceableSection = section(report, BalancingGroupId.BALANCEABLE)!!
        assertTrue(balanceableSection.rows.none { it.displayName == "Demais investimentos" })
        val rfSection = section(report, BalancingGroupId.FIXED_INCOME)!!
        assertTrue(rfSection.rows.none { it.displayName == "Demais investimentos" })
    }

    /**
     * Q1 — first section is Carteira Total with two child rows summing to total.
     */
    @Test
    public fun `GIVEN mixed universes WHEN calculate THEN first section is portfolio total with two rows`() {

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
        val rootSection = report.sections.first()
        assertEquals(BalancingGroupId.PORTFOLIO_TOTAL, rootSection.nodeId)
        assertEquals(2, rootSection.rows.size)
        assertEquals(1_000.0, rootSection.totalRow.actualValue, 0.01)
    }

    /**
     * Q2 — sections follow pre-order DFS of the catalog tree.
     */
    @Test
    public fun `GIVEN default catalog WHEN calculate THEN section order matches tree pre-order`() {

        // GIVEN
        val entries = emptyList<HoldingHistoryEntry>()
        val expectedOrder = listOf(
            BalancingGroupId.PORTFOLIO_TOTAL,
            BalancingGroupId.NON_BALANCEABLE,
            BalancingGroupId.BALANCEABLE,
            BalancingGroupId.FIXED_INCOME,
            BalancingGroupId.VARIABLE_INCOME,
            BalancingGroupId.RV_NATIONAL_STOCKS,
            BalancingGroupId.RV_REITS,
            BalancingGroupId.FII_REND,
            BalancingGroupId.FII_TAT,
            BalancingGroupId.FII_FOF,
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        assertEquals(expectedOrder, report.sections.map { it.nodeId })
    }

    /**
     * Dynamic components have ideal equal to actual and deviation zero.
     */
    @Test
    public fun `GIVEN dynamic components WHEN calculate THEN ideal equals actual and deviation is zero`() {

        // GIVEN
        val pensionAsset = InvestmentFundAsset(
            id = 1,
            name = "Pension Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.PENSION,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entries = listOf(
            entry(holdingId = 1, asset = pensionAsset, value = 150.0, quantity = 100.0),
        )

        // WHEN
        val report = PortfolioBalancingEngine.calculate(entries, referenceDate)

        // THEN
        val rootNonBalanceable = lineInSection(report, BalancingGroupId.PORTFOLIO_TOTAL, "Carteira Não Balanceável")
        val rootBalanceable = lineInSection(report, BalancingGroupId.PORTFOLIO_TOTAL, "Carteira Balanceável")
        assertEquals(0.0, rootNonBalanceable.deviation, 0.01)
        assertEquals(0.0, rootBalanceable.deviation, 0.01)
        assertEquals(rootNonBalanceable.actualValue, rootNonBalanceable.idealValue, 0.01)
    }

    /**
     * Report format uses five columns without normalized weight.
     */
    @Test
    public fun `GIVEN report with pension WHEN format THEN output uses five columns without normalized weight`() {

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
        assertTrue(formatted.contains("Peso configurado"))
        assertTrue(formatted.contains("Valor ideal"))
        assertTrue(formatted.contains("Desvio"))
        assertTrue(!formatted.contains("Peso normalizado"))
        assertTrue(!formatted.contains("Percentual actual"))
        assertTrue(formatted.startsWith("\n"))
        assertTrue(formatted.endsWith("\n"))
    }

    private fun section(report: PortfolioBalancingReport, nodeId: String): PortfolioBalancingReportSection? =
        report.sections.firstOrNull { it.nodeId == nodeId }

    private fun lineInSection(
        report: PortfolioBalancingReport,
        sectionId: String,
        displayName: String,
    ): PortfolioBalancingReportLine =
        section(report, sectionId)!!.rows.first { it.displayName == displayName }

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
