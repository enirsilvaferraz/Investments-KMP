package com.eferraz.presentation.features.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowUp
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.summary.SummaryCard
import com.eferraz.design_system_v2.theme.AppThemeV2
import com.eferraz.design_system_v2.theme.StatusKind
import com.eferraz.presentation.features.walletfilters.PanelHeader
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage

@Immutable
internal data class SummaryProperties(
    val previousValue: Double = 0.0,
    val actualValue: Double = 0.0,
    val contributions: Double = 0.0,
    val withdrawals: Double = 0.0,
    val growth: Double = 0.0,
    val growthPercent: Double = 0.0,
    val earnings: Double = 0.0,
    val earningsPercent: Double = 0.0,
)

@Composable
internal fun SummaryGridWidget(
    properties: SummaryProperties,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
) {

    val growthStatus = when {
        properties.growth > 0 -> StatusKind.Positive
        properties.growth < 0 -> StatusKind.Negative
        else -> StatusKind.Default
    }

    val earningsStatus = when {
        properties.earnings > 0 -> StatusKind.Positive
        properties.earnings < 0 -> StatusKind.Negative
        else -> StatusKind.Default
    }

    val earningsIcon = when {
        properties.earnings > 0 -> Icons.AutoMirrored.Outlined.TrendingUp
        properties.earnings < 0 -> Icons.AutoMirrored.Outlined.TrendingDown
        else -> Icons.Outlined.Equalizer
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(contentPadding),
    ) {

        PanelHeader(
            imageVector = null, title = "Resumo", onReset = null
        )

        Row(horizontalArrangement = Arrangement.spacedBy(contentPadding)) {

            SummaryCard(
                title = "Valor Anterior",
                value = properties.previousValue.currencyFormat(),
                status = StatusKind.Default,
                legend = "Soma dos valores anteriores",
                modifier = Modifier.weight(1f),
            )

            SummaryCard(
                title = "Valor Atual",
                value = properties.actualValue.currencyFormat(),
                status = StatusKind.Default,
                legend = "Soma do valor correntes",
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(contentPadding)) {

            SummaryCard(
                title = "Aportes",
                value = properties.contributions.currencyFormat(),
                status = if (properties.contributions > 0) StatusKind.Info else StatusKind.Default,
                legend = "Entrada de valores",
                icon = Icons.Outlined.Add,
                modifier = Modifier.weight(1f),
            )

            SummaryCard(
                title = "Retiradas",
                value = properties.withdrawals.currencyFormat(),
                status = if (properties.withdrawals > 0) StatusKind.Warning else StatusKind.Default,
                legend = "Saída de valores",
                icon = Icons.Outlined.Remove,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(contentPadding)) {

            SummaryCard(
                title = "Crescimento",
                value = properties.growth.currencyFormat(),
                status = growthStatus,
                legend = "Aportes − Retiradas + Lucro",
                icon = Icons.Outlined.KeyboardDoubleArrowUp,
                modifier = Modifier.weight(1f),
            )

            SummaryCard(
                title = "Crescimento",
                value = properties.growthPercent.toPercentage(),
                status = growthStatus,
                legend = "Em relação ao valor final",
                icon = Icons.Filled.Percent,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(contentPadding)) {

            SummaryCard(
                title = "Lucro",
                value = properties.earnings.currencyFormat(),
                status = earningsStatus,
                legend = "Rendimento dos investimentos",
                icon = earningsIcon,
                modifier = Modifier.weight(1f),
            )

            SummaryCard(
                title = "Lucro",
                value = properties.earningsPercent.toPercentage(),
                status = earningsStatus,
                legend = "Em relação ao valor final",
                icon = Icons.Filled.Percent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@Preview(device = Devices.FOLDABLE, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(device = Devices.FOLDABLE, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
private fun SummaryGridWidgetPreview() {
    AppThemeV2 {
        Surface {
            SummaryGridWidget(
                properties = SummaryProperties(
                    previousValue = 126248.76,
                    actualValue = 126248.76,
                    contributions = 1000.0,
                    withdrawals = 1500.0,
                    growth = -500.0,
                    growthPercent = -1.0,
                    earnings = 940.05,
                    earningsPercent = 1.32,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
