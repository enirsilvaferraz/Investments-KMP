package com.eferraz.entities.transactions

/**
 * Tipo de transação (genérico para todos os tipos de ativos).
 */
public enum class TransactionType {

    /**
     * Compra/Aporte - Aumenta a posição
     */
    PURCHASE,

    /**
     * Venda/Resgate - Diminui a posição
     */
    SALE
}