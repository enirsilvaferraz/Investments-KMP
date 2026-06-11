package com.eferraz.entities.brokeragenotes

import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.datetime.LocalDate

class NoteFeeAllocationTest {

    private val canonicalNote = BrokerageNote(
        date = LocalDate(2026, 1, 1),
        netValue = 1004.54,
        fees = BrokerageNoteFees(emoluments = 1.00, settlement = 3.54, incomeTax = 0.00),
        assets = listOf(
            NoteAsset("AJFI11", TradeType.BUY, 100.0, 10.00),
            NoteAsset("BRCO11", TradeType.SELL, 10.0, 100.00),
            NoteAsset("VILG11", TradeType.BUY, 1000.0, 1.00),
        ),
    )

    // --- User Story 1 ---

    /**
     * Canonical 3-asset note with equal volumes: proportional fees and net values per asset.
     */
    @Test
    fun `GIVEN canonical 3-asset note WHEN calculate THEN distributes fees proportionally`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals("AJFI11", result.allocations[0].ticker)
        assertEquals(1000.00, result.allocations[0].grossValue, 0.01)
        assertEquals(1.52, result.allocations[0].allocatedFee, 0.01)
        assertEquals(1001.52, result.allocations[0].netValue, 0.01)

        assertEquals("BRCO11", result.allocations[1].ticker)
        assertEquals(1000.00, result.allocations[1].grossValue, 0.01)
        assertEquals(1.51, result.allocations[1].allocatedFee, 0.01)
        assertEquals(998.49, result.allocations[1].netValue, 0.01)

        assertEquals("VILG11", result.allocations[2].ticker)
        assertEquals(1000.00, result.allocations[2].grossValue, 0.01)
        assertEquals(1.51, result.allocations[2].allocatedFee, 0.01)
        assertEquals(1001.51, result.allocations[2].netValue, 0.01)

