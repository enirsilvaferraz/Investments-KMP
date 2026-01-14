package com.eferraz.entities.rules

import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.FixedIncomeTransaction
import com.eferraz.entities.FundsTransaction
import com.eferraz.entities.TransactionType
import com.eferraz.entities.VariableIncomeTransaction

/**
 * Representa o balanço de aportes e retiradas de uma lista de transações.
 * Implements: [docs/rules/RN - Calcular Balanço de Transações.md]
 *
 * @property totalContributions Soma total de todos os aportes (transações PURCHASE).
 * @property totalWithdrawals Soma total de todas as retiradas (transações SALE).
 * @property balance Balanço final (totalContributions - totalWithdrawals).
 */
public class TransactionBalance private constructor(
    public val totalContributions: Double,
    public val totalWithdrawals: Double,
    public val balance: Double,
) {

    public companion object {

        /**
         * Calcula o balanço de aportes e retiradas a partir de uma lista de transações.
         *
         * @param transactions Lista de transações a serem processadas.
         * @return TransactionBalance com os totais calculados.
         */
        public fun calculate(transactions: List<AssetTransaction>): TransactionBalance {

            // 4.1. Validação da Lista
            if (transactions.isEmpty()) {
                return TransactionBalance(
                    totalContributions = 0.0,
                    totalWithdrawals = 0.0,
                    balance = 0.0
                )
            }

            // 4.2. Filtro por Tipo de Transação e Cálculo
            val totalContributions = transactions
                .filter { it.type == TransactionType.PURCHASE }
                .sumOf { calculateTransactionValue(it) }

            val totalWithdrawals = transactions
                .filter { it.type == TransactionType.SALE }
                .sumOf { calculateTransactionValue(it) }

            // 4.4. Cálculo do Balanço
            val balance = totalContributions - totalWithdrawals

            return TransactionBalance(
                totalContributions = totalContributions,
                totalWithdrawals = totalWithdrawals,
                balance = balance
            )
        }

        /**
         * Calcula o valor de uma transação baseado no tipo de ativo.
         *
         * - Renda Variável: quantity × unitPrice
         * - Renda Fixa: totalValue
         * - Fundos: totalValue
         */
        private fun calculateTransactionValue(transaction: AssetTransaction): Double {
            return when (transaction) {
                is VariableIncomeTransaction -> transaction.quantity * transaction.unitPrice
                is FixedIncomeTransaction -> transaction.totalValue
                is FundsTransaction -> transaction.totalValue
            }
        }
    }
}
