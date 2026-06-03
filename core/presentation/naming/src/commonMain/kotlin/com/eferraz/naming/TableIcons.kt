package com.eferraz.naming

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.Liquidity
import com.eferraz.design_system.theme.getSuccessColor
import com.eferraz.design_system.theme.getWarningColor

/**
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun Liquidity.BuildIcon() {

    val icon = when (this) {
        Liquidity.DAILY -> Icons.Default.EventAvailable
        else -> Icons.Default.EventBusy
    }

    val color = when (this) {
        Liquidity.DAILY -> getSuccessColor()
        else -> MaterialTheme.colorScheme.error
    }

    val tooltipText = asLabel()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.End),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltipText,
            modifier = Modifier.alpha(0.5f), // .size(18.dp),
            tint = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AssetClass.BuildIcon() {

    val icon = Icons.Default.AttachMoney

    val color = when (this) {
        AssetClass.FIXED_INCOME -> getSuccessColor()
        AssetClass.VARIABLE_INCOME -> MaterialTheme.colorScheme.error
        AssetClass.INVESTMENT_FUND -> getWarningColor()
    }

    val tooltipText = asLabel()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.End),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltipText,
            modifier = Modifier.alpha(0.5f), // .size(18.dp),
            tint = color
        )
    }
}
