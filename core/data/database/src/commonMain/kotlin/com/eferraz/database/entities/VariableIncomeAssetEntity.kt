package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.entities.VariableIncomeAssetType

/**
 * Entidade Room para a tabela variable_income_assets.
 * Representa os atributos específicos de ativos de renda variável.
 * Relacionamento 1-1 com AssetEntity.
 */
@Entity(
    tableName = "variable_income_assets",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ticker"], unique = true)]
)
internal data class VariableIncomeAssetEntity(
    @PrimaryKey
    val assetId: Long,
    val type: VariableIncomeAssetType,
    val ticker: String // UNIQUE
)

