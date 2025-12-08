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
}

