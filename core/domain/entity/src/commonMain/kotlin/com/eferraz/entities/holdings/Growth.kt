package com.eferraz.entities.holdings

/**
 * Representa o resultado de crescimento total (absoluto e percentual) de uma posição de investimento em um mês específico.
 * Implements: [docs/rules/RN - Calcular Crescimento de uma Posição.md]
 *
 * O crescimento representa a variação total do patrimônio entre dois momentos.
 *
 * @property value O valor financeiro do crescimento (positivo) ou decrescimento (negativo).
 * @property percentage O percentual de crescimento sobre o valor inicial.
 */
public class Growth private constructor(
    public val value: Double,
    public val percentage: Double,
) {

    public companion object {

        /**
         * Calcula o crescimento de forma simples.
         * Função auxiliar que realiza apenas o cálculo matemático.
         *
         * @param previousValue Valor anterior (deve ser > 0 para evitar divisão por zero).
         * @param currentValue Valor atual.
         */
        private fun calculate(
            previousValue: Double,
            currentValue: Double,
        ): Growth {

            val growthValue = currentValue - previousValue

            val percentage =
                if (previousValue > 0) growthValue / previousValue * 100
                else 0.0

            return Growth(value = growthValue, percentage = percentage)
        }

        /**
         * Calcula o crescimento com base nos valores anterior e atual, considerando contribuições e retiradas.
         *
         * @param previousValue Valor anterior da posição.
         * @param currentValue Valor atual da posição.
         * @param contributions Total de contribuições no período.
         * @param withdrawals Total de retiradas no período.
         * @throws IllegalArgumentException se previousValue <= 0 e não houver contribuições.
         */
        public fun calculate(
            previousValue: Double,
            currentValue: Double,
            contributions: Double,
            withdrawals: Double,
        ): Growth {

            val balance = contributions - withdrawals

            if (previousValue == 0.0 && balance == 0.0) {
                return Growth(value = 0.0, percentage = 0.0)
            }

            // Define qual é o valor anterior para o cálculo:
            // - Se tem valor anterior, usa ele
            // - Se não tem (primeiro mês), usa contributions como base
            val adjustedPrevious = if (previousValue > 0) previousValue else contributions

            return calculate(
                previousValue = adjustedPrevious,
                currentValue = currentValue
            )
        }
    }
}