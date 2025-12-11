package com.eferraz.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela issuers.
 * Representa a entidade que emitiu o ativo.
 */
@Entity(tableName = "issuers")
internal data class IssuerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "is_in_liquidation", defaultValue = "0")
    val isInLiquidation: Boolean = false
)

