package com.eferraz.entities.brokeragenotes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NoteFeeAllocationTest {

    private val canonicalNote = CanonicalNoteFixtures.simplifiedThreeAssetNote()

    private val ajfi11 = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00)
    private val brco11 = NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL, 10.0, 100.00, 1000.00)
    private val vilg11 = NoteAsset("VILG11", "VILG11 CI", TradeType.BUY, 1000.0, 1.00, 1000.00)

    // --- User Story 1 ---

    /**
     * Empty asset list throws IllegalArgumentException before allocation.
     */
    @Test
    fun `GIVEN empty asset list WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(assets = emptyList())

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals("assets must not be empty", exception.message)
    }

    /**
     * Negative settlement fee throws IllegalArgumentException with descriptive message.
     */
    @Test
    fun `GIVEN negative settlement fee WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            financialSummary = canonicalNote.financialSummary.copy(
                apportionableFees = canonicalNote.financialSummary.apportionableFees.copy(
                    settlement = -1.00,
                ),
            ),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals(true, exception.message?.contains("must not be negative") == true)
    }

    /**
     * Non-positive quantity throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN asset with non-positive quantity WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            assets = listOf(NoteAsset("XPTO3", "XPTO3 ON", TradeType.BUY, -5.0, 10.0, -50.0)),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals("asset XPTO3: quantity and unitPrice must be > 0", exception.message)
    }

    /**
     * Volume mismatch between assets and declared total throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN totalVolumeTraded mismatch WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            financialSummary = canonicalNote.financialSummary.copy(totalVolumeTraded = 9999.00),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals(true, exception.message?.contains("volume mismatch") == true)
    }

    /**
     * grossValue inconsistent with quantity times unitPrice throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN grossValue mismatch WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val badAsset = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 999.00)
        val note = canonicalNote.copy(
            financialSummary = canonicalNote.financialSummary.copy(
                totalVolumeTraded = 999.00,
                totalBuys = 999.00,
            ),
            assets = listOf(badAsset),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals(true, exception.message?.contains("quantity×unitPrice") == true)
    }

    /**
     * Incorrect BUY subtotal throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN incorrect BUY subtotal WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            financialSummary = canonicalNote.financialSummary.copy(totalBuys = 9999.00),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals(true, exception.message?.contains("buys/sells totals mismatch") == true)
    }

    /**
     * Structurally identical assets throw IllegalArgumentException.
     */
    @Test
    fun `GIVEN duplicate identical assets WHEN validate THEN throws IllegalArgumentException`() {

        // GIVEN
        val note = canonicalNote.copy(
            financialSummary = canonicalNote.financialSummary.copy(
                totalVolumeTraded = 2000.00,
                totalBuys = 2000.00,
                totalSells = 0.00,
            ),
            assets = listOf(
                NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00),
                NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00),
            ),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            BrokerageNoteValidator.validate(note)
        }
        assertEquals(true, exception.message?.contains("duplicate asset") == true)
    }

    // --- User Story 2 ---

    /**
     * Canonical 3-asset note distributes fees with last asset absorbing rounding residue.
     */
    @Test
    fun `GIVEN canonical 3-asset note WHEN calculateFeeAllocation THEN distributes fees proportionally`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(1001.51, result.getValue(ajfi11), 0.01)
        assertEquals(998.49, result.getValue(brco11), 0.01)
        assertEquals(1001.52, result.getValue(vilg11), 0.01)
        assertEquals(4.54, totalAllocatedFee(note, result), 0.01)
    }

    /**
     * Full canonical note from docs/nota.json satisfies fee sum and accounting closure.
     */
    @Test
    fun `GIVEN full canonical note WHEN calculateFeeAllocation THEN closure and fee sum hold`() {

        // GIVEN
        val note = CanonicalNoteFixtures.fullCanonicalNote()

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(30, result.size)
        assertEquals(14.66, totalAllocatedFee(note, result), 0.01)
        assertEquals(note.metadata.netValue, buySellNetDifference(note, result), 0.01)
    }

    /**
     * Single BUY asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single BUY asset WHEN calculateFeeAllocation THEN asset absorbs all fees`() {

        // GIVEN
        val asset = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00)
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 1004.54,
            assets = listOf(asset),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(1, result.size)
        assertEquals(1004.54, result.getValue(asset), 0.01)
    }

    /**
     * Single SELL asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single SELL asset WHEN calculateFeeAllocation THEN asset absorbs all fees`() {

        // GIVEN
        val asset = NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL, 10.0, 100.00, 1000.00)
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -995.46,
            assets = listOf(asset),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(1, result.size)
        assertEquals(995.46, result.getValue(asset), 0.01)
    }

    /**
     * Note with all SELL assets satisfies closure with negative netValue.
     */
    @Test
    fun `GIVEN note with all SELL assets WHEN calculateFeeAllocation THEN closure holds`() {

        // GIVEN
        val assets = listOf(
            NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL, 10.0, 100.00, 1000.00),
            NoteAsset("VILG11", "VILG11 CI", TradeType.SELL, 10.0, 100.00, 1000.00),
        )
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -1995.46,
            assets = assets,
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(2, result.size)
        assertEquals(note.metadata.netValue, buySellNetDifference(note, result), 0.01)
    }

    /**
     * Zero fees yield netValue equal to grossValue for every asset.
     */
    @Test
    fun `GIVEN zero fees WHEN calculateFeeAllocation THEN netValue equals grossValue`() {

        // GIVEN
        val note = canonicalNote.copy(
            metadata = canonicalNote.metadata.copy(netValue = 1000.00),
            financialSummary = canonicalNote.financialSummary.copy(
                apportionableFees = ApportionableFees(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            ),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        note.assets.forEach { asset ->
            assertEquals(asset.grossValue, result.getValue(asset), 0.01)
        }
    }

    /**
     * Same ticker with different specification produces distinct map keys.
     */
    @Test
    fun `GIVEN same ticker different specification WHEN calculateFeeAllocation THEN distinct keys`() {

        // GIVEN
        val assetA = NoteAsset("BRBI11", "BRB111F UNT N2", TradeType.BUY, 10.0, 15.00, 150.00)
        val assetB = NoteAsset("BRBI11", "BRB111 UNT N2", TradeType.BUY, 10.0, 15.00, 150.00)
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 300.00,
            apportionableFees = ApportionableFees(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            assets = listOf(assetA, assetB),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(2, result.size)
        assertEquals(true, result.containsKey(assetA))
        assertEquals(true, result.containsKey(assetB))
    }

    // --- User Story 3 ---

    /**
     * Canonical note satisfies accounting closure equation.
     */
    @Test
    fun `GIVEN canonical note WHEN calculateFeeAllocation THEN accounting closure holds`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(1001.51 + 1001.52, buyTotal(note, result), 0.01)
        assertEquals(998.49, sellTotal(note, result), 0.01)
        assertEquals(note.metadata.netValue, buySellNetDifference(note, result), 0.01)
    }

    /**
     * Incorrect metadata.netValue throws IllegalStateException with descriptive message.
     */
    @Test
    fun `GIVEN incorrect netValue WHEN calculateFeeAllocation THEN throws IllegalStateException`() {

        // GIVEN
        val note = canonicalNote.copy(
            metadata = canonicalNote.metadata.copy(netValue = 9999.00),
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalStateException> {
            note.calculateFeeAllocation()
        }
        assertEquals(true, exception.message?.contains("accounting closure failed") == true)
    }

    /**
     * All-SELL note satisfies closure with negative metadata.netValue.
     */
    @Test
    fun `GIVEN all SELL note WHEN calculateFeeAllocation THEN closure equation holds`() {

        // GIVEN
        val assets = listOf(
            NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL, 10.0, 100.00, 1000.00),
            NoteAsset("VILG11", "VILG11 CI", TradeType.SELL, 10.0, 100.00, 1000.00),
        )
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -1995.46,
            assets = assets,
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(0.0, buyTotal(note, result), 0.01)
        assertEquals(note.metadata.netValue, buySellNetDifference(note, result), 0.01)
    }

    // --- User Story 4 ---

    /**
     * Three equal-volume assets: last asset receives rounding residue.
     */
    @Test
    fun `GIVEN three equal-volume assets WHEN calculateFeeAllocation THEN last asset absorbs residue`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(1.51, allocatedFee(ajfi11, result.getValue(ajfi11)), 0.01)
        assertEquals(1.51, allocatedFee(brco11, result.getValue(brco11)), 0.01)
        assertEquals(1.52, allocatedFee(vilg11, result.getValue(vilg11)), 0.01)
        assertEquals(4.54, totalAllocatedFee(note, result), 0.01)
    }

    /**
     * Two equal-volume assets: second (last) asset receives rounding residue.
     */
    @Test
    fun `GIVEN two equal-volume assets WHEN calculateFeeAllocation THEN last asset absorbs residue`() {

        // GIVEN
        val first = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00)
        val second = NoteAsset("BRCO11", "BRCO11 CI", TradeType.BUY, 100.0, 10.00, 1000.00)
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 2001.01,
            apportionableFees = ApportionableFees(
                settlement = 0.51,
                emoluments = 0.50,
                transfer = 0.00,
                brokerage = 0.00,
                iss = 0.00,
                others = 0.00,
            ),
            assets = listOf(first, second),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(0.51, allocatedFee(first, result.getValue(first)), 0.01)
        assertEquals(0.50, allocatedFee(second, result.getValue(second)), 0.01)
        assertEquals(1.01, totalAllocatedFee(note, result), 0.01)
    }

    /**
     * Single asset receives the full fee total as residual.
     */
    @Test
    fun `GIVEN single asset WHEN calculateFeeAllocation THEN allocatedFee equals somaFees`() {

        // GIVEN
        val asset = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00)
        val note = CanonicalNoteFixtures.simplifiedThreeAssetNote(assets = listOf(asset))

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(note.financialSummary.apportionableFees.total, allocatedFee(asset, result.getValue(asset)), 0.01)
    }

    /**
     * Zero somaFees keeps netValue equal to grossValue and satisfies closure.
     */
    @Test
    fun `GIVEN zero somaFees WHEN calculateFeeAllocation THEN closure holds with zero allocation`() {

        // GIVEN
        val note = canonicalNote.copy(
            metadata = canonicalNote.metadata.copy(netValue = 1000.00),
            financialSummary = canonicalNote.financialSummary.copy(
                apportionableFees = ApportionableFees(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            ),
        )

        // WHEN
        val result = note.calculateFeeAllocation()

        // THEN
        assertEquals(0.0, totalAllocatedFee(note, result), 0.01)
        assertEquals(note.metadata.netValue, buySellNetDifference(note, result), 0.01)
    }

    /**
     * Repeated calls on the same note instance return identical maps and key order.
     */
    @Test
    fun `GIVEN same note instance WHEN calculateFeeAllocation twice THEN results are identical`() {

        // GIVEN
        val note = canonicalNote

        // WHEN
        val first = note.calculateFeeAllocation()
        val second = note.calculateFeeAllocation()

        // THEN
        assertEquals(first, second)
        assertEquals(first.keys.toList(), second.keys.toList())
    }

    private fun allocatedFee(asset: NoteAsset, netValue: Double): Double = when (asset.tradeType) {
        TradeType.BUY -> netValue - asset.grossValue
        TradeType.SELL -> asset.grossValue - netValue
    }

    private fun totalAllocatedFee(note: BrokerageNote, result: NoteFeeAllocation): Double =
        note.assets.sumOf { asset -> allocatedFee(asset, result.getValue(asset)) }

    private fun buyTotal(note: BrokerageNote, result: NoteFeeAllocation): Double =
        note.assets
            .filter { it.tradeType == TradeType.BUY }
            .sumOf { result.getValue(it) }

    private fun sellTotal(note: BrokerageNote, result: NoteFeeAllocation): Double =
        note.assets
            .filter { it.tradeType == TradeType.SELL }
            .sumOf { result.getValue(it) }

    private fun buySellNetDifference(note: BrokerageNote, result: NoteFeeAllocation): Double =
        buyTotal(note, result) - sellTotal(note, result)
}
