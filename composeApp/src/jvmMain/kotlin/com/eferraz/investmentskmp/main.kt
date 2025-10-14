package com.eferraz.investmentskmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

public fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InvestmentsKMP",
    ) {
        App()
    }
}