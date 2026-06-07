package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
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

public class BalancingUniverseIndexTest {

    private val referenceDate = YearMonth(2024, Month.JANUARY)
    private val issuer = Issuer(id = 1, name = "Issuer")
    private val owner = Owner(id = 1, name = "Owner")
    private val brokerage = Brokerage(id = 1, name = "Broker")

    /**
     * R1 progressive filtering — RF universe is subset of balanceable universe.
     */
    @Test
    public fun `GIVEN mixed portfolio WHEN build index THEN RF universe is filtered from balanceable universe`() {

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
        val pensionAsset = InvestmentFundAsset(
            id = 2,
            name = "Pension",
            issuer = issuer,
            type = InvestmentFundAssetType.PENSION,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val entries = listOf(
            entry(holdingId = 1, asset = rfAsset, value = 100.0, quantity = 10.0),
            entry(holdingId = 2, asset = pensionAsset, value = 50.0, quantity = 10.0),
        )

        // WHEN
        val index = BalancingUniverseIndex.build(PortfolioBalancingCatalog.root, entries)

        // THEN
        val rootSize = index.byNodeId.getValue(BalancingGroupId.PORTFOLIO_TOTAL).size
        val balanceableSize = index.byNodeId.getValue(BalancingGroupId.BALANCEABLE).size
        val rfSize = index.byNodeId.getValue(BalancingGroupId.FIXED_INCOME).size
        val preSize = index.byNodeId.getValue(BalancingGroupId.RF_PRE_FIXED).size

        assertEquals(2, rootSize)
        assertEquals(1, balanceableSize)
        assertTrue(rfSize <= balanceableSize)
        assertTrue(preSize <= rfSize)
        assertEquals(1, preSize)
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
