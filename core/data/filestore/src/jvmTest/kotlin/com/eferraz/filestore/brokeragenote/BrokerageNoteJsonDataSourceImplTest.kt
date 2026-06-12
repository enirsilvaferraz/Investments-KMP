package com.eferraz.filestore.brokeragenote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class BrokerageNoteJsonDataSourceImplTest {

    /**
     * loadNote parses Nota2JsonFixture, validates the document, and returns BrokerageNoteV2.
     */
    @Test
    fun `GIVEN data source WHEN loadNote THEN returns success with parsed note`() = runTest {

        // GIVEN
        val dataSource = BrokerageNoteJsonDataSourceImpl()

        // WHEN
        val result = dataSource.loadNote()

        // THEN
        assertTrue(result.isSuccess)
        val note = result.getOrThrow()
        assertEquals(56402.04, note.totalVolumeTraded, 0.01)
        assertEquals(12294.92, note.netValue, 0.01)
        assertEquals(17.85, note.apportionableFees, 0.01)
        assertEquals(1.71, note.withheldTaxes, 0.01)
        assertEquals(47, note.assets.size)
    }
}
