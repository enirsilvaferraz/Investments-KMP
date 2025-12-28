package com.eferraz.presentation.features.assets

import com.eferraz.entities.Asset

internal sealed interface AssetsIntent {
    data class UpdateAsset(val asset: Asset) : AssetsIntent
}

