package com.eferraz.usecases.entities

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionBalance
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth

/**
 * Representa uma linha da tabela de visualização do histórico mensal de posições.
 */
@ConsistentCopyVisibility
public data class HoldingHistoryRow private constructor(
    private val currentEntry: HoldingHistoryEntry,
    private val previousEntry: HoldingHistoryEntry,
    private val appreciation: Appreciation,
    private val periodTransactionBalance: TransactionBalance,
) {

    public val entry: HoldingHistoryEntry
        get() = currentEntry

    public val assetClass: AssetClass
        get() = currentEntry.holding.asset.assetClass

    public val liquidity: Liquidity
        get() = when (val asset = currentEntry.holding.asset) {
            is FixedIncomeAsset -> asset.liquidity
            is VariableIncomeAsset -> asset.liquidity
            is InvestmentFundAsset -> asset.liquidity
        }

    public val brokerageName: String
        get() = currentEntry.holding.brokerage.name

    public val displayName: String
        get() = formatDisplayName(currentEntry.holding.asset)

    public val observation: String
        get() = currentEntry.holding.asset.observations.orEmpty()

    public val previousValue: Double
        get() = previousEntry.endOfMonthValue * previousEntry.endOfMonthQuantity

    public val currentValue: Double
        get() = currentEntry.endOfMonthValue * currentEntry.endOfMonthQuantity

    public val periodTransactionValue: Double
        get() = periodTransactionBalance.balance

    public val transactions: List<AssetTransaction>
        get() = currentEntry.holding.transactions

    public val appreciationPercentage: Double
        get() = appreciation.percentage

    public val appreciationValue: Double
        get() = appreciation.value

    public val b3IdentifierStatus: B3IdentifierStatus
        get() = when (val asset = currentEntry.holding.asset) {
            is FixedIncomeAsset -> asset.b3Identifier?.trim()?.takeIf { it.isNotBlank() }
                ?.let(B3IdentifierStatus::Informed)
                ?: B3IdentifierStatus.NotInformed

            is VariableIncomeAsset -> B3IdentifierStatus.Informed(asset.ticker)
            is InvestmentFundAsset -> B3IdentifierStatus.NotInformed
        }

    public val isLiquidated: Boolean
        get() = currentValue == 0.0

    public fun isCurrentValueEnabled(): Boolean =
        assetClass != AssetClass.VARIABLE_INCOME

    public companion object {

        public fun build(
            period: YearMonth,
            previousEntries: List<HoldingHistoryEntry>,
            currentEntries: List<HoldingHistoryEntry>,
        ): List<HoldingHistoryRow> {

            val previousByHoldingId = previousEntries.associateBy { it.holding.id }
            val previousDate = period.minusMonth()

            return currentEntries.map { current ->
                val previous = previousByHoldingId[current.holding.id]
                    ?: HoldingHistoryEntry(
                        holding = current.holding,
                        referenceDate = previousDate,
                    )

                val periodTransactions = current.holding.transactions.filter {
                    it.date.year == period.year && it.date.month == period.month
                }

                val transactionBalance = TransactionBalance.calculate(periodTransactions)

                val appreciation = Appreciation.calculate(
                    previousValue = previous.marketValue(),
                    currentValue = current.marketValue(),
                    contributions = transactionBalance.contributions,
                    withdrawals = transactionBalance.withdrawals,
                )

                HoldingHistoryRow(
                    currentEntry = current,
                    previousEntry = previous,
                    appreciation = appreciation,
                    periodTransactionBalance = transactionBalance,
                )
            }
        }

        private fun HoldingHistoryEntry.marketValue(): Double =
            endOfMonthValue * endOfMonthQuantity

        private fun formatDisplayName(asset: Asset): String =
            when (asset) {
                is FixedIncomeAsset -> asset.formattedDisplayName()
                is VariableIncomeAsset -> asset.formattedDisplayName()
                is InvestmentFundAsset -> asset.formattedDisplayName()
            }

        private fun FixedIncomeAsset.formattedDisplayName(): String =
            when (indexer) {
                YieldIndexer.POST_FIXED -> "${type.name} de $contractedYield% do CDI (venc: $expirationDate)"
                YieldIndexer.PRE_FIXED -> "${type.name} de $contractedYield% a.a. (venc: $expirationDate)"
                YieldIndexer.INFLATION_LINKED -> "${type.name} + $contractedYield% (venc: $expirationDate)"
            }

        private fun VariableIncomeAsset.formattedDisplayName(): String {
            val typeLabel = when (type) {
                VariableIncomeAssetType.NATIONAL_STOCK -> "Ação Nacional"
                VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ação Internacional"
                VariableIncomeAssetType.REAL_ESTATE_FUND -> "Fundo Imobiliário"
                VariableIncomeAssetType.ETF -> "ETF"
            }
            return "$typeLabel - $ticker"
        }

        private fun InvestmentFundAsset.formattedDisplayName(): String {
            val typeLabel = when (type) {
                InvestmentFundAssetType.PENSION -> "Previdência"
                InvestmentFundAssetType.STOCK_FUND -> "Fundo de Ação"
                InvestmentFundAssetType.MULTIMARKET_FUND -> "Fundo Multimercado"
            }
            return "$typeLabel - $name"
        }
    }
}
