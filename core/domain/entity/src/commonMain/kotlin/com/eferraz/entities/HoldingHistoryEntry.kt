package com.eferraz.entities

import kotlinx.datetime.YearMonth

/**
 * Representa um registro de histórico mensal para uma `AssetHolding`.
 *
 * @property id O identificador único do registro de histórico (chave primária).
 * @property holding A referência direta para a `AssetHolding` a que este registro pertence.
 * @property referenceDate O mês e ano de referência para este snapshot.
 * @property endOfMonthValue O valor de mercado total da posição no final do mês.
 * @property endOfMonthQuantity A quantidade do ativo detida no final do mês.
 * @property endOfMonthAverageCost O custo médio do ativo na posição no final do mês.
 * @property totalInvested O valor total investido na posição até o final do mês.
 */
public data class HoldingHistoryEntry(
    public val id: Long? = null,
    public val holding: AssetHolding,
    public val referenceDate: YearMonth,
    public val endOfMonthValue: Double = DEFAULT_VALUE,
    public val endOfMonthQuantity: Double = DEFAULT_QUANTITY,
    public val endOfMonthAverageCost: Double = DEFAULT_VALUE,
    public val totalInvested: Double = endOfMonthAverageCost, // TODO ENIR
) {
    private companion object {
        private const val DEFAULT_VALUE = 0.0
        private const val DEFAULT_QUANTITY = 1.0
    }
}
