package com.eferraz.presentation.features.assets

import com.eferraz.entities.Asset
import com.eferraz.entities.Brokerage

internal sealed interface AssetsIntent {
    data class UpdateAsset(val asset: Asset) : AssetsIntent
    data class UpdateBrokerage(val assetId: Long, val brokerage: Brokerage?) : AssetsIntent
}

