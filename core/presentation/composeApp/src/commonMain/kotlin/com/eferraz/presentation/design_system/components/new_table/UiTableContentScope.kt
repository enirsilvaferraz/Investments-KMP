package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

public interface UiTableContentScope<T> {

    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        cellContent: @Composable BoxScope.(T) -> Unit,
        footer: (List<T>) -> String = { "" },
    )

    public fun column(
        header: String,
        sortedBy: ((T) -> Comparable<*>)? = null,
        cellValue: @Composable (T) -> String,
        footer: (List<T>) -> String = { "" },
    )
}