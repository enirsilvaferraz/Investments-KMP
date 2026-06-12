package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NoteFeeAllocationTest {

    private val canonicalV2 = CanonicalNoteFixtures.simplifiedThreeAssetNote()

    private val ajfi11 = canonicalV2.assets[0]
    private val brco11 = canonicalV2.assets[1]
    private val vilg11 = canonicalV2.assets[2]

    // --- User Story 2 ---

    /**
     * Canonical 3-asset note distributes fees with last asset absorbing rounding residue.
     */
    @Test
    fun `GIVEN canonical 3-asset note WHEN calculate THEN distributes fees proportionally`() {

        // GIVEN
        val v2 = canonicalV2

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1001.51, result.getValue(ajfi11), 0.01)
        assertEquals(998.49, result.getValue(brco11), 0.01)
        assertEquals(1001.52, result.getValue(vilg11), 0.01)
        assertEquals(4.54, totalAllocatedFee(v2, result), 0.01)
    }

    /**
     * Full canonical note from docs/nota.json satisfies fee sum and accounting closure.
     */
    @Test
    fun `GIVEN full canonical note WHEN calculate THEN closure and fee sum hold`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.fullCanonicalNote()

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(30, result.size)
        assertEquals(14.66, totalAllocatedFee(v2, result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(v2, result), 0.01)
    }

    /**
     * Single BUY asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single BUY asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 1004.54,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 100.0, 10.00),
            ),
        )
        val asset = v2.assets.single()

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1, result.size)
        assertEquals(1004.54, result.getValue(asset), 0.01)
    }

    /**
     * Single SELL asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single SELL asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
            ),
        )
        val asset = v2.assets.single()

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1, result.size)
        assertEquals(995.46, result.getValue(asset), 0.01)
    }

    /**
     * Note with all SELL assets satisfies closure with negative netValue.
     */
    @Test
    fun `GIVEN note with all SELL assets WHEN calculate THEN closure holds`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -1995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
                CanonicalNoteFixtures.asset(1, TransactionType.SALE, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(2, result.size)
        assertEquals(v2.netValue, buySellNetDifference(v2, result), 0.01)
    }

    /**
     * Zero fees yield netValue equal to grossValue for every asset.
     */
    @Test
    fun `GIVEN zero fees WHEN calculate THEN netValue equals grossValue`() {

        // GIVEN
        val v2 = canonicalV2.copy(
            netValue = 1000.00,
            apportionableFees = 0.0,
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        v2.assets.forEach { asset ->
            assertEquals(asset.totalValue, result.getValue(asset), 0.01)
        }
    }

    /**
     * Two transactions with different unit prices produce distinct map keys.
     */
    @Test
    fun `GIVEN two transactions with different unit prices WHEN calculate THEN distinct keys`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 310.00,
            apportionableFees = 0.0,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 10.0, 15.00),
                CanonicalNoteFixtures.asset(1, TransactionType.PURCHASE, 10.0, 16.00),
            ),
        )
        val assetA = v2.assets[0]
        val assetB = v2.assets[1]

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

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
    fun `GIVEN canonical note WHEN calculate THEN accounting closure holds`() {

        // GIVEN
        val v2 = canonicalV2

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1001.51 + 1001.52, buyTotal(v2, result), 0.01)
        assertEquals(998.49, sellTotal(v2, result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(v2, result), 0.01)
    }

    /**
     * Incorrect netValue throws IllegalStateException with descriptive message.
     */
    @Test
    fun `GIVEN incorrect netValue WHEN calculate THEN throws IllegalStateException`() {

        // GIVEN
        val v2 = canonicalV2.copy(netValue = 9999.00)

        // WHEN / THEN
        val exception = assertFailsWith<IllegalStateException> {
            NoteFeeAllocation.calculate(v2)
        }
        assertEquals(true, exception.message?.contains("accounting closure failed") == true)
    }

    /**
     * All-SELL note satisfies closure with negative metadata.netValue.
     */
    @Test
    fun `GIVEN all SELL note WHEN calculate THEN closure equation holds`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -1995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
                CanonicalNoteFixtures.asset(1, TransactionType.SALE, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.0, buyTotal(v2, result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(v2, result), 0.01)
    }

    // --- User Story 4 ---

    /**
     * Three equal-volume assets: last asset receives rounding residue.
     */
    @Test
    fun `GIVEN three equal-volume assets WHEN calculate THEN last asset absorbs residue`() {

        // GIVEN
        val v2 = canonicalV2

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1.51, allocatedFee(ajfi11, result.getValue(ajfi11)), 0.01)
        assertEquals(1.51, allocatedFee(brco11, result.getValue(brco11)), 0.01)
        assertEquals(1.52, allocatedFee(vilg11, result.getValue(vilg11)), 0.01)
        assertEquals(4.54, totalAllocatedFee(v2, result), 0.01)
    }

    /**
     * Two equal-volume assets: second (last) asset receives rounding residue.
     */
    @Test
    fun `GIVEN two equal-volume assets WHEN calculate THEN last asset absorbs residue`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 2001.01,
            apportionableFees = 1.01,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 100.0, 10.00),
                CanonicalNoteFixtures.asset(1, TransactionType.PURCHASE, 100.0, 10.00),
            ),
        )
        val first = v2.assets[0]
        val second = v2.assets[1]

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.51, allocatedFee(first, result.getValue(first)), 0.01)
        assertEquals(0.50, allocatedFee(second, result.getValue(second)), 0.01)
        assertEquals(1.01, totalAllocatedFee(v2, result), 0.01)
    }

    /**
     * Single asset receives the full fee total as residual.
     */
    @Test
    fun `GIVEN single asset WHEN calculate THEN allocatedFee equals somaFees`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 100.0, 10.00),
            ),
        )
        val asset = v2.assets.single()

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(v2.apportionableFees, allocatedFee(asset, result.getValue(asset)), 0.01)
    }

    /**
     * Zero somaFees keeps netValue equal to grossValue and satisfies closure.
     */
    @Test
    fun `GIVEN zero somaFees WHEN calculate THEN closure holds with zero allocation`() {

        // GIVEN
        val v2 = canonicalV2.copy(
            netValue = 1000.00,
            apportionableFees = 0.0,
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.0, totalAllocatedFee(v2, result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(v2, result), 0.01)
    }

    /**
     * Repeated calls on the same note instance return identical maps and key order.
     */
    @Test
    fun `GIVEN same note instance WHEN calculate twice THEN results are identical`() {

        // GIVEN
        val v2 = canonicalV2

        // WHEN
        val first = NoteFeeAllocation.calculate(v2)
        val second = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(first, second)
        assertEquals(first.keys.toList(), second.keys.toList())
    }

    private fun allocatedFee(asset: AssetTransaction, netValue: Double): Double = when (asset.type) {
        TransactionType.PURCHASE -> netValue - asset.totalValue
        TransactionType.SALE -> asset.totalValue - netValue
    }

    private fun totalAllocatedFee(
        v2: BrokerageNote,
        result: NoteFeeAllocation,
    ): Double = v2.assets.sumOf { asset -> allocatedFee(asset, result.getValue(asset)) }

    private fun buyTotal(v2: BrokerageNote, result: NoteFeeAllocation): Double =
        v2.assets
            .filter { it.type == TransactionType.PURCHASE }
            .sumOf { result.getValue(it) }

    private fun sellTotal(v2: BrokerageNote, result: NoteFeeAllocation): Double =
        v2.assets
            .filter { it.type == TransactionType.SALE }
            .sumOf { result.getValue(it) }

    private fun buySellNetDifference(
        v2: BrokerageNote,
        result: NoteFeeAllocation,
    ): Double = buyTotal(v2, result) - sellTotal(v2, result)
}
