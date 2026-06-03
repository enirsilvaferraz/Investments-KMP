package com.eferraz.usecases.screens

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.SequenceMonths
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.repositories.DateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

/** Limite superior (inclusive) das opções do filtro «Vence até». */
public val MaturityFilterRangeEnd: YearMonth = YearMonth(2030, Month.DECEMBER)

/**
 * Meses consecutivos para o dropdown «Vence até», de [from] até [MaturityFilterRangeEnd].
 */
public fun maturityFilterMonthRange(from: YearMonth): List<YearMonth> =
    if (from > MaturityFilterRangeEnd) {
        emptyList()
    } else {
        SequenceMonths.build(from, MaturityFilterRangeEnd).entries
    }

/**
 * Opções do filtro «Vence até» a partir do mês corrente do sistema.
 */
@Factory
public class GetHistoryMaturityMonthsUseCase(
    private val dateProvider: DateProvider,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, List<YearMonth>>(context) {

    override suspend fun execute(param: Unit): List<YearMonth> =
        maturityFilterMonthRange(dateProvider.getCurrentYearMonth())
}

/** Meses de vencimento distintos (renda fixa) a partir das linhas de histórico. */
public fun List<HistoryTableData>.maturityMonthsFromHistory(): List<YearMonth> =
    mapNotNull { row ->
        (row as? FixedIncomeHistoryTableData)?.expirationDate?.let { date ->
            YearMonth(date.year, date.month)
        }
    }
        .distinct()
        .sortedWith(compareBy({ it.year }, { it.month }))
