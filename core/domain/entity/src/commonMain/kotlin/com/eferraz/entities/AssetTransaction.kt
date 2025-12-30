package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Contrato base para todas as transações de ativos.
 * Cada categoria de ativo possui suas próprias subclasses com regras específicas.
 */
public sealed interface AssetTransaction {

    /**
     * O identificador único da transação.
     */
    public val id: Long

    /**
     * A posição (holding) à qual esta transação pertence.
     */
    public val holding: AssetHolding

    /**
     * A data da transação.
     */
    public val date: LocalDate

    /**
     * O tipo de transação (compra ou venda).
     */
    public val type: TransactionType

    /**
     * Notas e observações adicionais sobre a transação (opcional).
     */
    public val observations: String?

    /**
     * O valor total da transação.
     */
    public val totalValue: Double
}
