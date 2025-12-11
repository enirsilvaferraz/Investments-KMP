package com.eferraz.usecases.providers

import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Implementação de DateProvider que usa o sistema de relógio do sistema.
 */
@Factory(binds = [DateProvider::class])
@OptIn(ExperimentalTime::class)
public class SystemDateProvider : DateProvider {
    override fun getCurrentYearMonth(): YearMonth {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return YearMonth(now.year, now.month)
    }
}
