package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry

/**
 * Representa o resultado do histórico de uma posição de ativo.
 *
 * @property holding A posição de ativo (holding).
 * @property currentEntry A entrada de histórico do mês de referência.
 * @property previousEntry A entrada de histórico do mês anterior.
 */
public data class HoldingHistoryResult(
    public val holding: AssetHolding,
    public val currentEntry: HoldingHistoryEntry,
    public val previousEntry: HoldingHistoryEntry,
)
