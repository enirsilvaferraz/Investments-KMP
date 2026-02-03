package com.eferraz.database.entities.assets

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eferraz.database.entities.supports.IssuerEntity
import com.eferraz.entities.assets.Liquidity

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
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @Deprecated("Esse name nao faz sentido")
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "issuerId")
    val issuerId: Long,

    @ColumnInfo(name = "category")
    val category: String, // 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND'

    @ColumnInfo(name = "liquidity")
    val liquidity: Liquidity,

    @ColumnInfo(name = "observations")
    val observations: String? = null
)