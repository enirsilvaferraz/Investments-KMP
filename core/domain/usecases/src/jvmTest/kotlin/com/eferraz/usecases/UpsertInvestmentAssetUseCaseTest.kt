package com.eferraz.usecases

import com.eferraz.entities.assets.CNPJ
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.BrokerageRepository
import com.eferraz.usecases.repositories.IssuerRepository
import com.eferraz.usecases.repositories.OwnerRepository
import com.eferraz.usecases.repositories.RegisterInvestmentAssetPersistence
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpsertInvestmentAssetUseCaseTest {

    private val issuerRepository: IssuerRepository = mockk(relaxed = true)
    private val brokerageRepository: BrokerageRepository = mockk(relaxed = true)
    private val ownerRepository: OwnerRepository = mockk(relaxed = true)
    private val registerInvestmentAssetPersistence: RegisterInvestmentAssetPersistence = mockk(relaxed = true)

    private val useCase: UpsertInvestmentAssetUseCase = UpsertInvestmentAssetUseCase(
        issuerRepository = issuerRepository,
        brokerageRepository = brokerageRepository,
        ownerRepository = ownerRepository,
        registerInvestmentAssetPersistence = registerInvestmentAssetPersistence,
        context = Dispatchers.Unconfined,
    )

    private val issuer: Issuer = Issuer(id = 3L, name = "Banco X", isInLiquidation = false)
    private val owner: Owner = Owner(id = 1L, name = "Titular")
    private val brokerage: Brokerage = Brokerage(id = 7L, name = "Corretora")
    private val futureDate: LocalDate = LocalDate(2030, 6, 1)
    private val pastDate: LocalDate = LocalDate(2000, 1, 15)

    private fun stubBrokerageOwnerAndPersistence(returnedAssetId: Long = 42L) {

        coEvery { brokerageRepository.getById(7L) } returns brokerage
        coEvery { ownerRepository.getFirst() } returns owner
        coEvery {
            registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
        } returns returnedAssetId
    }

    /**
     * Happy path: fixed income with valid issuer and future expiration persists a [FixedIncomeAsset].
     */
    @Test
    fun `GIVEN fixed income registration and issuer exists WHEN execute THEN returns asset id from upsert`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence(42L)

            val param = UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = "nota",
                brokerageId = 7L,
                type = FixedIncomeAssetType.PRE_FIXED,
                subType = FixedIncomeSubType.CDB,
                expirationDate = futureDate,
                contractedYield = 10.5,
                cdiRelativeYield = 110.0,
                liquidity = Liquidity.DAILY,
            )

            // WHEN
            val result = useCase(param).getOrThrow()

            // THEN
            assertEquals(42L, result)
            val slot = slot<FixedIncomeAsset>()
            coVerify(exactly = 1) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(
                    capture(slot),
                    owner.id,
                    7L,
                )
            }
            val saved = slot.captured
            assertEquals(0L, saved.id)
            assertEquals(issuer, saved.issuer)
            assertEquals(FixedIncomeAssetType.PRE_FIXED, saved.type)
            assertEquals(FixedIncomeSubType.CDB, saved.subType)
            assertEquals(futureDate, saved.expirationDate)
            assertEquals(10.5, saved.contractedYield)
            assertEquals(110.0, saved.cdiRelativeYield)
            assertEquals(Liquidity.DAILY, saved.liquidity)
            assertEquals("nota", saved.observations)
        }

    /**
     * Issuer id zero or negative must fail before repository calls.
     */
    @Test
    fun `GIVEN issuer id zero WHEN execute THEN fails with issuer validation message`() =
        runTest {

            // GIVEN
            val param = fixedIncomeParam(issuerId = 0L)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Selecione um emissor", error.messages["issuer"])
            coVerify(exactly = 0) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
            }
        }

    /**
     * Negative issuer id is treated like missing selection.
     */
    @Test
    fun `GIVEN negative issuer id WHEN execute THEN fails with Select issuer message`() =
        runTest {

            // GIVEN
            val param = fixedIncomeParam(issuerId = -1L)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Selecione um emissor", error.messages["issuer"])
        }

    /**
     * Valid positive issuer id but catalog miss.
     */
    @Test
    fun `GIVEN issuer id positive but not in catalog WHEN execute THEN fails with catalog message`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(99L) } returns null
            val param = fixedIncomeParam(issuerId = 99L)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Emissor não encontrado no catálogo", error.messages["issuer"])
            coVerify(exactly = 0) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
            }
        }

    /**
     * Fixed income: expiration on or before today is invalid.
     */
    @Test
    fun `GIVEN fixed income expiration not in the future WHEN execute THEN ValidateException on expirationDate`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = fixedIncomeParam(expirationDate = pastDate)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Data de vencimento deve ser futura", error.messages["expirationDate"])
        }

    /**
     * Fixed income: non-positive yield.
     */
    @Test
    fun `GIVEN fixed income non positive yield WHEN execute THEN ValidateException on contractedYield`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = fixedIncomeParam(contractedYield = 0.0)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Rentabilidade deve ser positiva", error.messages["contractedYield"])
        }

    /**
     * Fixed income: optional CDI relative yield must be positive when provided.
     */
    @Test
    fun `GIVEN fixed income non positive CDI relative WHEN execute THEN ValidateException on cdiRelativeYield`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = fixedIncomeParam(cdiRelativeYield = 0.0)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals(
                "Rentabilidade relativa ao CDI deve ser positiva",
                error.messages["cdiRelativeYield"],
            )
        }

    /**
     * Multiple fixed-income validation failures are aggregated.
     */
    @Test
    fun `GIVEN fixed income past expiration and zero yield WHEN execute THEN multiple field errors`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = fixedIncomeParam(
                expirationDate = pastDate,
                contractedYield = -1.0,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertTrue(error.messages.containsKey("expirationDate"))
            assertTrue(error.messages.containsKey("contractedYield"))
        }

    /**
     * Variable income: trims name and ticker; persists with optional CNPJ.
     */
    @Test
    fun `GIVEN variable income registration WHEN execute THEN upserts trimmed VariableIncomeAsset`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence(7L)
            val validCnpjDigits = "11222333000181"
            val param = UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                assetName = "  PETR  ",
                type = VariableIncomeAssetType.NATIONAL_STOCK,
                ticker = "  PETR4  ",
                cnpjRaw = validCnpjDigits,
            )

            // WHEN
            val result = useCase(param).getOrThrow()

            // THEN
            assertEquals(7L, result)
            val slot = slot<VariableIncomeAsset>()
            coVerify(exactly = 1) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(
                    capture(slot),
                    owner.id,
                    7L,
                )
            }
            val saved = slot.captured
            assertEquals("PETR", saved.name)
            assertEquals("PETR4", saved.ticker)
            assertIs<CNPJ>(saved.cnpj)
            assertEquals(validCnpjDigits, saved.cnpj?.get())
        }

    @Test
    fun `GIVEN variable income blank asset name WHEN execute THEN ValidateException on assetName`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                assetName = "   ",
                type = VariableIncomeAssetType.ETF,
                ticker = "HASH11",
                cnpjRaw = null,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Campo obrigatório", error.messages["assetName"])
        }

    @Test
    fun `GIVEN variable income blank ticker WHEN execute THEN ValidateException on ticker`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                assetName = "Ativo",
                type = VariableIncomeAssetType.ETF,
                ticker = "",
                cnpjRaw = null,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Campo obrigatório", error.messages["ticker"])
        }

    @Test
    fun `GIVEN variable income invalid CNPJ WHEN execute THEN ValidateException on cnpj`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                assetName = "Ativo",
                type = VariableIncomeAssetType.ETF,
                ticker = "T",
                cnpjRaw = "123",
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("CNPJ inválido", error.messages["cnpj"])
        }

    @Test
    fun `GIVEN variable income empty CNPJ WHEN execute THEN cnpj is null on asset`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence(1L)
            val param = UpsertInvestmentAssetUseCase.Param.VariableIncomeRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                assetName = "X",
                type = VariableIncomeAssetType.ETF,
                ticker = "Y",
                cnpjRaw = "   ",
            )

            // WHEN
            useCase(param).getOrThrow()

            // THEN
            val slot = slot<VariableIncomeAsset>()
            coVerify(exactly = 1) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(
                    capture(slot),
                    owner.id,
                    7L,
                )
            }
            assertNull(slot.captured.cnpj)
        }

    /**
     * Investment fund: happy path with trimmed name.
     */
    @Test
    fun `GIVEN investment fund registration WHEN execute THEN upserts InvestmentFundAsset`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence(99L)
            val param = UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = "obs",
                brokerageId = 7L,
                name = "  Fundo ABC  ",
                type = InvestmentFundAssetType.MULTIMARKET_FUND,
                liquidity = Liquidity.D_PLUS_DAYS,
                liquidityDays = 30,
                expirationDate = futureDate,
            )

            // WHEN
            val result = useCase(param).getOrThrow()

            // THEN
            assertEquals(99L, result)
            val slot = slot<InvestmentFundAsset>()
            coVerify(exactly = 1) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(
                    capture(slot),
                    owner.id,
                    7L,
                )
            }
            val saved = slot.captured
            assertEquals("Fundo ABC", saved.name)
            assertEquals(InvestmentFundAssetType.MULTIMARKET_FUND, saved.type)
            assertEquals(Liquidity.D_PLUS_DAYS, saved.liquidity)
            assertEquals(30, saved.liquidityDays)
            assertEquals(futureDate, saved.expirationDate)
            assertEquals("obs", saved.observations)
        }

    @Test
    fun `GIVEN investment fund blank name WHEN execute THEN ValidateException on name`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                name = "  ",
                type = InvestmentFundAssetType.STOCK_FUND,
                liquidity = Liquidity.DAILY,
                liquidityDays = 1,
                expirationDate = null,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Campo obrigatório", error.messages["name"])
        }

    @Test
    fun `GIVEN investment fund non positive liquidity days WHEN execute THEN ValidateException`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                name = "Fundo",
                type = InvestmentFundAssetType.STOCK_FUND,
                liquidity = Liquidity.DAILY,
                liquidityDays = 0,
                expirationDate = null,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals(
                "Dias para resgate deve ser um número positivo",
                error.messages["liquidityDays"],
            )
        }

    @Test
    fun `GIVEN investment fund optional expiration in the past WHEN execute THEN ValidateException on expirationDate`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            stubBrokerageOwnerAndPersistence()
            val param = UpsertInvestmentAssetUseCase.Param.InvestmentFundRegistration(
                assetId = 0L,
                issuerId = 3L,
                observations = null,
                brokerageId = 7L,
                name = "Fundo",
                type = InvestmentFundAssetType.PENSION,
                liquidity = Liquidity.AT_MATURITY,
                liquidityDays = 1,
                expirationDate = pastDate,
            )

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Data de vencimento deve ser futura", error.messages["expirationDate"])
        }

    private fun fixedIncomeParam(
        issuerId: Long = 3L,
        expirationDate: LocalDate = futureDate,
        contractedYield: Double = 10.5,
        cdiRelativeYield: Double? = null,
        brokerageId: Long = 7L,
    ): UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration =
        UpsertInvestmentAssetUseCase.Param.FixedIncomeRegistration(
            assetId = 0L,
            issuerId = issuerId,
            observations = null,
            brokerageId = brokerageId,
            type = FixedIncomeAssetType.PRE_FIXED,
            subType = FixedIncomeSubType.CDB,
            expirationDate = expirationDate,
            contractedYield = contractedYield,
            cdiRelativeYield = cdiRelativeYield,
            liquidity = Liquidity.DAILY,
        )

    @Test
    fun `GIVEN brokerage id zero WHEN execute THEN fails with brokerage validation message`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            val param = fixedIncomeParam(brokerageId = 0L)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Selecione uma corretora", error.messages["brokerage"])
            coVerify(exactly = 0) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
            }
        }

    @Test
    fun `GIVEN brokerage not in catalog WHEN execute THEN fails with catalog message`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            coEvery { brokerageRepository.getById(7L) } returns null
            val param = fixedIncomeParam(brokerageId = 7L)

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertEquals("Corretora não encontrada no catálogo", error.messages["brokerage"])
            coVerify(exactly = 0) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
            }
        }

    @Test
    fun `GIVEN owner missing WHEN execute THEN fails with owner message`() =
        runTest {

            // GIVEN
            coEvery { issuerRepository.getById(3L) } returns issuer
            coEvery { brokerageRepository.getById(7L) } returns brokerage
            coEvery { ownerRepository.getFirst() } returns null
            val param = fixedIncomeParam()

            // WHEN
            val error = assertFailsWith<ValidateException> {
                useCase(param).getOrThrow()
            }

            // THEN
            assertTrue(error.messages.containsKey("owner"))
            coVerify(exactly = 0) {
                registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(any(), any(), any())
            }
        }
}
