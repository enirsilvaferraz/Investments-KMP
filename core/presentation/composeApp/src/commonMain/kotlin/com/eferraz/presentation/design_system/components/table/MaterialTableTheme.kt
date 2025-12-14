package com.eferraz.presentation.design_system.components.table

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Implementação do TableTheme usando Material Design 3
 */
@Composable
internal fun MaterialTableTheme(): TableTheme = object : TableTheme {

    private val colors = MaterialTheme.colorScheme

    override val headerBackground: Color
        get() = colors.surfaceContainerHighest

    override val headerText: Color
        get() = colors.onSurface

    override val evenRowBackground: Color
        get() = colors.surface

    override val oddRowBackground: Color
        get() = colors.surfaceContainerLow

    override val divider: Color
        get() = colors.outlineVariant

    override val sortIcon: Color
        get() = colors.primary
}
