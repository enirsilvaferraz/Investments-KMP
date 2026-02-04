package com.eferraz.investments

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

public fun main(): Unit = application {
    Window(
        state = rememberWindowState(width = 2000.dp, height = 1000.dp, position = WindowPosition.Aligned(Alignment.Center)),
        onCloseRequest = ::exitApplication,
        title = " ",
    ) {
        App()
    }
}