package com.eferraz.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.eferraz.entities.InvestmentFundAssetType
import kotlinx.datetime.LocalDate

/**
 * Entidade Room para a tabela investment_fund_assets.
 * Representa os atributos específicos de fundos de investimento.
 * Relacionamento 1-1 com AssetEntity.
 */
@Entity(
    tableName = "investment_fund_assets",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class InvestmentFundAssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "assetId")
    val assetId: Long,
    @ColumnInfo(name = "type")
    val type: InvestmentFundAssetType,
    @ColumnInfo(name = "liquidityDays")
    val liquidityDays: Int, // Sempre presente quando liquidityRule = 'D_PLUS_DAYS'
    @ColumnInfo(name = "expirationDate")
    val expirationDate: LocalDate? = null // Opcional
)

