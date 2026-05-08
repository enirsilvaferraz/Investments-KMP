package com.eferraz.usecases.cruds

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.DateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class GetCurrentDateUseCase(
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, YearMonth>(context) {

    override suspend fun execute(param: Unit): YearMonth =
        dateProvider.getCurrentYearMonth()
}