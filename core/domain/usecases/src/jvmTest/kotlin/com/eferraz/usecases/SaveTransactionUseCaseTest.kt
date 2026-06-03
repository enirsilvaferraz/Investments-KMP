package com.eferraz.usecases

import com.eferraz.entities.transactions.FixedIncomeTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.repositories.AssetTransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SaveTransactionUseCaseTest {

    /**
     * Save delegates upsert with holding and transaction to the repository port.
     */
    @Test
    fun `GIVEN holding and transaction WHEN save THEN upsert uses both in param`() = runTest {

        // GIVEN
        val holding = createAssetHolding(asset = createFixedIncomeAsset())
        val transaction = FixedIncomeTransaction(
            id = 0L,
            date = LocalDate(2025, 1, 10),
            type = TransactionType.PURCHASE,
            totalValue = 1000.0,
        )
        val repository = mockk<AssetTransactionRepository>()
        coEvery { repository.upsert(holding, transaction) } returns 42L
        val useCase = SaveTransactionUseCase(repository, Dispatchers.Unconfined)

        // WHEN
        val id = useCase(SaveTransactionUseCase.Param(holding, transaction)).getOrThrow()

        // THEN
        assertEquals(42L, id)
        coVerify(exactly = 1) { repository.upsert(holding, transaction) }
    }
}
