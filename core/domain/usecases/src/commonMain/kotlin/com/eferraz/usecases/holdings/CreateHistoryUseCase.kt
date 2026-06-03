package com.eferraz.usecases.holdings

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/**
 * Use case responsável por criar registros de histórico mensal para posições de ativos.
 *
 * Este use case implementa a lógica de criação de snapshots mensais conforme descrito em
 * [Regras de Negócio - Criar novo registro de histórico](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md).
 *
 * O processo funciona da seguinte forma:
 * 1. Carrega todas as posições da carteira
 * 2. Valida se a data de referência está dentro do período válido (após Out/2025)
 * 3. Seleciona a estratégia apropriada baseada no tipo de ativo (Renda Fixa, Fundos ou Renda Variável)
 * 4. Executa a estratégia para criar ou copiar o histórico de cada posição
 * 5. Persiste automaticamente cada registro criado (upsert)
 *
 * Se a estratégia não conseguir criar um histórico (retornar null), um registro vazio é criado
 * e também persistido, conforme regra de negócio.
 *
 * @param strategies Lista de estratégias disponíveis para criação de histórico
 * @param repository Repositório para persistência dos registros de histórico
 * @param assetHoldingRepository Repositório de posições da carteira
 * @param context Dispatcher de corrotinas para execução (padrão: Dispatchers.Default)
 *
 * @see CopyHistoryStrategy Para entender as estratégias disponíveis
 * @see com.eferraz.usecases.repositories.HoldingHistoryRepository Para operações de persistência
 */
@Factory
public class CreateHistoryUseCase(
    private val strategies: List<CopyHistoryStrategy>,
    private val repository: HoldingHistoryRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CreateHistoryUseCase.Param, List<HoldingHistoryEntry>>(context) {

    /**
     * Parâmetros para criação de histórico em lote.
     *
     * @param referenceDate Mês e ano de referência para o snapshot (formato: YYYY-MM)
     */
    public data class Param(val referenceDate: YearMonth)

    /**
     * Executa a criação dos registros de histórico para todas as posições.
     *
     * @param param Parâmetros contendo a data de referência
     * @return Registros de histórico criados (podem ser vazios se não houver dados disponíveis)
     */
    override suspend fun execute(param: Param): List<HoldingHistoryEntry> {
        val holdings = assetHoldingRepository.getAll()
        return holdings.map { holding -> createForHolding(param.referenceDate, holding) }
    }

    private suspend fun createForHolding(
        referenceDate: YearMonth,
        holding: AssetHolding,
    ): HoldingHistoryEntry {

        if (isBeforeHistoryLimit(referenceDate)) {
            return createEmptyHistoryEntry(holding, referenceDate)
        }

        val strategy = findStrategyForHolding(holding)

        val historyEntry = strategy?.create(referenceDate, holding)
            ?: createEmptyHistoryEntry(holding, referenceDate)

        repository.upsert(historyEntry)

        return historyEntry
    }

    /**
     * Verifica se a data de referência está antes do limite histórico.
     *
     * Conforme regra de negócio, datas ≤ Out/2025 retornam registro vazio.
     * Esta é uma data limite histórica que marca o início do sistema de histórico.
     *
     * @param referenceDate Data de referência a ser verificada
     * @return true se a data está antes do limite, false caso contrário
     */
    private fun isBeforeHistoryLimit(referenceDate: YearMonth) =
        referenceDate <= HISTORY_LIMIT_DATE

    /**
     * Cria um registro de histórico vazio com valores padrão.
     *
     * Valores padrão conforme regra de negócio:
     * - Valor de mercado: 0,00
     * - Quantidade: 1,00
     * - Custo médio: 0,00
     * - Valor investido: 0,00
     *
     * @param holding A posição de ativo
     * @param referenceDate A data de referência
     * @return Registro de histórico vazio com valores padrão
     */
    private fun createEmptyHistoryEntry(holding: AssetHolding, referenceDate: YearMonth): HoldingHistoryEntry =
        HoldingHistoryEntry(
            holding = holding,
            referenceDate = referenceDate
        )

    /**
     * Encontra a estratégia apropriada para o tipo de ativo da posição.
     *
     * @param holding A posição de ativo
     * @return A estratégia que pode lidar com o tipo de ativo, ou null se nenhuma for encontrada
     */
    private fun findStrategyForHolding(holding: AssetHolding): CopyHistoryStrategy? =
        strategies.firstOrNull {
            it.canHandle(holding)
        }

    private companion object {
        /**
         * Data limite histórica: Outubro de 2025.
         *
         * Datas anteriores ou iguais a esta retornam registros vazios.
         * Conforme [Regras de Negócio - Criar novo registro de histórico](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md#41-data-limite).
         */
        private val HISTORY_LIMIT_DATE = YearMonth(2025, 10)
    }
}
