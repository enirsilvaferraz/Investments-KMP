package com.eferraz.entities.transactions

import kotlin.test.Test
import kotlin.test.assertTrue

class AssetTransactionContractTest {

    /**
     * Regression SC-005: transactions must not expose a holding back-reference.
     */
    @Test
    fun `GIVEN AssetTransaction type THEN no holding property exists`() {

        // WHEN
        val hasHolding = AssetTransaction::class.java.methods.any { method ->
            method.name == "getHolding" || method.name == "holding"
        }

        // THEN
        assertTrue(
            !hasHolding,
            "AssetTransaction must not expose a holding property",
        )
    }
}
