package com.eferraz.entities.transactions

/**
 * Representa o balanço de aportes e retiradas de uma lista de transações.
 * Implements: [docs/rules/RN - Calcular Balanço de Transações.md]
 *
 * @property contributions Soma total de todos os aportes (transações PURCHASE).
 * @property withdrawals Soma total de todas as retiradas (transações SALE).
 * @property balance Balanço final (totalContributions - totalWithdrawals).
 */
@ConsistentCopyVisibility
public data class TransactionBalance private constructor(
    public val contributions: Double,
    public val withdrawals: Double,
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
                    contributions = 0.0,
                    withdrawals = 0.0,
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
                contributions = totalContributions,
                withdrawals = totalWithdrawals,
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