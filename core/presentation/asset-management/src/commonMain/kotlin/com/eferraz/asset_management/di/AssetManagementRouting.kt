package com.eferraz.asset_management.di

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Rota Navigation 3 para o diálogo de gestão de ativos. */
@Serializable
public data class AssetManagementRouting(
    val holdingId: Long? = null,
) : NavKey