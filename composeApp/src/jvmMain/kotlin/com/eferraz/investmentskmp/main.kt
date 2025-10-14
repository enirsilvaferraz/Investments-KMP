package com.eferraz.investmentskmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "InvestmentsKMP",
    ) {
        App()
    }
}