package com.eferraz.usecases.strategies

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import kotlinx.datetime.YearMonth

/**
 * Estratégia para criação de entradas de histórico de posições.
 * Permite diferentes implementações baseadas no tipo de ativo (OCP).
 */
public interface HoldingHistoryEntryCreationStrategy {
    /**
     * Verifica se esta estratégia pode ser aplicada ao ativo fornecido.
     *
     * @param asset O ativo a ser verificado
     * @return true se a estratégia pode ser aplicada, false caso contrário
     */
    public fun canHandle(asset: com.eferraz.entities.Asset): Boolean

    /**
     * Cria uma entrada de histórico para o holding fornecido.
     *
     * @param holding A posição de ativo
     * @param referenceDate A data de referência para a entrada
     * @param previousEntry A entrada do mês anterior, se existir
     * @param currentEntry A entrada do mês atual, se existir
     * @return A nova entrada de histórico criada
     */
    public suspend fun createEntry(
        holding: AssetHolding,
        referenceDate: YearMonth,
        previousEntry: HoldingHistoryEntry?,
        currentEntry: HoldingHistoryEntry?,
    ): HoldingHistoryEntry
}
