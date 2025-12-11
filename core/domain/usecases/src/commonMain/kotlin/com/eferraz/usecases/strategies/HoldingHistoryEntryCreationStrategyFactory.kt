package com.eferraz.usecases.strategies

import com.eferraz.entities.Asset
import org.koin.core.annotation.Factory

/**
 * Factory para obter a estratégia apropriada baseada no tipo de ativo.
 * Segue o padrão Strategy para permitir extensão sem modificação (OCP).
 */
@Factory
public class HoldingHistoryEntryCreationStrategyFactory(
    private val strategies: List<HoldingHistoryEntryCreationStrategy>,
) {
    /**
     * Obtém a estratégia apropriada para o tipo de ativo fornecido.
     *
     * @param asset O ativo para o qual a estratégia será obtida
     * @return A estratégia apropriada, ou null se nenhuma estratégia puder lidar com o ativo
     */
    public fun getStrategy(asset: Asset): HoldingHistoryEntryCreationStrategy? {
        return strategies.firstOrNull { it.canHandle(asset) }
    }
}
