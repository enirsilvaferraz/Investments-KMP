package com.eferraz.entities.goals

/**
 * Representa os dados consolidados mensais de uma meta financeira.
 * Implements: [docs/RN - Calcular Histórico de Meta Financeira.md]
 *
 * Esta estrutura é compartilhada entre histórico e projeções de metas financeiras.
 *
 * @property value Valor consolidado no final do mês.
 * @property contributions Aportes consolidados do mês.
 * @property withdrawals Retiradas consolidadas do mês.
 * @property growth Variação total do patrimônio (value(m) - value(m-1)).
 * @property growthRate Percentual de crescimento sobre o valor inicial.
 * @property appreciation Lucro/prejuízo isolado (sem considerar aportes/retiradas).
 * @property appreciationRate Rentabilidade sobre o capital investido.
 */
public data class GoalMonthlyData(
    public val value: Double,
    public val contributions: Double,
    public val withdrawals: Double,
    public val growth: Double,
    public val growthRate: Double,
    public val appreciation: Double,
    public val appreciationRate: Double,
)