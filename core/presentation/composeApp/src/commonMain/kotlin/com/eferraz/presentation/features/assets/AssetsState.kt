package com.eferraz.presentation.features.assets

import com.eferraz.entities.Asset
import com.eferraz.entities.Brokerage
import com.eferraz.entities.Issuer

internal data class AssetsState(
    val list: List<Asset> = emptyList(),
    val issuers: List<Issuer> = emptyList(),
    val brokerages: List<Brokerage> = emptyList(),
    val assetBrokerages: Map<Long, Brokerage?> = emptyMap(),
)

