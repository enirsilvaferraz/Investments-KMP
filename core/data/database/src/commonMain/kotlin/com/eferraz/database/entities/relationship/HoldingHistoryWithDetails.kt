package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.database.entities.BrokerageEntity
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.database.entities.OwnerEntity

internal class HoldingHistoryWithDetails(

    @Embedded
    val history: HoldingHistoryEntryEntity,

    @Embedded(prefix = "holding_")
    val holding: AssetHoldingEntity,

    @Relation(
        parentColumn = "holding_assetId",
        entityColumn = "id"
    )
    val asset: AssetEntity,

    @Relation(
        parentColumn = "holding_ownerId",
        entityColumn = "id"
    )
    val owner: OwnerEntity,

    @Relation(
        parentColumn = "holding_brokerageId",
        entityColumn = "id"
    )
    val brokerage: BrokerageEntity,
)

