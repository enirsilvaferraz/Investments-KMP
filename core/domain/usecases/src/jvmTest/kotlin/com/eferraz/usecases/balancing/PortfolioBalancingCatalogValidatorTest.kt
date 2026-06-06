package com.eferraz.usecases.balancing

import kotlin.test.Test

public class PortfolioBalancingCatalogValidatorTest {

    /**
     * Default catalog configured weights sum to 100% per group within tolerance.
     */
    @Test
    public fun `GIVEN default catalog WHEN validate THEN all groups pass configured weight sum check`() {

        // WHEN / THEN
        PortfolioBalancingCatalogValidator.validate()
    }
}
