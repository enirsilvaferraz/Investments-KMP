package com.eferraz.usecases

import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.repositories.AssetTransactionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteTransactionUseCaseTest {

    /**
     * Delete delegates holding-scoped id removal to the repository port.
     */
    @Test
    fun `GIVEN holding and transaction id WHEN delete THEN repository delete uses holding`() = runTest {

        // GIVEN
        val holding = createAssetHolding()
        val repository = mockk<AssetTransactionRepository>(relaxed = true)
        val useCase = DeleteTransactionUseCase(repository, Dispatchers.Unconfined)

        // WHEN
        useCase(DeleteTransactionUseCase.Param(holding, id = 7L)).getOrThrow()

        // THEN
        coVerify(exactly = 1) { repository.delete(holding, 7L) }
    }
}
