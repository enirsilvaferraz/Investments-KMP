package com.eferraz.design_system.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppContentDialog(
    modifier: Modifier = Modifier,
    title: String,
    onDismiss: () -> Unit,
    leadingIcon: ImageVector? = null,
    content: @Composable () -> Unit,
) {

    ElevatedCard(
        modifier = modifier.wrapContentWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {

        Column {

            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    leadingIcon?.let {
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large)
                                .size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = leadingIcon,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                        )
                    }
                },
                contentPadding = PaddingValues(top = 8.dp, end = 16.dp, bottom = 8.dp, start = 16.dp)
            )

            content()
        }
    }
}

@Preview
@Composable
private fun AppContentDialogPreview() {
    AppTheme {
        AppContentDialog(
            modifier = Modifier.padding(12.dp),
            title = "Novo investimento",
            leadingIcon = Icons.Default.Add,
            onDismiss = {},
        ) {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth())
        }
    }
}