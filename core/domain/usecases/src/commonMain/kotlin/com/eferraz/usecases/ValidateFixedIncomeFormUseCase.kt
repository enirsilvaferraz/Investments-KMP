package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.Liquidity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Dados do formulário de renda fixa para validação.
 */
public data class FixedIncomeFormData(
    val type: FixedIncomeAssetType?,
    val subType: FixedIncomeSubType?,
    val expirationDate: LocalDate?,
    val contractedYield: String,
    val cdiRelativeYield: String,
    val liquidity: Liquidity?,
    val issuerName: String,
)

@Factory
public class ValidateFixedIncomeFormUseCase {

    /**
     * Valida os dados do formulário de renda fixa.
     * 
     * @param formData Dados do formulário a serem validados
     * @return Map com erros de validação (chave = nome do campo, valor = mensagem de erro).
     *         Map vazio indica que o formulário é válido.
     */
    @OptIn(ExperimentalTime::class)
    public operator fun invoke(formData: FixedIncomeFormData): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.type == null) {
            errors["type"] = "Tipo de rendimento é obrigatório"
        }

        if (formData.subType == null) {
            errors["subType"] = "Subtipo é obrigatório"
        }

        if (formData.expirationDate == null) {
            errors["expirationDate"] = "Data de vencimento é obrigatória"
        } else {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            if (formData.expirationDate <= today) {
                errors["expirationDate"] = "Data de vencimento deve ser futura"
            }
        }

        val contractedYield = formData.contractedYield.toDoubleOrNull()
        if (contractedYield == null || contractedYield <= 0) {
            errors["contractedYield"] = "Rentabilidade contratada deve ser um número positivo"
        }

        if (formData.cdiRelativeYield.isNotBlank()) {
            val cdiYield = formData.cdiRelativeYield.toDoubleOrNull()
            if (cdiYield == null || cdiYield <= 0) {
                errors["cdiRelativeYield"] = "Rentabilidade relativa ao CDI deve ser um número positivo"
            }
        }

        if (formData.liquidity == null) {
            errors["liquidity"] = "Liquidez é obrigatória"
        }

        if (formData.issuerName.isBlank()) {
            errors["issuer"] = "Emissor é obrigatório"
        }

        return errors
    }
}

