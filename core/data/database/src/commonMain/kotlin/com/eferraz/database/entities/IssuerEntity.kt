package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela issuers.
 * Representa a entidade que emitiu o ativo.
 */
@Entity(tableName = "issuers")
internal data class IssuerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

