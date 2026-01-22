package com.eferraz.entities.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class AppreciationTest {

    @Test
    fun `GIVEN holding appreciation without transactions WHEN calculating THEN should return correct profit and percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding appreciation with purchase transaction WHEN calculating THEN should return correct profit and percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding appreciation with sale transaction WHEN calculating THEN should return correct profit and percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 200.0
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN day trade transaction resulting in profit WHEN calculating THEN should return correct profit and percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 1000.0,
            withdrawals = 1100.0
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation without transactions WHEN calculating THEN should return correct loss and negative percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation with purchase transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN holding depreciation with sale transaction WHEN calculating THEN should return correct loss and negative percentage`() {

        // WHEN
        val result = Appreciation.calculate(
            previousValue = 1000.0,
            currentValue = 700.0,
            contributions = 0.0,
            withdrawals = 200.0
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    // --- 3. Edge Cases Tests ---


    @Test
    fun `GIVEN previous value zero and no transactions WHEN calculating THEN should throw a exception`() {

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            Appreciation.calculate(
                previousValue = 0.0,
                currentValue = 1000.0,
                contributions = 0.0,
                withdrawals = 0.0
            )
        }

        assertEquals("Se valor anterior menor ou igual a zero, deve haver balanço positivo", exception.message)
    }
}
