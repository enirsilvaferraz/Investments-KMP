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
                id = BalancingGroupId.FIXED_INCOME,
                displayName = "Renda Fixa",
                targetWeight = TargetWeight.Fixed(82.5),
                matches = BalancingMatchers::isFixedIncome,
            ),
            BalancingComponent(
                id = BalancingGroupId.VARIABLE_INCOME,
                displayName = "Renda Variável",
                targetWeight = TargetWeight.Fixed(16.5),
                matches = BalancingMatchers::isVariableIncomeExcludingCrypto,
            ),
            BalancingComponent(
                id = BalancingGroupId.PENSION_FUNDS,
                displayName = "Fundos de Previdência",
                targetWeight = TargetWeight.Dynamic,
                matches = BalancingMatchers::isPensionFund,
            ),
            BalancingComponent(
                id = BalancingGroupId.FGTS_FUNDS,
                displayName = "Fundos do FGTS",
                targetWeight = TargetWeight.Dynamic,
                matches = BalancingMatchers::isFGTSFund
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
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers.isNationalStock(),
            ),
            BalancingComponent(
                id = BalancingGroupId.RV_INTERNATIONAL,
                displayName = "Ações Internacionais",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers::isInternationalStock,
            ),
            BalancingComponent(
                id = BalancingGroupId.RV_REITS,
                displayName = "FIIs",
                targetWeight = TargetWeight.Fixed(33.33),
                parentId = BalancingGroupId.VARIABLE_INCOME,
                matches = BalancingMatchers::isRealEstateFund,
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
                displayName = "FII - Renda",
                targetWeight = TargetWeight.Fixed(70.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS.keys),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_TAT,
                displayName = "FII - Tatica",
                targetWeight = TargetWeight.Fixed(30.0),
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS.keys),
            ),
            BalancingComponent(
                id = BalancingGroupId.FII_FOF,
                displayName = "FII - FoFs",
                targetWeight = TargetWeight.Zero,
                parentId = BalancingGroupId.RV_REITS,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS.keys),
            ),
        ),
    )

    internal val fundsFoFGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FII_FOF,
        displayName = "FIIs - FOFs",
        universeFilter = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS.keys),
        components = BalancingConstants.FII_FOF_TICKERS.map { (ticker, weight) ->
            BalancingComponent(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                parentId = BalancingGroupId.FII_FOF,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }
    )

    internal val fundsTatGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FII_TAT,
        displayName = "FIIs - Tatica",
        universeFilter = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS.keys),
        components = BalancingConstants.FII_TAT_TICKERS.map { (ticker, weight) ->
            BalancingComponent(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                parentId = BalancingGroupId.FII_TAT,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }
    )

    internal val fundsRendGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.FII_REND,
        displayName = "FIIs - Renda",
        universeFilter = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS.keys),
        components = BalancingConstants.FII_REND_TICKERS.map { (ticker, weight) ->
            BalancingComponent(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                parentId = BalancingGroupId.FII_REND,
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }
    )

    internal val acoesGroup: BalancingGroup = BalancingGroup(
        id = BalancingGroupId.RV_NATIONAL_STOCKS,
        displayName = "Ações Nacionais",
        universeFilter = BalancingMatchers.isNationalStock(),
        components = BalancingConstants.NATIONAL_STOCK_TICKERS.map { (ticker, weight) ->
            BalancingComponent(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                parentId = BalancingGroupId.RV_NATIONAL_STOCKS,
                matches = BalancingMatchers.isNationalStockWithTickerIn(setOf(ticker)),
            )
        }
    )

    val groups: List<BalancingGroup> = listOf(
        portfolioTotalGroup,
//        fixedIncomeGroup,
        variableIncomeGroup,
        acoesGroup,
        fundsGroup,
        fundsRendGroup,
        fundsTatGroup,
        fundsFoFGroup,
    ).withDefaultOtherInvestments()
}
