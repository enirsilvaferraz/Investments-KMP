package com.eferraz.entities.rules

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.TransactionType
import kotlinx.datetime.YearMonth

/**
 * Representa o resultado de lucro ou prejuízo de uma posição de investimento em um mês específico.
 * Implements: [docs/RN - Calculo do Lucro ou Prejuizo de uma Posição.md]
 *
 * @property referenceDate O mês de referência do cálculo.
 * @property profitOrLossValue O valor financeiro do lucro (se positivo) ou prejuízo (se negativo).
 * @property roiPercentage A rentabilidade percentual sobre o capital investido.
 */
public class PositionProfitOrLoss private constructor(
    public val referenceDate: YearMonth,
    public val profitOrLossValue: Double,
    public val roiPercentage: Double,
) {

    public companion object {

        /**
         * Calcula o lucro ou prejuízo da posição com base no histórico e transações.
         *
         * @param holding A posição para a qual o cálculo está sendo realizado.
         * @param referenceDate Mês de referência.
         * @param currentHistory Histórico atual.
         * @param previousHistory Histórico anterior (opcional).
         * @param transactions Lista de transações do mês.
         * @throws IllegalArgumentException Se os dados informados não pertencerem ao holding especificado.
         */
        public fun calculate(
            holding: AssetHolding,
            referenceDate: YearMonth,
            currentHistory: HoldingHistoryEntry,
            previousHistory: HoldingHistoryEntry?,
            transactions: List<AssetTransaction>,
        ): PositionProfitOrLoss {

            // 4.1. Validação de Pertencimento
            validateOwnerShip(holding, currentHistory, previousHistory, transactions)

            // Regra de Exceção: Primeiro mês sem transações (Implantação de Saldo)
            if (previousHistory == null && transactions.isEmpty()) {
                return PositionProfitOrLoss(
                    referenceDate = referenceDate,
                    profitOrLossValue = 0.0,
                    roiPercentage = 0.0
                )
            }

            // 4.2. Determinação do Valor Anterior
            val previousValue = determinePreviousValue(previousHistory)

            // 4.3. Cálculo do Fluxo de Caixa (Net Flow)
            val balance = TransactionBalance.calculate(transactions = transactions)

            // 4.4. Cálculo do Lucro/Prejuízo Financeiro
            val financialAppreciation = calculateFinancialAppreciation(
                currentValue = currentHistory.endOfMonthValue,
                previousValue = previousValue,
                netFlow = balance.balance
            )

            // 4.5. Cálculo da Rentabilidade Percentual
            val percentageAppreciation = calculatePercentageAppreciation(
                financialAppreciation = financialAppreciation,
                previousValue = previousValue,
                purchases = balance.totalContributions
            )

            return PositionProfitOrLoss(
                referenceDate = referenceDate,
                profitOrLossValue = financialAppreciation,
                roiPercentage = percentageAppreciation
            )
        }

        /**
         * Valida se os dados informados pertencem ao holding especificado.
         */
        private fun validateOwnerShip(
            holding: AssetHolding,
            currentHistory: HoldingHistoryEntry,
            previousHistory: HoldingHistoryEntry?,
            transactions: List<AssetTransaction>,
        ) {
            require(currentHistory.holding.id == holding.id) {
                "Current history does not belong to holding ${holding.id}"
            }
            if (previousHistory != null) {
                require(previousHistory.holding.id == holding.id) {
                    "Previous history does not belong to holding ${holding.id}"
                }
            }
            transactions.forEach { transaction ->
                require(transaction.holding.id == holding.id) {
                    "Transaction ${transaction.id} does not belong to holding ${holding.id}"
                }
            }
        }

        /**
         * Determina o valor anterior do investimento com base no histórico do mês anterior.
         */
        private fun determinePreviousValue(previousHistory: HoldingHistoryEntry?) =
            previousHistory?.endOfMonthValue ?: 0.0

        /**
         * Calcula o valor financeiro do lucro (se positivo) ou prejuízo (se negativo).
         */
        private fun calculateFinancialAppreciation(currentValue: Double, previousValue: Double, netFlow: Double) =
            currentValue - previousValue - netFlow

        /**
         * Calcula a rentabilidade percentual sobre o capital investido.
         */
        private fun calculatePercentageAppreciation(financialAppreciation: Double, previousValue: Double, purchases: Double): Double {
            val balance = previousValue + purchases
            return if (balance > 0) (financialAppreciation / balance) * 100 else 0.0
        }
    }
}
