package com.eferraz.entities.goals

import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth

/**
 * Representa o mapa de projeções mensais de uma meta financeira.
 * Implements: [docs/rules/RN - Gerar Mapa de Projeção de Meta Financeira.md]
 *
 * @property projections Mapa com o valor projetado por mês (YearMonth).
 */
public class GoalProjections private constructor(
    public val projections: Map<YearMonth, GoalProjectedValue>,
) {

    public companion object {
        private const val DEFAULT_MAX_MONTHS = 120

        /**
         * Gera o mapa de projeções mensais de uma meta financeira com base em um plano.
         *
         * @param plan Parâmetros do plano de investimento.
         * @param maxMonths Número máximo de meses a projetar (padrão 120).
         * @return FinancialGoalProjections com o mapa ordenado cronologicamente.
         * @throws IllegalArgumentException se a meta for inalcançável com os parâmetros informados.
         */
        public fun calculate(
            plan: GoalInvestmentPlan,
            maxMonths: Int = DEFAULT_MAX_MONTHS,
        ): GoalProjections {

            require(maxMonths >= 1) { "maxMonths deve ser maior ou igual a 1. Valor recebido: $maxMonths" }

            val projections = linkedMapOf<YearMonth, GoalProjectedValue>()

            var currentValue = plan.initialValue
            var currentMonth = plan.goal.startDate.yearMonth

            for (monthOffset in 0 until maxMonths) {

                val goalProjectedValue = GoalProjectedValue.calculate(
                    currentValue = currentValue,
                    appreciationRate = plan.appreciationRate,
                    contribution = plan.contribution
                )

                projections[currentMonth] = goalProjectedValue

                currentValue = goalProjectedValue.projectedValue
                currentMonth = currentMonth.plusMonth()

                if (currentValue >= plan.goal.targetValue) break
            }

            return GoalProjections(projections = projections)
        }
    }
}