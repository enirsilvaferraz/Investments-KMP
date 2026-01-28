package com.eferraz.presentation.features.goals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.eferraz.presentation.GoalsMonitoringRouting
import com.eferraz.presentation.config
import com.eferraz.presentation.design_system.components.AppScaffold
import com.eferraz.presentation.design_system.components.SegmentedControl
import com.eferraz.presentation.design_system.components.SegmentedOption
import com.eferraz.presentation.design_system.components.new_table.UiTable
import com.eferraz.presentation.features.goals.GoalsMonitoringViewModel.GoalsMonitoringIntent
import com.eferraz.presentation.features.goals.GoalsMonitoringViewModel.GoalsMonitoringState
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import com.eferraz.usecases.entities.GoalsMonitoringTableData
import com.eferraz.usecases.entities.PeriodType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun GoalsMonitoringRoute() {
    val viewModel = koinViewModel<GoalsMonitoringViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(config, GoalsMonitoringRouting)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<GoalsMonitoringRouting> {
                GoalsMonitoringScreen(state, viewModel, backStack)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun GoalsMonitoringScreen(
    state: GoalsMonitoringState,
    viewModel: GoalsMonitoringViewModel,
    backStack: NavBackStack<NavKey>,
) {
    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

    AppScaffold(
        title = "Acompanhamento de Metas",
        navigator = navigator,
        mainPane = {

            Box(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.fillMaxSize()) {
                    GoalsHistoryTable(state.historyData)
                }

                if (state.goals.isNotEmpty()) {
                    SegmentedControl(
                        options = state.goals.map { goal -> SegmentedOption(value = goal, label = goal.name) },
                        selectedValue = state.selectedGoal ?: state.goals.first(),
                        onValueChange = { viewModel.processIntent(GoalsMonitoringIntent.SelectGoal(it)) },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp).align(Alignment.BottomStart)
                    )
                }
            }
        },
        supportingPane = {
            state.goalDetails?.let { details ->
                GoalDetailsPanel(details)
            }
        },
        actions = {
            val periodOptions = remember { listOf(PeriodType.MENSAL, PeriodType.ANUAL) }
            SegmentedControl(
                options = periodOptions.map { period -> SegmentedOption(value = period, label = period.label()) },
                selectedValue = state.periodType,
                onValueChange = { viewModel.processIntent(GoalsMonitoringIntent.SelectPeriodType(it)) },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        }
    )
}

@Composable
private fun GoalsHistoryTable(data: List<GoalsMonitoringTableData>) {

    fun Double.getValid() = takeIf { it != 0.0 }

    UiTable(data = data) {

        column(
            header = "Mês/Ano",
            sortedBy = { it.monthYear },
            alignment = Alignment.CenterHorizontally,
            cellValue = { it.monthYear.formated() }
        )

        column(
            header = "Meta",
            alignment = Alignment.End,
            cellValue = { it.goalValue.currencyFormat() }
        )

        column(
            header = "Valor Total",
            alignment = Alignment.End,
            cellValue = { it.totalValue.getValid()?.currencyFormat().orEmpty() }
        )

        column(
            header = "Aportes",
            alignment = Alignment.End,
            cellValue = { it.contributions.getValid()?.currencyFormat().orEmpty() },
            footer = { list -> list.sumOf { it.contributions }.currencyFormat() }
        )

        column(
            header = "Retiradas",
            alignment = Alignment.End,
            cellValue = { it.withdrawals.getValid()?.currencyFormat().orEmpty() },
            footer = { list -> list.sumOf { it.withdrawals }.currencyFormat() }
        )

        column(
            header = "Crescimento (R$)",
            alignment = Alignment.End,
            cellValue = { it.growthValue.getValid()?.currencyFormat().orEmpty() },
            footer = { list -> list.sumOf { it.growthValue }.currencyFormat() }
        )

        column(
            header = "Crescimento (%)",
            alignment = Alignment.End,
            cellValue = { it.growthPercent.getValid()?.toPercentage().orEmpty() }
        )

//        column(
//            header = "Lucro (R$)",
//            alignment = Alignment.End,
//            cellValue = { it.profitValue.currencyFormat() }
//        )
//
//        column(
//            header = "Lucro (%)",
//            alignment = Alignment.End,
//            cellValue = { it.profitPercent.toPercentage() }
//        )

        column(
            header = "Saldo",
            alignment = Alignment.End,
            cellValue = { it.balance.getValid()?.currencyFormat().orEmpty()  }
        )
    }
}

@Composable
private fun GoalDetailsPanel(details: GoalDetails) {

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {

        Text(
            text = "Resumo da Meta",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DetailRow("Nome", details.name)
        DetailRow("Valor Alvo", details.targetValue.currencyFormat())
        DetailRow("Data de Início", details.startDate)

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Previsão de Evolução",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DetailRow("Data Prevista", details.targetDate)
        DetailRow("Aporte Mensal", details.expectedMonthlyContribution.currencyFormat())
        DetailRow("Rentabilidade Anual", details.expectedAnnualReturn.toPercentage())

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Evolução Real",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DetailRow("Data Prevista", details.targetDate)
        DetailRow("Aporte Mensal (Médio)", details.expectedMonthlyContribution.currencyFormat())
        DetailRow("Rentabilidade Anual (Real)", details.expectedAnnualReturn.toPercentage())

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Ativos Vinculados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        UiTable(
            data = details.linkedAssets,
            modifier = Modifier.height((54 + details.linkedAssets.size * 45 + details.linkedAssets.size).dp)
        ) {

            column(
                header = "Ativo",
                cellValue = { it },
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun PeriodType.label(): String = when (this) {
    PeriodType.MENSAL -> "Mensal"
    PeriodType.ANUAL -> "Anual"
}
