package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.transactions.TransactionBalance
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.GetTransactionsByHoldingUseCase
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
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetHistoryTableDataUseCase.Param, List<HistoryTableData>>(context) {

    public data class Param(
        val referenceDate: YearMonth,
        val category: InvestmentCategory,
    )

    override suspend fun execute(param: Param): List<HistoryTableData> {

        val results = mergeHistoryUseCase(MergeHistoryUseCase.Param(param.referenceDate, param.category))
            .onFailure { println("Error: ${it.message}") }
            .getOrNull() ?: emptyList()

        return results.map { result ->

            val asset = result.holding.asset
            val previousValue = result.previousEntry.endOfMonthValue * result.previousEntry.endOfMonthQuantity
            val currentValue = result.currentEntry.endOfMonthValue * result.currentEntry.endOfMonthQuantity
            val appreciation = result.profitOrLoss.percentage

            // Obter transações do holding e calcular balanço
            val transactions = getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(result.holding))
                .getOrNull()
                ?.filter { it.date.month == param.referenceDate.month && it.date.year == param.referenceDate.year }
                ?: emptyList()

            val transactionBalance = TransactionBalance.Companion.calculate(transactions)
            val totalContributions = transactionBalance.contributions
            val totalWithdrawals = transactionBalance.withdrawals

            when (asset) {

                is FixedIncomeAsset -> FixedIncomeHistoryTableData(
                    currentEntry = result.currentEntry,
                    brokerageName = result.holding.brokerage.name,
                    subType = asset.subType,
                    type = asset.type,
                    expirationDate = asset.expirationDate,
                    contractedYield = asset.contractedYield,
                    cdiRelativeYield = asset.cdiRelativeYield,
                    issuerName = asset.issuer.name,
                    liquidity = asset.liquidity,
                    observations = asset.observations ?: "",
                    previousValue = previousValue,
                    currentValue = currentValue,
                    appreciation = appreciation,
                    editable = true,
                    totalContributions = totalContributions,
                    totalWithdrawals = totalWithdrawals,
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
                    displayName = asset.formated()
                )
            }
        }
    }

    internal fun FixedIncomeAsset.formated(): String = when (type) {
        FixedIncomeAssetType.POST_FIXED -> "${subType.name} de $contractedYield% do CDI (venc: $expirationDate)"
        FixedIncomeAssetType.PRE_FIXED -> "${subType.name} de $contractedYield% a.a. (venc: $expirationDate)"
        FixedIncomeAssetType.INFLATION_LINKED -> "${subType.name} + $contractedYield% (venc: $expirationDate)"
    }

    internal fun VariableIncomeAsset.formated(): String = "${type.name} - $ticker"

    internal fun InvestmentFundAsset.formated(): String = type.name
}