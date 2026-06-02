package com.eferraz.design_system_v2.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.theme.AppThemeV2
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal const val MaturityAnyLabel: String = "Qualquer vencimento"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MaturityFilterDropdown(
    selection: YearMonth?,
    months: List<YearMonth>,
    onSelectMonth: (YearMonth?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {

    var expanded by remember { mutableStateOf(false) }
    val label = remember(selection) { maturitySelectionLabel(selection) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {

        FilledTonalButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier
                .height(40.dp)
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled),
            contentPadding = PaddingValues(horizontal = 16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {

                MaturityDropdownItem(MaturityAnyLabel, selection == null) {
                    expanded = false
                    onSelectMonth(null)
                }

                months.forEach { month ->
                    MaturityDropdownItem(
                        text = formatMaturityMonth(month),
                        selected = selection == month,
                        onClick = {
                            expanded = false
                            onSelectMonth(month)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaturityDropdownItem(text: String, selected: Boolean, onClick: () -> Unit) {

    DropdownMenuItem(
        onClick = onClick,
        text = { Text(text = text, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (selected) Modifier.background(MaterialTheme.colorScheme.tertiaryContainer) else Modifier),
        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
    )
}

internal fun maturitySelectionLabel(selection: YearMonth?): String =
    selection?.let(::formatMaturityMonth) ?: MaturityAnyLabel

internal fun formatMaturityMonth(yearMonth: YearMonth) =
    "${maturityMonthNames.getValue(yearMonth.month)} de ${yearMonth.year}"

private val maturityMonthNames: Map<Month, String> =
    mapOf(
        Month.JANUARY to "Janeiro",
        Month.FEBRUARY to "Fevereiro",
        Month.MARCH to "Março",
        Month.APRIL to "Abril",
        Month.MAY to "Maio",
        Month.JUNE to "Junho",
        Month.JULY to "Julho",
        Month.AUGUST to "Agosto",
        Month.SEPTEMBER to "Setembro",
        Month.OCTOBER to "Outubro",
        Month.NOVEMBER to "Novembro",
        Month.DECEMBER to "Dezembro",
    )

private class MaturityFilterPreviewProvider : PreviewParameterProvider<YearMonth?> {

    override val values: Sequence<YearMonth?> =
        sequenceOf(
            null,
            YearMonth(2027, Month.NOVEMBER),
        )

    override fun getDisplayName(index: Int): String? =
        when (index) {
            0 -> "Qualquer vencimento"
            1 -> "Mês seleccionado"
            else -> null
        }
}

@Preview(name = "Dropdown — Light", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Dropdown — Dark", showBackground = true, widthDp = 360, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun MaturityFilterDropdownPreview(
    @PreviewParameter(MaturityFilterPreviewProvider::class) initialSelection: YearMonth?,
) {

    val previewMonths = listOf(
        YearMonth(2026, Month.DECEMBER),
        YearMonth(2027, Month.NOVEMBER),
        YearMonth(2028, Month.MARCH),
    )

    var selection by remember { mutableStateOf(initialSelection) }

    AppThemeV2 {
        Surface {
            MaturityFilterDropdown(
                modifier = Modifier.padding(16.dp),
                selection = selection,
                months = previewMonths,
                onSelectMonth = { selection = it },
            )
        }
    }
}
