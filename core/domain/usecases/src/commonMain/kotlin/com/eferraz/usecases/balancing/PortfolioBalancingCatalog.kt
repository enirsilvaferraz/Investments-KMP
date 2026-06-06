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
                id = BalancingGroupId.CRYPTO,
                displayName = "Cripto Ativos",
                targetWeight = TargetWeight.Fixed(1.0),
                matches = BalancingMatchers::isCrypto,
            ),
            BalancingComponent(
                id = BalancingGroupId.PENSION_FUNDS,
                displayName = "Fundos de Previdência",
                targetWeight = TargetWeight.Dynamic,
                matches = BalancingMatchers::isPensionFund,
            ),
            BalancingComponent(
                id = BalancingGroupId.FIXED_INCOME,
                displayName = "Renda Fixa",
                targetWeight = TargetWeight.Fixed(50.0),
                matches = BalancingMatchers::isFixedIncome,
            ),
            BalancingComponent(
                id = BalancingGroupId.VARIABLE_INCOME,
                displayName = "Renda Variável",
                targetWeight = TargetWeight.Fixed(49.0),
                matches = BalancingMatchers::isVariableIncomeExcludingCrypto,
            ),
            BalancingComponent(
                id = BalancingGroupId.OTHER_INVESTMENTS,
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
                id = BalancingGroupId.RF_POST_FIXED,
                displayName = "Pós-fixados",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.FIXED_INCOME,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.POST_FIXED),
            ),
            BalancingComponent(
                id = BalancingGroupId.RF_PRE_FIXED,
                displayName = "Pré-fixado",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.FIXED_INCOME,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.PRE_FIXED),
            ),
            BalancingComponent(
                id = BalancingGroupId.RF_INFLATION_LINKED,
                displayName = "Atrelado a inflação",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.FIXED_INCOME,
                matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.INFLATION_LINKED),
            ),
            BalancingComponent(
                id = BalancingGroupId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.FIXED_INCOME,
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
                id = BalancingGroupId.RV_NATIONAL_STOCKS,
                displayName = "Ações Nacionais",
                targetWeight = TargetWeight.Fixed(50.0),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers::isNationalStockExcludingSpecialTickers,
            ),
            BalancingComponent(
                id = BalancingGroupId.RV_INTERNATIONAL,
                displayName = "Ações Internacionais",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers::isInternationalStock,
            ),
            BalancingComponent(
                id = BalancingGroupId.RV_REITS,
                displayName = "FIIs",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers::isRealEstateFund,
            ),
            BalancingComponent(
                id = BalancingGroupId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Fixed(10.0),
                parentId = BalancingGroupId.VARIABLE_INCOME,
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
                id = BalancingGroupId.FII_REND,
                displayName = "FII - Renda%",
                targetWeight = TargetWeight.Fixed(70.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_TAT,
                displayName = "FII - Tatica",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_FOF,
                displayName = "FII - FoFs",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    internal val fundsFoFGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FII_FOF,
        displayName = "FIIs",
        universeFilter = BalancingMatchers::isRealEstateFund,
        components = listOf(
            BalancingComponent(
                id = BalancingGroupId.FII_REND,
                displayName = "FII - Renda%",
                targetWeight = TargetWeight.Fixed(70.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_TAT,
                displayName = "FII - Tatica",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_FOF,
                displayName = "FII - FoFs",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS),
            ),
            BalancingComponent(
                id = BalancingGroupId.OTHER_INVESTMENTS,
                displayName = "Demais investimentos",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers::always,
            ),
        ),
    )

    val groups: List<BalancingGroup> = listOf(portfolioTotalGroup, fixedIncomeGroup, variableIncomeGroup, fundsGroup)
}
