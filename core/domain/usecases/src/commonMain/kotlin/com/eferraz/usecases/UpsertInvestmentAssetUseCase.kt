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
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.AssetRepository
import com.eferraz.usecases.repositories.IssuerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Persiste um ativo a partir do diálogo de cadastro: emissor **apenas** por id de catálogo (**RF-012**).
 * Não cria nem actualiza [com.eferraz.entities.holdings.AssetHolding] (corretora/meta fora deste fluxo).
 */
@Factory
public class UpsertInvestmentAssetUseCase(
    private val assetRepository: AssetRepository,
    private val issuerRepository: IssuerRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpsertInvestmentAssetUseCase.Param, Long>(context) {

    public sealed class Param {

        public abstract val assetId: Long
        public abstract val issuerId: Long
        public abstract val observations: String?

        public data class FixedIncomeRegistration(
            override val assetId: Long,
            override val issuerId: Long,
            override val observations: String?,
            public val type: FixedIncomeAssetType,
            public val subType: FixedIncomeSubType,
            public val expirationDate: LocalDate,
            public val contractedYield: Double,
            public val cdiRelativeYield: Double?,
            public val liquidity: Liquidity,
        ) : Param()

        public data class VariableIncomeRegistration(
            override val assetId: Long,
            override val issuerId: Long,
            override val observations: String?,
            public val assetName: String,
            public val type: VariableIncomeAssetType,
            public val ticker: String,
            public val cnpjRaw: String?,
        ) : Param()

        public data class InvestmentFundRegistration(
            override val assetId: Long,
            override val issuerId: Long,
            override val observations: String?,
            public val name: String,
            public val type: InvestmentFundAssetType,
            public val liquidity: Liquidity,
            public val liquidityDays: Int,
            public val expirationDate: LocalDate?,
        ) : Param()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(param: Param): Long {

        val issuer = resolveIssuerOrThrow(param)

        val specificErrors: Map<String, String> = when (param) {
            is Param.FixedIncomeRegistration -> validateFixedIncome(param)
            is Param.VariableIncomeRegistration -> validateVariableIncome(param)
            is Param.InvestmentFundRegistration -> validateInvestmentFund(param)
        }

        if (specificErrors.isNotEmpty()) {
            throw ValidateException(specificErrors)
        }

        return persistAsset(param, issuer)
    }

    private suspend fun resolveIssuerOrThrow(param: Param): Issuer {

        val issuer = when {
            param.issuerId <= 0L -> null
            else -> issuerRepository.getById(param.issuerId)
        }

        return issuer ?: throw ValidateException(
            mapOf(
                "issuer" to when {
                    param.issuerId <= 0L -> "Selecione um emissor"
                    else -> "Emissor não encontrado no catálogo"
                },
            ),
        )
    }

    private suspend fun persistAsset(param: Param, issuer: Issuer): Long {

        val asset = when (param) {

            is Param.FixedIncomeRegistration -> FixedIncomeAsset(
                id = param.assetId,
                issuer = issuer,
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
                issuer = issuer,
                type = param.type,
                ticker = param.ticker.trim(),
                cnpj = runCatching { CNPJ(param.cnpjRaw) }.getOrNull(),
                observations = param.observations,
            )

            is Param.InvestmentFundRegistration -> InvestmentFundAsset(
                id = param.assetId,
                name = param.name.trim(),
                issuer = issuer,
                type = param.type,
                liquidity = param.liquidity,
                liquidityDays = param.liquidityDays,
                expirationDate = param.expirationDate,
                observations = param.observations,
            )
        }

        return assetRepository.upsert(asset)
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
