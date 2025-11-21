package com.eferraz.database.core.initialization

import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset

/**
 * Gera comandos SQL para popular o banco de dados com dados iniciais.
 * Os dados são extraídos de AssetInMemoryDataSource e convertidos em comandos INSERT.
 */
internal object DatabaseSeedData {

    private const val DEFAULT_QUANTITY = 0.0
    private const val DEFAULT_COST = 0.0
    private const val DEFAULT_VALUE = 0.0
    private const val INITIAL_ID = 1L

    /**
     * Gera todos os comandos SQL necessários para popular o banco de dados a partir de uma lista de AssetHolding.
     * Retorna uma lista de comandos SQL ordenados:
     * 1. INSERTs para issuers
     * 2. INSERTs para owners
     * 3. INSERTs para brokerages
     * 4. INSERTs para assets (tabela base)
     * 5. INSERTs para subclasses (fixed_income_assets, variable_income_assets, investment_fund_assets)
     * 6. INSERTs para asset_holdings
     */
    fun generateSQLFromAssetHoldings(holdings: List<AssetHolding> = AssetInMemoryDataSourceImpl.getAllAssets()): List<String> {

        val sqlStatements = mutableListOf<String>()

        // Extrair e mapear entidades únicas
        val uniqueEntities = extractUniqueEntities(holdings)
        val idMappings = createIdMappings(uniqueEntities)

        // Gerar INSERTs na ordem correta (respeitando foreign keys)
        sqlStatements.addAll(generateIssuerInserts(uniqueEntities.issuers))
        sqlStatements.addAll(generateOwnerInserts(uniqueEntities.owners))
        sqlStatements.addAll(generateBrokerageInserts(uniqueEntities.brokerages))
        sqlStatements.addAll(generateAssetInserts(holdings = holdings, issuerIdMap = idMappings.issuerIdMap, assetIdMap = idMappings.assetIdMap))
        sqlStatements.addAll(generateAssetHoldingInserts(holdings = holdings, assetIdMap = idMappings.assetIdMap, ownerIdMap = idMappings.ownerIdMap, brokerageIdMap = idMappings.brokerageIdMap))

        return sqlStatements
    }

    /**
     * Extrai entidades únicas dos holdings.
     */
    private fun extractUniqueEntities(holdings: List<AssetHolding>): UniqueEntities {
        return UniqueEntities(
            issuers = holdings.map { it.asset.issuer }.distinctBy { it.name },
            owners = holdings.map { it.owner }.distinctBy { it.name },
            brokerages = holdings.map { it.brokerage }.distinctBy { it.name }
        )
    }

    /**
     * Cria mapeamentos de nomes para IDs sequenciais.
     */
    private fun createIdMappings(entities: UniqueEntities): IdMappings {
        return IdMappings(
            issuerIdMap = entities.issuers.mapIndexed { index, issuer -> issuer.name to (index + INITIAL_ID) }.toMap(),
            ownerIdMap = entities.owners.mapIndexed { index, owner -> owner.name to (index + INITIAL_ID) }.toMap(),
            brokerageIdMap = entities.brokerages.mapIndexed { index, brokerage -> brokerage.name to (index + INITIAL_ID) }.toMap(),
            assetIdMap = mutableMapOf()
        )
    }

    /**
     * Gera INSERTs para a tabela issuers.
     */
    private fun generateIssuerInserts(issuers: List<com.eferraz.entities.Issuer>): List<String> {
        return issuers.map { issuer ->
            "INSERT INTO issuers (name) VALUES (${escapeString(issuer.name)});"
        }
    }

    /**
     * Gera INSERTs para a tabela owners.
     */
    private fun generateOwnerInserts(owners: List<com.eferraz.entities.Owner>): List<String> {
        return owners.map { owner ->
            "INSERT INTO owners (name) VALUES (${escapeString(owner.name)});"
        }
    }

    /**
     * Gera INSERTs para a tabela brokerages.
     */
    private fun generateBrokerageInserts(brokerages: List<com.eferraz.entities.Brokerage>): List<String> {
        return brokerages.map { brokerage ->
            "INSERT INTO brokerages (name) VALUES (${escapeString(brokerage.name)});"
        }
    }

    /**
     * Gera INSERTs para assets (tabela base e subclasses).
     */
    private fun generateAssetInserts(
        holdings: List<AssetHolding>,
        issuerIdMap: Map<String, Long>,
        assetIdMap: MutableMap<Asset, Long>
    ): List<String> {

        val sqlStatements = mutableListOf<String>()
        var currentAssetId = INITIAL_ID

        holdings.forEach { holding ->

            val asset = holding.asset

            // Evitar duplicar assets (mesmo asset pode aparecer em múltiplos holdings)
            if (assetIdMap.containsKey(asset)) { return@forEach }

            val issuerId = issuerIdMap[asset.issuer.name] ?: error("Issuer não encontrado: ${asset.issuer.name}")

            // Inserir asset base
            sqlStatements.add(buildAssetBaseInsert(asset, issuerId))

            // Inserir subclasse específica
            sqlStatements.add(buildAssetSubclassInsert(asset, currentAssetId))

            assetIdMap[asset] = currentAssetId

            currentAssetId++
        }

        return sqlStatements
    }

