package com.eferraz.naming

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YesOrNo
import com.eferraz.entities.transactions.TransactionType

/** Rótulo do dropdown de corretora no cadastro de investimento. */
public const val BROKERAGE_FIELD_LABEL: String = "Corretora"

/** Rótulo do campo Identificador B3 no cadastro de renda fixa. */
public const val B3_IDENTIFIER_FIELD_LABEL: String = "Identificador B3"

public fun Asset.asLabel(): String =
    when (this) {
        is FixedIncomeAsset -> "Renda Fixa"
        is InvestmentFundAsset -> "Fundos" // "Fundo de Investimento"
        is VariableIncomeAsset -> "Renda Variável"
    }


public fun FixedIncomeAssetType.asLabel(): String =
    when (this) {
        FixedIncomeAssetType.POST_FIXED -> "Pós-fixado"
        FixedIncomeAssetType.PRE_FIXED -> "Pré-fixado"
        FixedIncomeAssetType.INFLATION_LINKED -> "Atrelado à inflação"
    }

public fun FixedIncomeSubType.asLabel(): String =
    when (this) {
        FixedIncomeSubType.CDB -> "CDB"
        FixedIncomeSubType.LCI -> "LCI"
        FixedIncomeSubType.LCA -> "LCA"
        FixedIncomeSubType.LIG -> "LIG"
        FixedIncomeSubType.DEBENTURE -> "Debênture"
        FixedIncomeSubType.SELIC -> "Tesouro Selic"
        FixedIncomeSubType.PRECATORIO -> "Precatório"
    }

public fun VariableIncomeAssetType.asLabel(): String =
    when (this) {
        VariableIncomeAssetType.NATIONAL_STOCK -> "Ação"
        VariableIncomeAssetType.INTERNATIONAL_STOCK -> "Internacional"
        VariableIncomeAssetType.REAL_ESTATE_FUND -> "FII"
        VariableIncomeAssetType.ETF -> "ETF"
    }

public fun InvestmentFundAssetType.asLabel(): String =
    when (this) {
        InvestmentFundAssetType.PENSION -> "Previdência"
        InvestmentFundAssetType.STOCK_FUND -> "Fundo de ações"
        InvestmentFundAssetType.MULTIMARKET_FUND -> "Fundo multimercado"
    }

public fun InvestmentCategory.asLabel(): String =
    when (this) {
        InvestmentCategory.FIXED_INCOME -> "Renda fixa"
        InvestmentCategory.VARIABLE_INCOME -> "Renda variável"
        InvestmentCategory.INVESTMENT_FUND -> "Fundo de investimento"
    }

public fun Liquidity.asLabel(): String =
    when (this) {
        Liquidity.DAILY -> "Diária"
        Liquidity.AT_MATURITY -> "Vencimento"
        Liquidity.D_PLUS_DAYS -> "D+dias"
    }

public fun YesOrNo.asLabel(): String =
    when (this) {
        YesOrNo.YES -> "Sim"
        YesOrNo.NO -> "Não"
    }

public fun TransactionType.asLabel(): String =
    when (this) {
        TransactionType.PURCHASE -> "Compra"
        TransactionType.SALE -> "Venda"
    }
