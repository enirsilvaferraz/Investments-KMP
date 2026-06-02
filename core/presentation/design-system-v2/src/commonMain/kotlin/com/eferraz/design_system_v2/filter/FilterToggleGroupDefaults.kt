package com.eferraz.design_system_v2.filter

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object FilterToggleGroupDefaults {

    val StandardToggleHeight: Dp = 40.dp
    val SmallToggleHeight: Dp = 32.dp
    val MinTouchTarget: Dp = 48.dp

    @Composable
    internal fun textStyle(size: FilterToggleSize): TextStyle =
        when (size) {
            FilterToggleSize.Standard -> MaterialTheme.typography.labelLarge
            FilterToggleSize.Small -> MaterialTheme.typography.labelMedium
        }

    internal fun toggleHeight(size: FilterToggleSize): Dp =
        when (size) {
            FilterToggleSize.Standard -> StandardToggleHeight
            FilterToggleSize.Small -> SmallToggleHeight
        }
}
