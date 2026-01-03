package com.eferraz.presentation.design_system.components.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Footer da tabela
 */
@Composable
internal fun <T> TableFooter(
    columns: List<TableColumn<T>>,
    data: List<T>,
    dividerColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        columns.forEach { column ->
            Box(
                modifier = Modifier.weight(column.weight),
                contentAlignment = when (column.alignment) {
                    Alignment.Start -> Alignment.CenterStart
                    Alignment.CenterHorizontally -> Alignment.Center
                    Alignment.End -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
            ) {
                val footerValue = column.footerOperation?.invoke(data)
                if (footerValue != null) {
                    Text(
                        text = footerValue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
