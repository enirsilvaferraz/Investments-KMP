package com.eferraz.database.entities.assets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import kotlinx.datetime.LocalDate

/**
 * Entidade Room para a tabela fixed_income_assets.
 * Representa os atributos específicos de ativos de renda fixa.
 * Relacionamento 1-1 com AssetEntity.
 */
@Entity(
    tableName = "fixed_income_assets",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class FixedIncomeAssetEntity(

    @PrimaryKey
    @ColumnInfo(name = "assetId")
    val assetId: Long,

    @ColumnInfo(name = "type")
    val type: FixedIncomeAssetType,

    @ColumnInfo(name = "subType")
    val subType: FixedIncomeSubType,

    @ColumnInfo(name = "expirationDate")
    val expirationDate: LocalDate,

    @ColumnInfo(name = "contractedYield")
    val contractedYield: Double,

    @ColumnInfo(name = "cdiRelativeYield")
    val cdiRelativeYield: Double? = null

) : BaseAssetEntity