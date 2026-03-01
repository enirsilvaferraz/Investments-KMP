package com.eferraz.presentation.features.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.Owner
import com.eferraz.presentation.design_system.theme.AppTheme
import com.eferraz.presentation.features.transactions.TransactionForm
import com.eferraz.presentation.features.transactions.TransactionPanel

@Composable
internal fun AssetForm(
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column (
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        ) {

            Text(text = "Edição")

            TransactionPanel(
                selectedHolding = AssetHolding(
                    id = 1,
                    asset = VariableIncomeAsset(
                        name = "A",
                        issuer = Issuer(1,"1"),
                        type = VariableIncomeAssetType.ETF,
                        ticker = ""
                    ),
                    owner = Owner(id = 1, "A"),
                    brokerage = Brokerage(1, "a")
                )
            )
        }
    }
}

@Composable
@Preview
internal fun AssetFormPreview() {
    AppTheme {
        AssetForm()
    }
}