package com.eferraz.design_system_v2.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppThemeV2(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme =
            if (darkTheme) {
                darkExpressiveColorScheme()
            } else {
                lightExpressiveColorScheme()
            },
        typography = AppTypographyV2,
        shapes = AppShapesV2,
        content = content,
    )
}
