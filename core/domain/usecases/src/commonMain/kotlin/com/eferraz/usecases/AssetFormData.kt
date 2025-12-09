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

    /**
     * Cria uma cópia deste AssetFormData com os campos comuns atualizados.
     * Permite atualizar campos comuns sem precisar fazer when para cada tipo.
     * 
     * @param issuerName Novo valor para issuerName. Se null, mantém o valor atual.
     * @param observations Novo valor para observations. Se null, mantém o valor atual.
     * @param brokerageName Novo valor para brokerageName. Se null, mantém o valor atual.
     */
    public abstract fun copy(
        issuerName: String? = null,
        observations: String? = null,
        brokerageName: String? = null,
    ): AssetFormData

    public companion object {

        public fun build(category: InvestmentCategory): AssetFormData = when (category) {
            InvestmentCategory.FIXED_INCOME -> FixedIncomeFormData(category = category)
            InvestmentCategory.INVESTMENT_FUND -> InvestmentFundFormData(category = category)
            InvestmentCategory.VARIABLE_INCOME -> VariableIncomeFormData(category = category)
        }
    }
}

