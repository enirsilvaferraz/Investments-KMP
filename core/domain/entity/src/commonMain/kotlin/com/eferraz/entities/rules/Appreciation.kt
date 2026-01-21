package com.eferraz.entities.rules

import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.HoldingHistoryEntry

/**
 * Representa o resultado de apreciação ou depreciação de uma posição de investimento em um mês específico.
 * Implements: [docs/rules/RN - Calcular Apreciação de uma Posição.md]
 *
 * @property value O valor financeiro da apreciação (se positivo) ou depreciação (se negativo).
 * @property percentage A rentabilidade percentual sobre o capital investido.
 */
public class Appreciation private constructor(
    public val value: Double,
    public val percentage: Double,
) {

    public companion object {

        /**
         * Calcula o lucro ou prejuízo da posição com base no histórico e transações.
         *
         * @param currentHistory Histórico atual.
         * @param previousHistory Histórico anterior (opcional).
         * @param transactions Lista de transações do mês.
         */
        public fun calculate(
            currentHistory: HoldingHistoryEntry,
            previousHistory: HoldingHistoryEntry?,
            transactions: List<AssetTransaction>,
        ): Appreciation {

            // Regra de Exceção: Primeiro mês sem transações (Implantação de Saldo)
            if (previousHistory == null && transactions.isEmpty())
                return Appreciation(value = 0.0, percentage = 0.0)

            return calculate(
                currentHistory = currentHistory,
                previousHistory = previousHistory,
                transactionsBalance = TransactionBalance.calculate(transactions = transactions)
            )
        }

        /**
         * Calcula o lucro ou prejuízo da posição com base no histórico e transações.
         *
         * @param currentHistory Histórico atual.
         * @param previousHistory Histórico anterior (opcional).
         * @param transactionsBalance balanço do mes.
         */
        public fun calculate(
            currentHistory: HoldingHistoryEntry,
            previousHistory: HoldingHistoryEntry?,
            transactionsBalance: TransactionBalance,
        ): Appreciation {

            val currentValue = currentHistory.endOfMonthValue
            val previousValue = previousHistory?.endOfMonthValue ?: 0.0

            // 4.3. Cálculo do balanço
            val (contributions, _, balance) = transactionsBalance

            // 4.4. Cálculo do Lucro/Prejuízo Financeiro
            val appreciation = currentValue - previousValue - balance

            // 4.5. Cálculo da Rentabilidade Percentual
            val base = previousValue + balance

            val effectiveBase =
                if (base > 0) base
                else if (contributions > 0) contributions
                else 0.0

            val percentage = if (effectiveBase > 0) appreciation / effectiveBase * 100 else 0.0

            return Appreciation(value = appreciation, percentage = percentage)
        }
    }
}
