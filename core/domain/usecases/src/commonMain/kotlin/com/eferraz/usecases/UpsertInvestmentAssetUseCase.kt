package com.eferraz.usecases

import com.eferraz.entities.assets.Asset
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
import com.eferraz.usecases.repositories.OwnerRepository
import com.eferraz.usecases.repositories.RegisterInvestmentAssetPersistence
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Persiste um ativo a partir do diálogo de cadastro: emissor e corretora como [Issuer] / [Brokerage]
 * (linhas do catálogo já carregadas na UI), **sem** nova consulta a repositórios de catálogo.
 * Cria também a [com.eferraz.entities.holdings.AssetHolding] inicial (corretora obrigatória, meta nula)
 * na mesma transação que o ativo (cadastro via diálogo — **RF-004**, **RF-007**).
 */
@Factory
public class UpsertInvestmentAssetUseCase(
    private val ownerRepository: OwnerRepository,
    private val registerInvestmentAssetPersistence: RegisterInvestmentAssetPersistence,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpsertInvestmentAssetUseCase.Param, Long>(context) {

    public sealed class Param {

        public abstract val assetId: Long
        public abstract val issuer: Issuer
        public abstract val observations: String?
        public abstract val brokerage: Brokerage

        public data class FixedIncomeRegistration(
            override val assetId: Long,
            override val issuer: Issuer,
            override val observations: String?,
            override val brokerage: Brokerage,
            public val type: FixedIncomeAssetType,
            public val subType: FixedIncomeSubType,
            public val expirationDate: LocalDate,
            public val contractedYield: Double,
            public val cdiRelativeYield: Double?,
            public val liquidity: Liquidity,
        ) : Param()

        public data class VariableIncomeRegistration(
            override val assetId: Long,
            override val issuer: Issuer,
            override val observations: String?,
            override val brokerage: Brokerage,
            public val assetName: String,
            public val type: VariableIncomeAssetType,
            public val ticker: String,
            public val cnpjRaw: String?,
        ) : Param()

        public data class InvestmentFundRegistration(
            override val assetId: Long,
            override val issuer: Issuer,
            override val observations: String?,
            override val brokerage: Brokerage,
            public val name: String,
            public val type: InvestmentFundAssetType,
            public val liquidity: Liquidity,
            public val liquidityDays: Int,
            public val expirationDate: LocalDate?,
        ) : Param()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(param: Param): Long {

        validateIssuer(param.issuer)
        validateBrokerage(param.brokerage)
        val owner = resolveOwnerOrThrow()

        val specificErrors: Map<String, String> = when (param) {
            is Param.FixedIncomeRegistration -> validateFixedIncome(param)
            is Param.VariableIncomeRegistration -> validateVariableIncome(param)
            is Param.InvestmentFundRegistration -> validateInvestmentFund(param)
        }

        if (specificErrors.isNotEmpty()) {
            throw ValidateException(specificErrors)
        }

        val asset = buildAsset(param)
        return registerInvestmentAssetPersistence.persistNewAssetAndInitialHolding(
            asset = asset,
            ownerId = owner.id,
            brokerage = param.brokerage,
            issuer = param.issuer,
        )
    }

    private fun validateIssuer(issuer: Issuer) {

        val message = when {
            issuer.id <= 0L -> "Selecione um emissor"
            issuer.name.isBlank() -> "Emissor inválido"
            else -> null
        }
        if (message != null) {
            throw ValidateException(mapOf("issuer" to message))
        }
    }

    private fun validateBrokerage(brokerage: Brokerage) {

        val message = when {
            brokerage.id <= 0L -> "Selecione uma corretora"
            brokerage.name.isBlank() -> "Corretora inválida"
            else -> null
        }
        if (message != null) {
            throw ValidateException(mapOf("brokerage" to message))
        }
    }

    private suspend fun resolveOwnerOrThrow(): Owner {

        val owner = ownerRepository.getFirst()
        return owner ?: throw ValidateException(
            mapOf("owner" to "Não há titular configurado. Registe um titular antes de guardar."),
        )
    }

    private fun buildAsset(param: Param): Asset =
        when (param) {

            is Param.FixedIncomeRegistration -> FixedIncomeAsset(
                id = param.assetId,
                issuer = param.issuer,
                type = param.type,
                subType = param.subType,
                expirationDate = param.expirationDate,
                contractedYield = param.contractedYield,
                cdiRelativeYield = param.cdiRelativeYield,
                liquidity = param.liquidity,
                observations = param.observations,
            )

            is Param.VariableIncomeRegistration -> VariableIncomeAsset(
                id = param.assetId,
                name = param.assetName.trim(),
                issuer = param.issuer,
                type = param.type,
                ticker = param.ticker.trim(),
                cnpj = runCatching { CNPJ(param.cnpjRaw) }.getOrNull(),
                observations = param.observations,
            )

            is Param.InvestmentFundRegistration -> InvestmentFundAsset(
                id = param.assetId,
                name = param.name.trim(),
                issuer = param.issuer,
                type = param.type,
                liquidity = param.liquidity,
                liquidityDays = param.liquidityDays,
                expirationDate = param.expirationDate,
                observations = param.observations,
            )
        }

    @OptIn(ExperimentalTime::class)
    private fun validateFixedIncome(param: Param.FixedIncomeRegistration): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (param.expirationDate <= today) {
            errors["expirationDate"] = "Data de vencimento deve ser futura"
        }

        if (param.contractedYield <= 0) {
            errors["contractedYield"] = "Rentabilidade deve ser positiva"
        }

        param.cdiRelativeYield?.let { cdi ->
            if (cdi <= 0) {
                errors["cdiRelativeYield"] = "Rentabilidade relativa ao CDI deve ser positiva"
            }
        }

        return errors
    }

    private fun validateVariableIncome(param: Param.VariableIncomeRegistration): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        if (param.assetName.isBlank()) {
            errors["assetName"] = "Campo obrigatório"
        }

        if (param.ticker.isBlank()) {
            errors["ticker"] = "Campo obrigatório"
        }

        if (param.cnpjRaw.orEmpty().isNotBlank() && runCatching { CNPJ(param.cnpjRaw) }.getOrNull() == null) {
            errors["cnpj"] = "CNPJ inválido"
        }

        return errors
    }

    @OptIn(ExperimentalTime::class)
    private fun validateInvestmentFund(param: Param.InvestmentFundRegistration): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        if (param.name.isBlank()) {
            errors["name"] = "Campo obrigatório"
        }

        if (param.liquidityDays <= 0) {
            errors["liquidityDays"] = "Dias para resgate deve ser um número positivo"
        }

        param.expirationDate?.let { date ->

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            if (date <= today) {
                errors["expirationDate"] = "Data de vencimento deve ser futura"
            }
        }

        return errors
    }
}
