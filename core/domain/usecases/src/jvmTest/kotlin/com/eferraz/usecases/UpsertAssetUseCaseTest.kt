package com.eferraz.usecases

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.usecases.cruds.UpsertAssetUseCase
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.AssetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpsertAssetUseCaseTest {

    private val assetRepository: AssetRepository = mockk(relaxed = true)
    private val useCase: UpsertAssetUseCase = UpsertAssetUseCase(
        assetRepository = assetRepository,
        context = Dispatchers.Unconfined,
    )

    private val issuer: Issuer = Issuer(id = 3L, name = "Banco X", isInLiquidation = false)
    private val futureDate: LocalDate = LocalDate(2030, 6, 1)
    private val pastDate: LocalDate = LocalDate(2000, 1, 15)

    @Test
    fun `GIVEN valid fixed income asset WHEN execute THEN returns id from repository`() = runTest {

        coEvery { assetRepository.upsert(any()) } returns 42L
        val asset = FixedIncomeAsset(
            id = 0L,
            issuer = issuer,
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = futureDate,
            contractedYield = 10.5,
            cdiRelativeYield = 110.0,
            liquidity = Liquidity.DAILY,
            observations = "nota",
        )
        val result = useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        assertEquals(42L, result)
        val slot = slot<FixedIncomeAsset>()
        coVerify(exactly = 1) { assetRepository.upsert(capture(slot)) }
        assertEquals("nota", slot.captured.observations)
    }

    @Test
    fun `GIVEN issuer id zero WHEN execute THEN ValidateException on issuer`() = runTest {

        val asset = FixedIncomeAsset(
            id = 0L,
            issuer = Issuer(id = 0L, name = "Banco"),
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = futureDate,
            contractedYield = 1.0,
            liquidity = Liquidity.DAILY,
        )
        val error = assertFailsWith<ValidateException> {
            useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        }
        assertEquals("Selecione um emissor", error.messages["issuer"])
        coVerify(exactly = 0) { assetRepository.upsert(any()) }
    }

    @Test
    fun `GIVEN fixed income past expiration WHEN execute THEN ValidateException`() = runTest {

        val asset = FixedIncomeAsset(
            id = 0L,
            issuer = issuer,
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = pastDate,
            contractedYield = 1.0,
            liquidity = Liquidity.DAILY,
        )
        val error = assertFailsWith<ValidateException> {
            useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        }
        assertEquals("Data de vencimento deve ser futura", error.messages["expirationDate"])
    }

    @Test
    fun `GIVEN variable income valid WHEN execute THEN calls repository`() = runTest {

        coEvery { assetRepository.upsert(any()) } returns 7L
        val asset = VariableIncomeAsset(
            id = 0L,
            name = "PETR",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val result = useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        assertEquals(7L, result)
        coVerify(exactly = 1) { assetRepository.upsert(any()) }
    }

    @Test
    fun `GIVEN fund valid WHEN execute THEN calls repository`() = runTest {

        coEvery { assetRepository.upsert(any()) } returns 99L
        val asset = InvestmentFundAsset(
            id = 0L,
            name = "Fundo ABC",
            issuer = issuer,
            type = InvestmentFundAssetType.MULTIMARKET_FUND,
            liquidity = Liquidity.D_PLUS_DAYS,
            liquidityDays = 30,
            expirationDate = futureDate,
        )
        val result = useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        assertEquals(99L, result)
    }

    @Test
    fun `GIVEN fund blank name WHEN execute THEN ValidateException`() = runTest {

        val asset = InvestmentFundAsset(
            id = 0L,
            name = "   ",
            issuer = issuer,
            type = InvestmentFundAssetType.STOCK_FUND,
            liquidity = Liquidity.DAILY,
            liquidityDays = 1,
            expirationDate = null,
        )
        val error = assertFailsWith<ValidateException> {
            useCase(UpsertAssetUseCase.Param(asset)).getOrThrow()
        }
        assertTrue(error.messages.containsKey("name"))
    }
}
