package com.eferraz.usecases

import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.providers.DateProvider
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.strategies.HoldingHistoryEntryCreationStrategyFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

/**
 * Use case responsável por obter o histórico de posições de ativos para uma data de referência.
 *
 * Este use case:
 * - Busca todas as posições de ativos (holdings)
 * - Obtém as entradas de histórico para o mês de referência e o mês anterior
 * - Cria entradas faltantes usando estratégias baseadas no tipo de ativo
 *
 * @property assetHoldingRepository Repositório para acesso a posições de ativos
 * @property holdingHistoryRepository Repositório para acesso ao histórico de posições
 * @property strategyFactory Factory para obter estratégias de criação de entradas
 * @property dateProvider Provider para obter a data atual
 */
@Factory
public class GetHoldingHistoryUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val strategyFactory: HoldingHistoryEntryCreationStrategyFactory,
    private val dateProvider: DateProvider,
) {

    /**
     * Obtém o histórico de posições para uma data de referência.
     *
     * @param referenceDate A data de referência (mês/ano) para a qual o histórico será obtido
     * @return Lista de resultados contendo cada posição com suas entradas de histórico atual e anterior
     * @throws Exception Se houver erro ao buscar dados dos repositórios
     */
    public suspend operator fun invoke(referenceDate: YearMonth): List<HoldingHistoryResult> = withContext(Dispatchers.Default) {
        try {
            val previousMonth = referenceDate.minusMonth()

            val holdings = assetHoldingRepository.getAll()
            val currentEntries = holdingHistoryRepository.getByReferenceDate(referenceDate)
            val previousEntries = holdingHistoryRepository.getByReferenceDate(previousMonth)

            val historyMap = buildHoldingHistoryMap(holdings, currentEntries, previousEntries)

            createMissingEntries(historyMap, referenceDate, previousEntries)

            historyMap.values.toList()
        } catch (e: Exception) {
            throw Exception("Erro ao obter histórico de posições: ${e.message}", e)
        }
    }

    /**
     * Constrói o mapa inicial de histórico com todas as posições.
     */
    private fun buildHoldingHistoryMap(
        holdings: List<com.eferraz.entities.AssetHolding>,
        currentEntries: List<HoldingHistoryEntry>,
        previousEntries: List<HoldingHistoryEntry>,
    ): MutableMap<Long, HoldingHistoryResult> {
        val historyMap = holdings.associateBy(
            keySelector = { it.id },
            valueTransform = { HoldingHistoryResult(holding = it) }
        ).toMutableMap()

        populateEntries(historyMap, currentEntries) { result, entry ->
            result.copy(currentEntry = entry)
        }

        populateEntries(historyMap, previousEntries) { result, entry ->
            result.copy(previousEntry = entry)
        }

        return historyMap
    }

    /**
     * Popula entradas no mapa de histórico de forma genérica.
     *
     * @param historyMap O mapa de histórico a ser populado
     * @param entries As entradas a serem adicionadas
     * @param updateResult Função para atualizar o resultado com a entrada
     */
    private fun populateEntries(
        historyMap: MutableMap<Long, HoldingHistoryResult>,
        entries: List<HoldingHistoryEntry>,
        updateResult: (HoldingHistoryResult, HoldingHistoryEntry) -> HoldingHistoryResult,
    ) {
        entries.forEach { entry ->
            val existingResult = historyMap[entry.holding.id]
            if (existingResult != null) {
                historyMap[entry.holding.id] = updateResult(existingResult, entry)
            }
        }
    }

    /**
     * Cria entradas faltantes usando estratégias baseadas no tipo de ativo.
     */
    private suspend fun createMissingEntries(
        historyMap: MutableMap<Long, HoldingHistoryResult>,
        referenceDate: YearMonth,
        previousEntries: List<HoldingHistoryEntry>,
    ) {
        val previousEntriesMap = previousEntries.associateBy { it.holding.id }
        val isCurrentMonth = referenceDate == dateProvider.getCurrentYearMonth()

        historyMap.values
            .filter { result -> result.currentEntry == null }
            .forEach { result ->
                val strategy = strategyFactory.getStrategy(result.holding.asset)
                    ?: return@forEach

                // Para renda variável, só cria se for o mês atual
                if (result.holding.asset is VariableIncomeAsset && !isCurrentMonth) {
                    return@forEach
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

                    historyMap[result.holding.id] = result.copy(currentEntry = entryWithId)
                } catch (e: Exception) {
                    // Log do erro mas continua processando outras entradas
                    // Em produção, considerar usar um logger apropriado
                    throw Exception(
                        "Erro ao criar entrada de histórico para holding ${result.holding.id}: ${e.message}",
                        e
                    )
                }
            }
    }
}
