package com.eferraz.entities.assets

/**
 * Contrato para as características intrínsecas de um ativo de investimento.
 *
 * @property id O identificador único do ativo.
 * @property issuer A entidade que emitiu o ativo.
 * @property observations Notas e observações adicionais sobre o ativo (opcional).
 */
public sealed interface Asset {
    public val id: Long
    public val issuer: Issuer
    public val observations: String?
}
