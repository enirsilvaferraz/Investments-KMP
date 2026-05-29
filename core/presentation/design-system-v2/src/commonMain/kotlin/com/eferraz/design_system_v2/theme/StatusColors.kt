package com.eferraz.design_system_v2.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

public enum class StatusKind {
    Default,
    Info,
    Warning,
    Positive,
    Negative,
}

@Immutable
public data class StatusColorRoles(
    public val color: Color,
    public val onColor: Color,
    public val container: Color,
    public val onContainer: Color,
    public val fixed: Color,
    public val fixedDim: Color,
    public val onFixed: Color,
    public val onFixedVariant: Color,
)

internal val LocalStatusColorRoles =
    compositionLocalOf<(StatusKind) -> StatusColorRoles> {
        error("AppThemeV2 required")
    }

@Composable
public fun MaterialTheme.statusColors(status: StatusKind): StatusColorRoles =
    LocalStatusColorRoles.current(status)
