package com.eferraz.presentation.helpers

import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.transactions.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

internal object Formatters {

    internal fun LocalDate?.formated() =
        this?.format(
            LocalDate.Format {
                year()
                char('.')
                monthNumber()
                char('.')
                day()
            }
        ) ?: "-"

    internal fun YearMonth.formated(): String =
        format(
            YearMonth.Format {
                year()
                char('.')
                monthNumber()
            }
        )

    internal fun Liquidity.formated(liquidityDays: Int? = null): String =
        when (this) {
            Liquidity.DAILY -> "Diária"
            Liquidity.AT_MATURITY -> "No vencimento"
            Liquidity.D_PLUS_DAYS -> "D+${liquidityDays ?: 0}"
        }

    internal fun InvestmentCategory?.formated(): String =
        when (this) {
            InvestmentCategory.FIXED_INCOME -> "Renda Fixa"
            InvestmentCategory.VARIABLE_INCOME -> "Renda Variável"
            InvestmentCategory.INVESTMENT_FUND -> "Fundos"
            else -> ""
        }

    internal fun TransactionType.formated(): String =
        when (this) {
            TransactionType.PURCHASE -> "Compra"
            TransactionType.SALE -> "Venda"
        }
}
