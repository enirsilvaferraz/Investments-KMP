package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.YieldIndexer

internal object PortfolioBalancingCatalog {

    private val previdenciaNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.PENSION_FUNDS,
        displayName = "Fundos de Previdência",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isPensionFund,
    )

    private val fgtsEletrobrasNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.FGTS_FUNDS,
        displayName = "Fundos do FGTS (Eletrobrás)",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isFGTSFund,
    )

    private val fgtsAccountNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.FGTS_ACCOUNT,
        displayName = "Conta FGTS",
        targetWeight = TargetWeight.Dynamic,
        matches = BalancingMatchers::isFGTSAccount,
    )

    internal val nonBalanceableNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.NON_BALANCEABLE,
            displayName = "Carteira Não Balanceável",
            targetWeight = TargetWeight.Dynamic,
            matches = BalancingMatchers::isNonBalanceable,
            children = listOf(
                previdenciaNode,
                fgtsAccountNode,
                fgtsEletrobrasNode
            ),
        )
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
            targetWeight = TargetWeight.Fixed(80.0),
            matches = BalancingMatchers::isFixedIncome,
            children = listOf(
                rfPostNode,
                rfPreNode,
                rfIpcaNode,
            ),
        ),
    )

    internal val nationalStocksNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.RV_NATIONAL_STOCKS,
            displayName = "Ações Nacionais",
            targetWeight = TargetWeight.Fixed(33.33),
            matches = BalancingMatchers.isNationalStock(),
            children = BalancingConstants.NATIONAL_STOCK_TICKERS.map { (ticker, weight) ->
                BalancingTreeNode(
                    id = ticker,
                    displayName = ticker,
                    targetWeight = TargetWeight.Fixed(weight),
                    matches = BalancingMatchers.isNationalStockWithTickerIn(setOf(ticker)),
                )
            },
        ),
    )

    private val internationalNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RV_INTERNATIONAL,
        displayName = "Ações Internacionais",
        targetWeight = TargetWeight.Fixed(33.33),
        matches = BalancingMatchers::isInternationalStock,
    )

    private val fiiRendNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_REND,
            displayName = "FII - Renda%",
            targetWeight = TargetWeight.Fixed(70.0),
            matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_REND_TICKERS.keys),
            children = BalancingConstants.FII_REND_TICKERS.map { (ticker, weight) ->
                BalancingTreeNode(
                    id = ticker,
                    displayName = ticker,
                    targetWeight = TargetWeight.Fixed(weight),
                    matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
                )
            },
        ),
    )

    private val fiiTatNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_TAT,
            displayName = "FII - Tatica",
            targetWeight = TargetWeight.Fixed(30.0),
            matches = BalancingMatchers.isRealEstateFundWithTickerIn(BalancingConstants.FII_TAT_TICKERS.keys),
            children = BalancingConstants.FII_TAT_TICKERS.map { (ticker, weight) ->
                BalancingTreeNode(
                    id = ticker,
                    displayName = ticker,
                    targetWeight = TargetWeight.Fixed(weight),
                    matches = BalancingMatchers.isRealEstateFundWithTickerIn(setOf(ticker)),
                )
            },
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
            ),
        ),
    )

    internal val rvNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.VARIABLE_INCOME,
            displayName = "Renda Variável",
            targetWeight = TargetWeight.Fixed(18.5),
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
        targetWeight = TargetWeight.Fixed(1.5),
        matches = BalancingMatchers::isCrypto,
    )

    internal val balanceableNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.BALANCEABLE,
            displayName = "Carteira Balanceável",
            targetWeight = TargetWeight.Dynamic,
            matches = BalancingMatchers::isBalanceable,
            children = listOf(
                cryptoNode,
                rfNode,
                rvNode
            ),
        )
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
