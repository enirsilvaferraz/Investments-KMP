package com.eferraz.presentation.design_system.utils

import androidx.compose.ui.Modifier

public fun Modifier.thenIf(
    condition: Boolean,
    ifTrue: () -> Modifier = { Modifier },
    ifFalse: () -> Modifier = { Modifier },
): Modifier = then(if (condition) ifTrue() else ifFalse())