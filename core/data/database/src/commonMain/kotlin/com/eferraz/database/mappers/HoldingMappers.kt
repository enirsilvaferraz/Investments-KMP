package com.eferraz.database.mappers

import com.eferraz.database.entities.holdings.AssetHoldingWithDetails
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.database.entities.supports.OwnerEntity
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner

internal fun AssetHoldingWithDetails.toDomain(
    asset: Asset,
    goal: FinancialGoal?,
): AssetHolding =
    AssetHolding(
        id = holding.id,
        asset = asset,
        owner = owner.toHoldingOwner(),
        brokerage = brokerage.toHoldingBrokerage(),
        goal = goal,
        transactions = transactions
            .map { it.toDomain() }
            .sortedBy { it.date },
    )

private fun OwnerEntity.toHoldingOwner(): Owner =
    Owner(id = id, name = name)

private fun BrokerageEntity.toHoldingBrokerage(): Brokerage =
    Brokerage(id = id, name = name)
