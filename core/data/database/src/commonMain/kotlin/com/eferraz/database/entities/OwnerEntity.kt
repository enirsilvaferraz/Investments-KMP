package com.eferraz.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

