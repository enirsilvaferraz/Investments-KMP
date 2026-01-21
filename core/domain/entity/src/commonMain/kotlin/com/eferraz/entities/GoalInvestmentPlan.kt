package com.eferraz.entities

/**
 * Representa o plano de investimento de uma meta financeira.
 *
 * Este Value Object encapsula os parâmetros do plano de investimento, que podem ser
 * utilizados tanto para simulações hipotéticas quanto para definir e acompanhar o
 * plano oficial de uma meta. Permite ao usuário planejar quanto tempo levará para
 * atingir seu objetivo com base em aportes mensais e taxa de retorno esperada.
 *
 * @property id O identificador único do plano (opcional, null para simulações ad-hoc).
 * @property goal A meta financeira associada ao plano (opcional, null para simulações).
 * @property contribution O valor de aporte mensal planejado.
 * @property appreciationRate A taxa de retorno mensal esperada (em percentual, ex: 0.80 para 0,80%).
 * @property initialValue O valor inicial da meta (opcional, padrão é 0.0).
 */
public data class GoalInvestmentPlan(
    public val id: Long? = null,
    public val goal: FinancialGoal,
    public val contribution: Double,
    public val appreciationRate: Double,
    public val initialValue: Double = 0.0
) {

    init {
        require(contribution != 0.0 || appreciationRate != 0.0 ) {
            "Meta inalcançável: sem aporte e sem rentabilidade"
        }
    }
}
