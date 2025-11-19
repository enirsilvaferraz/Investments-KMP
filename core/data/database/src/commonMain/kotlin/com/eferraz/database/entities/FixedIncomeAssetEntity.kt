package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeSubType
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
    val assetId: Long,
    val type: FixedIncomeAssetType,
    val subType: FixedIncomeSubType,
    val expirationDate: LocalDate,
    val contractedYield: Double,
    val cdiRelativeYield: Double? = null
)

