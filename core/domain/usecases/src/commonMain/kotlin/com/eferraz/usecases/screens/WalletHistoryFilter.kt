package com.eferraz.usecases.screens

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

public data class WalletHistoryFilterCriteria(
    val assetClasses: Set<AssetClass> = emptySet(),
    val subtypes: Set<WalletHistorySubtype> = emptySet(),
    val liquidities: Set<Liquidity> = emptySet(),
    val b3Informed: Set<Boolean> = emptySet(),
    val settled: Set<Boolean> = emptySet(),
    val maturityUpTo: YearMonth? = null,
    val brokerageIds: Set<Long> = emptySet(),
) {
    public companion object {
        /** Histórico por defeito: só posições não liquidadas; restantes grupos inactivos. */
        public fun defaultForHistory(): WalletHistoryFilterCriteria =
            WalletHistoryFilterCriteria(settled = setOf(false))
    }
}

public sealed interface WalletHistorySubtype {
    public data class FixedIncome(val value: FixedIncomeAssetType) : WalletHistorySubtype
    public data class VariableIncome(val value: VariableIncomeAssetType) : WalletHistorySubtype
    public data class InvestmentFund(val value: InvestmentFundAssetType) : WalletHistorySubtype
}

public data class WalletHistoryFilterCandidate(
    val assetClass: AssetClass,
    val subtype: WalletHistorySubtype,
    val liquidity: Liquidity?,
    val b3Informed: Boolean,
    val settled: Boolean,
    /** Data de vencimento do activo (RF); usada pelo filtro «Vence até». */
    val expirationDate: LocalDate?,
    val brokerageId: Long = 0L,
)

/** Último dia do mês seleccionado em «Vence até» (inclusive). */
internal fun YearMonth.lastDayOfMonth(): LocalDate =
    LocalDate(year, month, 1)
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))

internal fun WalletHistorySubtype.assetClass(): AssetClass =
    when (this) {
        is WalletHistorySubtype.FixedIncome -> AssetClass.FIXED_INCOME
        is WalletHistorySubtype.VariableIncome -> AssetClass.VARIABLE_INCOME
        is WalletHistorySubtype.InvestmentFund -> AssetClass.INVESTMENT_FUND
    }
