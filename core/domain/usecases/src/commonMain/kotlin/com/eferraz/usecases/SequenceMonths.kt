package com.eferraz.usecases

import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth

/**
 * Representa uma sequência imutável de meses consecutivos entre dois períodos.
 *
 * Esta classe garante que:
 * - A sequência seja válida (início não posterior ao fim)
 * - Não haja risco de loops infinitos com limite configurável
 * - A lista seja imutável e segura para uso concorrente
 *
 * @property entries Lista imutável de meses no intervalo [start, end]
 */
@ConsistentCopyVisibility
internal data class SequenceMonths private constructor(
    val entries: List<YearMonth>,
) : List<YearMonth> by entries {

    companion object {

        /** Limite padrão de meses que podem ser gerados para prevenir loops infinitos */
        const val DEFAULT_MAX_MONTHS = 120 // 10 anos

        /**
         * Constrói uma sequência de meses entre [start] e [end] (ambos inclusivos).
         *
         * @param start Mês inicial (inclusive)
         * @param end Mês final (inclusive)
         * @param maxMonths Número máximo de meses permitidos (padrão: 120)
         * @return Uma instância de [SequenceMonths] contendo todos os meses no intervalo
         * @throws IllegalArgumentException se start > end ou se o intervalo exceder maxMonths
         */
        fun build(
            start: YearMonth,
            end: YearMonth,
            maxMonths: Int = DEFAULT_MAX_MONTHS
        ): SequenceMonths {

            require(start <= end) {
                "O mês inicial ($start) não pode ser posterior ao mês final ($end)"
            }

            require(maxMonths > 0) {
                "maxMonths deve ser maior que zero. Valor recebido: $maxMonths"
            }

            val months = mutableListOf<YearMonth>()
            var current = start

            while (current <= end) {

                months.add(current)
                current = current.plusMonth()

                if (months.size > maxMonths) {
                    throw IllegalArgumentException(
                        "O intervalo entre $start e $end excede o limite de $maxMonths meses. " +
                        "Total de meses calculados: ${months.size}"
                    )
                }
            }

            return SequenceMonths(months)
        }
    }
}