package com.eferraz.usecases

import com.eferraz.usecases.providers.DateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth
import org.koin.core.annotation.Factory

@Factory
public class GetDataPeriodUseCase(
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, List<YearMonth>>(context) {

    override suspend fun execute(param: Unit): List<YearMonth> {

        val currentMonth = dateProvider.getCurrentYearMonth()

        return generateSequence(start) { it.plusMonth() }
            .takeWhile { it <= currentMonth }
            .toList()
    }

    private companion object {
        val start = YearMonth(2025, 11)
    }
}