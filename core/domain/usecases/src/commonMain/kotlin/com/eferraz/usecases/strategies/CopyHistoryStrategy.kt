package com.eferraz.usecases.strategies

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.GetQuotesUseCase
import com.eferraz.usecases.repositories.AssetRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

/**
 * Interface para estratégias de criação de histórico de posições de ativos.
 *
 * Cada estratégia implementa uma lógica específica baseada no tipo de ativo,
 * conforme descrito em [Regras de Negócio - Criar novo registro de histórico](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md#3-estratégias-por-tipo-de-ativo).
 */
public interface CopyHistoryStrategy {

    /**
     * Verifica se esta estratégia pode lidar com o tipo de ativo da posição.
     *
     * @param holding A posição de ativo a ser verificada
     * @return true se a estratégia pode lidar com este tipo de ativo, false caso contrário
     */
    public fun canHandle(holding: AssetHolding): Boolean

    /**
     * Cria um registro de histórico para a posição na data de referência especificada.
     *
     * @param referenceDate A data de referência (mês/ano) para o histórico
     * @param holding A posição de ativo
     * @return O registro de histórico criado, ou null se não for possível criar
     */
    public suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry?

    /**
     * Estratégia para criação de histórico de ativos de Renda Fixa e Fundos de Investimento.
     *
     * Esta estratégia copia todos os dados do histórico do mês anterior, conforme
     * [Regras de Negócio - Renda Fixa e Fundos](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md#31-renda-fixa-e-fundos).
     *
     * **Comportamento:**
     * - Busca o histórico do mês anterior
     * - Se encontrado, copia todos os dados para o novo mês
     * - Se não encontrado, retorna null (use case cria registro vazio)
     *
     * **Aplicável a:**
     * - [FixedIncomeAsset]
     * - [InvestmentFundAsset]
     */
    @Factory(binds = [CopyHistoryStrategy::class])
    public class FixedIncomeHistoryStrategy(
        private val holdingHistoryRepository: HoldingHistoryRepository,
    ) : CopyHistoryStrategy {

        override fun canHandle(holding: AssetHolding): Boolean =
            holding.asset is FixedIncomeAsset || holding.asset is InvestmentFundAsset

        override suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? =
            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)?.copy(id = null, referenceDate = referenceDate)
    }

    /**
     * Estratégia para criação de histórico de ativos de Renda Variável.
     *
     * Esta estratégia implementa a lógica de criação de histórico conforme
     * [Regras de Negócio - Renda Variável](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md#32-renda-variável).
     *
     * **Comportamento:**
     * - Se histórico existe para o mês: retorna o histórico existente (sem alterações)
     * - Se não existe: tenta copiar todos os dados do mês anterior (sem buscar API)
     * - Se não existe histórico anterior: busca cotação na API BR API e cria novo histórico
     * - Se não há cotação disponível: retorna null (use case cria registro vazio)
     *
     * **Aplicável a:**
     * - [VariableIncomeAsset] (ações, ETFs, etc.)
     */
    @Factory(binds = [CopyHistoryStrategy::class])
    public class VariableIncomeHistoryStrategy(
        private val holdingHistoryRepository: HoldingHistoryRepository,
        private val getQuotesUseCase: GetQuotesUseCase,
        private val assetRepository: AssetRepository,
    ) : CopyHistoryStrategy {

        override fun canHandle(holding: AssetHolding): Boolean =
            holding.asset is VariableIncomeAsset

        override suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {

            val asset = holding.asset as? VariableIncomeAsset
                ?: throw IllegalStateException("Asset is not a VariableIncomeAsset")

            // Se já existe histórico no mês atual, retorna sem alterações
            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding)?.let {
                return it
            }

            // Tenta copiar do mês anterior (sem buscar API)
            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)?.let { previousHistory ->
                return HoldingHistoryEntry(
                    holding = holding,
                    referenceDate = referenceDate,
                    endOfMonthValue = previousHistory.endOfMonthValue,
                    endOfMonthQuantity = previousHistory.endOfMonthQuantity
                )
            }

            // Se não há histórico anterior, busca cotação na API BR API
            return try {
                val quoteHistory = getQuotesUseCase(GetQuotesUseCase.Params(asset.ticker, referenceDate))
                    .getOrThrow()

                // Preencher nome automaticamente se ainda não foi preenchido
                quoteHistory.companyName?.let { companyName ->
                    // Verifica se o nome está vazio ou é igual ao ticker (indicando que não foi preenchido)
                    if (asset.name.isBlank() || asset.name == asset.ticker) {
                        val updatedAsset = asset.copy(name = companyName)
                        assetRepository.upsert(updatedAsset)
                    }
                }

                val endOfMonthValue = (quoteHistory.close ?: quoteHistory.adjustedClose)
                    ?: return null

                HoldingHistoryEntry(
                    holding = holding,
                    referenceDate = referenceDate,
                    endOfMonthValue = endOfMonthValue,
                    endOfMonthQuantity = 1.0 // Valor padrão quando não há histórico anterior
                )
            } catch (e: Exception) {
                println(e.message)
                // Se a API falhar, retorna null (use case criará registro vazio)
                null
            }
        }
    }
}