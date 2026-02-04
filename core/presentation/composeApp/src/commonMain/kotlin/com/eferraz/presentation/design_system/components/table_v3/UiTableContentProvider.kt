package com.eferraz.presentation.design_system.components.table_v3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal interface UiTableContentProvider {

    @Composable
    fun Column(text: String)

    @Composable
    fun Cell(text: String)

    @Composable
    fun DefaultFooter(text: String)

    @Composable
    fun Header(text: String, color: androidx.compose.ui.graphics.Color)

    @Composable
    fun SubFooter(text: String)
}

internal object UiTableContentProviderImpl : UiTableContentProvider {

    @Composable
    override fun Header(text: String, color: androidx.compose.ui.graphics.Color) {
        Box(Modifier.fillMaxWidth().background(color)) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Composable
    override fun Column(
        text: String,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun Cell(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun DefaultFooter(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    @Composable
    override fun SubFooter(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp)
        )
    }
}