        val totalAllocatedFee = result.allocations.sumOf { it.allocatedFee }
        assertEquals(4.54, totalAllocatedFee, 0.01)
    }

    /**
     * Single BUY asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single BUY asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val note = BrokerageNote(
            date = LocalDate(2026, 1, 1),
            netValue = 1004.54,
            fees = BrokerageNoteFees(emoluments = 1.00, settlement = 3.54, incomeTax = 0.00),
            assets = listOf(
                NoteAsset("AJFI11", TradeType.BUY, 100.0, 10.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals(1, result.allocations.size)
        assertEquals(4.54, result.allocations[0].allocatedFee, 0.01)
        assertEquals(1004.54, result.allocations[0].netValue, 0.01)
    }

    /**
     * Single SELL asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single SELL asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val note = BrokerageNote(
            date = LocalDate(2026, 1, 1),
            netValue = -995.46,
            fees = BrokerageNoteFees(emoluments = 1.00, settlement = 3.54, incomeTax = 0.00),
            assets = listOf(
                NoteAsset("BRCO11", TradeType.SELL, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals(1, result.allocations.size)
        assertEquals(4.54, result.allocations[0].allocatedFee, 0.01)
        assertEquals(995.46, result.allocations[0].netValue, 0.01)
    }

    /**
     * Note with all SELL assets and negative netValue satisfies closure equation.
     */
    @Test
    fun `GIVEN note with all SELL assets WHEN calculate THEN closure holds with negative netValue`() {

        // GIVEN
        val note = BrokerageNote(
            date = LocalDate(2026, 1, 1),
            netValue = -1995.46,
            fees = BrokerageNoteFees(emoluments = 1.00, settlement = 3.54, incomeTax = 0.00),
            assets = listOf(
                NoteAsset("BRCO11", TradeType.SELL, 10.0, 100.00),
                NoteAsset("VILG11", TradeType.SELL, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals(2, result.allocations.size)
        val sellsTotal = result.allocations.sumOf { it.netValue }
        assertEquals(-note.netValue, sellsTotal, 0.01)
    }

    /**
     * Zero fees yield zero allocatedFee and netValue equal to grossValue.
     */
    @Test
    fun `GIVEN zero fees WHEN calculate THEN allocatedFee is zero and netValue equals grossValue`() {

        // GIVEN
        val note = canonicalNote.copy(
            fees = BrokerageNoteFees(emoluments = 0.0, settlement = 0.0, incomeTax = 0.0),
            netValue = 1000.00,
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        result.allocations.forEach { allocation ->
            assertEquals(0.0, allocation.allocatedFee, 0.01)
            assertEquals(allocation.grossValue, allocation.netValue, 0.01)
        }
    }

    /**
     * Empty asset list throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN empty asset list WHEN calculate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(assets = emptyList())

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            NoteFeeAllocation.calculate(note)
        }
        assertEquals("assets must not be empty", exception.message)
    }

    /**
     * Asset with non-positive quantity throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN asset with non-positive quantity WHEN calculate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            assets = listOf(NoteAsset("XPTO3", TradeType.BUY, -5.0, 10.0)),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            NoteFeeAllocation.calculate(note)
        }
        assertEquals("asset XPTO3: quantity and unitPrice must be > 0", exception.message)
    }

    /**
     * Asset with non-positive unitPrice throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN asset with non-positive unitPrice WHEN calculate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            assets = listOf(NoteAsset("XPTO3", TradeType.BUY, 10.0, 0.0)),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            NoteFeeAllocation.calculate(note)
        }
        assertEquals("asset XPTO3: quantity and unitPrice must be > 0", exception.message)
    }

    // --- User Story 2 ---

    /**
     * Canonical note satisfies accounting closure: sum BUY net minus sum SELL net equals note.netValue.
     */
    @Test
    fun `GIVEN canonical note WHEN calculate THEN accounting closure holds`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        val buysTotal = result.allocations
            .zip(note.assets)
            .filter { (_, asset) -> asset.tradeType == TradeType.BUY }
            .sumOf { (allocation, _) -> allocation.netValue }
        val sellsTotal = result.allocations
            .zip(note.assets)
            .filter { (_, asset) -> asset.tradeType == TradeType.SELL }
            .sumOf { (allocation, _) -> allocation.netValue }

        assertEquals(1001.52 + 1001.51, buysTotal, 0.01)
        assertEquals(998.49, sellsTotal, 0.01)
        assertEquals(note.netValue, buysTotal - sellsTotal, 0.01)
    }

    /**
     * Incorrect note.netValue throws IllegalStateException with descriptive message.
     */
    @Test
    fun `GIVEN incorrect netValue WHEN calculate THEN throws IllegalStateException`() {

        // GIVEN
        val note = canonicalNote.copy(netValue = 9999.00)

        // WHEN / THEN
        val exception = assertFailsWith<IllegalStateException> {
            NoteFeeAllocation.calculate(note)
        }
        assertEquals(
            "accounting closure failed: expected 9999.0, got 1004.54",
            exception.message,
        )
    }

    // --- User Story 3 ---

    /**
     * Three equal-volume assets: remainder cent goes to first asset with max volume.
     */
    @Test
    fun `GIVEN three equal-volume assets WHEN calculate THEN remainder cent goes to first max-volume asset`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals(1.52, result.allocations[0].allocatedFee, 0.01)
        assertEquals(1.51, result.allocations[1].allocatedFee, 0.01)
        assertEquals(1.51, result.allocations[2].allocatedFee, 0.01)
        assertEquals(4.54, result.allocations.sumOf { it.allocatedFee }, 0.01)
    }

    /**
     * Two equal-volume assets: remainder cent goes to the first asset in the list.
     */
    @Test
    fun `GIVEN two equal-volume assets WHEN calculate THEN remainder cent goes to first asset`() {

        // GIVEN
        val note = BrokerageNote(
            date = LocalDate(2026, 1, 1),
            netValue = 2001.01,
            fees = BrokerageNoteFees(emoluments = 0.50, settlement = 0.51, incomeTax = 0.00),
            assets = listOf(
                NoteAsset("AJFI11", TradeType.BUY, 100.0, 10.00),
                NoteAsset("BRCO11", TradeType.BUY, 100.0, 10.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        assertEquals(0.51, result.allocations[0].allocatedFee, 0.01)
        assertEquals(0.50, result.allocations[1].allocatedFee, 0.01)
        assertEquals(1.01, result.allocations.sumOf { it.allocatedFee }, 0.01)
    }

    /**
     * For any valid note, sum of allocated fees in cents equals total fees in cents.
     */
    @Test
    fun `GIVEN canonical note WHEN calculate THEN allocated fee cents sum equals total fee cents`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = NoteFeeAllocation.calculate(note)

        // THEN
        val allocatedCents = result.allocations.sumOf { round(it.allocatedFee * 100.0).toLong() }
        val totalFeeCents = round(note.fees.total * 100.0).toLong()
        assertEquals(totalFeeCents, allocatedCents)
    }
}
