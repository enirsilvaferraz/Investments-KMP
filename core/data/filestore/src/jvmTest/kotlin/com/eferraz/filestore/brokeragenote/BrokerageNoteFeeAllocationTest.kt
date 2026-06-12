package com.eferraz.filestore.brokeragenote

import com.eferraz.entities.brokeragenotes.NoteFeeAllocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class BrokerageNoteFeeAllocationTest {

    /**
     * Last CASH3 line in nota2 receives its proportional fee after allocation.
     */
    @Test
    fun `GIVEN nota2 WHEN calculate fees THEN last CASH3 has non-zero allocatedFee`() = runTest {

        // GIVEN
        val note = BrokerageNoteJsonDataSourceImpl().loadNote().getOrThrow()

        // WHEN
        val allocation = NoteFeeAllocation.calculate(note)
        val cash3Last = allocation.assets.last { it.ticker == "CASH3" && it.transaction.quantity == 300.0 }

        // THEN
        assertEquals(0.36, cash3Last.transaction.allocatedFee, 0.01)
        assertTrue(cash3Last.transaction.allocatedFee > 0.0)
    }
}
