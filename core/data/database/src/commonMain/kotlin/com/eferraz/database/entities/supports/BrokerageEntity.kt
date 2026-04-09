package com.eferraz.database.entities.supports

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Entidade Room para a tabela brokerages.
 * Representa a instituição financeira onde o ativo está custodiado.
 */
@Entity(tableName = "brokerages")
internal data class BrokerageEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String
)
