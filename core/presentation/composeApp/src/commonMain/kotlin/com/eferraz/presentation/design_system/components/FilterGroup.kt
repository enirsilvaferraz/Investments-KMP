package com.eferraz.presentation.design_system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal data class FilterGroup(
    val title: String,
    val options: List<String>,
    val selectRule: SelectedRule,
    val level: Level = Level.PRIMARY,
    val visibleOnColapse: Boolean = true
) {
    enum class Level {
        PRIMARY, SECONDARY, TERTIARY
    }

    sealed interface SelectedRule
    data class MultiChoice(val selected: List<String>) : SelectedRule
    data class SingleChoice(val selected: String) : SelectedRule
}

@Composable
internal fun HistoryFilter(
    filters: List<FilterGroup> = listOf(
        FilterGroup(
            "Período",
            listOf("2025-10", "2025-11", "2025-12", "2026-01"),
            FilterGroup.SingleChoice("2025-12"),
            FilterGroup.Level.PRIMARY
        ),
        FilterGroup(
            "Colunas da Tabela",
            listOf(
                "Corretora",
                "Categoria",
                "SubCategoria",
                "Descrição",
                "Vencimento",
                "Emissor",
                "Observações",
                "Valor Anterior",
                "Valor Atual",
                "Valorização",
                "Situação"
            ),
            FilterGroup.MultiChoice(
                listOf(
                    "Corretora",
                    "Categoria",
                    "SubCategoria",
                    "Descrição",
                    "Vencimento",
                    "Emissor",
                    "Observações",
                    "Valor Anterior",
                    "Valor Atual",
                    "Valorização",
                    "Situação"
                )
            ),
            FilterGroup.Level.TERTIARY,
            false
        )
    ),
) {

    val colors = MaterialTheme.colorScheme

    Box(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {

        var expanded by remember { mutableStateOf(true) }

        FlowRow(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(
                onClick = { expanded = !expanded },
            ) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filtros")
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text("Filtros", Modifier.padding(start = 4.dp))
            }

            filters.forEach {

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(it.title, Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp))
                }

                val selected: SnapshotStateList<String> = remember {
                    mutableStateListOf<String>().apply {
                        addAll(
                            when (it.selectRule) {
                                is FilterGroup.MultiChoice -> it.selectRule.selected
                                is FilterGroup.SingleChoice -> listOf(it.selectRule.selected)
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(modifier = Modifier.padding(start = 0.dp))
                }

                it.options.forEach { current ->

                    val isSelected = selected.contains(current)

                    AnimatedVisibility(
                        visible = expanded || (it.visibleOnColapse && isSelected),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {

                        FilterChip(
                            onClick = {
                                if (isSelected) selected.remove(current)
                                else if (it.selectRule is FilterGroup.MultiChoice) selected.add(current)
                                else if (it.selectRule is FilterGroup.SingleChoice) selected[0] = current
                            },
                            label = { Text(current) },
                            selected = isSelected,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) colors.primaryContainer else colors.surface,
                                selectedContainerColor = when (it.level) {
                                    FilterGroup.Level.PRIMARY -> colors.primaryContainer
                                    FilterGroup.Level.SECONDARY -> colors.secondaryContainer
                                    FilterGroup.Level.TERTIARY -> colors.tertiaryContainer
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}