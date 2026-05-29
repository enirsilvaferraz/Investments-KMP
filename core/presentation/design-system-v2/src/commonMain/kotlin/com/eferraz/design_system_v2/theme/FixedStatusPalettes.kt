package com.eferraz.design_system_v2.theme

import androidx.compose.ui.graphics.Color

/**
 * Fixed status palettes — independent of [androidx.compose.material3.ColorScheme].
 * Default light/dark values calibrated to M3 Expressive baseline (SC-006):
 * surface, onSurface, onSurfaceVariant, outlineVariant, surfaceContainerHigh.
 */
internal object FixedStatusPalettes {

    internal fun light(): (StatusKind) -> StatusColorRoles = statusLookup(lightSet())

    internal fun dark(): (StatusKind) -> StatusColorRoles = statusLookup(darkSet())

    private fun statusLookup(set: StatusSet): (StatusKind) -> StatusColorRoles = { kind ->
        when (kind) {
            StatusKind.Default -> set.default
            StatusKind.Info -> set.info
            StatusKind.Warning -> set.warning
            StatusKind.Positive -> set.positive
            StatusKind.Negative -> set.negative
        }
    }

    private data class StatusSet(
        val default: StatusColorRoles,
        val info: StatusColorRoles,
        val warning: StatusColorRoles,
        val positive: StatusColorRoles,
        val negative: StatusColorRoles,
    )

    private fun lightSet(): StatusSet =
        StatusSet(
            default = roles(
                color = Color(0xFF605D66),
                onColor = Color(0xFFFFFFFF),
                container = Color(0xFFFEF7FF),
                onContainer = Color(0xFF1D1B20),
                fixed = Color(0xFFECE6F0),
                fixedDim = Color(0xFFECE6F0),
                onFixed = Color(0xFF49454F),
                onFixedVariant = Color(0xFFCAC4D0),
            ),
            info = roles(
                color = Color(0xFF0061A4),
                onColor = Color(0xFFFFFFFF),
                container = Color(0xFFD1E4FF),
                onContainer = Color(0xFF001D36),
                fixed = Color(0xFFB1D5FF),
                fixedDim = Color(0xFF81C1FF),
                onFixed = Color(0xFF001D36),
                onFixedVariant = Color(0xFF004A77),
            ),
            warning = roles(
                color = Color(0xFF7D5700),
                onColor = Color(0xFFFFFFFF),
                container = Color(0xFFFFDEA8),
                onContainer = Color(0xFF261900),
                fixed = Color(0xFFFACA6D),
                fixedDim = Color(0xFFDBA020),
                onFixed = Color(0xFF261900),
                onFixedVariant = Color(0xFF5E4200),
            ),
            positive = roles(
                color = Color(0xFF006E2C),
                onColor = Color(0xFFFFFFFF),
                container = Color(0xFFC8FFD4),
                onContainer = Color(0xFF002108),
                fixed = Color(0xFF97E1A7),
                fixedDim = Color(0xFF61BE77),
                onFixed = Color(0xFF002108),
                onFixedVariant = Color(0xFF005224),
            ),
            negative = roles(
                color = Color(0xFFBA1A1A),
                onColor = Color(0xFFFFFFFF),
                container = Color(0xFFFFDAD6),
                onContainer = Color(0xFF410002),
                fixed = Color(0xFFFFC3BC),
                fixedDim = Color(0xFFFF897D),
                onFixed = Color(0xFF410002),
                onFixedVariant = Color(0xFF93000A),
            ),
        )

    private fun darkSet(): StatusSet =
        StatusSet(
            default = roles(
                color = Color(0xFFCAC4D0),
                onColor = Color(0xFF322F35),
                container = Color(0xFF141218),
                onContainer = Color(0xFFE6E0E9),
                fixed = Color(0xFF5E5D62),
                fixedDim = Color(0xFF2B2930),
                onFixed = Color(0xFFCAC4D0),
                onFixedVariant = Color(0xFF49454F),
            ),
            info = roles(
                color = Color(0xFF9ECAFF),
                onColor = Color(0xFF003258),
                container = Color(0xFF004A77),
                onContainer = Color(0xFFD1E4FF),
                fixed = Color(0xFFB1D5FF),
                fixedDim = Color(0xFF81C1FF),
                onFixed = Color(0xFF003258),
                onFixedVariant = Color(0xFF81C1FF),
            ),
            warning = roles(
                color = Color(0xFFF9BD49),
                onColor = Color(0xFF422C00),
                container = Color(0xFF5E4200),
                onContainer = Color(0xFFFFDEA8),
                fixed = Color(0xFFFACA6D),
                fixedDim = Color(0xFFDBA020),
                onFixed = Color(0xFF422C00),
                onFixedVariant = Color(0xFFDBA020),
            ),
            positive = roles(
                color = Color(0xFF7DDA91),
                onColor = Color(0xFF003915),
                container = Color(0xFF005224),
                onContainer = Color(0xFFC8FFD4),
                fixed = Color(0xFF97E1A7),
                fixedDim = Color(0xFF61BE77),
                onFixed = Color(0xFF003915),
                onFixedVariant = Color(0xFF61BE77),
            ),
            negative = roles(
                color = Color(0xFFFFB4AB),
                onColor = Color(0xFF690005),
                container = Color(0xFF93000A),
                onContainer = Color(0xFFFFDAD6),
                fixed = Color(0xFFFFC3BC),
                fixedDim = Color(0xFFFF897D),
                onFixed = Color(0xFF690005),
                onFixedVariant = Color(0xFFFF897D),
            ),
        )

    private fun roles(
        color: Color,
        onColor: Color,
        container: Color,
        onContainer: Color,
        fixed: Color,
        fixedDim: Color,
        onFixed: Color,
        onFixedVariant: Color,
    ): StatusColorRoles =
        StatusColorRoles(
            color = color,
            onColor = onColor,
            container = container,
            onContainer = onContainer,
            fixed = fixed,
            fixedDim = fixedDim,
            onFixed = onFixed,
            onFixedVariant = onFixedVariant,
        )
}
