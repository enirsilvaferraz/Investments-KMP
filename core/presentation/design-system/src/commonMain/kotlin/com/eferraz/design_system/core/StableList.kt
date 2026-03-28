package com.eferraz.design_system.core

import androidx.compose.runtime.Immutable

@Immutable
public data class StableList<T>(
    val items: List<T>
)