package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.database.entities.BrokerageEntity
import com.eferraz.database.entities.OwnerEntity

/**
 * Data class intermediária que representa um holding completo com seu asset.
 * Usa @Relation para definir o relacionamento automaticamente.
 */
internal data class AssetHoldingWithDetails(

    @Embedded
    val holding: AssetHoldingEntity,

    @Relation(
        parentColumn = "assetId",
        entityColumn = "id"
    )
    val asset: AssetEntity,

    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id"
    )
    val owner: OwnerEntity,

    @Relation(
        parentColumn = "brokerageId",
        entityColumn = "id"
    )
    val brokerage: BrokerageEntity,
)

