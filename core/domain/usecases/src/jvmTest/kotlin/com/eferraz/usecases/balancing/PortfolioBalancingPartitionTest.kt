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
     * Each active position belongs to exactly one component per eligible group (FR-007a).
     */
    @Test
    public fun `GIVEN diverse active positions WHEN classify per group THEN each entry maps to exactly one component`() {

        // GIVEN
        val entries = listOf(
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
                asset = FixedIncomeAsset(
                    id = 2,
                    issuer = issuer,
                    indexer = YieldIndexer.PRE_FIXED,
                    type = FixedIncomeAssetType.LCI,
                    expirationDate = LocalDate(2025, Month.JUNE, 1),
                    contractedYield = 9.0,
                    liquidity = Liquidity.D_PLUS_DAYS,
                ),
                value = 200.0,
                quantity = 10.0,
            ),
            entry(
                holdingId = 3,
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
                holdingId = 4,
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
                holdingId = 5,
                asset = VariableIncomeAsset(
                    id = 5,
                    name = "International",
                    issuer = issuer,
                    type = VariableIncomeAssetType.INTERNATIONAL_STOCK,
                    ticker = "IVVB11",
                ),
                value = 400.0,
                quantity = 10.0,
            ),
            entry(
                holdingId = 6,
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
            entry(
                holdingId = 7,
                asset = InvestmentFundAsset(
                    id = 7,
                    name = "Other Fund",
                    issuer = issuer,
                    type = InvestmentFundAssetType.MULTIMARKET_FUND,
                    liquidity = Liquidity.D_PLUS_DAYS,
                ),
                value = 80.0,
                quantity = 10.0,
            ),
        )
        val activeEntries = entries.filter { PortfolioBalancingEngine.patrimony(it) > 0.0 }

        // WHEN / THEN
        PortfolioBalancingCatalog.groups.forEach { group ->
            val universe = PortfolioBalancingEngine.universeForGroup(group, activeEntries)

            for (entry in universe) {
                val matchingComponents = group.components.filter { component ->
                    component.matches(entry) &&
                        group.components.first { it.matches(entry) }.id == component.id
                }
                assertEquals(
                    1,
                    matchingComponents.size,
                    "Entry ${entry.holding.id} in group ${group.id} has ambiguous classification",
                )
            }

            val classifiedSum = PortfolioBalancingEngine.classifyAndSum(universe, group).values.sum()
            val universeSum = universe.sumOf { PortfolioBalancingEngine.patrimony(it) }
            assertEquals(universeSum, classifiedSum, 0.01)
        }
    }

    /**
     * No overlap between specific components — sum of classified equals universe total.
     */
    @Test
    public fun `GIVEN portfolio total group WHEN classify THEN sum equals universe without gaps`() {

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

        // WHEN
        val sums = PortfolioBalancingEngine.classifyAndSum(entries, PortfolioBalancingCatalog.portfolioTotalGroup)

        // THEN
        assertEquals(1, sums.values.count { it > 0.0 })
        assertEquals(1_000.0, sums.values.sum(), 0.01)
        assertTrue(sums.getValue(BalancingComponentId.VARIABLE_INCOME_TOTAL) > 0.0)
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
