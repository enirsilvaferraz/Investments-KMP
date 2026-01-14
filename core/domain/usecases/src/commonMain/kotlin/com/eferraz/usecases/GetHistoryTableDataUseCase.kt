package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.rules.TransactionBalance
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.entities.InvestmentFundHistoryTableData
import com.eferraz.usecases.entities.VariableIncomeHistoryTableData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Use case responsável por obter os dados da tabela de histórico de posições para exibição na tela.
 * 
 * Retorna uma lista de [HistoryTableData], onde cada item representa uma linha da tabela
 * com dados primitivos, enums e LocalDate para formatação na view.
 */
@Factory
public class GetHistoryTableDataUseCase(
    private val mergeHistoryUseCase: MergeHistoryUseCase,
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetHistoryTableDataUseCase.Param, List<HistoryTableData>>(context) {

    public data class Param(
        val referenceDate: kotlinx.datetime.YearMonth,
        val category: InvestmentCategory,
    )

    override suspend fun execute(param: Param): List<HistoryTableData> {

        val results = mergeHistoryUseCase(MergeHistoryUseCase.Param(param.referenceDate, param.category)).getOrNull() ?: emptyList()

        return results.map { result ->

            val asset = result.holding.asset
            val previousValue = result.previousEntry.endOfMonthValue * result.previousEntry.endOfMonthQuantity
            val currentValue = result.currentEntry.endOfMonthValue * result.currentEntry.endOfMonthQuantity
            val appreciation = result.profitOrLoss.roiPercentage

            // Obter transações do holding e calcular balanço
            val transactions = getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(result.holding))
                .getOrNull()
                ?.filter { it.date.month == param.referenceDate.month && it.date.year == param.referenceDate.year }
                ?: emptyList()

            val transactionBalance = TransactionBalance.calculate(transactions)
            val totalContributions = transactionBalance.totalContributions
            val totalWithdrawals = transactionBalance.totalWithdrawals

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
                    totalWithdrawals = totalWithdrawals
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
                    totalWithdrawals = totalWithdrawals
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
                    totalWithdrawals = totalWithdrawals
                )
            }
        }
    }
}

