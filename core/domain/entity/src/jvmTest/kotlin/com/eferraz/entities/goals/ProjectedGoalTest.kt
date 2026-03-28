package com.eferraz.entities.goals

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectedGoalTest {

    /**
     * Projection with current value, appreciation and monthly contribution yields expected value.
     */
    @Test
    fun `GIVEN current value appreciation and contribution WHEN calculate THEN returns expected projected value`() {

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = 10000.0,
            appreciationRate = 0.80,
            contribution = 1500.0,
        )

        // THEN
        assertEquals(11592.0, result.value, 0.01)
    }

    /**
     * Projection with appreciation only and zero contribution.
     */
    @Test
    fun `GIVEN appreciation only and zero contribution WHEN calculate THEN returns expected value`() {

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = 50000.0,
            appreciationRate = 1.0,
            contribution = 0.0,
        )

        // THEN
        assertEquals(50500.0, result.value, 0.01)
    }

    /**
     * Projection with contribution only and zero appreciation.
     */
    @Test
    fun `GIVEN contribution only and zero appreciation WHEN calculate THEN returns expected value`() {

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = 5000.0,
            appreciationRate = 0.0,
            contribution = 2000.0,
        )

        // THEN
        assertEquals(7000.0, result.value, 0.01)
    }

    /**
     * Zero current value with positive appreciation and contribution.
     */
    @Test
    fun `GIVEN zero current value with appreciation and contribution WHEN calculate THEN returns expected value`() {

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = 0.0,
            appreciationRate = 0.80,
            contribution = 1500.0,
        )

        // THEN
        assertEquals(1512.0, result.value, 0.01)
    }

    /**
     * All inputs zero yields zero projection.
     */
    @Test
    fun `GIVEN all inputs zero WHEN calculate THEN returns zero`() {

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = 0.0,
            appreciationRate = 0.0,
            contribution = 0.0,
        )

        // THEN
        assertEquals(0.0, result.value, 0.001)
    }

    /**
     * Negative current value is rejected with domain message.
     */
    @Test
    fun `GIVEN negative current value WHEN calculate THEN throws with expected message`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = -1000.0,
                appreciationRate = 0.80,
                contribution = 1500.0,
            )
        }

        // THEN
        assertEquals("O valor atual deve ser maior que zero. Valor recebido: -1000.0", exception.message)
    }

    /**
     * Negative appreciation rate is rejected.
     */
    @Test
    fun `GIVEN negative appreciation rate WHEN calculate THEN throws with expected message`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = 10000.0,
                appreciationRate = -0.80,
                contribution = 1500.0,
            )
        }

        // THEN
        assertEquals("A taxa de rentabilidade deve ser maior que zero. Valor recebido: -0.8", exception.message)
    }

    /**
     * Negative monthly contribution is rejected.
     */
    @Test
    fun `GIVEN negative contribution WHEN calculate THEN throws with expected message`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = 10000.0,
                appreciationRate = 0.80,
                contribution = -1500.0,
            )
        }

        // THEN
        assertEquals("O aporte deve ser maior que zero. Valor recebido: -1500.0", exception.message)
    }

    /**
     * When all inputs are negative, first validation (current value) fails.
     */
    @Test
    fun `GIVEN all negative inputs WHEN calculate THEN fails on current value validation first`() {

        // WHEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = -10000.0,
                appreciationRate = -0.80,
                contribution = -1500.0,
            )
        }

        // THEN
        assertEquals("O valor atual deve ser maior que zero. Valor recebido: -10000.0", exception.message)
    }
}
