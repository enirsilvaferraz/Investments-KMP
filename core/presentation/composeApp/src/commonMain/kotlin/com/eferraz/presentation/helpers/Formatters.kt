package com.eferraz.presentation.helpers

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

internal object Formatters {

    internal fun Asset.formated(): String = when (this) {
        is FixedIncomeAsset -> "Renda Fixa"
        is InvestmentFundAsset -> "Fundos"//"Fundo de Investimento"
        is VariableIncomeAsset -> "Renda Variável"
    }

    internal fun FixedIncomeAssetType.formated() = when (this) {
        FixedIncomeAssetType.POST_FIXED -> "Pós Fixado"
        FixedIncomeAssetType.PRE_FIXED -> "Prefixado"
        FixedIncomeAssetType.INFLATION_LINKED -> "IPCA"
    }

    internal fun FixedIncomeSubType.formated() = when (this) {
        FixedIncomeSubType.CDB -> "CDB"
        FixedIncomeSubType.LCI -> "LCI"
        FixedIncomeSubType.LCA -> "LCA"
        FixedIncomeSubType.CRA -> "CRA"
        FixedIncomeSubType.CRI -> "CRI"
        FixedIncomeSubType.DEBENTURE -> "Debênture"
        FixedIncomeSubType.SELIC -> "SELIC"
        FixedIncomeSubType.PRECATORIO -> "Precatório"
    }

    internal fun InvestmentFundAssetType.formated() = when (this) {
        InvestmentFundAssetType.PENSION -> "Previdência"
        InvestmentFundAssetType.STOCK_FUND -> "Fundos de Ação"
        InvestmentFundAssetType.MULTIMARKET_FUND -> "Multimercado"
    }

    internal fun VariableIncomeAssetType.formated() = when (this) {
        VariableIncomeAssetType.NATIONAL_STOCK -> "Ação"
        VariableIncomeAssetType.INTERNATIONAL_STOCK -> "BDR"
        VariableIncomeAssetType.REAL_ESTATE_FUND -> "FII"
        VariableIncomeAssetType.ETF -> "ETF"
    }

    internal fun LocalDate?.formated() =
        this?.format(LocalDate.Format { year(); char('-'); monthNumber(); char('-'); day() }) ?: "-"


    internal fun YearMonth.formated(): String =
        format(YearMonth.Format { monthNumber(); char('/'); year() })

    internal fun Liquidity.formated(liquidityDays: Int? = null): String = when (this) {
        Liquidity.DAILY -> "Diária"
        Liquidity.AT_MATURITY -> "No vencimento"
        Liquidity.D_PLUS_DAYS -> "D+${liquidityDays ?: 0}"
    }

    internal fun InvestmentCategory?.formated(): String = when (this) {
        InvestmentCategory.FIXED_INCOME -> "Renda Fixa"
        InvestmentCategory.VARIABLE_INCOME -> "Renda Variável"
        InvestmentCategory.INVESTMENT_FUND -> "Fundos"
        else -> ""
    }
}