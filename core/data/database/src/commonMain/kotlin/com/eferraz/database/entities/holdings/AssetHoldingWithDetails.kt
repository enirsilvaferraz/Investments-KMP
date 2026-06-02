package com.eferraz.database.entities.holdings

import androidx.room3.Embedded
import androidx.room3.Relation
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.goals.FinancialGoalEntity
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.database.entities.supports.OwnerEntity

/**
 * Data class intermediária que representa um holding completo com seu asset.
 * Usa @Relation para definir o relacionamento automaticamente.
 */
internal data class AssetHoldingWithDetails(

    @Embedded
    val holding: AssetHoldingEntity,

    @Relation(
        parentColumns = ["assetId"],
        entityColumns = ["id"],
    )
    val asset: AssetEntity,

    @Relation(
        parentColumns = ["ownerId"],
        entityColumns = ["id"],
    )
    val owner: OwnerEntity,

    @Relation(
        parentColumns = ["brokerageId"],
        entityColumns = ["id"],
    )
    val brokerage: BrokerageEntity,

    @Relation(
        parentColumns = ["goalId"],
        entityColumns = ["id"],
    )
    val goal: List<FinancialGoalEntity> = emptyList()
)
