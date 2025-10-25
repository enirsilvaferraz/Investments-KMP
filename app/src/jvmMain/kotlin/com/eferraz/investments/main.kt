package com.eferraz.investments

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

public fun main(): Unit = application {
    Window(
        state = rememberWindowState(width = 1900.dp, height = 900.dp),
        onCloseRequest = ::exitApplication,
        title = "Investments",
    ) {
        App()
    }
}