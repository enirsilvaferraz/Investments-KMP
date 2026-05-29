package com.eferraz.design_system_v2.summary

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

public enum class SummaryCardStatus {
    Default,
}

@Immutable
internal data class SummaryCardColors(
    val container: Color,
    val onContainer: Color,
    val title: Color,
    val legend: Color,
    val outline: Color,
    val badgeContainer: Color,
    val badgeIcon: Color,
    val badgeOutline: Color,
)

internal object SummaryCardStatusColors {

    internal fun resolve(
        status: SummaryCardStatus,
        colorScheme: ColorScheme,
    ): SummaryCardColors = when (status) {
        SummaryCardStatus.Default -> defaultColors(colorScheme)
    }

    private fun defaultColors(colorScheme: ColorScheme): SummaryCardColors =
        SummaryCardColors(
            container = colorScheme.surface,
            onContainer = colorScheme.onSurface,
            title = colorScheme.onSurfaceVariant,
            legend = colorScheme.onSurfaceVariant,
            outline = colorScheme.outlineVariant,
            badgeContainer = colorScheme.surfaceContainerHigh,
            badgeIcon = colorScheme.onSurfaceVariant,
            badgeOutline = colorScheme.outlineVariant,
        )
}
