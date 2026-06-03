package com.eferraz.presentation.features.walletfilters

import androidx.compose.runtime.Immutable
import com.eferraz.design_system_v2.filter.FilterToggleOption

@Immutable
internal data class FilterOption<T>(
    val id: T,
    val shortLabel: String,
    val fullLabel: String,
)

internal fun <T> FilterOption<T>.toToggleOption(): FilterToggleOption<T> =
    FilterToggleOption(
        id = id,
        label = shortLabel,
        contentDescription = fullLabel,
    )

internal fun <T> List<FilterOption<T>>.toToggleOptions(): List<FilterToggleOption<T>> =
    map { it.toToggleOption() }
