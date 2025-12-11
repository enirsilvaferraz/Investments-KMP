package com.eferraz.usecases.strategies

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Estratégia para criação de entradas de histórico para ativos de renda fixa.
 * Baseia-se no histórico anterior para manter os valores.
 */
@Factory
public class FixedIncomeEntryCreationStrategy : HoldingHistoryEntryCreationStrategy {

    override fun canHandle(asset: com.eferraz.entities.Asset): Boolean {
        return asset !is com.eferraz.entities.VariableIncomeAsset
    }

    override suspend fun createEntry(
        holding: AssetHolding,
        referenceDate: YearMonth,
        previousEntry: HoldingHistoryEntry?,
        currentEntry: HoldingHistoryEntry?,
    ): HoldingHistoryEntry {
        return if (previousEntry != null) {
            HoldingHistoryEntry(
                holding = holding,
                referenceDate = referenceDate,
                endOfMonthValue = previousEntry.endOfMonthValue,
                endOfMonthQuantity = previousEntry.endOfMonthQuantity,
                endOfMonthAverageCost = previousEntry.endOfMonthAverageCost,
                totalInvested = previousEntry.totalInvested,
            )
        } else {
            HoldingHistoryEntry(
                holding = holding,
                referenceDate = referenceDate,
                endOfMonthValue = DEFAULT_VALUE,
                endOfMonthQuantity = DEFAULT_QUANTITY,
                endOfMonthAverageCost = DEFAULT_VALUE,
                totalInvested = DEFAULT_VALUE,
            )
        }
    }

    private companion object {
        private const val DEFAULT_VALUE = 0.0
        private const val DEFAULT_QUANTITY = 1.0
    }
}
