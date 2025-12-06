package com.eferraz.presentation.design_system.components.panels

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ThreePaneScaffoldPaneScope.Pane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedPane(modifier = Modifier.safeContentPadding()) {
        Column(verticalArrangement = spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
internal fun Section(modifier: Modifier = Modifier, content: @Composable (() -> Unit)) {
    Surface(modifier = modifier.clip(RoundedCornerShape(12.dp)), content = content, color = MaterialTheme.colorScheme.surfaceContainerHigh)
}