package com.eferraz.presentation.design_system.components.table

import androidx.compose.ui.graphics.Color

/**
 * Abstração para customização visual da tabela
 * Permite diferentes implementações de tema sem acoplar ao MaterialTheme
 */
internal interface TableTheme {
    val headerBackground: Color
    val headerText: Color
    val evenRowBackground: Color
    val oddRowBackground: Color
    val divider: Color
    val sortIcon: Color
}
