package com.eferraz.database.datasources.impl

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveUpsertRowIdTest {

    /**
     * Insert path returns the SQLite rowid of the new holding.
     */
    @Test
    fun `GIVEN upsert inserted row WHEN resolve THEN returns rowid`() {
        assertEquals(5L, resolveUpsertRowIdInternal(upsertRowId = 5L, entityId = 0L))
    }

    /**
     * Update path returns -1; callers must keep the existing entity id for FK writes.
     */
    @Test
    fun `GIVEN upsert updated row WHEN resolve THEN returns existing entity id`() {
        assertEquals(3L, resolveUpsertRowIdInternal(upsertRowId = -1L, entityId = 3L))
    }
}
