package com.eferraz.entities.holdings

import kotlin.test.Test
import kotlin.test.assertEquals

class GrowthTest {

    /**
     * Pure appreciation without contributions or withdrawals.
     */
    @Test
    fun `GIVEN pure appreciation without flows WHEN calculate THEN returns growth on base`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(100.0, result.value, 0.001)
        assertEquals(10.0, result.percentage, 0.001)
    }

    /**
     * Appreciation plus contribution increases both value and percentage on expanded base.
     */
    @Test
    fun `GIVEN appreciation with contribution WHEN calculate THEN returns growth on total base`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(600.0, result.value, 0.001)
        assertEquals(60.0, result.percentage, 0.001)
    }

    /**
     * Withdrawal reduces growth magnitude.
     */
    @Test
    fun `GIVEN appreciation with withdrawal WHEN calculate THEN reflects net move`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 100.0,
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * First month with contribution only: growth on contributed capital.
     */
    @Test
    fun `GIVEN only contributions and appreciation WHEN calculate THEN returns growth on flows`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1500.0,
            contributions = 1000.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(500.0, result.value, 0.001)
        assertEquals(50.0, result.percentage, 0.001)
    }

    /**
     * Loss masked by contribution.
     */
    @Test
    fun `GIVEN loss with contribution WHEN calculate THEN returns net growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(400.0, result.value, 0.001)
        assertEquals(40.0, result.percentage, 0.001)
    }

    /**
     * Partial withdrawal with smaller current value.
     */
    @Test
    fun `GIVEN profit with partial withdrawal WHEN calculate THEN returns net growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 850.0,
            contributions = 0.0,
            withdrawals = 150.0,
        )

        // THEN
        assertEquals(-150.0, result.value, 0.001)
        assertEquals(-15.0, result.percentage, 0.001)
    }

    /**
     * First month: previous zero, contribution equals current — zero growth.
     */
    @Test
    fun `GIVEN first month contribution matches market value WHEN calculate THEN zero growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1000.0,
            contributions = 1000.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }

    /**
     * First month with loss versus contribution.
     */
    @Test
    fun `GIVEN first month with loss versus contribution WHEN calculate THEN negative growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 900.0,
            contributions = 1000.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(-100.0, result.value, 0.001)
        assertEquals(-10.0, result.percentage, 0.001)
    }

    /**
     * Straight decline without flows.
     */
    @Test
    fun `GIVEN decline without flows WHEN calculate THEN negative growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 2000.0,
            currentValue = 1500.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(-500.0, result.value, 0.001)
        assertEquals(-25.0, result.percentage, 0.001)
    }

    /**
     * No previous value and no contributions.
     */
    @Test
    fun `GIVEN zero previous and no contributions WHEN calculate THEN zero growth`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 0.0,
            withdrawals = 0.0,
        )

        // THEN
        assertEquals(0.0, result.value, 0.001)
        assertEquals(0.0, result.percentage, 0.001)
    }
}
