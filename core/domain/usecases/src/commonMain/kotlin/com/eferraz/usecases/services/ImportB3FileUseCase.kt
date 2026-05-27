package com.eferraz.usecases.services

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.B3ImportDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Imports B3 position data from a local XLSX file and syncs end-of-month values into history.
 */
@Factory
public class ImportB3FileUseCase(
    private val port: B3ImportDataSource,
    private val syncUseCase: SyncB3HistoryUseCase,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, Unit>(context) {

    override suspend fun execute(param: Unit) {
        val records = port.import().getOrThrow()
        syncUseCase(records).getOrThrow()
    }
}
