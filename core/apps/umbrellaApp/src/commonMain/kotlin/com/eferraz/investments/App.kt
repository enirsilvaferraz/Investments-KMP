package com.eferraz.investments

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.eferraz.presentation.InternalApp
import org.koin.plugin.module.dsl.startKoin

@Composable
@Preview
public fun App() {

    startKoin<MyKoinApp>()

    InternalApp()
}