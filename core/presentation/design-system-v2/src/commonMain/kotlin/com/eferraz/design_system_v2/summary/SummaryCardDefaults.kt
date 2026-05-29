package com.eferraz.design_system_v2.summary

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object SummaryCardDefaults {

    val ContentPadding: Dp = 16.dp
    val VerticalSpacing: Dp = 8.dp
    val BadgeSize: Dp = 40.dp
    val IconSize: Dp = 24.dp

    @Composable
    internal fun cardShape(): Shape = MaterialTheme.shapes.medium
}
