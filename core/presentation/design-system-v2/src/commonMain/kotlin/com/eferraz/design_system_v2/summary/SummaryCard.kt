package com.eferraz.design_system_v2.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eferraz.design_system_v2.theme.AppThemeV2

@Composable
public fun SummaryCard(
    title: String,
    value: String,
    status: SummaryCardStatus,
    modifier: Modifier = Modifier,
    legend: String? = null,
    icon: ImageVector? = null,
) {

    val colors = SummaryCardStatusColors.resolve(status, MaterialTheme.colorScheme)
    val titleStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    val legendStyle = MaterialTheme.typography.bodySmall

    OutlinedCard(
        modifier = modifier,//.heightIn(min = SummaryCardDefaults.minHeight()),
        shape = SummaryCardDefaults.cardShape(),
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder(),
    ) {

        Column(
            modifier = Modifier.padding(SummaryCardDefaults.ContentPadding).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SummaryCardDefaults.VerticalSpacing),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SummaryCardDefaults.VerticalSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = title.uppercase(),
                    style = titleStyle,
                    color = colors.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                SummaryCardBadgeSlot(
                    icon = icon,
                    badgeContainerColor = colors.badgeContainer,
                    badgeIconColor = colors.badgeIcon,
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = legend.orEmpty(),
                style = legendStyle,
                color = colors.legend,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SummaryCardBadgeSlot(
    icon: ImageVector?,
    badgeContainerColor: Color,
    badgeIconColor: Color,
) {

    Box(
        modifier = Modifier.size(SummaryCardDefaults.BadgeSize),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {

            Box(
                modifier = Modifier.size(SummaryCardDefaults.BadgeSize).background(badgeContainerColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SummaryCardDefaults.IconSize).semantics { hideFromAccessibility() },
                    tint = badgeIconColor,
                )
            }
        }
    }
}

private class SummaryCardPreviewProvider : PreviewParameterProvider<SummaryCardCatalogItem> {

    override val values: Sequence<SummaryCardCatalogItem> = SummaryCardCatalog.items.asSequence()

    override fun getDisplayName(index: Int): String? =
        SummaryCardCatalog.items.getOrNull(index)?.title
}

@Preview(name = "Light", showBackground = true, widthDp = 300, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", showBackground = true, widthDp = 300, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun SummaryCardPreviewLight(
    @PreviewParameter(SummaryCardPreviewProvider::class) item: SummaryCardCatalogItem,
) {
    AppThemeV2 {
        SummaryCard(
            title = item.title,
            value = item.value,
            status = SummaryCardStatus.Default,
            legend = item.legend,
            icon = item.icon,
            modifier = Modifier.padding(8.dp),
        )
    }
}