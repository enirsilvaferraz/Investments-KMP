package com.eferraz.database.core.initialization

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset

/**
 * Gera comandos SQL para popular o banco de dados com dados iniciais.
 * Os dados são extraídos de AssetInMemoryDataSource e convertidos em comandos INSERT.
 */
internal object DatabaseSeedData {

    /**
     * Gera todos os comandos SQL necessários para popular o banco de dados.
     * Retorna uma lista de comandos SQL ordenados:
     * 1. INSERTs para issuers
     * 2. INSERTs para assets (tabela base)
     * 3. INSERTs para subclasses (fixed_income_assets, variable_income_assets, investment_fund_assets)
     */
    fun generateSeedSQL(): List<String> {

        val assets = AssetInMemoryDataSourceImpl.getAll()
        val issuers = assets.map { it.issuer }.distinctBy { it.name }

        val sqlStatements = mutableListOf<String>()

        // Mapear issuer name para ID sequencial (começando em 1)
        val issuerIdMap = issuers.mapIndexed { index, issuer ->
            issuer.name to (index + 1L)
        }.toMap()

        // 1. Inserir issuers
        issuers.forEach { issuer ->
            sqlStatements.add("INSERT INTO issuers (name) VALUES (${escapeString(issuer.name)});")
        }

        // 2. Inserir assets e suas subclasses
        var assetId = 1L
        assets.forEach { asset ->
            val issuerId = issuerIdMap[asset.issuer.name] ?: error("Issuer não encontrado: ${asset.issuer.name}")

            // Inserir asset base
            val assetName = asset.name
            val observations = asset.observations?.let { escapeString(it) } ?: "NULL"
            val liquidity = when (asset) {
                is FixedIncomeAsset -> asset.liquidity.name
                is VariableIncomeAsset -> asset.liquidity.name
                is InvestmentFundAsset -> asset.liquidity.name
            }
            val category = when (asset) {
                is FixedIncomeAsset -> "FIXED_INCOME"
                is VariableIncomeAsset -> "VARIABLE_INCOME"
                is InvestmentFundAsset -> "INVESTMENT_FUND"
            }

            sqlStatements.add(
                "INSERT INTO assets (name, issuerId, category, liquidity, observations) " +
                        "VALUES (${escapeString(assetName)}, $issuerId, '$category', '${liquidity}', $observations);"
            )

            // Inserir subclasse específica
            when (asset) {
                is FixedIncomeAsset -> {
                    val cdiRelativeYield = asset.cdiRelativeYield?.toString() ?: "NULL"
                    sqlStatements.add(
                        "INSERT INTO fixed_income_assets (assetId, type, subType, expirationDate, contractedYield, cdiRelativeYield) " +
                                "VALUES ($assetId, '${asset.type.name}', '${asset.subType.name}', '${asset.expirationDate}', ${asset.contractedYield}, $cdiRelativeYield);"
                    )
                }

                is VariableIncomeAsset -> {
                    sqlStatements.add(
                        "INSERT INTO variable_income_assets (assetId, type, ticker) " +
                                "VALUES ($assetId, '${asset.type.name}', ${escapeString(asset.ticker)});"
                    )
                }

                is InvestmentFundAsset -> {
                    val expirationDate = asset.expirationDate?.toString()?.let { "'$it'" } ?: "NULL"
                    sqlStatements.add(
                        "INSERT INTO investment_fund_assets (assetId, type, liquidityDays, expirationDate) " +
                                "VALUES ($assetId, '${asset.type.name}', ${asset.liquidityDays}, $expirationDate);"
                    )
                }
            }

            assetId++
        }

        return sqlStatements
    }

    /**
     * Escapa strings para uso em SQL, substituindo aspas simples por duas aspas simples.
     */
    private fun escapeString(value: String): String {
        return "'${value.replace("'", "''")}'"
    }
}

