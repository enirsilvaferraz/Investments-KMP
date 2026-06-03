package com.eferraz.entities.transactions

import kotlin.test.Test
import kotlin.test.assertTrue

class AssetTransactionContractTest {

    /**
     * Regression SC-005: transactions must not expose a holding back-reference on the sealed interface.
     */
    @Test
    fun `GIVEN AssetTransaction interface THEN no holding property exists`() {
        val typesToCheck = listOf(
            AssetTransaction::class.java,
            FixedIncomeTransaction::class.java,
            VariableIncomeTransaction::class.java,
            FundsTransaction::class.java,
        )

        for (type in typesToCheck) {
            val hasHolding = type.methods.any { method ->
                method.name == "getHolding" || method.name == "holding"
            }
            assertTrue(
                !hasHolding,
                "Type ${type.simpleName} must not expose a holding property",
            )
        }
    }
}
