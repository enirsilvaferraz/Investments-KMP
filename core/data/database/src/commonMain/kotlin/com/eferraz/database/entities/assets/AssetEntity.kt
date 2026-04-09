package com.eferraz.database.entities.assets

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
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
    val category: String, // 'FIXED_INCOME', 'VARIABLE_INCOME', 'INVESTMENT_FUND' // TODO Transformar em enum Category

    @ColumnInfo(name = "liquidity")
    val liquidity: Liquidity,

    @ColumnInfo(name = "observations")
    val observations: String? = null
)
