package com.eferraz.entities

import kotlinx.datetime.LocalDate

/**
 * Representa a posse de um ativo por um proprietário em uma corretora.
 * A modelagem utiliza um sistema de "unidades" para ser universalmente compatível.
 *
 * @property id O identificador único desta posição.
 * @property asset A referência para o ativo intrínseco (o "quê").
 * @property owner O proprietário desta posição (o "quem").
 * @property brokerage A corretora onde esta posição está custodiada (o "onde").
 * @property firstPurchaseDate A data da primeira compra que originou esta posição.
 * @property quantity O número de unidades detidas (ações, cotas, ou 1 para um título de Renda Fixa).
 * @property averageCost O custo médio pago por cada unidade.
 * @property investedValue O valor total investido na posição (calculado como quantity * averageCost).
 * @property currentValue O valor de mercado atual da posição.
 */
public data class AssetHolding(
    public val id: Long,
    public val asset: Asset,
    public val owner: Owner,
    public val brokerage: Brokerage,
    public val firstPurchaseDate: LocalDate,
    public val quantity: Double,
    public val averageCost: Double,
    public val investedValue: Double,
    public val currentValue: Double,
) {
    /**
     * Retorna uma nova instância de `AssetHolding` refletindo um novo aporte (compra).
     * A lógica de recálculo do custo médio está encapsulada aqui, garantindo consistência.
     *
     * @param purchaseQuantity A quantidade de novas unidades compradas.
     * @param costPerUnit O custo por unidade na nova compra.
     * @return Uma nova instância de `AssetHolding` com os valores atualizados.
     */
    public fun recordPurchase(purchaseQuantity: Double, costPerUnit: Double): AssetHolding {
        val newQuantity = this.quantity + purchaseQuantity
        val purchaseValue = costPerUnit * purchaseQuantity
        val newInvestedValue = this.investedValue + purchaseValue
        val newAverageCost = newInvestedValue / newQuantity

        return this.copy(
            quantity = newQuantity,
            averageCost = newAverageCost,
            investedValue = newInvestedValue
        )
    }
}
