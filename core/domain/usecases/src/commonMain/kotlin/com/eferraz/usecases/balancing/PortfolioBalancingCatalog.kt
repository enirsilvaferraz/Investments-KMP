package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer

internal object PortfolioBalancingCatalog {

    // Extensibility (SC-004 / OCP): new balancing components are added here only;
    // PortfolioBalancingEngine remains unchanged.

    internal const val HASH11: String = "HASH11"
    internal const val IVVB11: String = "IVVB11"

    internal val FII_REND_TICKERS: Set<String> = setOf(
        "BRCO11",
        "BTLG11",
        "HGBS11",
        "HSML11",
        "JSRE11",
        "KNCR11",
        "KNSC11",
        "MCCI11",
        "PVBI11",
    )

    internal val FII_TAT_TICKERS: Set<String> = setOf(
        "ALZR11",
        "CLIN11",
        "HGCR11",
        "KNIP11",
        "KORE11",
        "PMLL11",
        "VILG11",
        "VISC11",
    )

    internal val FII_FOF_TICKERS: Set<String> = setOf(
        "BTHF11",
        "BCIA11",
        "KNHF11",
        "MCRE11",
    )

    internal val portfolioTotalGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.PORTFOLIO_TOTAL,
        displayName = "Carteira Total",
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.CRYPTO,
                displayName = "Cripto Ativos",
                targetWeight = TargetWeight.Fixed(1.0),
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset && asset.ticker.uppercase() == HASH11
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.PENSION_FUNDS,
                displayName = "Fundos de Previdência",
                targetWeight = TargetWeight.Dynamic,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is InvestmentFundAsset && asset.type == InvestmentFundAssetType.PENSION
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.FIXED_INCOME_TOTAL,
                displayName = "Renda Fixa",
                targetWeight = TargetWeight.Fixed(50.0),
                matches = { entry -> entry.holding.asset is FixedIncomeAsset },
            ),
            BalancingComponent(
                id = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                displayName = "Renda Variável",
                targetWeight = TargetWeight.Fixed(49.0),
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset && asset.ticker.uppercase() != HASH11
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                matches = { true },
            ),
        ),
    )

    internal val fixedIncomeGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FIXED_INCOME,
        displayName = "Renda Fixa",
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.RF_POST_FIXED,
                displayName = "Pós-fixados",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is FixedIncomeAsset && asset.indexer == YieldIndexer.POST_FIXED
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.RF_PRE_FIXED,
                displayName = "Pré-fixado",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is FixedIncomeAsset && asset.indexer == YieldIndexer.PRE_FIXED
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.RF_INFLATION_LINKED,
                displayName = "Atrelado a inflação",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is FixedIncomeAsset && asset.indexer == YieldIndexer.INFLATION_LINKED
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = { true },
            ),
        ),
    )

    internal val variableIncomeGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.VARIABLE_INCOME,
        displayName = "Renda Variável",
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.RV_NATIONAL_STOCKS,
                displayName = "Ações Nacionais",
                targetWeight = TargetWeight.Fixed(50.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset &&
                        asset.type == VariableIncomeAssetType.NATIONAL_STOCK &&
                        asset.ticker.uppercase() !in setOf(HASH11, IVVB11)
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.RV_INTERNATIONAL,
                displayName = "Ações Internacionais",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset && asset.ticker.uppercase() == IVVB11
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.RV_REITS,
                displayName = "FIIs",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset && asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = { true },
            ),
        ),
    )

    internal val fundsGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.RV_REITS,
        displayName = "FIIs",
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.FII_REND,
                displayName = "FII - Renda%",
                targetWeight = TargetWeight.Fixed(70.0),
                parentId = BalancingComponentId.RV_REITS,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset &&
                        asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND &&
                        asset.ticker.uppercase() in FII_REND_TICKERS
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.FII_TAT,
                displayName = "FII - Tatica",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingComponentId.RV_REITS,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset &&
                        asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND &&
                        asset.ticker.uppercase() in FII_TAT_TICKERS
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.FII_FOF,
                displayName = "FII - FoFs",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.RV_REITS,
                matches = { entry ->
                    val asset = entry.holding.asset
                    asset is VariableIncomeAsset &&
                        asset.type == VariableIncomeAssetType.REAL_ESTATE_FUND &&
                        asset.ticker.uppercase() in FII_FOF_TICKERS
                },
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.RV_REITS,
                matches = { true },
            ),
        ),
    )

    val groups: List<BalancingGroup> = listOf(portfolioTotalGroup, fixedIncomeGroup, variableIncomeGroup, fundsGroup)
}
