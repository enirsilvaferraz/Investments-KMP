package com.eferraz.presentation.features.assets

import com.eferraz.entities.Asset
import com.eferraz.entities.Issuer

internal data class AssetsState(
    val list: List<Asset>,
    val issuers: List<Issuer>,
)

