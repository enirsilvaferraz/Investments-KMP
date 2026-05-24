package com.eferraz.naming

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.eferraz.design_system.theme.getInfoColor
import com.eferraz.design_system.theme.getWarningColor
import com.eferraz.usecases.entities.B3IdentifierStatus

private const val B3_NOT_INFORMED_TOOLTIP: String = "Identificador B3 não informado."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun B3IdentifierStatus.BuildCell(modifier: Modifier = Modifier) {
    when (this) {
        is B3IdentifierStatus.Informed -> B3StatusIcon(
            imageVector = Icons.Default.Info,
            tint = getInfoColor(),
            tooltipText = value,
            modifier = modifier,
        )

        B3IdentifierStatus.NotInformed -> B3StatusIcon(
            imageVector = Icons.Default.Warning,
            tint = getWarningColor(),
            tooltipText = B3_NOT_INFORMED_TOOLTIP,
            modifier = modifier,
        )

        B3IdentifierStatus.NotApplicable -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun B3StatusIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    tooltipText: String,
    modifier: Modifier = Modifier,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.End),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState(),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = tooltipText,
            modifier = modifier.alpha(0.5f),
            tint = tint,
        )
    }
}
