package com.eferraz.usecases

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner
import com.eferraz.usecases.cruds.UpsertAssetHoldingUseCase
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpsertAssetHoldingUseCaseTest {

    private val assetHoldingRepository: AssetHoldingRepository = mockk(relaxed = true)
    private val useCase: UpsertAssetHoldingUseCase = UpsertAssetHoldingUseCase(
        assetHoldingRepository = assetHoldingRepository,
        context = Dispatchers.Unconfined,
    )

    private val issuer: Issuer = Issuer(id = 3L, name = "Banco X", isInLiquidation = false)
    private val owner: Owner = Owner(id = 1L, name = "Titular")
    private val brokerage: Brokerage = Brokerage(id = 7L, name = "Corretora")
    private val futureDate: LocalDate = LocalDate(2030, 6, 1)

    @Test
    fun `GIVEN holding WHEN execute THEN returns id from repository`() = runTest {

        val asset = FixedIncomeAsset(
            id = 1L,
            issuer = issuer,
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = futureDate,
            contractedYield = 1.0,
            liquidity = Liquidity.DAILY,
        )
        val holding = AssetHolding(0L, asset, owner, brokerage, null)
        coEvery { assetHoldingRepository.upsert(any()) } returns 5L
        val result = useCase(UpsertAssetHoldingUseCase.Param(holding)).getOrThrow()
        assertEquals(5L, result)
        coVerify(exactly = 1) { assetHoldingRepository.upsert(holding) }
    }

    @Test
    fun `GIVEN brokerage id zero WHEN execute THEN ValidateException on brokerage`() = runTest {

        val asset = FixedIncomeAsset(
            id = 1L,
            issuer = issuer,
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = futureDate,
            contractedYield = 1.0,
            liquidity = Liquidity.DAILY,
        )
        val badBrokerage = Brokerage(id = 0L, name = "Corretora")
        val holding = AssetHolding(0L, asset, owner, badBrokerage, null)
        val error = assertFailsWith<ValidateException> {
            useCase(UpsertAssetHoldingUseCase.Param(holding)).getOrThrow()
        }
        assertEquals("Selecione uma corretora", error.messages["brokerage"])
        coVerify(exactly = 0) { assetHoldingRepository.upsert(any()) }
    }
}
