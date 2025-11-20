package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela brokerages.
 * Representa a instituição financeira onde o ativo está custodiado.
 */
@Entity(tableName = "brokerages")
internal data class BrokerageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

