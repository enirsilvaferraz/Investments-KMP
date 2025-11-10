package com.eferraz.entities

/**
 * Contrato para os diferentes tipos de rendimentos (proventos) que um ativo pode gerar.
 */
public sealed interface Earning {
    public val value: Double
}

/**
 * Representa o recebimento de dividendos.
 */
public data class Dividend(override val value: Double) : Earning

/**
 * Representa o recebimento de juros.
 */
public data class Interest(override val value: Double) : Earning
