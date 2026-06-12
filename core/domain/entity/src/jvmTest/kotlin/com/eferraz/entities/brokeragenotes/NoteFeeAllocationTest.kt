package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NoteFeeAllocationTest {

    private val canonicalV2 = CanonicalNoteFixtures.simplifiedThreeAssetNote()

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
        assertEquals(1.51, feeAt(result, 0), 0.01)
        assertEquals(1.51, feeAt(result, 1), 0.01)
        assertEquals(1.52, feeAt(result, 2), 0.01)
        assertEquals(4.54, totalAllocatedFee(result), 0.01)
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
        assertEquals(30, result.assets.size)
        assertEquals(14.66, totalAllocatedFee(result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(result), 0.01)
    }

    /**
     * Single BUY asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single BUY asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -1004.54,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 100.0, 10.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1, result.assets.size)
        assertEquals(4.54, feeAt(result, 0), 0.01)
    }

    /**
     * Single SELL asset absorbs 100% of note fees.
     */
    @Test
    fun `GIVEN single SELL asset WHEN calculate THEN asset absorbs all fees`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(1, result.assets.size)
        assertEquals(4.54, feeAt(result, 0), 0.01)
    }

    /**
     * Note with all SELL assets satisfies closure with positive netValue.
     */
    @Test
    fun `GIVEN note with all SELL assets WHEN calculate THEN closure holds`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 1995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
                CanonicalNoteFixtures.asset(1, TransactionType.SALE, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(2, result.assets.size)
        assertEquals(v2.netValue, buySellNetDifference(result), 0.01)
    }

    /**
     * Zero fees yield zero allocatedFee for every asset.
     */
    @Test
    fun `GIVEN zero fees WHEN calculate THEN allocatedFee is zero`() {

        // GIVEN
        val v2 = canonicalV2.copy(
            netValue = -1000.00,
            apportionableFees = 0.0,
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        result.assets.forEach { noteAsset ->
            assertEquals(0.0, noteAsset.transaction.allocatedFee, 0.01)
        }
    }

    /**
     * Two transactions with different unit prices produce distinct lines.
     */
    @Test
    fun `GIVEN two transactions with different unit prices WHEN calculate THEN distinct lines`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -310.00,
            apportionableFees = 0.0,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 10.0, 15.00),
                CanonicalNoteFixtures.asset(1, TransactionType.PURCHASE, 10.0, 16.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(2, result.assets.size)
        assertEquals("TICK0", result.assets[0].ticker)
        assertEquals("TICK1", result.assets[1].ticker)
    }

    /**
     * Identical transaction fields on different tickers keep separate fee entries.
     */
    @Test
    fun `GIVEN two tickers with identical transaction WHEN calculate THEN each line keeps its own fee`() {

        // GIVEN
        val sharedTransaction = AssetTransaction(
            id = 0L,
            date = LocalDate(2026, 1, 1),
            type = TransactionType.PURCHASE,
            quantity = 100.0,
            unitPrice = 10.0,
        )
        val first = BrokerageNoteAsset(ticker = "AAA11", transaction = sharedTransaction)
        val second = BrokerageNoteAsset(ticker = "BBB11", transaction = sharedTransaction)
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -2001.01,
            apportionableFees = 1.01,
            assets = listOf(first, second),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.51, feeAt(result, 0), 0.01)
        assertEquals(0.50, feeAt(result, 1), 0.01)
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
        assertEquals(1001.51 + 1001.52, buyTotal(result), 0.01)
        assertEquals(998.49, sellTotal(result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(result), 0.01)
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
     * All-SELL note satisfies closure with positive metadata.netValue.
     */
    @Test
    fun `GIVEN all SELL note WHEN calculate THEN closure equation holds`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 1995.46,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 100.00),
                CanonicalNoteFixtures.asset(1, TransactionType.SALE, 10.0, 100.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.0, buyTotal(result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(result), 0.01)
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
        assertEquals(1.51, feeAt(result, 0), 0.01)
        assertEquals(1.51, feeAt(result, 1), 0.01)
        assertEquals(1.52, feeAt(result, 2), 0.01)
        assertEquals(4.54, totalAllocatedFee(result), 0.01)
    }

    /**
     * Two equal-volume assets: second (last) asset receives rounding residue.
     */
    @Test
    fun `GIVEN two equal-volume assets WHEN calculate THEN last asset absorbs residue`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = -2001.01,
            apportionableFees = 1.01,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.PURCHASE, 100.0, 10.00),
                CanonicalNoteFixtures.asset(1, TransactionType.PURCHASE, 100.0, 10.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.51, feeAt(result, 0), 0.01)
        assertEquals(0.50, feeAt(result, 1), 0.01)
        assertEquals(1.01, totalAllocatedFee(result), 0.01)
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

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(v2.apportionableFees, feeAt(result, 0), 0.01)
    }

    /**
     * Zero somaFees keeps netValue equal to grossValue and satisfies closure.
     */
    @Test
    fun `GIVEN zero somaFees WHEN calculate THEN closure holds with zero allocation`() {

        // GIVEN
        val v2 = canonicalV2.copy(
            netValue = -1000.00,
            apportionableFees = 0.0,
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(0.0, totalAllocatedFee(result), 0.01)
        assertEquals(v2.netValue, buySellNetDifference(result), 0.01)
    }

    /**
     * Credit note with withheld taxes subtracts IRRF from trade balance in closure.
     */
    @Test
    fun `GIVEN credit note with withheld taxes WHEN calculate THEN closure subtracts irrf`() {

        // GIVEN
        val v2 = CanonicalNoteFixtures.simplifiedThreeAssetNote(
            netValue = 95.0,
            apportionableFees = 0.0,
            withheldTaxes = 5.0,
            assets = listOf(
                CanonicalNoteFixtures.asset(0, TransactionType.SALE, 10.0, 10.00),
            ),
        )

        // WHEN
        val result = NoteFeeAllocation.calculate(v2)

        // THEN
        assertEquals(v2.netValue, buySellNetDifference(result) - v2.withheldTaxes, 0.01)
    }

    /**
     * Repeated calls on the same note instance return identical results.
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
    }

    private fun feeAt(result: NoteFeeAllocation, index: Int): Double =
        result.assets[index].transaction.allocatedFee

    private fun totalAllocatedFee(result: NoteFeeAllocation): Double =
        result.assets.sumOf { it.transaction.allocatedFee }

    private fun buyTotal(result: NoteFeeAllocation): Double =
        result.assets
            .filter { it.transaction.type == TransactionType.PURCHASE }
            .sumOf { it.transaction.netValue }

    private fun sellTotal(result: NoteFeeAllocation): Double =
        result.assets
            .filter { it.transaction.type == TransactionType.SALE }
            .sumOf { it.transaction.netValue }

    private fun buySellNetDifference(result: NoteFeeAllocation): Double =
        sellTotal(result) - buyTotal(result)
}
