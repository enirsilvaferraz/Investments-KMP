package com.eferraz.entities

/**
 * Representa o proprietário legal (pessoa física ou jurídica) de um ativo.
 * @property id O identificador único do proprietário.
 * @property name O nome do proprietário.
 */
public data class Owner(val id: Long, val name: String)
