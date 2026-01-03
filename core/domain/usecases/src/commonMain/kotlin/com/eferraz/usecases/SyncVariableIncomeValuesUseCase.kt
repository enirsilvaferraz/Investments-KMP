package com.eferraz.usecases

import com.eferraz.entities.StockQuoteHistory
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetRepository
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class SyncVariableIncomeValuesUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val assetRepository: AssetRepository,
) : AppUseCase<SyncVariableIncomeValuesUseCase.Param, Unit>() {

    public data class Param(val referenceDate: YearMonth)

    override suspend fun execute(param: Param) {

        assetHoldingRepository.getAllVariableIncomeAssets().forEach { holding ->

            try {

                val asset = holding.asset as? VariableIncomeAsset
                    ?: throw IllegalStateException("Asset is not a VariableIncomeAsset")

                // Buscar histórico existente do mês corrente (se houver) para preservar campos
                val existingHistory = holdingHistoryRepository.getByHoldingAndReferenceDate(param.referenceDate, holding)
                    ?: throw IllegalStateException("History not found for holding ${holding.id} and reference date ${param.referenceDate}")

                // Buscar cotação atual da BR API (sem histórico, para o dia de hoje)
                val quoteHistory = getQuotesUseCase(GetQuotesUseCase.Params(asset.ticker, null))
                    .getOrThrow()

                // Obter valor de fechamento (prioridade: close, fallback: adjustedClose)
                val endOfMonthValue = quoteHistory.close ?: quoteHistory.adjustedClose
                ?: throw IllegalStateException("Cotação não possui valor de fechamento disponível")

                // Criar/atualizar HoldingHistoryEntry
                val historyEntry = existingHistory.copy(endOfMonthValue = endOfMonthValue)

                // Fazer upsert no repositório
                holdingHistoryRepository.upsert(historyEntry)

                // Atualizar dados do asset (especialmente nome) se necessário
                updateName(quoteHistory, asset)

            } catch (e: Exception) {

                // Registrar erro mas continuar processando outros holdings
                println("Erro ao sincronizar valores para holding ${holding.id}: ${e.message}")
            }
        }
    }

    private suspend fun updateName(quoteHistory: StockQuoteHistory, asset: VariableIncomeAsset) {

        quoteHistory.companyName?.let { companyName ->

            // Verifica se o nome está vazio ou é igual ao ticker (indicando que não foi preenchido)
            if (asset.name.isBlank() || asset.name == asset.ticker) {
                val updatedAsset = asset.copy(name = companyName)
                assetRepository.save(updatedAsset)
            }
        }
    }
}