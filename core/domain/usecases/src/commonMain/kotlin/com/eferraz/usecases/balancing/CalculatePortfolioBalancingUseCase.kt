package com.eferraz.usecases.balancing

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.cruds.GetHoldingHistoriesUseCase
import com.eferraz.usecases.repositories.DateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class CalculatePortfolioBalancingUseCase(
    private val dateProvider: DateProvider,
    private val getHoldingHistoriesUseCase: GetHoldingHistoriesUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, PortfolioBalancingReport>(context) {

    override suspend fun execute(param: Unit): PortfolioBalancingReport {
        val period = dateProvider.getCurrentYearMonth()
        val entries = getHoldingHistoriesUseCase(GetHoldingHistoriesUseCase.ByReferenceDate(period)).getOrThrow()
        return PortfolioBalancingEngine.calculate(entries, period)
    }
}
