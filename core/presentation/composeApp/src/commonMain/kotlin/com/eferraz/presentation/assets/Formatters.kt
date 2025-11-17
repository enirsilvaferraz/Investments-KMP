package com.eferraz.presentation.assets

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char

internal object Formatters {

    internal fun Asset.formated(): String = when (this) {
        is FixedIncomeAsset -> "Renda Fixa"
        is InvestmentFundAsset -> "Fundo de Investimento"
        is VariableIncomeAsset -> "Renda Variável"
    }

    internal fun FixedIncomeAssetType.formated() = when (this) {
        FixedIncomeAssetType.POST_FIXED -> "Pós Fixado"
        FixedIncomeAssetType.PRE_FIXED -> "Prefixado"
        FixedIncomeAssetType.INFLATION_LINKED -> "IPCA"
    }

    internal fun InvestmentFundAssetType.formated() = when (this) {
        InvestmentFundAssetType.PENSION -> "Previdência"
        InvestmentFundAssetType.STOCK_FUND -> "Fundos de Ação"
        InvestmentFundAssetType.MULTIMARKET_FUND -> "Multimercado"
    }

    internal fun VariableIncomeAssetType.formated() = when (this) {
        VariableIncomeAssetType.NATIONAL_STOCK -> "Ações Nacionais"
        VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ações Internacionais"
        VariableIncomeAssetType.REAL_ESTATE_FUND -> "Fundos de Imobiliários"
        VariableIncomeAssetType.ETF -> "ETF"
    }

    internal fun LocalDate?.formated() =
        this?.format(LocalDate.Companion.Format { year(); char('-'); monthNumber(); char('-'); day() }) ?: "-"
}