package com.eferraz.usecases.entities

import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.InvestmentCategory
import com.eferraz.entities.Liquidity

/**
 * Dados do formulário de renda fixa para validação.
 */
public data class FixedIncomeFormData(
    override val id: Long = 0,
    override val category: InvestmentCategory? = InvestmentCategory.FIXED_INCOME,
    val type: FixedIncomeAssetType? = null,
    val subType: FixedIncomeSubType? = null,
    val expirationDate: String? = null,
    val contractedYield: String? = null,
    val cdiRelativeYield: String? = null,
    val liquidity: Liquidity? = null,
    override val issuerName: String? = null,
    override val observations: String? = null,
    override val brokerageName: String? = null,
) : AssetFormData()