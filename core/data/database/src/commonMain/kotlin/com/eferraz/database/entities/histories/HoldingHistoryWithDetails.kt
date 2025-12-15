package com.eferraz.database.entities.histories

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.database.entities.supports.OwnerEntity

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

