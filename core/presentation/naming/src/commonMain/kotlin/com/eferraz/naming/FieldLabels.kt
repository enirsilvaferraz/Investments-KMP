package com.eferraz.naming

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.AssetType
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YesOrNo
import com.eferraz.entities.assets.YieldIndexer
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

public fun YieldIndexer.asLabel(): String =
    when (this) {
        YieldIndexer.POST_FIXED -> "Pós-fixado"
        YieldIndexer.PRE_FIXED -> "Pré-fixado"
        YieldIndexer.INFLATION_LINKED -> "Atrelado à inflação"
    }

public fun AssetType.asLabel(): String =
    when (this) {
        is FixedIncomeAssetType -> this.asLabel()
        is InvestmentFundAssetType -> this.asLabel()
        is VariableIncomeAssetType -> this.asLabel()
    }

public fun FixedIncomeAssetType.asLabel(): String =
    when (this) {
        FixedIncomeAssetType.CDB -> "CDB"
        FixedIncomeAssetType.LCI -> "LCI"
        FixedIncomeAssetType.LCA -> "LCA"
        FixedIncomeAssetType.LIG -> "LIG"
        FixedIncomeAssetType.DEBENTURE -> "Debênture"
        FixedIncomeAssetType.SELIC -> "Tesouro Selic"
        FixedIncomeAssetType.PRECATORIO -> "Precatório"
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

public fun AssetClass.asLabel(): String =
    when (this) {
        AssetClass.FIXED_INCOME -> "Renda fixa"
        AssetClass.VARIABLE_INCOME -> "Renda variável"
        AssetClass.INVESTMENT_FUND -> "Fundo de investimento"
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
