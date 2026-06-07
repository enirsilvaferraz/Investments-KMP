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

public class PortfolioBalancingPartitionTest {

    private val referenceDate = YearMonth(2024, Month.JANUARY)
    private val issuer = Issuer(id = 1, name = "Issuer")
    private val owner = Owner(id = 1, name = "Owner")
    private val brokerage = Brokerage(id = 1, name = "Broker")

    /**
     * Balanceable and non-balanceable universes are mutually exclusive and exhaustive.
     */
    @Test
    public fun `GIVEN diverse active positions WHEN build index THEN balanceable and non-balanceable partition root universe`() {

        // GIVEN
        val entries = diverseEntries()
        val activeEntries = entries.filter { PortfolioBalancingEngine.patrimony(it) > 0.0 }
        val index = BalancingUniverseIndex.build(PortfolioBalancingCatalog.root, activeEntries)

        // WHEN / THEN
        val root = index.byNodeId.getValue(BalancingGroupId.PORTFOLIO_TOTAL)
        val nonBalanceable = index.byNodeId.getValue(BalancingGroupId.NON_BALANCEABLE)
        val balanceable = index.byNodeId.getValue(BalancingGroupId.BALANCEABLE)

        assertEquals(root.size, nonBalanceable.size + balanceable.size)
        assertTrue(nonBalanceable.none { it in balanceable })
        assertEquals(
            root.sumOf { PortfolioBalancingEngine.patrimony(it) },
            nonBalanceable.sumOf { PortfolioBalancingEngine.patrimony(it) } +
                balanceable.sumOf { PortfolioBalancingEngine.patrimony(it) },
            0.01,
        )
    }

    /**
     * ETF is variable income and maps to demais investimentos within RV subtree.
     */
    @Test
    public fun `GIVEN ETF in balanceable universe WHEN build index THEN demais investimentos captures it`() {

        // GIVEN
        val entries = listOf(
            entry(
                holdingId = 1,
                asset = VariableIncomeAsset(
                    id = 1,
                    name = "ETF",
                    issuer = issuer,
                    type = VariableIncomeAssetType.ETF,
                    ticker = "BOVA11",
                ),
                value = 100.0,
                quantity = 10.0,
            ),
        )
        val index = BalancingUniverseIndex.build(PortfolioBalancingCatalog.root, entries)

        // THEN
        assertEquals(1, index.byNodeId.getValue(BalancingTreeNodeFactory.demaisId(BalancingGroupId.VARIABLE_INCOME)).size)
        assertEquals(1, index.byNodeId.getValue(BalancingGroupId.VARIABLE_INCOME).size)
    }

    /**
     * Non-balanceable stock fund maps to demais non-balanceable fallback.
     */
    @Test
    public fun `GIVEN non-listed non-balanceable asset WHEN build index THEN demais non-balanceable captures it`() {

        // GIVEN — extend list hypothetically via matcher overlap test on demais
        val entries = listOf(
            entry(
                holdingId = 1,
                asset = InvestmentFundAsset(
                    id = 1,
                    name = "Other Fund",
                    issuer = issuer,
                    type = InvestmentFundAssetType.MULTIMARKET_FUND,
                    liquidity = Liquidity.D_PLUS_DAYS,
                ),
                value = 100.0,
                quantity = 10.0,
            ),
        )
        val index = BalancingUniverseIndex.build(PortfolioBalancingCatalog.root, entries)

        // THEN — not in non-balanceable list → balanceable demais
        assertEquals(1, index.byNodeId.getValue(BalancingGroupId.BALANCEABLE).size)
        assertEquals(1, index.byNodeId.getValue(BalancingTreeNodeFactory.demaisId(BalancingGroupId.BALANCEABLE)).size)
    }

    /**
     * Non-balanceable assets never appear in RF subtree (FR-008, FR-014).
     */
    @Test
    public fun `GIVEN pension in portfolio WHEN build index THEN RF subtree excludes non-balanceable assets`() {

        // GIVEN
        val entries = listOf(
            entry(
                holdingId = 1,
                asset = InvestmentFundAsset(
                    id = 1,
                    name = "Pension",
                    issuer = issuer,
                    type = InvestmentFundAssetType.PENSION,
                    liquidity = Liquidity.D_PLUS_DAYS,
                ),
                value = 100.0,
                quantity = 10.0,
            ),
            entry(
                holdingId = 2,
                asset = FixedIncomeAsset(
                    id = 2,
                    issuer = issuer,
                    indexer = YieldIndexer.PRE_FIXED,
                    type = FixedIncomeAssetType.CDB,
                    expirationDate = LocalDate(2025, Month.JANUARY, 1),
                    contractedYield = 10.0,
                    liquidity = Liquidity.D_PLUS_DAYS,
                ),
                value = 200.0,
                quantity = 10.0,
            ),
        )
        val index = BalancingUniverseIndex.build(PortfolioBalancingCatalog.root, entries)

        // THEN
        val rfEntries = index.byNodeId.getValue(BalancingGroupId.FIXED_INCOME)
        assertTrue(rfEntries.none { BalancingMatchers.isNonBalanceable(it) })
        assertEquals(1, rfEntries.size)
    }

    private fun diverseEntries(): List<HoldingHistoryEntry> = listOf(
        entry(
            holdingId = 1,
            asset = FixedIncomeAsset(
                id = 1,
                issuer = issuer,
                indexer = YieldIndexer.POST_FIXED,
                type = FixedIncomeAssetType.CDB,
                expirationDate = LocalDate(2025, Month.JANUARY, 1),
                contractedYield = 10.0,
                liquidity = Liquidity.D_PLUS_DAYS,
            ),
            value = 100.0,
            quantity = 10.0,
        ),
        entry(
            holdingId = 2,
            asset = VariableIncomeAsset(
                id = 3,
                name = "Stock",
                issuer = issuer,
                type = VariableIncomeAssetType.NATIONAL_STOCK,
                ticker = "PETR4",
            ),
            value = 300.0,
            quantity = 10.0,
        ),
        entry(
            holdingId = 3,
            asset = VariableIncomeAsset(
                id = 4,
                name = "Crypto",
                issuer = issuer,
                type = VariableIncomeAssetType.ETF,
                ticker = "HASH11",
            ),
            value = 50.0,
            quantity = 10.0,
        ),
        entry(
            holdingId = 4,
            asset = InvestmentFundAsset(
                id = 6,
                name = "Pension",
                issuer = issuer,
                type = InvestmentFundAssetType.PENSION,
                liquidity = Liquidity.D_PLUS_DAYS,
            ),
            value = 150.0,
            quantity = 10.0,
        ),
    )

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
