package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

public data class WalletHistoryFilterCriteria(
    val categories: Set<InvestmentCategory> = emptySet(),
    val subtypes: Set<WalletHistorySubtype> = emptySet(),
    val liquidities: Set<Liquidity> = emptySet(),
    val b3Informed: Set<Boolean> = emptySet(),
    val settled: Set<Boolean> = emptySet(),
    val maturityUpTo: YearMonth? = null,
) {
    public companion object {
        /** Histórico por defeito: só posições não liquidadas; restantes grupos inactivos. */
        public fun defaultForHistory(): WalletHistoryFilterCriteria =
            WalletHistoryFilterCriteria(settled = setOf(false))
    }
}

public sealed interface WalletHistorySubtype {
    public data class FixedIncome(val value: FixedIncomeSubType) : WalletHistorySubtype
    public data class VariableIncome(val value: VariableIncomeAssetType) : WalletHistorySubtype
    public data class InvestmentFund(val value: InvestmentFundAssetType) : WalletHistorySubtype
}

public data class WalletHistoryFilterCandidate(
    val category: InvestmentCategory,
    val subtype: WalletHistorySubtype,
    val liquidity: Liquidity?,
    val b3Informed: Boolean,
    val settled: Boolean,
    /** Data de vencimento do activo (RF); usada pelo filtro «Vence até». */
    val expirationDate: LocalDate?,
)

public fun matchesWalletHistoryFilter(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (!matchesCategory(candidate, criteria)) return false
    if (!matchesSubtype(candidate, criteria)) return false
    if (!matchesLiquidity(candidate, criteria)) return false
    if (!matchesB3Informed(candidate, criteria)) return false
    if (!matchesSettled(candidate, criteria)) return false
    if (!matchesMaturity(candidate, criteria)) return false
    return true
}

private fun matchesCategory(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (criteria.categories.isEmpty()) return true
    return candidate.category in criteria.categories
}

private fun matchesSubtype(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (criteria.subtypes.isEmpty()) return true
    val applicable = criteria.subtypes.filter { it.category() == candidate.category }
    val effective = if (criteria.categories.isEmpty()) {
        applicable
    } else {
        applicable.filter { it.category() in criteria.categories }
    }
    if (effective.isEmpty()) return true
    return candidate.subtype in effective
}

private fun matchesLiquidity(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (criteria.liquidities.isEmpty()) return true
    if (candidate.category != InvestmentCategory.FIXED_INCOME) return true
    return candidate.liquidity in criteria.liquidities
}

private fun matchesB3Informed(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (criteria.b3Informed.isEmpty() || criteria.b3Informed.size == 2) return true
    return candidate.b3Informed in criteria.b3Informed
}

private fun matchesSettled(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    if (criteria.settled.isEmpty() || criteria.settled.size == 2) return true
    return candidate.settled in criteria.settled
}

private fun matchesMaturity(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean {
    val upTo = criteria.maturityUpTo ?: return true
    if (candidate.category != InvestmentCategory.FIXED_INCOME) return true
    if (candidate.liquidity == Liquidity.DAILY) return true
    val expiration = candidate.expirationDate ?: return false
    return expiration <= upTo.lastDayOfMonth()
}

/** Último dia do mês seleccionado em «Vence até» (inclusive). */
internal fun YearMonth.lastDayOfMonth(): LocalDate =
    LocalDate(year, month, 1)
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))

internal fun WalletHistorySubtype.category(): InvestmentCategory =
    when (this) {
        is WalletHistorySubtype.FixedIncome -> InvestmentCategory.FIXED_INCOME
        is WalletHistorySubtype.VariableIncome -> InvestmentCategory.VARIABLE_INCOME
        is WalletHistorySubtype.InvestmentFund -> InvestmentCategory.INVESTMENT_FUND
    }
