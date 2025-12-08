package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.Liquidity
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Factory
public class SaveAssetUseCase(
    private val assetRepository: AssetRepository,
    private val getOrCreateIssuerUseCase: GetOrCreateIssuerUseCase,
) {

    /**
     * Salva ou atualiza um asset.
     *
     * @return ID do asset salvo
     */
    public suspend operator fun invoke(formData: AssetFormData?): Long? {

        require(formData != null) { "formData não pode ser nulo" }

        // Validação comum
        val commonErrors = validateCommonFields(formData)

        // Validação específica por tipo
        val specificErrors = when (formData) {
            is FixedIncomeFormData -> validateFixedIncome(formData)
            is InvestmentFundFormData -> validateInvestmentFund(formData)
            is VariableIncomeFormData -> validateVariableIncome(formData)
        }

        val allErrors = commonErrors + specificErrors
        if (allErrors.isNotEmpty()) {
            throw ValidateException(allErrors)
        }

        // Criar ou obter emissor usando UseCase
        val issuer = getOrCreateIssuerUseCase(formData.issuerName?.trim() ?: "") ?: return -1

        // Criar asset baseado no tipo
        return when (formData) {
            is FixedIncomeFormData -> {
                val asset = FixedIncomeAsset(
                    id = formData.id,
                    issuer = issuer,
                    type = formData.type!!,
                    subType = formData.subType!!,
                    expirationDate = LocalDate.parse(formData.expirationDate!!),
                    contractedYield = formData.contractedYield!!.toDouble(),
                    cdiRelativeYield = formData.cdiRelativeYield?.toDoubleOrNull(),
                    liquidity = formData.liquidity!!,
                    observations = formData.observations
                )
                assetRepository.save(asset)
            }

            is InvestmentFundFormData -> {
                val asset = InvestmentFundAsset(
                    id = formData.id,
                    name = formData.name!!,
                    issuer = issuer,
                    type = formData.type!!,
                    liquidity = Liquidity.D_PLUS_DAYS, // Sempre D_PLUS_DAYS para fundos
                    liquidityDays = formData.liquidityDays!!.toInt(),
                    expirationDate = formData.expirationDate?.let { LocalDate.parse(it) },
                    observations = formData.observations
                )
                assetRepository.save(asset)
            }

            is VariableIncomeFormData -> {
                val asset = VariableIncomeAsset(
                    id = formData.id,
                    name = formData.ticker!!, // Para renda variável, o ticker é usado como name
                    issuer = issuer,
                    type = formData.type!!,
                    ticker = formData.ticker!!,
                    observations = formData.observations
                )
                assetRepository.save(asset)
            }
        }
    }

    private fun validateCommonFields(formData: AssetFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.issuerName.isNullOrBlank()) {
            errors["issuer"] = "Campo obrigatório"
        }

        return errors
    }

    @OptIn(ExperimentalTime::class)
    private fun validateFixedIncome(formData: FixedIncomeFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.type == null) {
            errors["type"] = "Campo obrigatório"
        }

        if (formData.subType == null) {
            errors["subType"] = "Campo obrigatório"
        }

        if (formData.expirationDate == null) {
            errors["expirationDate"] = "Campo obrigatória"
        } else {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            if (LocalDate.parse(formData.expirationDate) <= today) {
                errors["expirationDate"] = "Data de vencimento deve ser futura"
            }
        }

        val contractedYield = formData.contractedYield?.toDoubleOrNull()
        if (contractedYield == null || contractedYield <= 0) {
            errors["contractedYield"] = "Campo obrigatório"
        }

        if (!formData.cdiRelativeYield.isNullOrBlank()) {
            val cdiYield = formData.cdiRelativeYield.toDoubleOrNull()
            if (cdiYield == null || cdiYield <= 0) {
                errors["cdiRelativeYield"] = "Rentabilidade relativa ao CDI deve ser um número positivo"
            }
        }

        if (formData.liquidity == null) {
            errors["liquidity"] = "Campo obrigatória"
        }

        return errors
    }

    @OptIn(ExperimentalTime::class)
    private fun validateInvestmentFund(formData: InvestmentFundFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.type == null) {
            errors["type"] = "Campo obrigatório"
        }

        if (formData.name.isNullOrBlank()) {
            errors["name"] = "Campo obrigatório"
        }

        // Liquidez para fundos é sempre D_PLUS_DAYS (não precisa validar, será definida automaticamente)

        val liquidityDays = formData.liquidityDays?.toIntOrNull()
        if (liquidityDays == null || liquidityDays <= 0) {
            errors["liquidityDays"] = "Dias para resgate deve ser um número inteiro positivo"
        }

        if (!formData.expirationDate.isNullOrBlank()) {
            val expirationDate = try {
                LocalDate.parse(formData.expirationDate)
            } catch (e: Exception) {
                null
            }
            if (expirationDate == null) {
                errors["expirationDate"] = "Data inválida"
            } else {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                if (expirationDate <= today) {
                    errors["expirationDate"] = "Data de vencimento deve ser futura"
                }
            }
        }

        return errors
    }

    private fun validateVariableIncome(formData: VariableIncomeFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.type == null) {
            errors["type"] = "Campo obrigatório"
        }

        if (formData.ticker.isNullOrBlank()) {
            errors["ticker"] = "Campo obrigatório"
        }

        return errors
    }
}

public class ValidateException(public val messages: Map<String, String>) : Exception()

