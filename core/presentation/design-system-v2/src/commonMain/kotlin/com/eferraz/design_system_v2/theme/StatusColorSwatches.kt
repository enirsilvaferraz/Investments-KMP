package com.eferraz.design_system_v2.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val roleLabels: List<Pair<String, (StatusColorRoles) -> Color>> =
    listOf(
        "color" to { it.color },
        "onColor" to { it.onColor },
        "container" to { it.container },
        "onContainer" to { it.onContainer },
        "fixed" to { it.fixed },
        "fixedDim" to { it.fixedDim },
        "onFixed" to { it.onFixed },
        "onFixedVariant" to { it.onFixedVariant },
    )

@Preview(name = "Swatches — Light", showBackground = true, widthDp = 720, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Preview(name = "Swatches — Dark", showBackground = true, widthDp = 720, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun StatusColorSwatches_preview() {

    AppThemeV2 {

        Surface {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {

                StatusKind.entries.forEach { kind ->

                    val roles = MaterialTheme.statusColors(kind)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                        Text(text = kind.name, style = MaterialTheme.typography.labelMedium)

                        Row(Modifier.fillMaxWidth()) {

                            roleLabels.forEach { (label, selector) ->

                                Column(modifier = Modifier.width(85.dp)) {

                                    Box(modifier = Modifier.size(32.dp).background(selector(roles)))

                                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
