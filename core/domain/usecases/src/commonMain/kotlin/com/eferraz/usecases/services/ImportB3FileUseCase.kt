package com.eferraz.usecases.services

import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.B3ImportDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Factory

/**
 * Imports B3 position data from a local XLSX file and logs tabular output to the console.
 */
@Factory
public class ImportB3FileUseCase(
    private val port: B3ImportDataSource,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, Unit>(context) {

    override suspend fun execute(param: Unit) {
        try {
            withTimeout(30_000L) {
                port.importAndLog().getOrThrow()
            }
        } catch (e: TimeoutCancellationException) {
            println("TIMEOUT: Processamento cancelado — tempo limite de 30 s excedido")
            throw e
        }
    }
}
