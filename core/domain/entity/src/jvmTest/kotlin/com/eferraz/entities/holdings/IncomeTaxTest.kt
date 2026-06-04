package com.eferraz.entities.holdings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

class IncomeTaxTest {

    private val purchaseBase = LocalDate(2020, 1, 1)

    private fun referenceAfterDays(days: Int): LocalDate =
        purchaseBase.plus(DatePeriod(days = days))

    // --- User Story 1: acceptance scenarios (spec 1–4) ---

    /**
     * Spec scenario 1: profit 1000, 181 days → 20% and tax 200.
     */
    @Test
    fun `GIVEN profit 1000 and 181 days invested WHEN calculate THEN returns 20 percent and tax 200`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(181)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(20.0, result.taxRate, 0.001)
        assertEquals(200.0, result.taxValue, 0.01)
    }

    /**
     * Spec scenario 2a: profit 500, 90 days → 22.5% and tax 112.50.
     */
    @Test
    fun `GIVEN profit 500 and 90 days invested WHEN calculate THEN returns 22_5 percent and tax 112_50`() {

        // GIVEN
        val profit = 500.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(90)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(112.50, result.taxValue, 0.01)
    }

    /**
     * Spec scenario 2b: profit 500, 180 days → 22.5% and tax 112.50.
     */
    @Test
    fun `GIVEN profit 500 and 180 days invested WHEN calculate THEN returns 22_5 percent and tax 112_50`() {

        // GIVEN
        val profit = 500.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(180)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(112.50, result.taxValue, 0.01)
    }

    /**
     * Spec scenario 3: profit 800, 361 days → 17.5% and tax 140.
     */
    @Test
    fun `GIVEN profit 800 and 361 days invested WHEN calculate THEN returns 17_5 percent and tax 140`() {

        // GIVEN
        val profit = 800.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(361)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(17.5, result.taxRate, 0.001)
        assertEquals(140.0, result.taxValue, 0.01)
    }

    /**
     * Spec scenario 4: profit 2000, 721 days → 15% and tax 300.
     */
    @Test
    fun `GIVEN profit 2000 and 721 days invested WHEN calculate THEN returns 15 percent and tax 300`() {

        // GIVEN
        val profit = 2000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(721)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(15.0, result.taxRate, 0.001)
        assertEquals(300.0, result.taxValue, 0.01)
    }

    // --- User Story 1: zero/negative profit and minimal profit ---

    /**
     * Spec scenario 5 / SC-002: zero profit → taxValue 0, taxRate from bracket.
     */
    @Test
    fun `GIVEN zero profit and 181 days invested WHEN calculate THEN taxValue is zero and taxRate is 20 percent`() {

        // GIVEN
        val profit = 0.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(181)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(20.0, result.taxRate, 0.001)
        assertEquals(0.0, result.taxValue, 0.01)
    }

    /**
     * Spec scenario 5 / SC-002: negative profit → taxValue 0, taxRate from bracket.
     */
    @Test
    fun `GIVEN negative profit and 90 days invested WHEN calculate THEN taxValue is zero and taxRate is 22_5 percent`() {

        // GIVEN
        val profit = -100.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(90)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(0.0, result.taxValue, 0.01)
    }

    /**
     * Edge case: minimal positive profit yields proportional raw taxValue.
     */
    @Test
    fun `GIVEN profit 0_01 and 180 days invested WHEN calculate THEN taxValue is proportional`() {

        // GIVEN
        val profit = 0.01
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(180)

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(0.01 * 22.5 / 100.0, result.taxValue, 0.001)
    }

    /**
     * Same purchase and reference day (0 days) → up to 180 days bracket (22.5%).
     */
    @Test
    fun `GIVEN profit 1000 and same purchase and reference day WHEN calculate THEN returns 22_5 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = LocalDate(2024, 6, 15)
        val referenceDate = purchaseDate

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(225.0, result.taxValue, 0.01)
    }

    // --- User Story 2: exact bracket boundaries ---

    /**
     * US2: exactly 180 days → 22.5% bracket.
     */
    @Test
    fun `GIVEN exactly 180 days invested WHEN calculate THEN taxRate is 22_5 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(180)
        assertEquals(180, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(22.5, result.taxRate, 0.001)
        assertEquals(225.0, result.taxValue, 0.01)
    }

    /**
     * US2: exactly 181 days → 20% bracket.
     */
    @Test
    fun `GIVEN exactly 181 days invested WHEN calculate THEN taxRate is 20 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(181)
        assertEquals(181, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(20.0, result.taxRate, 0.001)
        assertEquals(200.0, result.taxValue, 0.01)
    }

    /**
     * US2: exactly 360 days → 20% bracket.
     */
    @Test
    fun `GIVEN exactly 360 days invested WHEN calculate THEN taxRate is 20 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(360)
        assertEquals(360, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(20.0, result.taxRate, 0.001)
        assertEquals(200.0, result.taxValue, 0.01)
    }

    /**
     * US2: exactly 361 days → 17.5% bracket.
     */
    @Test
    fun `GIVEN exactly 361 days invested WHEN calculate THEN taxRate is 17_5 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(361)
        assertEquals(361, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(17.5, result.taxRate, 0.001)
        assertEquals(175.0, result.taxValue, 0.01)
    }

    /**
     * US2: exactly 720 days → 17.5% bracket.
     */
    @Test
    fun `GIVEN exactly 720 days invested WHEN calculate THEN taxRate is 17_5 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(720)
        assertEquals(720, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(17.5, result.taxRate, 0.001)
        assertEquals(175.0, result.taxValue, 0.01)
    }

    /**
     * US2: exactly 721 days → 15% bracket.
     */
    @Test
    fun `GIVEN exactly 721 days invested WHEN calculate THEN taxRate is 15 percent`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = purchaseBase
        val referenceDate = referenceAfterDays(721)
        assertEquals(721, purchaseDate.daysUntil(referenceDate))

        // WHEN
        val result = IncomeTax.calculate(profit, purchaseDate, referenceDate)

        // THEN
        assertEquals(15.0, result.taxRate, 0.001)
        assertEquals(150.0, result.taxValue, 0.01)
    }

    /**
     * FR-008: purchase after reference date throws IllegalArgumentException.
     */
    @Test
    fun `GIVEN purchase date after reference date WHEN calculate THEN throws IllegalArgumentException`() {

        // GIVEN
        val profit = 1000.0
        val purchaseDate = LocalDate(2024, 6, 1)
        val referenceDate = LocalDate(2024, 1, 1)

        // WHEN / THEN
        assertFailsWith<IllegalArgumentException> {
            IncomeTax.calculate(profit, purchaseDate, referenceDate)
        }
    }
}
