package com.eferraz.usecases.services

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.entities.B3Record
import com.eferraz.usecases.repositories.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SyncB3HistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<List<B3Record>, Unit>(context) {

    override suspend fun execute(param: List<B3Record>) {
        val currentMonth = dateProvider.getCurrentYearMonth()
        val historyEntries = holdingHistoryRepository.getByReferenceDate(currentMonth).filter { it.endOfMonthValue != 0.0 }

        syncFromImport(param, historyEntries)
        syncFromHistory(param, historyEntries)
    }

    private suspend fun syncFromImport(records: List<B3Record>, historyEntries: List<HoldingHistoryEntry>) {

        for (record in records) {

            val matches = historyEntries.filter { entry ->
                (entry.holding.asset as? VariableIncomeAsset)?.ticker == record.identifier
                    || (entry.holding.asset as? FixedIncomeAsset)?.b3Identifier == record.identifier
            }

            if (matches.isNotEmpty()) {

                for (match in matches) {
                    holdingHistoryRepository.upsert(match.copy(endOfMonthValue = record.value))
                }

                println("ATUALIZADO: ${record.identifier} → ${record.value} (${matches.size} registro(s))",)
            } else {
                println("NÃO REGISTRADO: ${record.identifier}")
            }
        }
    }

    private fun syncFromHistory(records: List<B3Record>, historyEntries: List<HoldingHistoryEntry>) {

        val importedIds = records.map { it.identifier }.toSet()

        for (entry in historyEntries) {

            when (val asset = entry.holding.asset) {

                is VariableIncomeAsset ->
                    if (asset.ticker !in importedIds) println("IDENTIFICADOR INEXISTENTE: ${asset.ticker}")

                is FixedIncomeAsset ->
                    when (val b3Id = asset.b3Identifier) {
                        null -> println("IGNORADO: ${asset.issuer.name} — renda fixa sem identificador B3")
                        else -> if (b3Id !in importedIds) println("IDENTIFICADOR INEXISTENTE: $b3Id")
                    }

                is InvestmentFundAsset ->
                    println("IGNORADO: ${asset.name} — fundo sem identificador B3")
            }
        }
    }
}
