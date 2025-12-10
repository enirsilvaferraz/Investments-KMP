package com.eferraz.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room para a tabela stock_quote_history.
 * Armazena histórico diário de cotações de ações (OHLCV) obtidas da API brapi.
 */
@Entity(
    tableName = "stock_quote_history",
    foreignKeys = [
        ForeignKey(
            entity = VariableIncomeAssetEntity::class,
            parentColumns = ["ticker"],
            childColumns = ["ticker"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ticker"]),
        Index(value = ["date"]),
        Index(value = ["ticker", "date"], unique = true)
    ]
)
internal data class StockQuoteHistoryEntity(
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "ticker")
    val ticker: String,
    
    @ColumnInfo(name = "date")
    val date: String, // Formato ISO 8601 (YYYY-MM-DD)
    
    @ColumnInfo(name = "open")
    val open: Double? = null,
    
    @ColumnInfo(name = "high")
    val high: Double? = null,
    
    @ColumnInfo(name = "low")
    val low: Double? = null,
    
    @ColumnInfo(name = "close")
    val close: Double? = null,
    
    @ColumnInfo(name = "volume")
    val volume: Long? = null,
    
    @ColumnInfo(name = "adjusted_close")
    val adjustedClose: Double? = null
)

