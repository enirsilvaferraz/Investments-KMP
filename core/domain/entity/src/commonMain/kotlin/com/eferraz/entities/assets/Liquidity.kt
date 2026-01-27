package com.eferraz.entities.assets

/**
 * Enum que representa as diferentes regras de liquidez de um ativo.
 *
 * Para liquidez do tipo D_PLUS_DAYS, o número de dias deve ser armazenado
 * separadamente nas entidades que utilizam este tipo de liquidez.
 */
public enum class Liquidity {

    /**
     * Representa a liquidez diária, onde o resgate pode ser solicitado a qualquer momento.
     */
    DAILY,

    /**
     * Representa a liquidez apenas no vencimento do título.
     */
    AT_MATURITY,

    /**
     * Representa a liquidez onde o resgate ocorre um número específico de dias após a solicitação.
     * O número de dias deve ser armazenado separadamente na entidade que utiliza este tipo.
     */
    D_PLUS_DAYS
}