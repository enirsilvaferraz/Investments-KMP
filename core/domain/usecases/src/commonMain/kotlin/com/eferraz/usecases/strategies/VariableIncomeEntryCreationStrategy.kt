package com.eferraz.usecases.strategies

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.GetQuotesUseCase
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Estratégia para criação de entradas de histórico para ativos de renda variável.
 * Usa cotações atuais para obter o valor de mercado.
 */
@Factory
public class VariableIncomeEntryCreationStrategy(
    private val getQuotesUseCase: GetQuotesUseCase,
) : HoldingHistoryEntryCreationStrategy {

    override fun canHandle(asset: com.eferraz.entities.Asset): Boolean {
        return asset is VariableIncomeAsset
    }

    override suspend fun createEntry(
        holding: AssetHolding,
        referenceDate: YearMonth,
        previousEntry: HoldingHistoryEntry?,
        currentEntry: HoldingHistoryEntry?,
    ): HoldingHistoryEntry {
        val variableIncomeAsset = holding.asset as? VariableIncomeAsset
            ?: throw IllegalArgumentException("Asset deve ser VariableIncomeAsset")

        val quoteHistory = getQuotesUseCase(variableIncomeAsset.ticker)
        val endOfMonthValue = quoteHistory.close ?: quoteHistory.adjustedClose ?: DEFAULT_VALUE
        val endOfMonthQuantity = currentEntry?.endOfMonthQuantity
            ?: previousEntry?.endOfMonthQuantity
            ?: DEFAULT_QUANTITY

        return HoldingHistoryEntry(
            holding = holding,
            referenceDate = referenceDate,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity,
            endOfMonthAverageCost = DEFAULT_VALUE,
            totalInvested = DEFAULT_VALUE,
        )
    }

    private companion object {
        private const val DEFAULT_VALUE = 0.0
        private const val DEFAULT_QUANTITY = 1.0
    }
}
