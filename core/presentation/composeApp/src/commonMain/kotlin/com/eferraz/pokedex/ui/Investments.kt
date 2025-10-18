package com.eferraz.pokedex.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.eferraz.pokedex.utils.currencyFormat
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
import kotlin.random.Random

@Composable
internal fun FixedIncomeDataTable(
    data: List<FixedIncomeInvestment> = debugFixedIncomeList,
    modifier: Modifier = Modifier,
    onClickParam: (FixedIncomeInvestment) -> Unit = {},
) {

    val colorEven = MaterialTheme.colorScheme.surfaceBright
    val colorOdd = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.3f)

    var sortIndex by remember { mutableStateOf<Int?>(0) }
    var sortAscending by remember { mutableStateOf(true) }

    val criteria: (it: FixedIncomeInvestment) -> String = {
        when (sortIndex) {
            0 -> it.owner
            1 -> it.brokerageFirm
            2 -> it.investedAmount
            3 -> it.currentAmount
            4 -> it.purchaseDate
            5 -> it.maturityDate
            6 -> it.profitability
            7 -> it.profitabilityIndex
            8 -> it.type
            9 -> it.liquidity
            10 -> it.issuerBank
            else -> throw IllegalArgumentException("Invalid sort index")
        }
    }

    val newData by remember(data, sortAscending) {
        derivedStateOf {
            if (sortAscending) data.sortedBy(criteria)
            else data.sortedByDescending(criteria)
        }
    }

    val onSort: (Int, Boolean) -> Unit = { index, ascending ->
        sortIndex = index
        sortAscending = ascending
    }

    DataTable(
        modifier = modifier.fillMaxSize(),
        sortColumnIndex = sortIndex,
        sortAscending = sortAscending,
        columns =
            listOf(
                DataColumn(onSort = onSort) { Text("Owner") },
                DataColumn(onSort = onSort) { Text("Brokerage Firm") },
                DataColumn(onSort = onSort) { Text("Invested Amount") },
                DataColumn(onSort = onSort) { Text("Current Amount") },
                DataColumn(onSort = onSort) { Text("Purchase Date") },
                DataColumn(onSort = onSort) { Text("Maturity Date") },
                DataColumn(onSort = onSort) { Text("Profitability") },
                DataColumn(onSort = onSort) { Text("Profitability Index") },
                DataColumn(onSort = onSort) { Text("Type") },
                DataColumn(onSort = onSort) { Text("Liquidity") },
                DataColumn(onSort = onSort) { Text("Issuer Bank") }
            )
    ) {
        newData.forEachIndexed { index, investment ->
            row {
                backgroundColor = if (index % 2 == 0) colorEven else colorOdd
                onClick = { onClickParam(investment) }
                cell { Text(investment.owner) }
                cell { Text(investment.brokerageFirm) }
                cell { Text(investment.investedAmount) }
                cell { Text(investment.currentAmount) }
                cell { Text(investment.purchaseDate) }
                cell { Text(investment.maturityDate) }
                cell { Text(investment.profitability) }
                cell { Text(investment.profitabilityIndex) }
                cell { Text(investment.type) }
                cell { Text(investment.liquidity) }
                cell { Text(investment.issuerBank) }
            }
        }
    }
}

internal val debugFixedIncomeList = List(40) {
    FixedIncomeInvestment(
        id = it.toString(),
        owner = if (Random.nextBoolean()) "Enir" else "Camila",
        brokerageFirm = listOf("Inter", "NuBank", "BMG", "XP", "BTG Pactual").random(),
        description = "CDB 100% DI",
        investedAmount = Random.nextDouble(1000.0, 5000.0).currencyFormat(),
        currentAmount = Random.nextDouble(5000.0, 10000.0).currencyFormat(),
        purchaseDate = "10/10/2023",
        maturityDate = "10/10/2028",
        profitability = "100%",
        profitabilityIndex = "CDI",
        type = "CDB",
        liquidity = "Diária",
        issuerBank = listOf("Banco do Brasil", "Itaú", "Bradesco", "Santander").random(),
        note = "Observation $it"
    )
}