package com.eferraz.design_system.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

public interface UiTableContentProvider {

    @Composable
    public fun Header(text: String, color: Color)

    @Composable
    public fun Column(index: Int, text: String)

    @Composable
    public fun Cell(index: Int, data: Any)

    @Composable
    public fun SubFooter(index: Int, data: Any)

    @Composable
    public fun Footer(footer: @Composable () -> Unit)
}

public open class UiTableContentProviderImpl : UiTableContentProvider {

    @Composable
    override fun Header(text: String, color: Color) {
        Box(Modifier.fillMaxWidth().background(color)) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Composable
    override fun Column(index: Int, text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun Cell(index: Int, data: Any) {
        Text(
            text = data.toString(),
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun SubFooter(index: Int, data: Any) {
        Text(
            text = data.toString(),
            modifier = Modifier.padding(8.dp)
        )
    }

    @Composable
    override fun Footer(footer: @Composable () -> Unit) {
//        Box(
//            modifier = Modifier.fillMaxWidth()
//        ) {
            footer()
//        }
    }
}