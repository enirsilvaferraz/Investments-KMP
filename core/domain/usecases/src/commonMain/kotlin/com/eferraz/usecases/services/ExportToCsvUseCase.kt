package com.eferraz.usecases.services

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.DateProvider
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Exporta posições de renda fixa em CSV para o período retornado por [DateProvider.getCurrentYearMonth],
 * carregando os registros via [HoldingHistoryRepository.getByReferenceDate].
 */
@Factory
public class ExportToCsvUseCase(
    private val repository: HoldingHistoryRepository,
    private val dateProvider: DateProvider,
    private val context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, Unit>(context) {

    override suspend fun execute(param: Unit) {
        val period = dateProvider.getCurrentYearMonth()
        val entries = repository.getByReferenceDate(period)
        val fixedIncomeOnly = entries.filter { it.holding.asset is FixedIncomeAsset }
        repository.exportToCSV(fixedIncomeOnly)
    }
}
