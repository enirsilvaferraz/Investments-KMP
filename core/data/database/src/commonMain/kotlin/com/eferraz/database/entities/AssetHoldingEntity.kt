package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela asset_holdings.
 * Representa a posse de um ativo por um proprietário em uma corretora.
 */
@Entity(
    tableName = "asset_holdings",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = BrokerageEntity::class,
            parentColumns = ["id"],
            childColumns = ["brokerageId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["brokerageId"]),
        Index(value = ["assetId"])
    ]
)
internal data class AssetHoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val ownerId: Long,
    val brokerageId: Long,
    val quantity: Double,
    val averageCost: Double,
    val investedValue: Double,
    val currentValue: Double
)

