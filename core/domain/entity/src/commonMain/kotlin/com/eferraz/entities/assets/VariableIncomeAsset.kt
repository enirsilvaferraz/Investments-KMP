package com.eferraz.entities.assets

import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.CNPJ

/**
 * Representa um ativo de renda variável.
 *
 * @property type O tipo de ativo de renda variável (ação, FII, etc.).
 * @property ticker O código de negociação único do ativo (ex: "PETR4").
 * @property cnpj O CNPJ do ativo (opcional). Aceita formato com máscara (XX.XXX.XXX/XXXX-XX) ou sem máscara (14 dígitos).
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 * 
 * **Nota sobre liquidez:** Para ativos de renda variável, `liquidity` e `liquidityDays` são valores fixos
 * hardcoded e não são parâmetros do construtor:
 * - `liquidity` sempre será `Liquidity.D_PLUS_DAYS`
 * - `liquidityDays` sempre será `2`
 * Estes valores são propriedades calculadas da classe, não podem ser alterados na criação da instância.
 */
public data class VariableIncomeAsset(
    override val id: Long = 0,
    override val name: String,
    override val issuer: Issuer,
    public val type: VariableIncomeAssetType,
    public val ticker: String,
    public val cnpj: CNPJ? = null,
    override val observations: String? = null
) : Asset {

    /**
     * A regra de liquidez que se aplica ao ativo.
     * Para ativos de renda variável, este valor é sempre `Liquidity.D_PLUS_DAYS` (hardcoded).
     */
    public val liquidity: Liquidity = Liquidity.D_PLUS_DAYS

    /**
     * O número de dias para resgate quando liquidity é D_PLUS_DAYS.
     * Para ativos de renda variável, este valor é sempre `2` (hardcoded).
     */
    public val liquidityDays: Int = 2
}
