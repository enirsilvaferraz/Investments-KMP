package com.eferraz.asset_management.helpers

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType

/** Rótulo do dropdown de corretora no cadastro de investimento. */
internal const val BROKERAGE_FIELD_LABEL: String = "Corretora"

internal fun FixedIncomeAssetType.asLabel(): String =
    when (this) {
        FixedIncomeAssetType.POST_FIXED -> "Pós-fixado"
        FixedIncomeAssetType.PRE_FIXED -> "Pré-fixado"
        FixedIncomeAssetType.INFLATION_LINKED -> "Atrelado à inflação"
    }

internal fun FixedIncomeSubType.asLabel(): String =
    when (this) {
        FixedIncomeSubType.CDB -> "CDB"
        FixedIncomeSubType.LCI -> "LCI"
        FixedIncomeSubType.LCA -> "LCA"
        FixedIncomeSubType.LIG -> "LIG"
        FixedIncomeSubType.DEBENTURE -> "Debênture"
        FixedIncomeSubType.SELIC -> "Tesouro Selic"
        FixedIncomeSubType.PRECATORIO -> "Precatório"
    }

internal fun VariableIncomeAssetType.asLabel(): String =
    when (this) {
        VariableIncomeAssetType.NATIONAL_STOCK -> "Ação nacional"
        VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Ação internacional"
        VariableIncomeAssetType.REAL_ESTATE_FUND -> "Fundo imobiliário"
        VariableIncomeAssetType.ETF -> "ETF"
    }

internal fun InvestmentFundAssetType.asLabel(): String =
    when (this) {
        InvestmentFundAssetType.PENSION -> "Previdência"
        InvestmentFundAssetType.STOCK_FUND -> "Fundo de ações"
        InvestmentFundAssetType.MULTIMARKET_FUND -> "Fundo multimercado"
    }

internal fun InvestmentCategory.asLabel(): String =
    when (this) {
        InvestmentCategory.FIXED_INCOME -> "Renda fixa"
        InvestmentCategory.VARIABLE_INCOME -> "Renda variável"
        InvestmentCategory.INVESTMENT_FUND -> "Fundo de investimento"
    }

internal fun Liquidity.asLabel(): String =
    when (this) {
        Liquidity.DAILY -> "Diária"
        Liquidity.AT_MATURITY -> "No vencimento"
        Liquidity.D_PLUS_DAYS -> "D + dias"
    }
