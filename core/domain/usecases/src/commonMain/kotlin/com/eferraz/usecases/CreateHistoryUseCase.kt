package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import com.eferraz.usecases.strategies.CopyHistoryStrategy
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
 * 1. Valida se a data de referência está dentro do período válido (após Out/2025)
 * 2. Seleciona a estratégia apropriada baseada no tipo de ativo (Renda Fixa, Fundos ou Renda Variável)
 * 3. Executa a estratégia para criar ou copiar o histórico
 * 4. Persiste automaticamente o registro criado (upsert)
 *
 * Se a estratégia não conseguir criar um histórico (retornar null), um registro vazio é criado
 * e também persistido, conforme regra de negócio.
 *
 * @param strategies Lista de estratégias disponíveis para criação de histórico
 * @param repository Repositório para persistência dos registros de histórico
 * @param context Dispatcher de corrotinas para execução (padrão: Dispatchers.Default)
 *
 * @see CopyHistoryStrategy Para entender as estratégias disponíveis
 * @see HoldingHistoryRepository Para operações de persistência
 */
@Factory
public class CreateHistoryUseCase(
    private val strategies: List<CopyHistoryStrategy>,
    private val repository: HoldingHistoryRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<CreateHistoryUseCase.Param, HoldingHistoryEntry>(context) {

    /**
     * Parâmetros para criação de histórico.
     *
     * @param referenceDate Mês e ano de referência para o snapshot (formato: YYYY-MM)
     * @param holding A posição de ativo para a qual o histórico será gerado
     */
    public data class Param(val referenceDate: YearMonth, val holding: AssetHolding)

    /**
     * Executa a criação do registro de histórico.
     *
     * @param param Parâmetros contendo a data de referência e a posição de ativo
     * @return Registro de histórico criado (pode ser vazio se não houver dados disponíveis)
     * @throws IllegalArgumentException Se os parâmetros forem inválidos
     */
    override suspend fun execute(param: Param): HoldingHistoryEntry {

        validateParam(param)

        // Se a data está antes do limite histórico, retorna registro vazio
        if (isBeforeHistoryLimit(param.referenceDate)) {
            return createEmptyHistoryEntry(param.holding, param.referenceDate)
        }

        // Busca estratégia apropriada para o tipo de ativo
        val strategy = findStrategyForHolding(param.holding)
        
        // Executa estratégia para criar histórico
        val historyEntry = strategy?.create(param.referenceDate, param.holding)
            ?: createEmptyHistoryEntry(param.holding, param.referenceDate)

        // Persiste o registro (conforme regra de negócio: cada histórico criado é salvo automaticamente)
        repository.upsert(historyEntry)
        
        return historyEntry
    }

    /**
     * Valida os parâmetros de entrada.
     *
     * @throws IllegalArgumentException Se os parâmetros forem inválidos
     */
    private fun validateParam(param: Param) {
        require(param.holding.id > 0) { "Holding ID deve ser maior que zero" }
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
    private fun createEmptyHistoryEntry(
        holding: AssetHolding,
        referenceDate: YearMonth
    ): HoldingHistoryEntry {
        return HoldingHistoryEntry(
            holding = holding,
            referenceDate = referenceDate
        )
    }

    /**
     * Encontra a estratégia apropriada para o tipo de ativo da posição.
     *
     * @param holding A posição de ativo
     * @return A estratégia que pode lidar com o tipo de ativo, ou null se nenhuma for encontrada
     */
    private fun findStrategyForHolding(holding: AssetHolding): CopyHistoryStrategy? {
        return strategies.firstOrNull { it.canHandle(holding) }
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

