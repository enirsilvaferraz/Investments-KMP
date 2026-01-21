package com.eferraz.entities.rules

import com.eferraz.entities.FinancialGoal
import com.eferraz.entities.GoalInvestmentPlan
import com.eferraz.entities.Owner
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class FinancialGoalProjectionsTest {

    @Test
    fun `GIVEN valid plan WHEN calculating THEN should generate projections until target is reached`() {
        // GIVEN (Example 5.1 from docs)
        val goal = FinancialGoal(
            id = 1,
            owner = Owner(id = 1, name = "Ana"),
            name = "Casa própria",
            targetValue = 100000.0,
            startDate = LocalDate(2026, Month.JANUARY, 15)
        )
        val plan = GoalInvestmentPlan(
            goal = goal,
            monthlyContribution = 1500.0,
            monthlyReturnRate = 0.80,
            initialValue = 0.0
        )

        // WHEN
        val result = FinancialGoalProjections.calculate(plan)

        // THEN
        assertTrue(result.projections.size >= 4)
        val month1 = requireNotNull(result.projections[YearMonth(2026, Month.JANUARY)])
        val month2 = requireNotNull(result.projections[YearMonth(2026, Month.FEBRUARY)])
        val month3 = requireNotNull(result.projections[YearMonth(2026, Month.MARCH)])
        val month4 = requireNotNull(result.projections[YearMonth(2026, Month.APRIL)])

        assertEquals(1512.0, month1.projectedValue, 0.01)
        assertEquals(3036.10, month2.projectedValue, 0.01)
        assertEquals(4572.39, month3.projectedValue, 0.01)
        assertEquals(6120.97, month4.projectedValue, 0.01)

        val entries = result.projections.entries.toList()
        val lastEntry = entries.last()
        assertEquals(YearMonth(2026, Month.JANUARY), entries.first().key)
        assertTrue(lastEntry.value.projectedValue >= goal.targetValue)

        if (entries.size > 1) {
            val previousEntry = entries[entries.size - 2]
            assertTrue(previousEntry.value.projectedValue < goal.targetValue)
        }

        val lastDate = goal.startDate.plus(DatePeriod(months = entries.size - 1))
        assertEquals(YearMonth(lastDate.year, lastDate.month), lastEntry.key)
    }

    @Test
    fun `GIVEN maxMonths limit WHEN calculating THEN should stop at maxMonths`() {
        // GIVEN (Example 5.3 from docs - limited to 12 months)
        val goal = FinancialGoal(
            id = 2,
            owner = Owner(id = 2, name = "Bruno"),
            name = "Reserva",
            targetValue = 500000.0,
            startDate = LocalDate(2026, Month.JANUARY, 1)
        )
        val plan = GoalInvestmentPlan(
            goal = goal,
            monthlyContribution = 500.0,
            monthlyReturnRate = 0.50,
            initialValue = 0.0
        )

        // WHEN
        val result = FinancialGoalProjections.calculate(plan, maxMonths = 12)

        // THEN
        assertEquals(12, result.projections.size)
        val lastEntry = result.projections.entries.last()
        assertEquals(YearMonth(2026, Month.DECEMBER), lastEntry.key)
        assertTrue(lastEntry.value.projectedValue < goal.targetValue)
    }

    @Test
    fun `GIVEN no contribution and no return with lower initial value WHEN calculating THEN should throw`() {
        // GIVEN
        val goal = FinancialGoal(
            id = 3,
            owner = Owner(id = 3, name = "Carla"),
            name = "Viagem",
            targetValue = 1000.0,
            startDate = LocalDate(2026, Month.JANUARY, 1)
        )


        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {

            val plan = GoalInvestmentPlan(
                goal = goal,
                monthlyContribution = 0.0,
                monthlyReturnRate = 0.0,
                initialValue = 0.0
            )

            FinancialGoalProjections.calculate(plan)
        }

        assertEquals("Meta inalcançável: sem aporte e sem rentabilidade", exception.message)
    }

    @Test
    fun `GIVEN invalid maxMonths WHEN calculating THEN should throw`() {
        // GIVEN
        val goal = FinancialGoal(
            id = 4,
            owner = Owner(id = 4, name = "Diego"),
            name = "Estudos",
            targetValue = 10000.0,
            startDate = LocalDate(2026, Month.JANUARY, 1)
        )
        val plan = GoalInvestmentPlan(
            goal = goal,
            monthlyContribution = 100.0,
            monthlyReturnRate = 1.0,
            initialValue = 0.0
        )

        // WHEN / THEN
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialGoalProjections.calculate(plan, maxMonths = 0)
        }
        assertEquals(
            "maxMonths deve ser maior ou igual a 1. Valor recebido: 0",
            exception.message
        )
    }
}
