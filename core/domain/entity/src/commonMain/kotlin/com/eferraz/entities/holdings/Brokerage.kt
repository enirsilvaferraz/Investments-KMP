package com.eferraz.entities.holdings

/**
 * Representa a instituição financeira onde o ativo está custodiado.
 * @property id O identificador único da corretora.
 * @property name O nome da corretora.
 */
public data class Brokerage(val id: Long, val name: String)
