package com.eferraz.database.entities.assets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.entities.assets.VariableIncomeAssetType

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
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index(value = ["ticker"], unique = true)]
)
internal data class VariableIncomeAssetEntity(

    @PrimaryKey
    @ColumnInfo(name = "assetId")
    val assetId: Long,

    @ColumnInfo(name = "type")
    val type: VariableIncomeAssetType,

    @ColumnInfo(name = "ticker")
    val ticker: String // UNIQUE

) : BaseAssetEntity