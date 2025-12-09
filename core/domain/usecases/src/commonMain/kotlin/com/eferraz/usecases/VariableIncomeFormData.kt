package com.eferraz.usecases

import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.VariableIncomeAssetType

/**
 * Dados do formulário de renda variável para validação.
 */
public data class VariableIncomeFormData(
    override val id: Long = 0,
    override val category: InvestmentCategory? = InvestmentCategory.VARIABLE_INCOME,
    val type: VariableIncomeAssetType? = null,
    val ticker: String? = null,
    override val issuerName: String? = null,
    override val observations: String? = null,
    override val brokerageName: String? = null,
) : AssetFormData() {

    override fun copy(
        issuerName: String?,
        observations: String?,
        brokerageName: String?,
    ): AssetFormData = this.copy(
        issuerName = issuerName ?: this.issuerName,
        observations = observations ?: this.observations,
        brokerageName = brokerageName ?: this.brokerageName
    )
}

