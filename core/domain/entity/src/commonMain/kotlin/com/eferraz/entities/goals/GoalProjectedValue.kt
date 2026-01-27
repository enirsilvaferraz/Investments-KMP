package com.eferraz.entities.goals

/**
 * Representa o valor projetado de uma meta financeira para um único mês.
 * Implements: [docs/rules/RN - Calcular Valor Projetado de Meta Financeira.md]
 *
 * @property projectedValue O valor projetado após aplicar rentabilidade e aporte.
 */
public class GoalProjectedValue private constructor(
    public val projectedValue: Double,
) {

    public companion object {

        /**
         * Calcula o valor projetado de uma meta financeira para um único mês,
         * aplicando a taxa de retorno sobre o valor atual e adicionando o aporte mensal.
         *
         * O aporte mensal é adicionado primeiro ao valor atual, depois a rentabilidade
         * é aplicada sobre o total. Esta abordagem reflete o cenário em que o investidor
         * aporta no início do mês, permitindo que o aporte já renda no período corrente.
         *
         * Fórmula: valorProjetado = (currentValue + monthlyContribution) × (1 + monthlyReturnRate/100)
         *
         * @param currentValue O valor atual (do mês anterior ou valor inicial). Deve ser ≥ 0.
         * @param appreciationRate A taxa de retorno mensal esperada (em percentual, ex: 0.80 para 0,80%). Deve ser ≥ 0.
         * @param contribution O valor de aporte mensal. Deve ser ≥ 0.
         * @return GoalProjectedValue com o valor projetado calculado.
         * @throws IllegalArgumentException se qualquer valor de entrada for negativo.
         */
        public fun calculate(
            currentValue: Double,
            appreciationRate: Double,
            contribution: Double,
        ): GoalProjectedValue {

            // 4.1. Validação das Entradas
            require(currentValue >= 0) {
                "currentValue deve ser não-negativo. Valor recebido: $currentValue"
            }
            require(appreciationRate >= 0) {
                "monthlyReturnRate deve ser não-negativo. Valor recebido: $appreciationRate"
            }
            require(contribution >= 0) {
                "monthlyContribution deve ser não-negativo. Valor recebido: $contribution"
            }

            // 4.2. Ordem de Aplicação
            // 1. Adiciona o aporte ao valor atual
            val valueWithContribution = currentValue + contribution

            // 2. Aplica a rentabilidade sobre o total
            val projectedValue = valueWithContribution * (1 + appreciationRate / 100)

            return GoalProjectedValue(
                projectedValue = projectedValue
            )
        }
    }
}