package com.eferraz.entities.holdings

/**
 * Representa o resultado de apreciação ou depreciação de uma posição de investimento em um mês específico.
 * Implements: [docs/rules/RN - Calcular Apreciação de uma Posição.md]
 *
 * @property value O valor financeiro da apreciação (se positivo) ou depreciação (se negativo).
 * @property percentage A rentabilidade percentual sobre o capital investido.
 */
@ConsistentCopyVisibility
public data class Appreciation private constructor(
    public val value: Double,
    public val percentage: Double,
) {

    public companion object {

        /**
         * Calcula o lucro ou prejuízo com base no histórico e transações.
         *
         * @param previousValue Valor anterior.
         * @param currentValue Valor atual.
         * @param contributions Total de contribuições.
         * @param withdrawals Total de retiradas.
         */
        public fun calculate(
            previousValue: Double,
            currentValue: Double,
            contributions: Double,
            withdrawals: Double,
        ): Appreciation {

            val balance = contributions - withdrawals

            if (previousValue == 0.0 && balance == 0.0) {
                return Appreciation(value = 0.0, percentage = 0.0)
            }

            val appreciation = currentValue - previousValue - balance

            // Se tiver valor anterior usa ele como base, se nao tiver, a base é o total de aportes
            val base = if (previousValue > 0) previousValue else contributions

            val percentage = if (base > 0) appreciation / base * 100 else 0.0

            return Appreciation(value = appreciation, percentage = percentage)
        }
    }
}