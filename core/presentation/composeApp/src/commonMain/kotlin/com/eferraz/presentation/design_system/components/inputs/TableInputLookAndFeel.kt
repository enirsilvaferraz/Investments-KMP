package com.eferraz.presentation.design_system.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eferraz.presentation.design_system.utils.thenIf

@Composable
internal fun TableInputLookAndFeel(
    modifier: Modifier = Modifier,
    actualInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    isHovered: Boolean = false,
    isFocused: Boolean = false,
    isError: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {

    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    val showBorder = (isHovered || isFocused || isError) && enabled

    val backgroundColor = if (showBorder) colors.surfaceContainerHighest else Color.Transparent

    val borderColor = when {
        isError -> colors.error
        isFocused -> colors.primary
        showBorder -> colors.outline
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .hoverable(interactionSource = actualInteractionSource)
            .thenIf(
                condition = showBorder,
                ifTrue = { modifier.background(backgroundColor, shapes.medium).border(1.dp, borderColor, shapes.medium) }
            )
            .height(35.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}