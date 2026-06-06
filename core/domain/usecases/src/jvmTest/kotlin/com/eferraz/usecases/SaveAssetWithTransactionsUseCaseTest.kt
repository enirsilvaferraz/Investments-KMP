package com.eferraz.usecases

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.usecases.cruds.UpsertAssetUseCase
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.AssetHoldingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
internal class SaveAssetWithTransactionsUseCaseTest {

    private val upsertAssetUseCase: UpsertAssetUseCase = mockk()
    private val assetHoldingRepository: AssetHoldingRepository = mockk(relaxed = true)
    private val useCase = SaveAssetWithTransactionsUseCase(
        upsertAssetUseCase = upsertAssetUseCase,
        assetHoldingRepository = assetHoldingRepository,
        context = Dispatchers.Unconfined,
    )

    private val issuer = Issuer(id = 3L, name = "Banco X", isInLiquidation = false)
    private val owner = Owner(id = 1L, name = "Titular")
    private val brokerage = Brokerage(id = 7L, name = "Corretora")
    private val futureDate = LocalDate(2030, 6, 1)

    private fun sampleAsset(id: Long = 1L) = FixedIncomeAsset(
        id = id,
        issuer = issuer,
        indexer = YieldIndexer.PRE_FIXED,
        type = FixedIncomeAssetType.CDB,
        expirationDate = futureDate,
        contractedYield = 1.0,
        liquidity = Liquidity.DAILY,
    )

    private fun sampleHolding(
        asset: FixedIncomeAsset = sampleAsset(),
        transactions: List<AssetTransaction> = emptyList(),
    ) = AssetHolding(
        id = 5L,
        asset = asset,
        owner = owner,
        brokerage = brokerage,
        transactions = transactions,
    )

    /**
     * Valid holding with transactions persists asset first then holding with transactions.
     */
    @Test
    fun `GIVEN valid holding WHEN save THEN upserts asset and holding`() = runTest {

        // GIVEN
        val transaction = AssetTransaction(
            id = 10L,
            date = LocalDate(2025, 1, 10),
            type = TransactionType.PURCHASE,
            quantity = 1.0,
            unitPrice = 1000.0,
        )
        val holding = sampleHolding(transactions = listOf(transaction))
        val savedAsset = sampleAsset(id = 99L)
        coEvery { upsertAssetUseCase(UpsertAssetUseCase.Param(holding.asset)) } returns Result.success(savedAsset)

        // WHEN
        useCase(SaveAssetWithTransactionsUseCase.Param(holding)).getOrThrow()

        // THEN
        coVerify(exactly = 1) { upsertAssetUseCase(UpsertAssetUseCase.Param(holding.asset)) }
        coVerify(exactly = 1) {
            assetHoldingRepository.upsertWithTransactions(holding.copy(asset = savedAsset))
        }
    }

    /**
     * Asset upsert failure prevents holding persistence.
     */
    @Test
    fun `GIVEN upsert asset fails WHEN save THEN holding not called`() = runTest {

        // GIVEN
        val holding = sampleHolding()
        coEvery { upsertAssetUseCase(UpsertAssetUseCase.Param(holding.asset)) } returns
            Result.failure(ValidateException(mapOf("issuer" to "Selecione um emissor")))

        // WHEN / THEN
        assertFailsWith<ValidateException> {
            useCase(SaveAssetWithTransactionsUseCase.Param(holding)).getOrThrow()
        }
        coVerify(exactly = 0) { assetHoldingRepository.upsertWithTransactions(any()) }
    }

    /**
     * Empty transaction list is a valid save.
     */
    @Test
    fun `GIVEN empty transactions WHEN save THEN succeeds`() = runTest {

        // GIVEN
        val holding = sampleHolding(transactions = emptyList())
        val savedAsset = sampleAsset(id = 99L)
        coEvery { upsertAssetUseCase(UpsertAssetUseCase.Param(holding.asset)) } returns Result.success(savedAsset)

        // WHEN
        useCase(SaveAssetWithTransactionsUseCase.Param(holding)).getOrThrow()

        // THEN
        coVerify(exactly = 1) {
            assetHoldingRepository.upsertWithTransactions(holding.copy(asset = savedAsset, transactions = emptyList()))
        }
    }

    /**
     * Final transaction list (after user removals) is passed to repository as-is.
     */
    @Test
    fun `GIVEN holding with removed transaction ids WHEN save THEN upsertWithTransactions receives final list`() = runTest {

        // GIVEN
        val remaining = AssetTransaction(
            id = 10L,
            date = LocalDate(2025, 1, 10),
            type = TransactionType.PURCHASE,
            quantity = 1.0,
            unitPrice = 1000.0,
        )
        val holding = sampleHolding(transactions = listOf(remaining))
        val savedAsset = sampleAsset(id = 99L)
        coEvery { upsertAssetUseCase(UpsertAssetUseCase.Param(holding.asset)) } returns Result.success(savedAsset)

        // WHEN
        useCase(SaveAssetWithTransactionsUseCase.Param(holding)).getOrThrow()

        // THEN
        coVerify(exactly = 1) {
            assetHoldingRepository.upsertWithTransactions(
                holding.copy(asset = savedAsset, transactions = listOf(remaining)),
            )
        }
    }
}
