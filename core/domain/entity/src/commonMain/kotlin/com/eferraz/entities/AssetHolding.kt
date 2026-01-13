package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
 * @property goal A meta financeira à qual esta posição contribui (opcional).
 * 
 * Nota: Os valores de quantity, averageCost, investedValue e currentValue
 * são calculados dinamicamente a partir das transações (AssetTransaction)
 * quando necessário, não sendo armazenados nesta entidade.
 * 
 * Regra: Se a posição estiver associada a uma meta (goal), o Owner da posição
 * deve ser o mesmo Owner da meta.
 */
public data class AssetHolding(
    public val id: Long,
    public val asset: Asset,
    public val owner: Owner,
    public val brokerage: Brokerage,
    public val goal: FinancialGoal? = null
) {
    /**
     * Retorna uma nova instância de `AssetHolding` refletindo um novo aporte (compra).
     * A lógica de recálculo do custo médio está encapsulada aqui, garantindo consistência.
     *
     * @param purchaseQuantity A quantidade de novas unidades compradas.
     * @param costPerUnit O custo por unidade na nova compra.
     * @return Uma nova instância de `AssetHolding` com os valores atualizados.
     */
//    public fun recordPurchase(purchaseQuantity: Double, costPerUnit: Double): AssetHolding {
//        val newQuantity = this.quantity + purchaseQuantity
//        val purchaseValue = costPerUnit * purchaseQuantity
//        val newInvestedValue = this.investedValue + purchaseValue
//        val newAverageCost = newInvestedValue / newQuantity
//
//        return this.copy(
//            quantity = newQuantity,
//            averageCost = newAverageCost,
//            investedValue = newInvestedValue
//        )
//    }
}
