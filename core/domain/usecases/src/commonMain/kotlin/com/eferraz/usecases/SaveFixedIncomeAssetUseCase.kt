package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Factory
public class SaveFixedIncomeAssetUseCase(
    private val assetRepository: AssetRepository,
    private val getOrCreateIssuerUseCase: GetOrCreateIssuerUseCase,
) {

    /**
     * Salva ou atualiza um asset de renda fixa.
     *
     * @return ID do asset salvo
     */
    public suspend operator fun invoke(formData: FixedIncomeFormData): Long? {

        // Validação usando UseCase
        validade(formData).takeIf { it.isNotEmpty() }?.let {
            throw ValidateException(it)
        }

        // Criar ou obter emissor usando UseCase
        val issuer = getOrCreateIssuerUseCase(formData.issuerName?.trim() ?: "") ?: return -1

        // Criar asset
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

        return assetRepository.save(asset)
    }

    @OptIn(ExperimentalTime::class)
    private fun validade(formData: FixedIncomeFormData): Map<String, String> {

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

        if (formData.issuerName.isNullOrBlank()) {
            errors["issuer"] = "Campo obrigatório"
        }

        return errors
    }
}

public class ValidateException(public val messages: Map<String, String>) : Exception()

