package com.eferraz.entities.goals

import com.eferraz.entities.holdings.Owner
import kotlinx.datetime.LocalDate

/**
 * Representa uma meta financeira a ser alcançada.
 *
 * @property id O identificador único da meta.
 * @property owner O proprietário da meta (pessoa física ou jurídica).
 * @property name O nome descritivo da meta (ex: "Aposentadoria", "Casa própria", "Reserva de emergência").
 * @property targetValue O valor monetário objetivo a ser atingido.
 * @property startDate A data de início da meta (quando começou a poupar para ela).
 * @property description Descrição opcional com detalhes adicionais sobre a meta.
 *
 * Nota: O valor atual da meta, a data de conclusão estimada, a média de aportes e
 * a rentabilidade média são todos calculados dinamicamente a partir do histórico
 * das posições (AssetHolding) associadas a esta meta.
 */
public data class FinancialGoal(
    public val id: Long,
    public val owner: Owner,
    public val name: String,
    public val targetValue: Double,
    public val startDate: LocalDate,
    public val description: String? = null,
) {

    init {
        require(targetValue > 0) { "O valor objetivo da meta deve ser maior que zero." }
    }
}