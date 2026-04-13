package com.eferraz.design_system.core

import androidx.compose.runtime.Immutable

@Immutable
public data class StableMap<K, V>(
    val map: Map<K, V>,
) {
    public operator fun get(key: K): V? =
        map[key]
}
