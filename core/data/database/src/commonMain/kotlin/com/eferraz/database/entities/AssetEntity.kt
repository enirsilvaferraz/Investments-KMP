package com.eferraz.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.entities.liquidity.Liquidity

/**
 * Entidade Room para a tabela base assets.
 * Representa os campos comuns a todos os tipos de ativos.
 *
 * @property category Discriminador: 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND'
 */
@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(
            entity = IssuerEntity::class,
            parentColumns = ["id"],
            childColumns = ["issuerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["issuerId"]),
        Index(value = ["category"])
    ]
)
internal data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val issuerId: Long,
    val category: String, // 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND'
    val liquidity: Liquidity,
    val observations: String? = null
)

