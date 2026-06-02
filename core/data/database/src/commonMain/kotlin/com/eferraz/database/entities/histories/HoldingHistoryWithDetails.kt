package com.eferraz.database.entities.histories

import androidx.room3.Embedded
import androidx.room3.Relation
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
        parentColumns = ["holding_assetId"],
        entityColumns = ["id"],
    )
    val asset: AssetEntity,

    @Relation(
        parentColumns = ["holding_ownerId"],
        entityColumns = ["id"],
    )
    val owner: OwnerEntity,

    @Relation(
        parentColumns = ["holding_brokerageId"],
        entityColumns = ["id"],
    )
    val brokerage: BrokerageEntity,
)
