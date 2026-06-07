package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.YieldIndexer

internal object PortfolioBalancingCatalog {

    private val demaisNonBalanceableNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.DEMAIS_NON_BALANCEABLE,
        displayName = "Demais não balanceáveis",
        targetWeight = TargetWeight.Dynamic,
        matches = { entry ->
            BalancingMatchers.isDemaisAmong(
                entry = entry,
                inUniverse = BalancingMatchers::isNonBalanceable,
                siblingMatchers = listOf(
                    BalancingMatchers::isPensionFund,
                    BalancingMatchers::isFGTSFund,
                ),
            )
        },
    )

    private val previdenciaNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.PENSION_FUNDS,
        displayName = "Fundos de Previdência",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isPensionFund,
    )

    private val fgtsNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.FGTS_FUNDS,
        displayName = "Fundos do FGTS",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isFGTSFund,
    )

    internal val nonBalanceableNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.NON_BALANCEABLE,
        displayName = "Carteira Não Balanceável",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isNonBalanceable,
        children = listOf(
            previdenciaNode,
            fgtsNode,
            demaisNonBalanceableNode,
        ),
    )

    private val rfPreNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RF_PRE_FIXED,
        displayName = "Pré-fixado",
        targetWeight = TargetWeight.Fixed(33.33),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.PRE_FIXED),
    )

    private val rfPostNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RF_POST_FIXED,
        displayName = "Pós-fixados",
        targetWeight = TargetWeight.Fixed(33.33),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.POST_FIXED),
    )

    private val rfIpcaNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RF_INFLATION_LINKED,
        displayName = "Atrelado a inflação",
        targetWeight = TargetWeight.Fixed(33.33),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.INFLATION_LINKED),
    )

    internal val rfNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FIXED_INCOME,
            displayName = "Renda Fixa",
            targetWeight = TargetWeight.Fixed(82.5),
            matches = BalancingMatchers::isFixedIncome,
            children = listOf(
                rfPostNode,
                rfPreNode,
                rfIpcaNode,
            ),
        ),
    )

    private val nationalStockTickerNodes: List<BalancingTreeNode> =
        BalancingConstants.NATIONAL_STOCK_TICKERS.map { (ticker, weight) ->
            BalancingTreeNode(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                matches = BalancingMatchers.isNationalStockWithTickerIn(setOf(ticker)),
            )
        }

    internal val nationalStocksNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.RV_NATIONAL_STOCKS,
            displayName = "Ações Nacionais",
            targetWeight = TargetWeight.Fixed(33.33),
            matches = BalancingMatchers.isNationalStock(),
            children = nationalStockTickerNodes,
        ),
    )

    private val internationalNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RV_INTERNATIONAL,
        displayName = "Ações Internacionais",
        targetWeight = TargetWeight.Fixed(33.33),
        matches = BalancingMatchers::isInternationalStock,
    )

    private val fiiRendTickerNodes: List<BalancingTreeNode> =
        BalancingConstants.FII_REND_TICKERS.map { (ticker, weight) ->
            BalancingTreeNode(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }

    private val fiiTatTickerNodes: List<BalancingTreeNode> =
        BalancingConstants.FII_TAT_TICKERS.map { (ticker, weight) ->
            BalancingTreeNode(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }

    private val fiiFoFTickerNodes: List<BalancingTreeNode> =
        BalancingConstants.FII_FOF_TICKERS.map { (ticker, weight) ->
            BalancingTreeNode(
                id = ticker,
                displayName = ticker,
                targetWeight = TargetWeight.Fixed(weight),
                matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
            )
        }

    private val fiiRendNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_REND,
            displayName = "FII - Renda%",
            targetWeight = TargetWeight.Fixed(70.0),
            matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS.keys),
            children = fiiRendTickerNodes,
        ),
    )

    private val fiiTatNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_TAT,
            displayName = "FII - Tatica",
            targetWeight = TargetWeight.Fixed(30.0),
            matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS.keys),
            children = fiiTatTickerNodes,
        ),
    )

    private val fiiFoFNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_FOF,
            displayName = "FII - FoFs",
            targetWeight = TargetWeight.Zero,
            matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_FOF_TICKERS.keys),
            children = fiiFoFTickerNodes,
        ),
    )

    internal val reitsNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.RV_REITS,
            displayName = "FIIs",
            targetWeight = TargetWeight.Fixed(33.33),
            matches = BalancingMatchers::isRealEstateFund,
            children = listOf(
                fiiRendNode,
                fiiTatNode,
                fiiFoFNode,
            ),
        ),
    )

    internal val rvNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.VARIABLE_INCOME,
            displayName = "Renda Variável",
            targetWeight = TargetWeight.Fixed(16.5),
            matches = BalancingMatchers::isVariableIncomeExcludingCrypto,
            children = listOf(
                nationalStocksNode,
                internationalNode,
                reitsNode,
            ),
        ),
    )

    private val cryptoNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.CRYPTO,
        displayName = "Cripto Ativos",
        targetWeight = TargetWeight.Fixed(1.0),
        matches = BalancingMatchers::isCrypto,
    )

    private val demaisInvestimentosNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.OTHER_INVESTMENTS,
        displayName = "Demais investimentos",
        targetWeight = TargetWeight.Zero,
        matches = { entry ->
            BalancingMatchers.isDemaisAmong(
                entry = entry,
                inUniverse = BalancingMatchers::isBalanceable,
                siblingMatchers = listOf(
                    BalancingMatchers::isCrypto,
                    BalancingMatchers::isFixedIncome,
                    BalancingMatchers::isVariableIncomeExcludingCrypto,
                ),
            )
        },
    )

    internal val balanceableNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.BALANCEABLE,
        displayName = "Carteira Balanceável",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isBalanceable,
        children = listOf(
            cryptoNode,
            rfNode,
            rvNode,
            demaisInvestimentosNode,
        ),
    )

    internal val carteiraTotalNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.PORTFOLIO_TOTAL,
        displayName = "Carteira Total",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::always,
        children = listOf(
            nonBalanceableNode,
            balanceableNode,
        ),
    )

    val root: BalancingTreeNode = carteiraTotalNode
}
