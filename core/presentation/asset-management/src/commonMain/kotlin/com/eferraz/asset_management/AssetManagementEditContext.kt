package com.eferraz.asset_management

/**
 * Parâmetro Koin `@Provided` para distinguir cadastro novo (`holdingId == null`) de edição.
 */
public data class AssetManagementEditContext(public val holdingId: Long?)
