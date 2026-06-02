package com.eferraz.presentation.features.walletfilters

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * FR-M3-017 — mapa explícito de ícones por secção do painel de filtros.
 */
internal object WalletFilterSectionIcons {
    val panelHeader: ImageVector = Icons.Outlined.FilterList
    val assetClass: ImageVector = Icons.Outlined.Layers
    val subtype: ImageVector = Icons.Outlined.FilterList
    val liquidity: ImageVector = Icons.Outlined.CalendarMonth
    val b3Informed: ImageVector = Icons.Outlined.Info
    val settled: ImageVector = Icons.Outlined.Sync
    val maturity: ImageVector = Icons.Outlined.CalendarMonth
}
