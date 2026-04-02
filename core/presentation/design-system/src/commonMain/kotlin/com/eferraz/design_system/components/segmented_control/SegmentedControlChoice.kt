package com.eferraz.design_system.components.segmented_control

import androidx.compose.runtime.Immutable

@Immutable
public data class SegmentedControlChoice<T>(
    val id: T,
    val label: String,
)
