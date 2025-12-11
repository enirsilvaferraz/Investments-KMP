package com.eferraz.usecases

import com.eferraz.entities.InvestmentCategory

/**
 * Classe base sealed para dados de formulário de assets.
 * Contém campos comuns a todos os tipos de assets.
 */
public sealed class AssetFormData {
    public abstract val id: Long
    public abstract val category: InvestmentCategory?
    public abstract val issuerName: String?
    public abstract val observations: String?
    public abstract val brokerageName: String?

    public companion object {

        public fun build(category: InvestmentCategory): AssetFormData = when (category) {
            InvestmentCategory.FIXED_INCOME -> FixedIncomeFormData(category = category)
            InvestmentCategory.INVESTMENT_FUND -> InvestmentFundFormData(category = category)
            InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFormData(category = category)
        }
    }
}

