package com.eferraz.usecases.balancing

import com.eferraz.entities.assets.YieldIndexer

internal object PortfolioBalancingCatalog {

    private val targetWeightPercents: Map<String, Double> = mapOf(

        // Carteira Balanceável
        BalancingGroupId.CRYPTO to 1.5,
        BalancingGroupId.FIXED_INCOME to 79.0,
        BalancingGroupId.VARIABLE_INCOME to 19.5,

        // Renda Fixa
        BalancingGroupId.RF_POST_FIXED to 33.33,
        BalancingGroupId.RF_PRE_FIXED to 33.33,
        BalancingGroupId.RF_INFLATION_LINKED to 33.33,

        // Renda Variavel
        BalancingGroupId.RV_NATIONAL_STOCKS to 35.0,
        BalancingGroupId.RV_INTERNATIONAL to 35.0,
        BalancingGroupId.RV_REITS to 30.0,

        // FII
        BalancingGroupId.FII_REND to 70.0,
        BalancingGroupId.FII_TAT to 30.0,
    )

    private fun fixedWeight(id: String): TargetWeight.Fixed =
        TargetWeight.Fixed(targetWeightPercents.getValue(id))

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
        targetWeight = fixedWeight(BalancingGroupId.RF_PRE_FIXED),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.PRE_FIXED),
    )

    private val rfPostNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RF_POST_FIXED,
        displayName = "Pós-fixados",
        targetWeight = fixedWeight(BalancingGroupId.RF_POST_FIXED),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.POST_FIXED),
    )

    private val rfIpcaNode: BalancingTreeNode = BalancingTreeNode(
        id = BalancingGroupId.RF_INFLATION_LINKED,
        displayName = "Atrelado a inflação",
        targetWeight = fixedWeight(BalancingGroupId.RF_INFLATION_LINKED),
        matches = BalancingMatchers.isFixedIncomeWithIndexer(YieldIndexer.INFLATION_LINKED),
    )

    internal val rfNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FIXED_INCOME,
            displayName = "Renda Fixa",
            targetWeight = fixedWeight(BalancingGroupId.FIXED_INCOME),
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
            targetWeight = fixedWeight(BalancingGroupId.RV_NATIONAL_STOCKS),
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
        targetWeight = fixedWeight(BalancingGroupId.RV_INTERNATIONAL),
        matches = BalancingMatchers::isInternationalStock,
    )

    private val fiiRendNode: BalancingTreeNode = BalancingTreeNodeFactory.withDemaisInvestimentos(
        node = BalancingTreeNode(
            id = BalancingGroupId.FII_REND,
            displayName = "FII - Renda",
            targetWeight = fixedWeight(BalancingGroupId.FII_REND),
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
            targetWeight = fixedWeight(BalancingGroupId.FII_TAT),
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
            targetWeight = fixedWeight(BalancingGroupId.RV_REITS),
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
            targetWeight = fixedWeight(BalancingGroupId.VARIABLE_INCOME),
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
        targetWeight = fixedWeight(BalancingGroupId.CRYPTO),
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
