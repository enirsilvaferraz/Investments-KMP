package com.eferraz.entities

/**
 * Representa a entidade que emitiu o ativo.
 * @property id O identificador único do emissor.
 * @property name O nome do emissor.
 * @property isInLiquidation Indica se o emissor está em processo de liquidação por falência.
 */
public data class Issuer(
    val id: Long,
    val name: String,
    val isInLiquidation: Boolean = false,
)
