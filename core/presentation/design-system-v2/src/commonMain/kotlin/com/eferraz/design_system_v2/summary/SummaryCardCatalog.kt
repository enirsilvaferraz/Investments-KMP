package com.eferraz.design_system_v2.summary

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.ui.graphics.vector.ImageVector
import com.eferraz.design_system_v2.theme.StatusKind

internal data class SummaryCardCatalogItem(
    val title: String,
    val value: String,
    val status: StatusKind,
    val legend: String? = null,
    val icon: ImageVector? = null,
)

/** Dados estáticos FR-008 para preview de catálogo (8 métricas da carteira). */
internal object SummaryCardCatalog {
    val items: List<SummaryCardCatalogItem> =
        listOf(
            SummaryCardCatalogItem(
                title = "Valor Anterior",
                value = "R$ 126.248,76",
                status = StatusKind.Info,
                legend = "Soma dos valores anteriores",
                icon = Icons.Outlined.BusinessCenter,
            ),
            SummaryCardCatalogItem(
                title = "Valor Atual",
                value = "R$ 71.182,11",
                status = StatusKind.Default,
                legend = "Soma do valor atualizado",
                icon = Icons.Outlined.AccountBalanceWallet,
            ),
            SummaryCardCatalogItem(
                title = "Aportes",
                value = "R$ 0,00",
                status = StatusKind.Default,
                legend = "Soma das transações (compras)",
                icon = Icons.Outlined.Add,
            ),
            SummaryCardCatalogItem(
                title = "Retiradas",
                value = "-R$ 73.375,43",
                status = StatusKind.Negative,
                legend = "Soma das transações (vendas)",
                icon = Icons.Outlined.Close,
            ),
            SummaryCardCatalogItem(
                title = "Crescimento",
                value = "-R$ 72.435,38",
                status = StatusKind.Negative,
                legend = "Aportes − Retiradas + Valorização",
                icon = Icons.Outlined.Layers,
            ),
            SummaryCardCatalogItem(
                title = "% Crescimento",
                value = "-57,38%",
                status = StatusKind.Warning,
                legend = "% em relação ao valor anterior",
                icon = Icons.Outlined.BarChart,
            ),
            SummaryCardCatalogItem(
                title = "Lucro",
                value = "+R$ 940,05",
                status = StatusKind.Positive,
                legend = "Rendimento dos investimentos",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
            ),
            SummaryCardCatalogItem(
                title = "Valorização",
                value = "1,32%",
                status = StatusKind.Positive,
                legend = "% em relação ao valor final",
                icon = Icons.AutoMirrored.Outlined.ShowChart,
            ),
        )
}
