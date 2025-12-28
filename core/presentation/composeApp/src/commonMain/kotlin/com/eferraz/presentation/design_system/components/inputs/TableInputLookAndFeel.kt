package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.utils.thenIf

@Composable
internal fun TableInputLookAndFeel(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    isError: Boolean = false,
    isHovered: Boolean? = null,
    isFocused: Boolean? = null,
    content: @Composable BoxScope.() -> Unit,
) {

    val isHoveredState by interactionSource.collectIsHoveredAsState()
    val isFocusedState by interactionSource.collectIsFocusedAsState()

    val actualIsHovered = isHovered ?: isHoveredState
    val actualIsFocused = isFocused ?: isFocusedState

    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    val showBorder = (actualIsHovered || actualIsFocused || isError) && enabled

    val backgroundColor = if (showBorder) colors.surfaceContainerHighest else Color.Transparent

    val borderColor = when {
        isError -> colors.error
        actualIsFocused -> colors.primary
        showBorder -> colors.outline
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .thenIf(
                condition = showBorder,
                ifTrue = { modifier.background(backgroundColor, shapes.medium).border(1.dp, borderColor, shapes.medium) }
            )
            .height(35.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}