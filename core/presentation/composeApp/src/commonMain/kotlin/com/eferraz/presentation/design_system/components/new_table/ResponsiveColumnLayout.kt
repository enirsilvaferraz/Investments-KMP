package com.eferraz.presentation.design_system.components.new_table

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gerencia o estado e cálculo de larguras responsivas para colunas.
 * Pode ser reutilizado em qualquer LazyColumn.
 *
 * @param columnCount Número de colunas que serão gerenciadas
 */
internal class UiTableResponsiveState(
    val columnCount: Int,
) {

    internal val columnWidths: SnapshotStateMap<Int, Int> = mutableStateMapOf()
    internal val columnWeights: SnapshotStateMap<Int, Float> = mutableStateMapOf()

    /**
     * Calcula os weights baseados nos percentuais das larguras naturais.
     * Deve ser chamado dentro de um remember.
     */
    internal fun calculateWeights(): Map<Int, Float> {

        val hasAllMeasurements = columnWidths.size == columnCount && columnCount > 0
        if (!hasAllMeasurements) return emptyMap()

        val totalWidth = columnWidths.values.sum()
        if (totalWidth <= 0) return emptyMap()

        // Calcula o percentual de cada coluna como weight
        return columnWidths.mapValues { (_, width) ->
            width.toFloat() / totalWidth
        }
    }

    /**
     * Calcula a largura da célula em pixels baseada no percentual.
     * Retorna null se a largura não deve ser aplicada (fase de medição inicial).
     *
     * @param index Índice da coluna
     * @param availableWidth Largura disponível do row pai em pixels
     * @return Largura da célula em pixels ou null se não deve aplicar largura
     */
    internal fun calculateCellWidth(index: Int, availableWidth: Int?): Int? =
        if (availableWidth != null && columnWeights[index] != null)
            (availableWidth * columnWeights[index]!!).toInt()
        else null
}

/**
 * Cria e retorna um [UiTableResponsiveState] que será lembrado durante a composição.
 * O estado calcula automaticamente os weights baseados nas larguras naturais das colunas.
 *
 * @param columnCount Número de colunas que serão gerenciadas
 */
@Composable
internal fun rememberUiTableResponsiveState(columnCount: Int): UiTableResponsiveState {

    val state = remember(columnCount) { UiTableResponsiveState(columnCount) }

    // Calcula os weights baseados nos percentuais das larguras naturais
    val calculatedWeights = remember(state.columnWidths, columnCount) {
        derivedStateOf {
            state.calculateWeights()
        }
    }

    // Atualiza columnWeights quando o cálculo muda
    LaunchedEffect(calculatedWeights.value) {
        state.columnWeights.clear()
        state.columnWeights.putAll(calculatedWeights.value)
    }

    return state
}

/**
 * Row responsivo que distribui espaço proporcionalmente entre as colunas.
 * Quando a largura disponível muda, as colunas são redimensionadas automaticamente
 * mantendo suas proporções.
 *
 * @param state Estado responsivo das colunas
 * @param modifier Modifier para customização
 * @param height Altura do row (padrão: 52.dp)
 * @param showDivider Se deve mostrar um divider abaixo do row
 * @param content Lista de composables para cada célula (índice corresponde à coluna)
 */
@Composable
internal fun UiTableResponsiveRow(
    state: UiTableResponsiveState,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    showDivider: Boolean = false,
    content: List<@Composable BoxScope.() -> Unit>,
) {

    var rowWidth by remember { mutableStateOf<Int?>(null) }

    Column {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .onGloballyPositioned { coordinates ->
                    rowWidth = coordinates.size.width
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            content.forEachIndexed { index, cellContent ->
                ResponsiveCell(
                    state = state,
                    index = index,
                    availableWidth = rowWidth,
                    content = cellContent
                )
            }
        }

        if (showDivider)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
    }
}

/**
 * Célula responsiva que ajusta sua largura baseada no percentual calculado.
 * Durante a fase inicial de medição, a célula permite que o conteúdo determine
 * sua largura natural. Após o cálculo dos weights, a largura é ajustada
 * proporcionalmente.
 *
 * @param state Estado responsivo das colunas
 * @param index Índice da coluna (deve corresponder à posição no row)
 * @param availableWidth Largura disponível do row pai (medida automaticamente)
 * @param content Conteúdo da célula
 * @param cellPadding Padding interno da célula (padrão: 8.dp)
 */
@Composable
private fun ResponsiveCell(
    state: UiTableResponsiveState,
    index: Int,
    availableWidth: Int?,
    content: @Composable BoxScope.() -> Unit,
    cellPadding: Dp = 8.dp,
) {

    val density = LocalDensity.current

    val cellModifier = state.calculateCellWidth(index, availableWidth)?.let { widthPx ->
        Modifier.width(with(density) { widthPx.toDp() })
    } ?: Modifier

    Box(
        modifier = cellModifier.onGloballyPositioned { coordinates ->
            if (coordinates.size.width > (state.columnWidths[index] ?: 0))
                state.columnWidths[index] = coordinates.size.width
        },
    ) {

        Box(modifier = Modifier.padding(cellPadding), content = content)
    }
}

