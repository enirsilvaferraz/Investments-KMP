package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.presentation.features.walletfilters.WalletFiltersCatalog
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.presentation.features.walletfilters.revertToHistoryDefaults
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetCurrentYearMonthUseCase
import com.eferraz.usecases.cruds.GetHoldingHistoriesUseCase
import com.eferraz.usecases.entities.HoldingHistoryRow
import com.eferraz.usecases.holdings.CreateHistoryUseCase
import com.eferraz.usecases.screens.FilterHoldingHistoryUseCase
import com.eferraz.usecases.screens.GetMonthSummaryUseCase
import com.eferraz.usecases.balancing.CalculatePortfolioBalancingUseCase
import com.eferraz.usecases.balancing.formatPortfolioBalancingReport
import com.eferraz.usecases.services.ExportToCsvUseCase
import com.eferraz.usecases.services.ImportB3FileUseCase
import com.eferraz.usecases.services.SyncVariableIncomeValuesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    private val getCurrentYearMonthUseCase: GetCurrentYearMonthUseCase,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getMonthSummaryUseCase: GetMonthSummaryUseCase,
    private val getHoldingHistoriesUseCase: GetHoldingHistoriesUseCase,
    private val filterHoldingHistoryUseCase: FilterHoldingHistoryUseCase,
    private val createHistoryUseCase: CreateHistoryUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
    private val exportToCsvUseCase: ExportToCsvUseCase,
    private val importB3FileUseCase: ImportB3FileUseCase,
    private val calculatePortfolioBalancingUseCase: CalculatePortfolioBalancingUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState> field = MutableStateFlow(HistoryState())

    init {

        viewModelScope.launch {

            val periods = async {
                getDataPeriodUseCase(Unit).getOrThrow()
            }

            val selected = async {
                getCurrentYearMonthUseCase(Unit).getOrThrow()
            }

            state.update {
                it.copy(period = HistoryState.Choice(selected.await(), periods.await()))
            }

            processIntent(HistoryIntent.LoadInitialData)

            calculatePortfolioBalancing()
        }
    }

    internal fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.LoadInitialData -> loadInitialData()
            is HistoryIntent.Sync -> sync()
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entry, intent.value)
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.WalletFiltersChanged -> walletFiltersChanged(intent.filters)
            is HistoryIntent.ExportFixedIncomeCsv -> exportFixedIncomeCsv()
            is HistoryIntent.ImportB3File -> importB3File()
            is HistoryIntent.CalculatePortfolioBalancing -> calculatePortfolioBalancing()
        }
    }

    private fun selectPeriod(period: YearMonth) {
        state.update {
            it.copy(
                period = it.period.copy(selected = period),
                walletFilters = it.walletFilters.revertToHistoryDefaults(),
            )
        }
        processIntent(HistoryIntent.LoadInitialData)
    }

    private fun walletFiltersChanged(filters: WalletFiltersUiState) {
        state.update { it.copy(walletFilters = filters) }
        loadInitialData()
    }

    private fun updateEntryValue(entry: HoldingHistoryEntry, value: Double) {
        viewModelScope.launch {
            updateFixedIncomeAndFundsHistoryValueUseCase(
                UpdateFixedIncomeAndFundsHistoryValueUseCase.Params(entry = entry, endOfMonthValue = value)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }.onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun sync() {
        viewModelScope.launch {
            // TODO mostrar progresso na tela
            updateVariableIncomeValues(
                SyncVariableIncomeValuesUseCase.Param(state.value.period.selected!!)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData)
            }.onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun exportFixedIncomeCsv() {
        viewModelScope.launch {
            exportToCsvUseCase(Unit).onFailure {
                println(it.message) // TODO Mostrar mensagem pro usuario
            }
        }
    }

    private fun importB3File() {
        state.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            try {
                importB3FileUseCase(Unit)
            } finally {
                state.update { it.copy(isImporting = false) }
            }
        }
    }

    private fun calculatePortfolioBalancing() {
        viewModelScope.launch {
            calculatePortfolioBalancingUseCase(Unit)
                .onSuccess { println(formatPortfolioBalancingReport(it)) }
                .onFailure { println("Balanceamento: ${it.message}") }
        }
    }

    internal fun loadInitialData() {

        val period = state.value.period.selected!!

        viewModelScope.launch {

            // 0. Configurar opções de filtro
            val brokerages = getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
            val walletFilterOptions = WalletFiltersCatalog.staticPanelOptions(period, brokerages)

            // 1. Obter dados da tabela
            val currentEntries = async {
                getHoldingHistoriesUseCase(GetHoldingHistoriesUseCase.ByReferenceDate(period)).getOrNull()?.takeIf { it.isNotEmpty() }
                    ?: createHistoryUseCase(CreateHistoryUseCase.Param(period)).getOrThrow()
            }

            val previousEntries = async {
                getHoldingHistoriesUseCase(GetHoldingHistoriesUseCase.ByReferenceDate(period.minusMonth())).getOrNull()?.takeIf { it.isNotEmpty() }
                    ?: createHistoryUseCase(CreateHistoryUseCase.Param(period)).getOrThrow()
            }

            // 2. Filtrar dados da tabela
            val walletFilter = state.value.walletFilters.toWalletHistoryFilterCriteria()

            val filteredCurrentEntries = filterHoldingHistoryUseCase(FilterHoldingHistoryUseCase.Param(currentEntries.await(), walletFilter)).getOrThrow()
            val filteredPreviousEntries = previousEntries.await().filter { it.holding.id in filteredCurrentEntries.map { it.holding.id } }

            // 3. Construir dados da tabela
            val tableRows = HoldingHistoryRow.build(period, filteredPreviousEntries, filteredCurrentEntries)//.sortedBy { it.assetClass }

            // 4. Obter resumo do mês
            val monthSummary = getMonthSummaryUseCase(GetMonthSummaryUseCase.Param(referenceDate = period, current = filteredCurrentEntries, previous = filteredPreviousEntries),).getOrThrow()

            // 5. Atualiza tela
            state.update {
                it.copy(tableData = tableRows, walletFilterOptions = walletFilterOptions, monthSummary = monthSummary)
            }
        }
    }
}

internal sealed interface HistoryIntent {
    data object LoadInitialData : HistoryIntent
    data object Sync : HistoryIntent
    data object ExportFixedIncomeCsv : HistoryIntent
    data object ImportB3File : HistoryIntent
    data object CalculatePortfolioBalancing : HistoryIntent
    data class UpdateEntryValue(val entry: HoldingHistoryEntry, val value: Double) : HistoryIntent
    data class SelectPeriod(val period: YearMonth) : HistoryIntent
    data class WalletFiltersChanged(val filters: WalletFiltersUiState) : HistoryIntent
}
