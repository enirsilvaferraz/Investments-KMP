package com.eferraz.investments

import androidx.compose.runtime.Composable
import com.eferraz.presentation.InternalApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
internal fun App() {
    InternalApp(config = AppDI())
}