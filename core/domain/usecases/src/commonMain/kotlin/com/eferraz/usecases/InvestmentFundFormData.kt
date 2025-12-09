package com.eferraz.usecases

import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.Liquidity

/**
 * Dados do formulário de fundos de investimento para validação.
 */
public data class InvestmentFundFormData(
    override val id: Long = 0,
    override val category: InvestmentCategory? = InvestmentCategory.INVESTMENT_FUND,
    val type: InvestmentFundAssetType? = null,
    val name: String? = null,
    val liquidityDays: String? = null,
    val expirationDate: String? = null,
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

