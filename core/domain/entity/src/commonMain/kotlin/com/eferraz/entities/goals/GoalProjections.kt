package com.eferraz.entities.goals

import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth

/**
 * Representa o mapa de projeções mensais de uma meta financeira.
 * Implements: [docs/rules/RN - Gerar Mapa de Projeção de Meta Financeira.md]
 *
 * @property map Mapa ordenado cronologicamente com o valor projetado por mês (YearMonth).
 */
public class GoalProjections private constructor(
    public val map: Map<YearMonth, ProjectedGoal>,
) {

    /**
     * Retorna a projeção de um mês específico, se existir.
     *
     * @param month Mês a ser consultado.
     * @return ProjectedGoal do mês ou null se não existir projeção para o mês.
     */
    public operator fun get(month: YearMonth): ProjectedGoal? = map[month]

    public companion object {
        private const val DEFAULT_MAX_MONTHS = 120

        /**
         * Gera o mapa de projeções mensais de uma meta financeira com base em um plano.
         *
         * O algoritmo projeta iterativamente mês a mês, aplicando rentabilidade e aportes,
         * até que a meta seja alcançada ou o limite de meses seja atingido.
         *
         * @param plan Parâmetros do plano de investimento.
         * @param maxMonths Número máximo de meses a projetar (padrão 120 meses = 10 anos).
         * @return GoalProjections com o mapa ordenado cronologicamente.
         * @throws IllegalArgumentException se maxMonths for menor que 1 ou se os parâmetros forem inválidos.
         */
        public fun calculate(
            plan: GoalInvestmentPlan,
            maxMonths: Int = DEFAULT_MAX_MONTHS,
        ): GoalProjections {

            // Extrai valores do plano para evitar múltiplos acessos de propriedades
            val targetValue = plan.goal.targetValue
            val appreciationRate = plan.appreciationRate
            val contribution = plan.contribution
            val startMonth = plan.goal.startDate.yearMonth
            val initialValue = plan.initialValue

            return calculate(
                maxMonths = maxMonths,
                initialValue = initialValue,
                startMonth = startMonth,
                appreciationRate = appreciationRate,
                contribution = contribution,
                targetValue = targetValue
            )
        }

        /**
         * Gera o mapa de projeções mensais de uma meta financeira com parâmetros diretos.
         *
         * Esta função calcula iterativamente as projeções mensais aplicando rentabilidade
         * e aportes mês a mês, até que o valor alvo seja alcançado ou o limite de meses
         * seja atingido.
         *
         * O algoritmo funciona da seguinte forma:
         * 1. Inicia com o valor inicial fornecido
         * 2. Para cada mês:
         *    - Adiciona o aporte mensal ao valor acumulado
         *    - Aplica a taxa de rentabilidade sobre o total
         *    - Armazena a projeção no mapa com a chave YearMonth
         * 3. Interrompe quando o valor alvo é alcançado ou maxMonths é atingido
         *
         *
         * @param maxMonths Número máximo de meses a projetar. Deve ser >= 1.
         * @param initialValue Valor inicial da projeção (valor acumulado até o momento). Deve ser >= 0.
         * @param startMonth Mês inicial da projeção (ano e mês).
         * @param appreciationRate Taxa de rentabilidade mensal esperada em percentual (ex: 0.80 para 0,80%). Deve ser >= 0.
         * @param contribution Valor de aporte mensal planejado. Deve ser >= 0.
         * @param targetValue Valor alvo da meta a ser alcançado. Usado como critério de parada.
         *
         * @return GoalProjections contendo o mapa ordenado cronologicamente de projeções.
         *         O mapa pode conter de 1 até maxMonths entradas, dependendo de quando
         *         o targetValue é alcançado.
         *
         * @throws IllegalArgumentException se maxMonths for menor que 1.
         *
         * @see ProjectedGoal.calculate para entender o cálculo de cada mês
         * @see GoalInvestmentPlan para a versão que recebe um plano completo
         */
        public fun calculate(
            initialValue: Double,
            startMonth: YearMonth,
            appreciationRate: Double,
            contribution: Double,
            targetValue: Double,
            maxMonths: Int = DEFAULT_MAX_MONTHS,
        ): GoalProjections {

            require(maxMonths >= 1) {
                "maxMonths deve ser maior ou igual a 1. Valor recebido: $maxMonths"
            }

            require(contribution > 0 || appreciationRate > 0) {
                "Contribuição ou taxa de rentabilidade devem ser maiores que zero. " +
                "Valores recebidos: contribution=$contribution, appreciationRate=$appreciationRate"
            }

            // Pré-aloca capacidade estimada para melhor performance
            // Usa capacidade inicial baseada em uma estimativa conservadora
            val initialCapacity = minOf(maxMonths, 64)
            val projections = LinkedHashMap<YearMonth, ProjectedGoal>(initialCapacity)

            // Inicia a projeção com o valor inicial da meta
            var accumulatedValue = initialValue
            var projectionMonth = startMonth

            // Projeta iterativamente até alcançar a meta ou limite de meses
            for (monthIndex in 0 until maxMonths) {

                // Calcula a projeção para o mês atual
                val monthProjection = ProjectedGoal.calculate(
                    currentValue = accumulatedValue,
                    appreciationRate = appreciationRate,
                    contribution = contribution
                )

                // Armazena a projeção no mapa
                projections[projectionMonth] = monthProjection

                // Atualiza valores para próxima iteração
                accumulatedValue = monthProjection.value
                projectionMonth = projectionMonth.plusMonth()

                // Interrompe se a meta foi alcançada
                if (accumulatedValue >= targetValue) break
            }

            return GoalProjections(map = projections)
        }
    }
}