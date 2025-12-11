package com.eferraz.usecases

import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.providers.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.strategies.HoldingHistoryEntryCreationStrategyFactory
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Use case dedicado para criar entradas faltantes de histórico.
 * Segue o princípio SRP (Single Responsibility Principle).
 *
 * Este use case pode ser usado separadamente quando se deseja apenas criar entradas faltantes
 * sem buscar todo o histórico, ou pode ser orquestrado junto com GetHoldingHistoryUseCase.
 *
 * @property holdingHistoryRepository Repositório para acesso ao histórico de posições
 * @property strategyFactory Factory para obter estratégias de criação de entradas
 * @property dateProvider Provider para obter a data atual
 */
@Factory
public class CreateMissingHoldingHistoryEntriesUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val strategyFactory: HoldingHistoryEntryCreationStrategyFactory,
    private val dateProvider: DateProvider,
) {

    /**
     * Cria entradas faltantes para as posições fornecidas.
     *
     * @param results Lista de resultados de histórico que podem ter entradas faltantes
     * @param referenceDate A data de referência para as entradas a serem criadas
     * @param previousEntries Entradas do mês anterior para usar como base
     * @return Lista atualizada de resultados com as entradas criadas
     * @throws Exception Se houver erro ao criar entradas
     */
    public suspend fun createMissingEntries(
        results: List<HoldingHistoryResult>,
        referenceDate: YearMonth,
        previousEntries: List<HoldingHistoryEntry>,
    ): List<HoldingHistoryResult> {
        val previousEntriesMap = previousEntries.associateBy { it.holding.id }
        val isCurrentMonth = referenceDate == dateProvider.getCurrentYearMonth()

        return results.map { result ->
            if (result.currentEntry != null) {
                return@map result
            }

            val strategy = strategyFactory.getStrategy(result.holding.asset)
                ?: return@map result

            // Para renda variável, só cria se for o mês atual
            if (result.holding.asset is VariableIncomeAsset && !isCurrentMonth) {
                return@map result
            }

            try {
                val previousEntry = previousEntriesMap[result.holding.id]
                val newEntry = strategy.createEntry(
                    holding = result.holding,
                    referenceDate = referenceDate,
                    previousEntry = previousEntry,
                    currentEntry = result.currentEntry,
                )

                val insertedId = holdingHistoryRepository.insert(newEntry)
                val entryWithId = newEntry.copy(id = insertedId)

                result.copy(currentEntry = entryWithId)
            } catch (e: Exception) {
                throw Exception(
                    "Erro ao criar entrada de histórico para holding ${result.holding.id}: ${e.message}",
                    e
                )
            }
        }
    }
}
