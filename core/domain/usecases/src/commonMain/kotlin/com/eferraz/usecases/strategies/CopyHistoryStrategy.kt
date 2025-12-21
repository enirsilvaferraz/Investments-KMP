package com.eferraz.usecases.strategies

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.GetQuotesUseCase
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
            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)
    }

    /**
     * Estratégia para criação de histórico de ativos de Renda Variável.
     *
     * Esta estratégia busca a cotação do mês e atualiza o valor de fechamento,
     * mantendo os demais dados, conforme
     * [Regras de Negócio - Renda Variável](../../../../../../../../../docs/RN%20-%20Criar%20novo%20registro%20de%20histórico.md#32-renda-variável).
     *
     * **Comportamento:**
     * - Busca cotação do mês de referência
     * - Extrai valor de fechamento (prioriza `close`, fallback para `adjustedClose`)
     * - Se histórico existe para o mês: atualiza apenas valor de fechamento
     * - Se não existe: copia dados do mês anterior e atualiza valor de fechamento
     * - Se não há cotação ou histórico anterior: retorna null (use case cria registro vazio)
     *
     * **Aplicável a:**
     * - [VariableIncomeAsset] (ações, ETFs, etc.)
     */
    @Factory(binds = [CopyHistoryStrategy::class])
    public class VariableIncomeHistoryStrategy(
        private val holdingHistoryRepository: HoldingHistoryRepository,
        private val getQuotesUseCase: GetQuotesUseCase,
    ) : CopyHistoryStrategy {

        override fun canHandle(holding: AssetHolding): Boolean =
            holding.asset is VariableIncomeAsset

        override suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {

            val asset = holding.asset as? VariableIncomeAsset
                ?: throw IllegalStateException("Asset is not a VariableIncomeAsset")

            val quoteHistory = getQuotesUseCase(GetQuotesUseCase.Params(asset.ticker, referenceDate))
                .getOrThrow()

            val endOfMonthValue = (quoteHistory.close ?: quoteHistory.adjustedClose)
                ?: throw IllegalStateException("Quote history is missing close or adjustedClose")

            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding)?.let {
                return it.copy(endOfMonthValue = endOfMonthValue)
            }

            return holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)?.let {
                HoldingHistoryEntry(holding = holding, referenceDate = referenceDate, endOfMonthValue = endOfMonthValue, endOfMonthQuantity = it.endOfMonthQuantity)
            }
        }
    }
}