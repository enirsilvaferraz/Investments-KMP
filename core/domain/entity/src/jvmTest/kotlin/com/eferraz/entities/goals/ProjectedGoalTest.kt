package com.eferraz.entities.goals

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFailsWith

class ProjectedGoalTest {

    @Test
    fun `GIVEN current value, monthly contribution and return rate WHEN calculating THEN should return correct projected value`() {

        // GIVEN
        val currentValue = 10000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(11592.0, result.value, 0.01)
    }

    @Test
    fun `GIVEN only return rate without contribution WHEN calculating THEN should return correct projected value`() {

        // GIVEN
        val currentValue = 50000.0
        val monthlyReturnRate = 1.0
        val monthlyContribution = 0.0

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(50500.0, result.value, 0.01)
    }

    @Test
    fun `GIVEN only contribution without return rate WHEN calculating THEN should return correct projected value`() {

        // GIVEN
        val currentValue = 5000.0
        val monthlyReturnRate = 0.0
        val monthlyContribution = 2000.0

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(7000.0, result.value, 0.01)
    }

    @Test
    fun `GIVEN zero current value with contribution and return rate WHEN calculating THEN should return correct projected value`() {

        // GIVEN
        val currentValue = 0.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(1512.0, result.value, 0.01)
    }


    @Test
    fun `GIVEN all zero values WHEN calculating THEN should return zero`() {

        // GIVEN
        val currentValue = 0.0
        val monthlyReturnRate = 0.0
        val monthlyContribution = 0.0

        // WHEN
        val result = ProjectedGoal.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(0.0, result.value, 0.001)
    }

    @Test
    fun `GIVEN negative current value WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentValue = -1000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals("O valor atual deve ser maior que zero. Valor recebido: -1000.0", exception.message)
    }

    @Test
    fun `GIVEN negative monthly return rate WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentValue = 10000.0
        val monthlyReturnRate = -0.80
        val monthlyContribution = 1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals("A taxa de rentabilidade deve ser maior que zero. Valor recebido: -0.8", exception.message)
    }

    @Test
    fun `GIVEN negative monthly contribution WHEN calculating THEN should throw IllegalArgumentException`() {

        // GIVEN
        val currentValue = 10000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = -1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals("O aporte deve ser maior que zero. Valor recebido: -1500.0", exception.message)
    }

    @Test
    fun `GIVEN all negative values WHEN calculating THEN should throw IllegalArgumentException for currentValue`() {

        // GIVEN - deve falhar na primeira validação (currentValue)
        val currentValue = -10000.0
        val monthlyReturnRate = -0.80
        val monthlyContribution = -1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            ProjectedGoal.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        // Deve falhar na primeira validação (currentValue)
        Assert.assertEquals("O valor atual deve ser maior que zero. Valor recebido: -10000.0", exception.message)
    }
}