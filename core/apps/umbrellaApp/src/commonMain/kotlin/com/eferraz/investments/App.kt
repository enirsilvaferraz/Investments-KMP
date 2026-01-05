package com.eferraz.investments

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.eferraz.presentation.InternalApp

@Composable
@Preview
public fun App() {
    InternalApp(config = AppDI())
}