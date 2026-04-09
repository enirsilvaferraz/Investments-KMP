package com.eferraz.database.entities.supports

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Entidade Room para a tabela owners.
 * Representa o proprietário legal (pessoa física ou jurídica) de um ativo.
 */
@Entity(tableName = "owners")
internal data class OwnerEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String
)
