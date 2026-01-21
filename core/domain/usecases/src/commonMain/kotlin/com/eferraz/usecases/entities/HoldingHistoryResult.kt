package com.eferraz.usecases.entities

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.rules.Appreciation

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
    public val profitOrLoss: Appreciation,
)