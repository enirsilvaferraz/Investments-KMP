package com.eferraz.entities.goals

import kotlin.math.pow

/**
 * Calcula a taxa de crescimento composta (CAGR - Compound Annual Growth Rate) entre dois valores.
 *
 * Esta classe utiliza a fórmula:
 * Taxa = (Valor Final / Valor Inicial)^(1/n) - 1
 *
 * Onde n é o número de períodos entre o valor inicial e final.
 *
 * Exemplo:
 * - Valor Inicial: 1.000,00 (Jan/2026)
 * - Valor Final: 1.400,00 (Mar/2026)
 * - Períodos: 2 meses
 * - Taxa = (1400/1000)^(1/2) - 1 = 0,1832 (18,32%)
 *
 * @property percentValue Taxa de crescimento em formato percentual (ex: 18.32 para 18,32%)
 * @property decimalValue Taxa de crescimento em formato decimal (ex: 0.1832 para 18,32%)
 */
public class GrowthRate private constructor(
    public val percentValue: Double,
) {

    public val decimalValue: Double = percentValue / 100.0

    public companion object {

        /**
         * Calcula a taxa de crescimento composta entre dois valores.
         *
         * O algoritmo utiliza a fórmula matemática de crescimento composto:
         * Taxa = (Valor Final / Valor Inicial)^(1/períodos) - 1
         *
         * Esta fórmula é amplamente utilizada em finanças para calcular retornos
         * anualizados de investimentos, crescimento populacional, inflação, etc.
         *
         * A taxa retornada representa o crescimento médio por período que,
         * quando aplicado compostos continuamente, leva do valor inicial
         * ao valor final no número de períodos especificado.
         *
         * @param initialValue Valor inicial da série. Deve ser maior que 0.
         * @param finalValue Valor final da série. Deve ser maior que 0.
         * @param periods Quantidade de períodos entre o valor inicial e final.
         *                Deve ser maior ou igual a 1.
         *                Note: se você tem 3 meses de dados (Jan, Fev, Mar),
         *                o número de períodos é 2 (Jan→Fev, Fev→Mar).
         *
         * @return GrowthRate contendo a taxa calculada em formato percentual e decimal.
         *
         * @throws IllegalArgumentException se initialValue <= 0, finalValue <= 0, ou periods < 1.
         *
         */
        public fun calculate(
            initialValue: Double,
            finalValue: Double,
            periods: Int,
        ): GrowthRate {

            require(initialValue > 0) {
                "initialValue deve ser maior que 0. Valor recebido: $initialValue"
            }

            require(finalValue > 0) {
                "finalValue deve ser maior que 0. Valor recebido: $finalValue"
            }

            require(periods >= 1) {
                "periods deve ser maior ou igual a 1. Valor recebido: $periods"
            }

            val ratio = finalValue / initialValue
            val n = periods.toDouble()
            val growthRateDecimal = ratio.pow(1.0 / n) - 1.0

            // Converte para percentual
            val growthRatePercent = growthRateDecimal * 100.0

            return GrowthRate(percentValue = growthRatePercent)
        }
    }
}
