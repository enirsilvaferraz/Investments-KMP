package com.eferraz.presentation.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Data class que representa uma opção do SegmentedControl
 */
internal data class SegmentedOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
)

/**
 * Componente interno para cada item do SegmentedControl
 */
@Composable
private fun <T> SegmentedControlItem(
    option: SegmentedOption<T>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            option.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = option.contentDescription ?: option.label,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }

            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}

/**
 * Componente SegmentedControl - Um seletor de opções em formato de pílula
 * 
 * @param options Lista de opções disponíveis
 * @param selectedValue Valor atualmente selecionado
 * @param onValueChange Callback chamado quando uma opção é selecionada
 * @param modifier Modifier para customização
 * @param containerColor Cor de fundo do container (padrão: surfaceContainerHigh)
 */
@Composable
internal fun <T> SegmentedControl(
    options: List<SegmentedOption<T>>,
    selectedValue: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    // Container principal em formato de pílula
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            options.forEachIndexed { index, option ->

                val isSelected = option.value == selectedValue

                SegmentedControlItem(
                    option = option,
                    isSelected = isSelected,
                    onClick = { onValueChange(option.value) },
                )
            }
        }
    }
}

// Enum para as categorias de investimento
internal enum class InvestmentCategory {
    FIXED_INCOME,
    VARIABLE_INCOME,
    FUNDS
}

@Preview
@Composable
private fun SegmentedControlPreview() {

    MaterialTheme {

        var selectedCategory by remember { mutableStateOf(InvestmentCategory.FIXED_INCOME) }

        SegmentedControl(
            options = listOf(
                SegmentedOption(
                    value = InvestmentCategory.FIXED_INCOME,
                    label = "Renda Fixa",
                    icon = Icons.Default.Savings,
                    contentDescription = "Renda Fixa"
                ),
                SegmentedOption(
                    value = InvestmentCategory.VARIABLE_INCOME,
                    label = "Renda Variável",
                    icon = Icons.Default.TrendingUp,
                    contentDescription = "Renda Variável"
                ),
                SegmentedOption(
                    value = InvestmentCategory.FUNDS,
                    label = "Fundos",
                    icon = Icons.Default.AccountBalance,
                    contentDescription = "Fundos"
                )
            ),
            selectedValue = selectedCategory,
            onValueChange = { selectedCategory = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun SegmentedControlPreviewWithoutIcons() {
    MaterialTheme {
        var selectedCategory by remember { mutableStateOf(InvestmentCategory.VARIABLE_INCOME) }

        SegmentedControl(
            options = listOf(
                SegmentedOption(
                    value = InvestmentCategory.FIXED_INCOME,
                    label = "Renda Fixa"
                ),
                SegmentedOption(
                    value = InvestmentCategory.VARIABLE_INCOME,
                    label = "Renda Variável"
                ),
                SegmentedOption(
                    value = InvestmentCategory.FUNDS,
                    label = "Fundos"
                )
            ),
            selectedValue = selectedCategory,
            onValueChange = { selectedCategory = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

