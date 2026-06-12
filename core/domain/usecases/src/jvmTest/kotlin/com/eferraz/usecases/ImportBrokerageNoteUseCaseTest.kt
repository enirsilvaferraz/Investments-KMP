package com.eferraz.usecases

import com.eferraz.entities.brokeragenotes.BrokerageNote
import com.eferraz.entities.brokeragenotes.BrokerageNoteAsset
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.BrokerageNoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ImportBrokerageNoteUseCaseTest {

    /**
     * Valid note with resolvable holdings persists all transactions with allocated fees.
     */
    @Test
    fun `GIVEN valid note and holdings WHEN import THEN saveAll persists all transactions`() = runTest {

        // GIVEN
        val purchase = AssetTransaction(
            id = 0L,
            date = LocalDate(2026, 1, 1),
            type = TransactionType.PURCHASE,
            quantity = 100.0,
            unitPrice = 10.0,
        )
        val sale = AssetTransaction(
            id = 0L,
            date = LocalDate(2026, 1, 1),
            type = TransactionType.SALE,
            quantity = 10.0,
            unitPrice = 100.0,
        )
        val note = BrokerageNote(
            totalVolumeTraded = 2000.0,
            apportionableFees = 4.54,
            withheldTaxes = 0.0,
            netValue = -1004.54,
            assets = listOf(
                BrokerageNoteAsset("AJFI11", purchase),
                BrokerageNoteAsset("BRCO11", sale),
                BrokerageNoteAsset("VILG11", purchase.copy(quantity = 1000.0, unitPrice = 1.0)),
            ),
        )
        val holdingA = mockk<com.eferraz.entities.holdings.AssetHolding> { every { id } returns 1L }
        val holdingB = mockk<com.eferraz.entities.holdings.AssetHolding> { every { id } returns 2L }
        val holdingC = mockk<com.eferraz.entities.holdings.AssetHolding> { every { id } returns 3L }

        val brokerageNoteRepository = mockk<BrokerageNoteRepository>()
        val assetHoldingRepository = mockk<AssetHoldingRepository>()
        val assetTransactionRepository = mockk<AssetTransactionRepository>(relaxed = true)

        coEvery { brokerageNoteRepository.loadNote() } returns Result.success(note)
        coEvery { assetHoldingRepository.getByTicker("AJFI11") } returns holdingA
        coEvery { assetHoldingRepository.getByTicker("BRCO11") } returns holdingB
        coEvery { assetHoldingRepository.getByTicker("VILG11") } returns holdingC

        val useCase = ImportBrokerageNoteUseCase(
            brokerageNoteRepository = brokerageNoteRepository,
            assetHoldingRepository = assetHoldingRepository,
            assetTransactionRepository = assetTransactionRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        // WHEN
        useCase(Unit).getOrThrow()

        // THEN
        coVerify(exactly = 1) {
            assetTransactionRepository.saveAll(
                match { entries ->
                    entries.size == 3 &&
                        kotlin.math.abs(entries.sumOf { (_, tx) -> tx.allocatedFee } - 4.54) < 0.01
                },
            )
        }
    }

    /**
     * Missing holding aborts import before any transaction is persisted.
     */
    @Test
    fun `GIVEN missing holding WHEN import THEN saveAll is never called`() = runTest {

        // GIVEN
        val note = BrokerageNote(
            totalVolumeTraded = 1000.0,
            apportionableFees = 1.0,
            withheldTaxes = 0.0,
            netValue = -1001.0,
            assets = listOf(
                BrokerageNoteAsset(
                    ticker = "UNKNOWN",
                    transaction = AssetTransaction(
                        id = 0L,
                        date = LocalDate(2026, 1, 1),
                        type = TransactionType.PURCHASE,
                        quantity = 100.0,
                        unitPrice = 10.0,
                    ),
                ),
            ),
        )
        val brokerageNoteRepository = mockk<BrokerageNoteRepository>()
        val assetHoldingRepository = mockk<AssetHoldingRepository>()
        val assetTransactionRepository = mockk<AssetTransactionRepository>(relaxed = true)

        coEvery { brokerageNoteRepository.loadNote() } returns Result.success(note)
        coEvery { assetHoldingRepository.getByTicker("UNKNOWN") } returns null

        val useCase = ImportBrokerageNoteUseCase(
            brokerageNoteRepository = brokerageNoteRepository,
            assetHoldingRepository = assetHoldingRepository,
            assetTransactionRepository = assetTransactionRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        // WHEN
        useCase(Unit).getOrThrow()

        // THEN
        coVerify(exactly = 0) { assetTransactionRepository.saveAll(any()) }
    }

    /**
     * Allocated fees from import sum to note apportionable fees.
     */
    @Test
    fun `GIVEN note with three assets WHEN import THEN allocated fees sum matches note fees`() = runTest {

        // GIVEN
        val note = BrokerageNote(
            totalVolumeTraded = 2000.0,
            apportionableFees = 4.54,
            withheldTaxes = 0.0,
            netValue = -1004.54,
            assets = listOf(
                BrokerageNoteAsset(
                    ticker = "AJFI11",
                    transaction = AssetTransaction(
                        id = 0L,
                        date = LocalDate(2026, 1, 1),
                        type = TransactionType.PURCHASE,
                        quantity = 100.0,
                        unitPrice = 10.0,
                    ),
                ),
                BrokerageNoteAsset(
                    ticker = "BRCO11",
                    transaction = AssetTransaction(
                        id = 1L,
                        date = LocalDate(2026, 1, 1),
                        type = TransactionType.SALE,
                        quantity = 10.0,
                        unitPrice = 100.0,
                    ),
                ),
                BrokerageNoteAsset(
                    ticker = "VILG11",
                    transaction = AssetTransaction(
                        id = 2L,
                        date = LocalDate(2026, 1, 1),
                        type = TransactionType.PURCHASE,
                        quantity = 1000.0,
                        unitPrice = 1.0,
                    ),
                ),
            ),
        )
        val holdings = note.assets.associate { noteAsset ->
            noteAsset.ticker to mockk<com.eferraz.entities.holdings.AssetHolding> {
                every { id } returns noteAsset.transaction.id + 1
            }
        }
        val brokerageNoteRepository = mockk<BrokerageNoteRepository>()
        val assetHoldingRepository = mockk<AssetHoldingRepository>()
        val assetTransactionRepository = mockk<AssetTransactionRepository>(relaxed = true)
        var savedEntries: List<Pair<com.eferraz.entities.holdings.AssetHolding, AssetTransaction>> = emptyList()

        coEvery { brokerageNoteRepository.loadNote() } returns Result.success(note)
        note.assets.forEach { noteAsset ->
            coEvery { assetHoldingRepository.getByTicker(noteAsset.ticker) } returns holdings.getValue(noteAsset.ticker)
        }
        coEvery { assetTransactionRepository.saveAll(any()) } coAnswers {
            savedEntries = firstArg()
        }

        val useCase = ImportBrokerageNoteUseCase(
            brokerageNoteRepository = brokerageNoteRepository,
            assetHoldingRepository = assetHoldingRepository,
            assetTransactionRepository = assetTransactionRepository,
            dispatcher = Dispatchers.Unconfined,
        )

        // WHEN
        useCase(Unit).getOrThrow()

        // THEN
        assertEquals(3, savedEntries.size)
        assertEquals(note.apportionableFees, savedEntries.sumOf { (_, tx) -> tx.allocatedFee }, 0.01)
    }
}