    /**
     * Gera INSERT para a tabela base assets.
     */
    private fun buildAssetBaseInsert(asset: Asset, issuerId: Long): String {
        val assetName = asset.name
        val observations = asset.observations?.let { escapeString(it) } ?: "NULL"
        val liquidity = extractLiquidity(asset)
        val category = extractCategory(asset)

        return "INSERT INTO assets (name, issuerId, category, liquidity, observations) " +
                "VALUES (${escapeString(assetName)}, $issuerId, '$category', '${liquidity}', $observations);"
    }

    /**
     * Gera INSERT para a subclasse específica do asset.
     */
    private fun buildAssetSubclassInsert(asset: Asset, assetId: Long): String {
        return when (asset) {
            is FixedIncomeAsset -> buildFixedIncomeAssetInsert(asset, assetId)
            is VariableIncomeAsset -> buildVariableIncomeAssetInsert(asset, assetId)
            is InvestmentFundAsset -> buildInvestmentFundAssetInsert(asset, assetId)
        }
    }

    /**
     * Gera INSERT para fixed_income_assets.
     */
    private fun buildFixedIncomeAssetInsert(asset: FixedIncomeAsset, assetId: Long): String {
        val cdiRelativeYield = asset.cdiRelativeYield?.toString() ?: "NULL"
        return "INSERT INTO fixed_income_assets (assetId, type, subType, expirationDate, contractedYield, cdiRelativeYield) " +
                "VALUES ($assetId, '${asset.type.name}', '${asset.subType.name}', '${asset.expirationDate}', ${asset.contractedYield}, $cdiRelativeYield);"
    }

    /**
     * Gera INSERT para variable_income_assets.
     */
    private fun buildVariableIncomeAssetInsert(asset: VariableIncomeAsset, assetId: Long): String {
        return "INSERT INTO variable_income_assets (assetId, type, ticker) " +
                "VALUES ($assetId, '${asset.type.name}', ${escapeString(asset.ticker)});"
    }

    /**
     * Gera INSERT para investment_fund_assets.
     */
    private fun buildInvestmentFundAssetInsert(asset: InvestmentFundAsset, assetId: Long): String {
        val expirationDate = asset.expirationDate?.toString()?.let { "'$it'" } ?: "NULL"
        return "INSERT INTO investment_fund_assets (assetId, type, liquidityDays, expirationDate) " +
                "VALUES ($assetId, '${asset.type.name}', ${asset.liquidityDays}, $expirationDate);"
    }

    /**
     * Gera INSERTs para asset_holdings.
     */
    private fun generateAssetHoldingInserts(
        holdings: List<AssetHolding>,
        assetIdMap: Map<Asset, Long>,
        ownerIdMap: Map<String, Long>,
        brokerageIdMap: Map<String, Long>
    ): List<String> {

        return holdings.map { holding ->

            val assetId = assetIdMap[holding.asset] ?: error("Asset não encontrado no mapeamento")
            val ownerId = ownerIdMap[holding.owner.name] ?: error("Owner não encontrado: ${holding.owner.name}")
            val brokerageId = brokerageIdMap[holding.brokerage.name] ?: error("Brokerage não encontrado: ${holding.brokerage.name}")

            // Como AssetHolding não tem quantity, averageCost, etc., usamos valores padrão
            buildAssetHoldingInsert(
                assetId = assetId,
                ownerId = ownerId,
                brokerageId = brokerageId,
                quantity = DEFAULT_QUANTITY,
                averageCost = DEFAULT_COST,
                investedValue = DEFAULT_VALUE,
                currentValue = DEFAULT_VALUE
            )
        }
    }

    /**
     * Constrói um INSERT para asset_holdings.
     */
    private fun buildAssetHoldingInsert(
        assetId: Long,
        ownerId: Long,
        brokerageId: Long,
        quantity: Double,
        averageCost: Double,
        investedValue: Double,
        currentValue: Double
    ): String {
        return "INSERT INTO asset_holdings (assetId, ownerId, brokerageId, quantity, averageCost, investedValue, currentValue) " +
                "VALUES ($assetId, $ownerId, $brokerageId, $quantity, $averageCost, $investedValue, $currentValue);"
    }

    /**
     * Extrai a liquidez do asset.
     */
    private fun extractLiquidity(asset: Asset): String {
        return when (asset) {
            is FixedIncomeAsset -> asset.liquidity.name
            is VariableIncomeAsset -> asset.liquidity.name
            is InvestmentFundAsset -> asset.liquidity.name
        }
    }

    /**
     * Extrai a categoria do asset.
     */
    private fun extractCategory(asset: Asset): String {
        return when (asset) {
            is FixedIncomeAsset -> "FIXED_INCOME"
            is VariableIncomeAsset -> "VARIABLE_INCOME"
            is InvestmentFundAsset -> "INVESTMENT_FUND"
        }
    }

    /**
     * Escapa strings para uso em SQL, substituindo aspas simples por duas aspas simples.
     */
    private fun escapeString(value: String): String {
        return "'${value.replace("'", "''")}'"
    }

    /**
     * Agrupa entidades únicas extraídas dos holdings.
     */
    private data class UniqueEntities(
        val issuers: List<com.eferraz.entities.Issuer>,
        val owners: List<com.eferraz.entities.Owner>,
        val brokerages: List<com.eferraz.entities.Brokerage>
    )

    /**
     * Agrupa mapeamentos de IDs para as entidades.
     */
    private data class IdMappings(
        val issuerIdMap: Map<String, Long>,
        val ownerIdMap: Map<String, Long>,
        val brokerageIdMap: Map<String, Long>,
        val assetIdMap: MutableMap<Asset, Long>
    )
}

