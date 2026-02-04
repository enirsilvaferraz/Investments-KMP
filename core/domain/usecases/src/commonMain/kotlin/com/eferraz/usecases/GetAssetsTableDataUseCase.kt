package com.eferraz.usecases

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.cruds.GetAssetsUseCase
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
        // Buscar assets e mapeamento de corretoras e metas
        val assets = getAssetsUseCase(GetAssetsUseCase.ByCategory(param.category))
            .getOrNull() ?: emptyList()
        
        // Buscar todos os holdings de uma vez e criar mapas
        // Se um asset tiver múltiplos holdings, mantém o primeiro encontrado
        val holdings = assetHoldingRepository.getAll().associateBy { it.asset.id }
        val assetBrokeragesMap = holdings.mapValues { it.value.brokerage }
        val assetGoalsMap = holdings.mapValues { it.value.goal?.name ?: "" }
        val assetGoalIdsMap = holdings.mapValues { it.value.goal?.id }

        // Converter cada asset para o formato de tabela
        return when (param.category) {
            InvestmentCategory.FIXED_INCOME -> {
                assets.filterIsInstance<FixedIncomeAsset>().map { asset ->
                    val brokerage = assetBrokeragesMap[asset.id]
                    val goalName = assetGoalsMap[asset.id] ?: ""
                    val goalId = assetGoalIdsMap[asset.id]
                    FixedIncomeAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        goalName = goalName,
                        goalId = goalId,
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
                    val goalName = assetGoalsMap[asset.id] ?: ""
                    val goalId = assetGoalIdsMap[asset.id]
                    VariableIncomeAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        goalName = goalName,
                        goalId = goalId,
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
                    val goalName = assetGoalsMap[asset.id] ?: ""
                    val goalId = assetGoalIdsMap[asset.id]
                    InvestmentFundAssetsTableData(
                        assetId = asset.id,
                        brokerageName = brokerage?.name ?: "",
                        brokerageId = brokerage?.id,
                        goalName = goalName,
                        goalId = goalId,
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

