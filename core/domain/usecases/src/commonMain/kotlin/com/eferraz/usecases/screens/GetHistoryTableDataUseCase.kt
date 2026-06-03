package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.MergeHistoryUseCase
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.entities.InvestmentFundHistoryTableData
import com.eferraz.usecases.entities.VariableIncomeHistoryTableData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Use case responsável por obter os dados da tabela de histórico de posições para exibição na tela.
 *
 * Retorna uma lista de [com.eferraz.usecases.entities.HistoryTableData], onde cada item representa uma linha da tabela
 * com dados primitivos, enums e LocalDate para formatação na view.
 */
@Factory
public class GetHistoryTableDataUseCase(
    private val mergeHistoryUseCase: MergeHistoryUseCase,
    private val filterHoldingHistoryUseCase: FilterHoldingHistoryUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetHistoryTableDataUseCase.Param, List<HistoryTableData>>(context) {

    public data class Param(
        val referenceDate: YearMonth,
        val walletFilter: WalletHistoryFilterCriteria,
    )

    override suspend fun execute(param: Param): List<HistoryTableData> {

        val results = mergeHistoryUseCase(MergeHistoryUseCase.Param(param.referenceDate, assetClass = null))
            .onFailure { println("Error: ${it.message}") }
            .getOrNull() ?: emptyList()

        val currentEntries = results.map { it.currentEntry }

        val filteredEntries = filterHoldingHistoryUseCase(FilterHoldingHistoryUseCase.Param(currentEntries, param.walletFilter)).getOrElse { emptyList() }

        val passingHoldingIds = filteredEntries.map { it.holding.id }.toSet()
        val filtered = results.filter { it.holding.id in passingHoldingIds }
        val sortedBy = filtered
            .mapNotNull { result ->

                val asset = result.holding.asset
                val previousValue = result.previousEntry.endOfMonthValue * result.previousEntry.endOfMonthQuantity
                val currentValue = result.currentEntry.endOfMonthValue * result.currentEntry.endOfMonthQuantity
                val appreciation = result.profitOrLoss.percentage

                val transactions = result.holding.transactions.filter {
                    it.date.year == param.referenceDate.year && it.date.month == param.referenceDate.month
                }
                val transactionBalance = TransactionBalance.calculate(transactions)
                val totalContributions = transactionBalance.contributions
                val totalWithdrawals = transactionBalance.withdrawals
                val totalBalance = transactionBalance.balance

                when (asset) {

                    is FixedIncomeAsset -> FixedIncomeHistoryTableData(
                        currentEntry = result.currentEntry,
                        brokerageName = result.holding.brokerage.name,
                        indexer = asset.indexer,
                        type = asset.type,
                        expirationDate = asset.expirationDate,
                        contractedYield = asset.contractedYield,
                        cdiRelativeYield = asset.cdiRelativeYield,
                        b3Identifier = asset.b3Identifier,
                        issuerName = asset.issuer.name,
                        liquidity = asset.liquidity,
                        observations = asset.observations ?: "",
                        previousValue = previousValue,
                        currentValue = currentValue,
                        appreciation = appreciation,
                        editable = true,
                        totalContributions = totalContributions,
                        totalWithdrawals = totalWithdrawals,
                        totalBalance = totalBalance,
                        displayName = asset.formated()
                    )

                    is VariableIncomeAsset -> VariableIncomeHistoryTableData(
                        currentEntry = result.currentEntry,
                        brokerageName = result.holding.brokerage.name,
                        type = asset.type,
                        ticker = asset.ticker,
                        cnpj = asset.cnpj?.get() ?: "",
                        name = asset.name,
                        issuerName = asset.issuer.name,
                        observations = asset.observations ?: "",
                        previousValue = previousValue,
                        currentValue = currentValue,
                        appreciation = appreciation,
                        editable = false,
                        totalContributions = totalContributions,
                        totalWithdrawals = totalWithdrawals,
                        totalBalance = totalBalance,
                        displayName = asset.formated()
                    )

                    is InvestmentFundAsset -> InvestmentFundHistoryTableData(
                        currentEntry = result.currentEntry,
                        brokerageName = result.holding.brokerage.name,
                        type = asset.type,
                        name = asset.name,
                        liquidity = asset.liquidity,
                        liquidityDays = asset.liquidityDays,
                        expirationDate = asset.expirationDate,
                        issuerName = asset.issuer.name,
                        observations = asset.observations ?: "",
                        previousValue = previousValue,
                        currentValue = currentValue,
                        appreciation = appreciation,
                        editable = true,
                        totalContributions = totalContributions,
                        totalWithdrawals = totalWithdrawals,
                        totalBalance = totalBalance,
                        displayName = asset.formated()
                    )
                }
            }
            .toList()
            .sortedBy { it.assetClass }
        return sortedBy
    }

    internal fun FixedIncomeAsset.formated(): String =
        when (indexer) {
            YieldIndexer.POST_FIXED -> "${type.name} de $contractedYield% do CDI (venc: $expirationDate)"
            YieldIndexer.PRE_FIXED -> "${type.name} de $contractedYield% a.a. (venc: $expirationDate)"
            YieldIndexer.INFLATION_LINKED -> "${type.name} + $contractedYield% (venc: $expirationDate)"
        }

    internal fun VariableIncomeAsset.formated(): String {
        val typeFormated = when (type) {
            VariableIncomeAssetType.NATIONAL_STOCK -> "Ação Nacional"
            VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ação Internacional"
            VariableIncomeAssetType.REAL_ESTATE_FUND -> "Fundo Imobiliário"
            VariableIncomeAssetType.ETF -> "ETF"
        }
        return "$typeFormated - $ticker"
    }

    internal fun InvestmentFundAsset.formated(): String =
        when (type) {
            InvestmentFundAssetType.PENSION -> "Previdência"
            InvestmentFundAssetType.STOCK_FUND -> "Fundo de Ação"
            InvestmentFundAssetType.MULTIMARKET_FUND -> "Fundo Multimercado"
        } + " - $name"
}

