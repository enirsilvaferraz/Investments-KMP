package com.eferraz.design_system_v2.dateselector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.theme.AppThemeV2
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plusMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MonthYearSelector(
    selected: YearMonth,
    options: List<YearMonth>,
    onItemSelect: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemLabel: (YearMonth) -> String = MonthYearLabelFormatter::format,
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {

        FilledTonalButton(
            modifier = Modifier.height(32.dp).width(198.dp),
            enabled = enabled,
            onClick = { expanded = true },
            contentPadding = PaddingValues(start = 12.dp)
        ) {
            RowItem(itemLabel, selected)
        }

        ExposedDropdownMenu(
            modifier = Modifier.width(198.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large,
        ) {

            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {

                options.forEach { item ->

                    DropdownMenuItem(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .then(if (item == selected) Modifier.background(color = MaterialTheme.colorScheme.tertiaryContainer) else Modifier),
                        text = {
                            RowItem({ itemLabel(item) }, selected)
                        },
                        onClick = {
                            expanded = false
                            onItemSelect(item)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowItem(
    itemLabel: (YearMonth) -> String,
    selected: YearMonth,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(16.dp).semantics { hideFromAccessibility() },
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = itemLabel(selected),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun MonthYearSelectorPreview() {

    AppThemeV2 {

        Surface {

            val defaultSelected = YearMonth(2026, Month.MAY)

            val defaultOptions: List<YearMonth> = buildList {
                var current = YearMonth(2025, Month.JUNE)
                repeat(12) {
                    add(current)
                    current = current.plusMonth()
                }
            }

            var selected by remember { mutableStateOf(defaultSelected) }

            MonthYearSelector(
                modifier = Modifier.padding(8.dp),
                selected = defaultSelected,
                options = defaultOptions,
                onItemSelect = { selected = it },
                itemLabel = { MonthYearLabelFormatter.format(it) },
            )
        }
    }
}