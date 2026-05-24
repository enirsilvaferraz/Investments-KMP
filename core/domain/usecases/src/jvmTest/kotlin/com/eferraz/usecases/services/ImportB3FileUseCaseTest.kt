package com.eferraz.usecases.services

import com.eferraz.usecases.repositories.B3ImportDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
public class ImportB3FileUseCaseTest {

    /**
     * Port completes successfully → use case returns success.
     */
    @Test
    public fun `GIVEN port returns success WHEN import with Unit THEN completes without error`() = runTest {

        // GIVEN
        val port = mockk<B3ImportDataSource>()
        coEvery { port.importAndLog() } returns Result.success(Unit)
        val useCase = ImportB3FileUseCase(
            port = port,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(Unit)

        // THEN
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { port.importAndLog() }
    }

    /**
     * Port returns Result.failure → use case propagates the failure.
     */
    @Test
    public fun `GIVEN port returns failure WHEN import with Unit THEN use case fails`() = runTest {

        // GIVEN
        val expectedError = IllegalStateException("import failed")
        val port = mockk<B3ImportDataSource>()
        coEvery { port.importAndLog() } returns Result.failure(expectedError)
        val useCase = ImportB3FileUseCase(
            port = port,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(Unit)

        // THEN
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("import failed", error.message)
        coVerify(exactly = 1) { port.importAndLog() }
    }

    /**
     * Port delays longer than 30 s → TimeoutCancellationException is thrown.
     */
    @Test
    public fun `GIVEN port delays over 30 seconds WHEN import with Unit THEN throws TimeoutCancellationException`() = runTest {

        // GIVEN
        val port = mockk<B3ImportDataSource>()
        coEvery { port.importAndLog() } coAnswers {
            delay(31_000L)
            Result.success(Unit)
        }
        val useCase = ImportB3FileUseCase(
            port = port,
            context = Dispatchers.Unconfined,
        )

        // WHEN
        val result = useCase(Unit)

        // THEN
        assertTrue(result.isFailure)
        assertFailsWith<TimeoutCancellationException> { result.getOrThrow() }
        coVerify(exactly = 1) { port.importAndLog() }
    }
}
