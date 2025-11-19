package com.eferraz.database.relationship

import androidx.room.Embedded
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um AssetEntity com seu IssuerEntity relacionado.
 * Usado para queries com JOIN entre assets e issuers.
 */
internal data class AssetWithIssuer(

    @Embedded
    val asset: AssetEntity,

    @Embedded(prefix = "issuer_")
    val issuer: IssuerEntity
)

