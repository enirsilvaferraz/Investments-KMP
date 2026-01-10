package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.entities.AssetsTableData
import com.eferraz.usecases.entities.FixedIncomeAssetsTableData
import com.eferraz.usecases.entities.InvestmentFundAssetsTableData
import com.eferraz.usecases.entities.VariableIncomeAssetsTableData
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Use case responsável por obter os dados da tabela de assets para exibição na tela.
 * 
 * Retorna uma lista de [AssetsTableData], onde cada item representa uma linha da tabela
 * com dados primitivos, enums e LocalDate para formatação na view.
 */
@Factory
public class GetAssetsTableDataUseCase(
    private val getAssetsUseCase: GetAssetsUseCase,
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetAssetsTableDataUseCase.Param, List<AssetsTableData>>(context) {

    public data class Param(val category: InvestmentCategory)

    override suspend fun execute(param: Param): List<AssetsTableData> {
        // Buscar assets e mapeamento de corretoras
        val assets = getAssetsUseCase(GetAssetsUseCase.ByCategory(param.category))
            .getOrNull() ?: emptyList()
        
        val assetBrokeragesMap = assetHoldingRepository.getAll()
            .associate { it.asset.id to it.brokerage }

        // Converter cada asset para o formato de tabela
        return when (param.category) {
            InvestmentCategory.FIXED_INCOME -> {
                assets.filterIsInstance<FixedIncomeAsset>().map { asset ->
                    val brokerage = assetBrokeragesMap[asset.id]
                    FixedIncomeAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        subType = asset.subType,
                        type = asset.type,
                        expirationDate = asset.expirationDate,
                        contractedYield = asset.contractedYield,
                        cdiRelativeYield = asset.cdiRelativeYield,
                        issuerName = asset.issuer.name,
                        issuerId = asset.issuer.id,
                        liquidity = asset.liquidity,
                        observations = asset.observations ?: ""
                    )
                }
            }
            InvestmentCategory.VARIABLE_INCOME -> {
                assets.filterIsInstance<VariableIncomeAsset>().map { asset ->
                    val brokerage = assetBrokeragesMap[asset.id]
                    VariableIncomeAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        type = asset.type,
                        ticker = asset.ticker,
                        cnpj = asset.cnpj?.get() ?: "",
                        name = asset.name,
                        issuerName = asset.issuer.name,
                        issuerId = asset.issuer.id,
                        observations = asset.observations ?: ""
                    )
                }
            }
            InvestmentCategory.INVESTMENT_FUND -> {
                assets.filterIsInstance<InvestmentFundAsset>().map { asset ->
                    val brokerage = assetBrokeragesMap[asset.id]
                    InvestmentFundAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        type = asset.type,
                        name = asset.name,
                        liquidity = asset.liquidity,
                        liquidityDays = asset.liquidityDays,
                        expirationDate = asset.expirationDate,
                        issuerName = asset.issuer.name,
                        issuerId = asset.issuer.id,
                        observations = asset.observations ?: ""
                    )
                }
            }
        }
    }
}

