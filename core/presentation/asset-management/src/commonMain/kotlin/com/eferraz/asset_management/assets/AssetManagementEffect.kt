package com.eferraz.asset_management.assets

internal sealed interface AssetManagementEffect {

    data object Dismiss : AssetManagementEffect
}
