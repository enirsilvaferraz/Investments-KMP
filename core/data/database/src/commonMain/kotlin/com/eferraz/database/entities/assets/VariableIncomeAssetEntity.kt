package com.eferraz.database.entities.assets

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
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
            onDelete = ForeignKey.CASCADE
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
