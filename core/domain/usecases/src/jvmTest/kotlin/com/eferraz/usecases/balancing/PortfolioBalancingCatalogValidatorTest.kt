package com.eferraz.usecases.balancing

import kotlin.test.Test
import kotlin.test.assertEquals

public class PortfolioBalancingCatalogValidatorTest {

    /**
     * National stock tickers configured in catalog sum to 100%.
     */
    @Test
    public fun `GIVEN national stock tickers constant WHEN sum weights THEN total is 100 percent`() {

        // WHEN
        val total = BalancingConstants.NATIONAL_STOCK_TICKERS.values.sum()

        // THEN
        assertEquals(14, BalancingConstants.NATIONAL_STOCK_TICKERS.size)
        assertEquals(100.0, total, 0.01)
    }

    /**
     * Default catalog tree passes recursive validation including balanceable weight sum.
     */
    @Test
    public fun `GIVEN default catalog WHEN validate THEN tree passes configured weight sum check`() {

        // WHEN / THEN
        PortfolioBalancingCatalogValidator.validate()
    }

    /**
     * Balanceable subtree fixed weights sum to 100% (FR-011).
     */
    @Test
    public fun `GIVEN balanceable node WHEN sum fixed and zero child weights THEN total is 100 percent`() {

        // WHEN
        val balanceable = PortfolioBalancingCatalog.balanceableNode
        val sum = balanceable.children.sumOf { child ->
            when (val weight = child.targetWeight) {
                is TargetWeight.Fixed -> weight.percent
                TargetWeight.Zero -> 0.0
                TargetWeight.Dynamic -> 0.0
            }
        }

        // THEN
        assertEquals(100.0, sum, 0.01)
    }
}
