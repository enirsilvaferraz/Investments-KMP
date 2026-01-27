package com.eferraz.entities.assets

import kotlin.jvm.JvmInline

/**
 * Representa um CNPJ (Cadastro Nacional da Pessoa Jurídica) validado.
 *
 * Aceita CNPJ com ou sem máscara de formatação:
 * - Com máscara: XX.XXX.XXX/XXXX-XX (ex: 12.345.678/0001-90)
 * - Sem máscara: 14 dígitos (ex: 12345678000190)
 *
 * @property value O valor do CNPJ como String
 * @throws IllegalArgumentException se o formato do CNPJ for inválido
 */
@JvmInline
public value class CNPJ(private val value: String) {

    init {
        val cnpjPattern = Regex("""^(\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}|\d{14})$""")
        if (!cnpjPattern.matches(value)) {
            throw IllegalArgumentException("CNPJ inválido: formato deve ser XX.XXX.XXX/XXXX-XX ou 14 dígitos")
        }
    }

    /**
     * Retorna o valor do CNPJ como String.
     */
    public fun get(): String = value
}