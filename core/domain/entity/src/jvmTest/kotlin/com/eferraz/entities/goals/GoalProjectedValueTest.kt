package com.eferraz.entities.goals

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFailsWith

class GoalProjectedValueTest {

    // --- 1. Calculation Tests (Based on Document Examples) ---

    @Test
    fun `GIVEN current value, monthly contribution and return rate WHEN calculating THEN should return correct projected value`() {
        // GIVEN (Example 6.1 from docs)
        // Valor atual: R$ 10.000,00
        // Taxa mensal: 0,80%
        // Aporte mensal: R$ 1.500,00
        // Expected: (10.000 + 1.500) × 1,008 = R$ 11.592,00
        val currentValue = 10000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(11592.0, result.projectedValue, 0.01)
    }

    @Test
    fun `GIVEN only return rate without contribution WHEN calculating THEN should return correct projected value`() {
        // GIVEN (Example 6.2 from docs)
        // Valor atual: R$ 50.000,00
        // Taxa mensal: 1,00%
        // Aporte mensal: R$ 0,00
        // Expected: (50.000 + 0) × 1,01 = R$ 50.500,00
        val currentValue = 50000.0
        val monthlyReturnRate = 1.0
        val monthlyContribution = 0.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(50500.0, result.projectedValue, 0.01)
    }

    @Test
    fun `GIVEN only contribution without return rate WHEN calculating THEN should return correct projected value`() {
        // GIVEN (Example 6.3 from docs)
        // Valor atual: R$ 5.000,00
        // Taxa mensal: 0,00%
        // Aporte mensal: R$ 2.000,00
        // Expected: (5.000 + 2.000) × 1,00 = R$ 7.000,00
        val currentValue = 5000.0
        val monthlyReturnRate = 0.0
        val monthlyContribution = 2000.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(7000.0, result.projectedValue, 0.01)
    }

    @Test
    fun `GIVEN zero current value with contribution and return rate WHEN calculating THEN should return correct projected value`() {
        // GIVEN (Example 6.4 from docs)
        // Valor atual: R$ 0,00
        // Taxa mensal: 0,80%
        // Aporte mensal: R$ 1.500,00
        // Expected: (0 + 1.500) × 1,008 = R$ 1.512,00
        val currentValue = 0.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(1512.0, result.projectedValue, 0.01)
    }

