package com.eferraz.design_system.components.inputs

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun TableInputLookAndFeel(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    isError: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showBorder = (isHovered || isFocused || isError) && enabled
    val colors = MaterialTheme.colorScheme
    val underlineColor = when {
        isError -> colors.error
        isFocused -> colors.primary
        showBorder -> colors.outline
        else -> Color.Transparent
    }

    val strokeWidth = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .drawBehind {
                if (showBorder) {
                    val lineWidth = strokeWidth.toPx()
                    val y = size.height - lineWidth / 2
                    drawLine(
                        color = underlineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = lineWidth,
                    )
                }
            }
            .height(35.dp),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}
