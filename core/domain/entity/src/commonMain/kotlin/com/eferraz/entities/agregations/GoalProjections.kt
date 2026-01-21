package com.eferraz.entities.agregations

import com.eferraz.entities.GoalInvestmentPlan
import com.eferraz.entities.rules.GoalProjectedValue
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
         * @param params Parâmetros do plano de investimento.
         * @param maxMonths Número máximo de meses a projetar (padrão 120).
         * @return FinancialGoalProjections com o mapa ordenado cronologicamente.
         * @throws IllegalArgumentException se a meta for inalcançável com os parâmetros informados.
         */
        public fun calculate(
            params: GoalInvestmentPlan,
            maxMonths: Int = DEFAULT_MAX_MONTHS,
        ): GoalProjections {

            require(maxMonths >= 1) { "maxMonths deve ser maior ou igual a 1. Valor recebido: $maxMonths" }

            val projections = linkedMapOf<YearMonth, GoalProjectedValue>()
            var currentValue = params.initialValue
            var currentMonth = params.goal.startDate.yearMonth
            val targetValue = params.goal.targetValue

            for (monthOffset in 0 until maxMonths) {

                val goalProjectedValue = GoalProjectedValue.Companion.calculate(
                    currentValue = currentValue,
                    monthlyReturnRate = params.monthlyReturnRate,
                    monthlyContribution = params.monthlyContribution
                )

                projections[currentMonth] = goalProjectedValue

                currentValue = goalProjectedValue.projectedValue
                currentMonth = currentMonth.plusMonth()

                if (currentValue >= targetValue) {
                    break
                }
            }

            return GoalProjections(projections = projections)
        }
    }
}