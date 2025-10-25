package com.eferraz.pokedex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.eferraz.pokedex.entities.FixedIncome
import com.eferraz.pokedex.repository.FixedIncomeRepositoryImpl
import com.eferraz.pokedex.ui.FixedIncomeInvestmentViewModel.Event
import com.eferraz.pokedex.ui.FixedIncomeViewModel.FilterAction.FillMaturityEnd
import com.eferraz.pokedex.ui.FixedIncomeViewModel.FilterAction.FillMaturityStart
import com.eferraz.pokedex.ui.FixedIncomeViewModel.FilterAction.FillSearchAnyWhere
import com.eferraz.pokedex.ui.FixedIncomeViewModel.FilterAction.SortBy
import com.eferraz.pokedex.utils.currencyFormat
import com.eferraz.pokedex.utils.shouldUseNavRail
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.TableColumnWidth
import com.seanproctor.datatable.material3.DataTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate.Companion.Format
import kotlinx.datetime.format.Padding
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
public fun HomeRoute(modifier: Modifier = Modifier) {


    val useNavRail = shouldUseNavRail()

    val destinations = listOf("A", "B", "C")
    var selected by remember { mutableStateOf(destinations[0]) }

    if (useNavRail) {
        Row(horizontalArrangement = Arrangement.Center) {
            NavigationRail(windowInsets = NavigationRailDefaults.windowInsets, containerColor = Color.Unspecified) {
                Spacer(Modifier.weight(1f))
                destinations.forEach { item ->
                    NavigationRailItem(
                        selected = selected == item,
                        onClick = { selected = item },
                        icon = {
                            Icon(Icons.Default.Home, item)
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            SupportingPane()
        }
    } else {
        Column(verticalArrangement = Arrangement.Bottom) {
            BottomAppBar {
                destinations.forEach { item ->
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(Icons.Default.Home, item)
                    }
                }
            }
            SupportingPane()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun SupportingPane() {

    val scope = rememberCoroutineScope()
    val navigator = rememberSupportingPaneScaffoldNavigator<Nothing>()

    val vm = koinViewModel<FixedIncomeViewModel>()

    var item by remember { mutableStateOf<FixedIncomeView?>(null) }

    Column(Modifier.padding(24.dp)) {

        Text("Renda Fixa", style = MaterialTheme.typography.headlineLarge)

        SupportingPaneScaffold(
            modifier = Modifier.alpha(1f).padding(top = 32.dp),
            directive = navigator.scaffoldDirective.copy(horizontalPartitionSpacerSize = 24.dp),
            value = navigator.scaffoldValue,
            mainPane = {
                AnimatedPane(modifier = Modifier.safeContentPadding()) {
                    FixedIncomeTablePane(vm) {
                        scope.launch {
                            item = it
                            navigator.navigateTo(SupportingPaneScaffoldRole.Extra)
                        }
                    }
                }
            },
            supportingPane = {
                AnimatedPane(modifier = Modifier.safeContentPadding()) {
                    FilterPane(vm)
                }
            },
            extraPane = {
                AnimatedPane(modifier = Modifier.safeContentPadding()) {
                    if (item != null) DetailPane(item!!)
                }
            },
        )
    }
}

@Composable
private fun FilterPane(vm: FixedIncomeViewModel) {

    Column(
        modifier = Modifier.padding(0.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {

        Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp)).weight(1f)) {

            val filter by vm.filter.collectAsState()

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = spacedBy(8.dp)
            ) {

//                Text("Data de Vencimento")

                OutlinedTextField(
                    label = { Text("Busca em qualquer lugar") },
                    onValueChange = { vm.onAction(FillSearchAnyWhere(it)) },
                    value = filter.searchAnyWhere.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                )

                Text("Data de Vencimento")

                Row(horizontalArrangement = spacedBy(16.dp)) {

                    OutlinedTextField(
                        label = { Text("Início") },
                        onValueChange = { vm.onAction(FillMaturityStart(it)) },
                        value = filter.maturityStart.orEmpty(),
                        modifier = Modifier.weight(1f),
                        visualTransformation = DateTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    )

                    OutlinedTextField(
                        label = { Text("Fim") },
                        onValueChange = { vm.onAction(FillMaturityEnd(it)) },
                        value = filter.maturityEnd.orEmpty(),
                        modifier = Modifier.weight(1f),
                        visualTransformation = DateTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    )
                }
            }
        }

        Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {

            val state by vm.state.collectAsState(null)

            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp)
            ) {
                Text("Total Investido")
                Text(state?.result()?.total.orEmpty())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailPane(x0: FixedIncomeView) {
    Scaffold(
        modifier = Modifier.padding(top = 70.dp).clip(RoundedCornerShape(12.dp))
    ) {

        val vm = koinViewModel<FixedIncomeInvestmentViewModel>(
            key = x0.id,
            parameters = { parametersOf(x0) }
        )

        val formData by vm.formData.collectAsState()

        LazyColumn(
            Modifier.padding(it).fillMaxWidth(),
            verticalArrangement = spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 24.dp)
        ) {

            item { Text("Edição", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)) }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.brokerageFirm,
                    onValueChange = { vm.onEvent(Event.UPDATE_BROKERAGE_FIRM, it) },
                    label = { Text("Brokerage Firm") })
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.investedAmount,
                    onValueChange = { vm.onEvent(Event.UPDATE_INVESTED_AMOUNT, it) },
                    label = { Text("Invested Amount") })
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.purchaseDate,
                    onValueChange = { vm.onEvent(Event.UPDATE_PURCHASE_DATE, it) },
                    label = { Text("Purchase Date") }
                )
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.maturityDate,
                    onValueChange = { vm.onEvent(Event.UPDATE_MATURITY_DATE, it) },
                    label = { Text("Maturity Date") }
                )
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.profitability,
                    onValueChange = { vm.onEvent(Event.UPDATE_PROFITABILITY, it) },
                    label = { Text("Profitability") })
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.profitabilityIndex,
                    onValueChange = { vm.onEvent(Event.UPDATE_INDEX, it) },
                    label = { Text("Profitability Index") })
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.type,
                    onValueChange = { vm.onEvent(Event.UPDATE_INVESTMENT_TYPE, it) },
                    label = { Text("Type") }
                )
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.liquidity,
                    onValueChange = { vm.onEvent(Event.UPDATE_LIQUIDITY, it) },
                    label = { Text("Liquidity") }
                )
            }

            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formData.issuerBank,
                    onValueChange = { vm.onEvent(Event.UPDATE_ISSUER_BANK, it) },
                    label = { Text("Issuer Bank") }
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Delete") }
                    Button(onClick = {}, modifier = Modifier.padding(top = 8.dp).weight(1f)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
internal fun FixedIncomeTablePane(
    vm: FixedIncomeViewModel,
    onItemSelected: (data: FixedIncomeView) -> Unit,
) {

    Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {

        val state by vm.state.collectAsState(null)
        if (state == null) return@Surface

        val filter by vm.filter.collectAsState()

        val colorEven = MaterialTheme.colorScheme.surfaceBright
        val colorOdd = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.3f)

        val onSort = { index: Int, ascending: Boolean ->
            vm.onAction(SortBy(index, ascending))
        }

        DataTable(
            modifier = Modifier,
            columns = listOf(
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Corretora") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Valor Investido") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Valor Atual") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Compra") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Vencimento") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Taxa") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Índice") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Tipo") },
                DataColumn(Alignment.Center, TableColumnWidth.Flex(1f), onSort) { Text("Liquidez") },
            ),
            sortColumnIndex = filter.columnIndex,
            sortAscending = filter.ascending
        ) {
            state?.list()?.forEachIndexed { index: Int, it: FixedIncomeView ->
                row {
                    backgroundColor = if (index % 2 == 0) colorOdd else colorEven
                    cell { Text(it.brokerageFirm) }
                    cell { Text(it.investedAmount) }
                    cell { Text(it.currentAmount) }
                    cell { Text(it.purchaseDate) }
                    cell { Text(it.maturityDate) }
                    cell { Text(it.profitability) }
                    cell { Text(it.profitabilityIndex) }
                    cell { Text(it.type) }
                    cell { Text(it.liquidity) }
                }
            }
        }
    }
}

