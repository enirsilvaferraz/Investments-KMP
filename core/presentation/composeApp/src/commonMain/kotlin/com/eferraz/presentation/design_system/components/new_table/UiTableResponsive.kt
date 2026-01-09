package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf

// ============================================================================
// INTERFACE DE ESTRATÉGIA DE LAYOUT RESPONSIVO
// ============================================================================

internal interface ResponsiveLayoutStrategy {
    fun getWeight(index: Int): Float?
    fun calculateCellWidth(index: Int, availableWidth: Int?): Int?
    fun updateWidth(index: Int, width: Int)
}

// ============================================================================
// ESTADO RESPONSIVO
// ============================================================================

@Stable
internal class ResponsiveState(
    val columnCount: Int
) : ResponsiveLayoutStrategy {

    val columnWidths: SnapshotStateMap<Int, Int> = mutableStateMapOf()

    override fun getWeight(index: Int): Float? {
        if (columnWidths.size != columnCount || columnCount == 0) return null
        val totalWidth = columnWidths.values.sum()
        if (totalWidth <= 0) return null
        return (columnWidths[index] ?: return null).toFloat() / totalWidth
    }

    override fun calculateCellWidth(index: Int, availableWidth: Int?): Int? {
        return availableWidth?.let { (it * (getWeight(index) ?: return null)).toInt() }
    }

    override fun updateWidth(index: Int, width: Int) {
        val currentWidth = columnWidths[index] ?: 0
        if (width > currentWidth) {
            columnWidths[index] = width
        }
    }
}

// ============================================================================
// COMPOSABLE DE ESTADO
// ============================================================================

@Composable
internal fun rememberResponsiveState(columnCount: Int): ResponsiveState {
    return remember(columnCount) { ResponsiveState(columnCount) }
}

