package com.eferraz.entities.liquidity

/**
 * Contrato para as diferentes regras de liquidez de um ativo.
 */
public sealed interface Liquidity

/**
 * Contrato para as regras de liquidez aplicáveis a ativos de Renda Fixa.
 */
public sealed interface FixedLiquidity : Liquidity

/**
 * Representa a liquidez diária, onde o resgate pode ser solicitado a qualquer momento.
 */
public data object Daily : FixedLiquidity

/**
 * Representa a liquidez apenas no vencimento do título.
 */
public data object AtMaturity : FixedLiquidity

/**
 * Representa a liquidez onde o resgate ocorre um número específico de dias após a solicitação.
 * @property days O número de dias para o resgate ser efetivado.
 */
public data class OnDaysAfterSale(val days: Int) : Liquidity