@KoinViewModel
internal class FixedIncomeViewModel(
    private val filterUseCase: FilterFixedIncomeUseCase,
) : ViewModel() {

    private val _allItems: MutableStateFlow<List<FixedIncome>> = MutableStateFlow(FixedIncomeRepositoryImpl().get())

    private val _filter = MutableStateFlow(FilterData())
    val filter = _filter.asStateFlow()

    val state = combine(_allItems, _filter) { items, filter ->
        State(filterUseCase(items, filter))
    }

    fun onAction(filterAction: FilterAction) {
        when (filterAction) {
            is SortBy -> _filter.update { it.copy(columnIndex = filterAction.columnIndex, ascending = filterAction.ascending) }
            is FillMaturityStart -> _filter.update { it.copy(maturityStart = filterAction.value) }
            is FillMaturityEnd -> _filter.update { it.copy(maturityEnd = filterAction.value) }
            is FillSearchAnyWhere -> _filter.update { it.copy(searchAnyWhere = filterAction.value) }
        }
    }

    sealed interface FilterAction {
        data class SortBy(val columnIndex: Int, val ascending: Boolean) : FilterAction
        data class FillMaturityStart(val value: String) : FilterAction
        data class FillMaturityEnd(val value: String) : FilterAction
        data class FillSearchAnyWhere(val value: String) : FilterAction
    }

    data class FilterData(
        val columnIndex: Int = 0,
        val ascending: Boolean = true,
        val searchAnyWhere: String? = null,
        val maturityStart: String? = null,
        val maturityEnd: String? = null,
    ) {

        fun String.toDate() = this.filter { it.isDigit() }.takeIf { it.length == 8 }?.let {
            LocalDate.parse(this, Format { day(); monthNumber(Padding.ZERO); year(Padding.ZERO) })
        }
    }

    data class State(
        private val list: List<FixedIncome>,
    ) {

        fun result() = ResultData(list)
        fun list() = list.map { FixedIncomeView(it) }

        data class ResultData(
            val total: String,
        ) {
            constructor(list: List<FixedIncome>) : this(
                total = list.sumOf { it.currentAmount }.currencyFormat()
            )
        }
    }
}