    @Test
    fun `GIVEN sequential months WHEN calculating THEN should return correct compound values`() {
        // GIVEN (Example 6.5 from docs - sequential calculation)
        // Month 1: (0 + 1.500) × 1,008 = R$ 1.512,00
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN - Month 1
        val month1 = GoalProjectedValue.calculate(
            currentValue = 0.0,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN - Month 1
        Assert.assertEquals(1512.0, month1.projectedValue, 0.01)

        // WHEN - Month 2: (1.512 + 1.500) × 1,008 = R$ 3.036,10
        val month2 = GoalProjectedValue.calculate(
            currentValue = month1.projectedValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN - Month 2
        Assert.assertEquals(3036.10, month2.projectedValue, 0.01)

        // WHEN - Month 3: (3.036,10 + 1.500) × 1,008 = R$ 4.572,39
        val month3 = GoalProjectedValue.calculate(
            currentValue = month2.projectedValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN - Month 3
        Assert.assertEquals(4572.39, month3.projectedValue, 0.01)

        // WHEN - Month 4: (4.572,39 + 1.500) × 1,008 = R$ 6.120,97
        val month4 = GoalProjectedValue.calculate(
            currentValue = month3.projectedValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN - Month 4
        Assert.assertEquals(6120.97, month4.projectedValue, 0.01)
    }

    // --- 2. Edge Cases Tests ---

    @Test
    fun `GIVEN all zero values WHEN calculating THEN should return zero`() {
        // GIVEN
        val currentValue = 0.0
        val monthlyReturnRate = 0.0
        val monthlyContribution = 0.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(0.0, result.projectedValue, 0.001)
    }

    @Test
    fun `GIVEN high return rate WHEN calculating THEN should return correct projected value`() {
        // GIVEN
        // Valor atual: R$ 100.000,00
        // Taxa mensal: 10,00% (cenário hipotético)
        // Aporte mensal: R$ 5.000,00
        // Expected: (100.000 + 5.000) × 1,10 = R$ 115.500,00
        val currentValue = 100000.0
        val monthlyReturnRate = 10.0
        val monthlyContribution = 5000.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(115500.0, result.projectedValue, 0.01)
    }

    @Test
    fun `GIVEN very small return rate WHEN calculating THEN should return correct projected value`() {
        // GIVEN
        // Valor atual: R$ 1.000,00
        // Taxa mensal: 0,01%
        // Aporte mensal: R$ 100,00
        // Expected: (1.000 + 100) × 1,0001 = R$ 1.100,11
        val currentValue = 1000.0
        val monthlyReturnRate = 0.01
        val monthlyContribution = 100.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN
        Assert.assertEquals(1100.11, result.projectedValue, 0.01)
    }

    // --- 3. Validation Tests ---

    @Test
    fun `GIVEN negative current value WHEN calculating THEN should throw IllegalArgumentException`() {
        // GIVEN
        val currentValue = -1000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = 1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjectedValue.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals(
            "currentValue deve ser não-negativo. Valor recebido: -1000.0",
            exception.message
        )
    }

    @Test
    fun `GIVEN negative monthly return rate WHEN calculating THEN should throw IllegalArgumentException`() {
        // GIVEN
        val currentValue = 10000.0
        val monthlyReturnRate = -0.80
        val monthlyContribution = 1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjectedValue.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals(
            "monthlyReturnRate deve ser não-negativo. Valor recebido: -0.8",
            exception.message
        )
    }

    @Test
    fun `GIVEN negative monthly contribution WHEN calculating THEN should throw IllegalArgumentException`() {
        // GIVEN
        val currentValue = 10000.0
        val monthlyReturnRate = 0.80
        val monthlyContribution = -1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjectedValue.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        Assert.assertEquals(
            "monthlyContribution deve ser não-negativo. Valor recebido: -1500.0",
            exception.message
        )
    }

    @Test
    fun `GIVEN all negative values WHEN calculating THEN should throw IllegalArgumentException for currentValue`() {
        // GIVEN - deve falhar na primeira validação (currentValue)
        val currentValue = -10000.0
        val monthlyReturnRate = -0.80
        val monthlyContribution = -1500.0

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            GoalProjectedValue.calculate(
                currentValue = currentValue,
                appreciationRate = monthlyReturnRate,
                contribution = monthlyContribution
            )
        }

        // Deve falhar na primeira validação (currentValue)
        Assert.assertEquals(
            "currentValue deve ser não-negativo. Valor recebido: -10000.0",
            exception.message
        )
    }

    // --- 4. Formula Verification Tests ---

    @Test
    fun `GIVEN contribution is applied before return rate WHEN calculating THEN order matters`() {
        // GIVEN
        // Valor atual: R$ 1.000,00
        // Taxa mensal: 10,00%
        // Aporte mensal: R$ 1.000,00
        //
        // Ordem correta (aporte primeiro):
        // (1.000 + 1.000) × 1,10 = 2.000 × 1,10 = R$ 2.200,00
        //
        // Ordem incorreta (rentabilidade primeiro):
        // (1.000 × 1,10) + 1.000 = 1.100 + 1.000 = R$ 2.100,00
        val currentValue = 1000.0
        val monthlyReturnRate = 10.0
        val monthlyContribution = 1000.0

        // WHEN
        val result = GoalProjectedValue.calculate(
            currentValue = currentValue,
            appreciationRate = monthlyReturnRate,
            contribution = monthlyContribution
        )

        // THEN - deve usar a ordem correta (aporte primeiro)
        Assert.assertEquals(2200.0, result.projectedValue, 0.01)
    }
}