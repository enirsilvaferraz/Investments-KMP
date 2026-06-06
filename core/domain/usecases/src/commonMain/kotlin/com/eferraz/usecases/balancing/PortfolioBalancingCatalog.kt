package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.YieldIndexer

internal object PortfolioBalancingCatalog {

    // Extensibility (SC-004 / OCP): new balancing components are added here only;
    // PortfolioBalancingEngine remains unchanged.

    internal val portfolioTotalGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.PORTFOLIO_TOTAL,
        displayName = "Carteira Total",
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.CRYPTO,
                displayName = "Cripto Ativos",
                targetWeight = TargetWeight.Fixed(1.0),
                matches = BalancingMatchers::isCrypto,
            ),
            BalancingComponent(
                id = BalancingComponentId.PENSION_FUNDS,
                displayName = "Fundos de Previdência",
                targetWeight = TargetWeight.Dynamic,
                matches = BalancingMatchers::isPensionFund,
            ),
            BalancingComponent(
                id = BalancingComponentId.FIXED_INCOME_TOTAL,
                displayName = "Renda Fixa",
                targetWeight = TargetWeight.Fixed(50.0),
                matches = BalancingMatchers::isFixedIncome,
            ),
            BalancingComponent(
                id = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                displayName = "Renda Variável",
                targetWeight = TargetWeight.Fixed(49.0),
                matches = BalancingMatchers::isVariableIncomeExcludingCrypto,
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    internal val fixedIncomeGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FIXED_INCOME,
        displayName = "Renda Fixa",
        universeFilter = BalancingMatchers::isFixedIncome,
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.RF_POST_FIXED,
                displayName = "Pós-fixados",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.POST_FIXED),
            ),
            BalancingComponent(
                id = BalancingComponentId.RF_PRE_FIXED,
                displayName = "Pré-fixado",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.PRE_FIXED),
            ),
            BalancingComponent(
                id = BalancingComponentId.RF_INFLATION_LINKED,
                displayName = "Atrelado a inflação",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.INFLATION_LINKED),
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.FIXED_INCOME_TOTAL,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    internal val variableIncomeGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.VARIABLE_INCOME,
        displayName = "Renda Variável",
        universeFilter = BalancingMatchers::isVariableIncomeExcludingCrypto,
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.RV_NATIONAL_STOCKS,
                displayName = "Ações Nacionais",
                targetWeight = TargetWeight.Fixed(50.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = BalancingMatchers::isNationalStockExcludingSpecialTickers,
            ),
            BalancingComponent(
                id = BalancingComponentId.RV_INTERNATIONAL,
                displayName = "Ações Internacionais",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = BalancingMatchers::isInternationalStock,
            ),
            BalancingComponent(
                id = BalancingComponentId.RV_REITS,
                displayName = "FIIs",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = BalancingMatchers::isRealEstateFund,
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingComponentId.VARIABLE_INCOME_TOTAL,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    internal val fundsGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.RV_REITS,
        displayName = "FIIs",
        universeFilter = BalancingMatchers::isRealEstateFund,
        components = listOf(
            BalancingComponent(
                id = BalancingComponentId.FII_REND,
                displayName = "FII - Renda%",
                targetWeight = TargetWeight.Fixed(70.0),
                parentId = BalancingComponentId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS),
            ),
            BalancingComponent(
                id = BalancingComponentId.FII_TAT,
                displayName = "FII - Tatica",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingComponentId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS),
            ),
            BalancingComponent(
                id = BalancingComponentId.FII_FOF,
                displayName = "FII - FoFs",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS),
            ),
            BalancingComponent(
                id = BalancingComponentId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingComponentId.RV_REITS,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    val groups: List<BalancingGroup> = listOf(portfolioTotalGroup, fixedIncomeGroup, variableIncomeGroup, fundsGroup)
}
