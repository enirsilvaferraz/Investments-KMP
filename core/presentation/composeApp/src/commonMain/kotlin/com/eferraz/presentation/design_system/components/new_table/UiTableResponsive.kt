package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

// ============================================================================
// ESTADO RESPONSIVO
// ============================================================================

@Stable
internal class ResponsiveState(
    val weights: List<Float>
) {
    val columnCount: Int get() = weights.size

    fun getWeight(index: Int): Float {
        return if (index in weights.indices) weights[index] else 1.0f
    }
}

// ============================================================================
// COMPOSABLE DE ESTADO
// ============================================================================

@Composable
internal fun rememberResponsiveState(weights: List<Float>): ResponsiveState {
    return remember(weights) { ResponsiveState(weights) }
}

