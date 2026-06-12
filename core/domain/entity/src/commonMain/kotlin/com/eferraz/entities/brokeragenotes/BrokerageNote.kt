package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction

public data class BrokerageNote(
    val totalVolumeTraded: Double,
    val apportionableFees: Double,
    val netValue: Double,
    val assets: List<AssetTransaction>,
)