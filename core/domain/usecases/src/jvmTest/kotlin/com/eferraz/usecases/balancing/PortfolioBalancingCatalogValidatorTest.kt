package com.eferraz.usecases.balancing

import kotlin.test.Test
import kotlin.test.assertEquals

public class PortfolioBalancingCatalogValidatorTest {

    /**
     * Every catalog group receives an automatic «Demais investimentos» fallback component.
     */
    @Test
    public fun `GIVEN default catalog WHEN inspect groups THEN each group ends with other investments fallback`() {

        // THEN
        PortfolioBalancingCatalog.groups.forEach { group ->
            val other = group.components.last()
            assertEquals(BalancingGroupId.OTHER_INVESTMENTS, other.id)
            assertEquals("Demais investimentos", other.displayName)
        }
    }

    /**
     * Default catalog configured weights sum to 100% per group within tolerance.
     */
    @Test
    public fun `GIVEN default catalog WHEN validate THEN all groups pass configured weight sum check`() {

        // WHEN / THEN
        PortfolioBalancingCatalogValidator.validate()
    }
}
