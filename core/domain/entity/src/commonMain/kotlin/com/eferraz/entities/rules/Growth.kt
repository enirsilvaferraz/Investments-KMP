package com.eferraz.entities.rules

import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.HoldingHistoryEntry

/**
 * Representa o resultado de crescimento total (absoluto e percentual) de uma posição de investimento em um mês específico.
 * Implements: [docs/rules/RN - Calcular Crescimento de uma Posição.md]
 *
 * O crescimento representa a variação total do patrimônio, considerando lucro, aportes e retiradas.
 *
 * @property value O valor financeiro total que entrou no patrimônio, em moeda corrente. Representa a soma entre os aportes realizados e o lucro obtido pela valorização do ativo.
 * @property percentage O percentual de crescimento sobre o capital inicial do período.
 */
public class Growth private constructor(
    public val value: Double,
    public val percentage: Double,
) {

    public companion object {

        /**
         * Calcula o crescimento da posição com base no histórico e transações.
         *
         * @param currentHistory Histórico atual.
         * @param previousHistory Histórico anterior (opcional).
         * @param transactions Lista de transações do mês.
         */
        public fun calculate(
            currentHistory: HoldingHistoryEntry,
            previousHistory: HoldingHistoryEntry?,
            transactions: List<AssetTransaction>,
        ): Growth {

            // Regra de Exceção: Primeiro mês sem transações
            if (previousHistory == null && transactions.isEmpty())
                return Growth(value = 0.0, percentage = 0.0)

            return calculate(
                previousHistory = previousHistory,
                balance = TransactionBalance.calculate(transactions = transactions),
                appreciation = Appreciation.calculate(currentHistory = currentHistory, previousHistory = previousHistory, transactions = transactions)
            )
        }

        /**
         * Calcula o crescimento da posição com base no histórico, balanço e apreciação.
         *
         * @param previousHistory Histórico anterior (opcional).
         * @param balance Balanço do mês.
         * @param appreciation Apreciação do mês.
         */
        public fun calculate(
            previousHistory: HoldingHistoryEntry?,
            balance: TransactionBalance,
            appreciation: Appreciation,
        ): Growth {

            val previousValue = previousHistory?.endOfMonthValue ?: 0.0

            // Cálculo do crescimento financeiro
            val growthValue = appreciation.value + balance.balance

            // Cálculo do percentual de crescimento
            val effectiveBase =
                if (previousHistory == null) 0.0
                else if (previousValue > 0) previousValue
                else if (balance.contributions > 0) balance.contributions
                else 0.0

            val percentage = if (effectiveBase > 0) growthValue / effectiveBase * 100 else 0.0

            return Growth(value = growthValue, percentage = percentage)
        }
    }
}
