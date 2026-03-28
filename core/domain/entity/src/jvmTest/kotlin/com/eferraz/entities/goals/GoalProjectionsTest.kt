package com.eferraz.entities.goals

import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GoalProjectionsTest {

    /**
     * Monthly projections with zero appreciation and fixed contribution until target.
     */
    @Test
    fun `GIVEN zero appreciation and fixed contribution WHEN calculate THEN monthly values reach target`() {

        // WHEN
        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.0,
            contribution = 10.0,
            targetValue = 50.0,
        )

        // THEN
        assertEquals(10.0, result.map[YearMonth(2026, Month.JANUARY)]!!.value, 0.01)
        assertEquals(20.0, result.map[YearMonth(2026, Month.FEBRUARY)]!!.value, 0.01)
        assertEquals(30.0, result.map[YearMonth(2026, Month.MARCH)]!!.value, 0.01)
        assertEquals(40.0, result.map[YearMonth(2026, Month.APRIL)]!!.value, 0.01)
        assertEquals(50.0, result.map[YearMonth(2026, Month.MAY)]!!.value, 0.01)
        assertEquals(null, result.map[YearMonth(2026, Month.JUNE)]?.value)
    }

    /**
     * Projection series starts at the given start month.
     */
    @Test
    fun `GIVEN start month WHEN calculate THEN first month matches start`() {

        // WHEN
        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.80,
            contribution = 1500.0,
            targetValue = 100_000.0,
        )

        // THEN
        val firstMonth = result.map.keys.first()
        assertEquals(YearMonth(2026, Month.JANUARY), firstMonth)
    }

    /**
     * Last projected value reaches at least the target; prior month stays below if more than one month.
     */
    @Test
    fun `GIVEN high target WHEN calculate THEN last value meets target and prior is below`() {

        // GIVEN
        val targetValue = 100_000.0

        // WHEN
        val result = GoalProjections.calculate(
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.80,
            contribution = 1500.0,
            targetValue = targetValue,
        )

        // THEN
        val lastValue = result.map.values.last().value
        assertTrue(lastValue >= targetValue, "Last value must be >= target")

        val values = result.map.values.toList()
        if (values.size > 1) {
            val penultimateValue = values[values.size - 2].value
            assertTrue(penultimateValue < targetValue, "Penultimate value must be < target")
        }
    }

    /**
     * maxMonths caps the projection length.
     */
    @Test
    fun `GIVEN maxMonths limit WHEN calculate THEN map size respects limit`() {

        // WHEN
        val result = GoalProjections.calculate(
            maxMonths = 12,
            initialValue = 0.0,
            startMonth = YearMonth(2026, Month.JANUARY),
            appreciationRate = 0.50,
            contribution = 500.0,
            targetValue = 500_000.0,
        )

        // THEN
        assertEquals(12, result.map.size)
        assertEquals(YearMonth(2026, Month.DECEMBER), result.map.keys.last())
    }

    /**
     * Zero contribution and zero appreciation cannot reach a positive target.
     */
    @Test
    fun `GIVEN zero contribution and zero appreciation WHEN calculate THEN throws`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjections.calculate(
                maxMonths = 120,
                initialValue = 0.0,
                startMonth = YearMonth(2026, Month.JANUARY),
                appreciationRate = 0.0,
                contribution = 0.0,
                targetValue = 1000.0,
            )
        }

        // THEN
        assertEquals(
            "Contribuição ou taxa de rentabilidade devem ser maiores que zero. " +
                "Valores recebidos: contribution=0.0, appreciationRate=0.0",
            exception.message,
        )
    }

    /**
     * maxMonths zero is invalid.
     */
    @Test
    fun `GIVEN maxMonths zero WHEN calculate THEN throws with maxMonths message`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjections.calculate(
                maxMonths = 0,
                initialValue = 0.0,
                startMonth = YearMonth(2026, Month.JANUARY),
                appreciationRate = 1.0,
                contribution = 100.0,
                targetValue = 10_000.0,
            )
        }

        // THEN
        assertEquals("maxMonths deve ser maior ou igual a 1. Valor recebido: 0", exception.message)
    }
}
