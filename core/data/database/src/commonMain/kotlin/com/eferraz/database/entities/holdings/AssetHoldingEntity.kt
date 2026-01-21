package com.eferraz.database.entities.holdings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.goals.FinancialGoalEntity
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.database.entities.supports.OwnerEntity

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
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.Companion.RESTRICT
        ),
        ForeignKey(
            entity = BrokerageEntity::class,
            parentColumns = ["id"],
            childColumns = ["brokerageId"],
            onDelete = ForeignKey.Companion.RESTRICT
        ),
        ForeignKey(
            entity = FinancialGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.Companion.RESTRICT
        )
    ],
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["brokerageId"]),
        Index(value = ["assetId"]),
        Index(value = ["goalId"])
    ]
)
internal data class AssetHoldingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "assetId")
    val assetId: Long,
    @ColumnInfo(name = "ownerId")
    val ownerId: Long,
    @ColumnInfo(name = "brokerageId")
    val brokerageId: Long,
    @ColumnInfo(name = "goalId")
    val goalId: Long? = null
)