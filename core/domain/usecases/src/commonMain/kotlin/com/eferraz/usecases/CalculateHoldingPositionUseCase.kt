package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.VariableIncomeTransaction
import com.eferraz.usecases.repositories.AssetTransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Use case para calcular os valores de posição (quantity, averageCost, investedValue)
 * de uma AssetHolding a partir das transações.
 */
@Factory
public class CalculateHoldingPositionUseCase(
    private val assetTransactionRepository: AssetTransactionRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CalculateHoldingPositionUseCase.Param, CalculateHoldingPositionUseCase.Result>(context) {

    public data class Param(val holding: AssetHolding)

    /**
     * Resultado do cálculo de posição.
     */
    public data class Result(
        /**
         * A quantidade do ativo detida.
         * Para Renda Variável: soma de compras menos soma de vendas.
         * Para Renda Fixa e Fundos: sempre 1.0.
         */
        val quantity: Double,

        /**
         * O custo médio do ativo.
         * Para Renda Variável: custo médio ponderado das compras.
         * Para Renda Fixa e Fundos: soma dos valores totais de todos os aportes.
         */
        val averageCost: Double,

        /**
         * O valor total investido na posição.
         * Para Renda Variável: soma dos valores totais de todas as compras.
         * Para Renda Fixa e Fundos: igual ao averageCost.
         */
        val investedValue: Double
    )

    override suspend fun execute(param: Param): Result {
        val transactions = assetTransactionRepository.getAllByHolding(param.holding)

        return when (param.holding.asset) {
            is VariableIncomeAsset -> calculateVariableIncomePosition(transactions)
            is FixedIncomeAsset, is InvestmentFundAsset -> calculateFixedIncomeOrFundsPosition(transactions)
        }
    }

    /**
     * Calcula a posição para ativos de Renda Variável.
     * - quantity: Soma de todas as compras (PURCHASE) menos soma de todas as vendas (SALE)
     * - averageCost: Custo médio ponderado calculado a partir das compras
     * - investedValue: Soma dos valores totais de todas as compras
     */
    private fun calculateVariableIncomePosition(
        transactions: List<AssetTransaction>
    ): Result {
        val variableIncomeTransactions = transactions.filterIsInstance<VariableIncomeTransaction>()

        var totalQuantity = 0.0
        var totalInvestedValue = 0.0
        var totalCostBasis = 0.0 // Soma de (quantity * unitPrice) para compras

        variableIncomeTransactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.PURCHASE -> {
                    totalQuantity += transaction.quantity
                    val purchaseValue = transaction.totalValue
                    totalInvestedValue += purchaseValue
                    totalCostBasis += purchaseValue
                }
                TransactionType.SALE -> {
                    totalQuantity -= transaction.quantity
                    // Para vendas, não alteramos o investedValue nem o averageCost
                    // O custo médio permanece baseado nas compras
                }
            }
        }

        // Custo médio ponderado = soma dos valores de compra / quantidade total de compras
        val averageCost = if (totalQuantity > 0 && totalCostBasis > 0) {
            // Recalcular o custo médio baseado apenas nas compras
            val purchaseTransactions = variableIncomeTransactions.filter { it.type == TransactionType.PURCHASE }
            val totalPurchaseQuantity = purchaseTransactions.sumOf { it.quantity }
            if (totalPurchaseQuantity > 0) {
                totalCostBasis / totalPurchaseQuantity
            } else {
                0.0
            }
        } else {
            0.0
        }

        return Result(
            quantity = totalQuantity.coerceAtLeast(0.0),
            averageCost = averageCost,
            investedValue = totalInvestedValue
        )
    }

    /**
     * Calcula a posição para ativos de Renda Fixa e Fundos.
     * - quantity: Sempre 1.0 (representa a posse do título/fundo)
     * - averageCost: Soma dos valores totais de todos os aportes (PURCHASE)
     * - investedValue: Igual ao averageCost
     */
    private fun calculateFixedIncomeOrFundsPosition(
        transactions: List<AssetTransaction>
    ): Result {
        val fixedIncomeTransactions = transactions.filterIsInstance<FixedIncomeTransaction>()
        val fundsTransactions = transactions.filterIsInstance<FundsTransaction>()
        val allTransactions = fixedIncomeTransactions + fundsTransactions

        val totalInvested = allTransactions
            .filter { it.type == TransactionType.PURCHASE }
            .sumOf { 
                when (it) {
                    is FixedIncomeTransaction -> it.totalValue
                    is FundsTransaction -> it.totalValue
                    else -> 0.0
                }
            }

        return Result(
            quantity = 1.0,
            averageCost = totalInvested,
            investedValue = totalInvested
        )
    }
}
