package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.holdings.Appreciation
import com.eferraz.entities.holdings.Growth
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.presentation.features.summary.SummaryProperties
import com.eferraz.presentation.features.walletfilters.WalletFiltersCatalog
import com.eferraz.presentation.features.walletfilters.WalletFiltersUiState
import com.eferraz.presentation.features.walletfilters.revertToHistoryDefaults
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.cruds.GetBrokeragesUseCase
import com.eferraz.usecases.cruds.GetCurrentYearMonthUseCase
import com.eferraz.usecases.entities.HoldingHistoryView
import com.eferraz.usecases.screens.GetHistoryTableDataUseCase
import com.eferraz.usecases.screens.maturityFilterMonthRange
import com.eferraz.usecases.services.ExportToCsvUseCase
import com.eferraz.usecases.services.ImportB3FileUseCase
import com.eferraz.usecases.services.SyncVariableIncomeValuesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    private val getCurrentYearMonthUseCase: GetCurrentYearMonthUseCase,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getHistoryTableDataUseCase: GetHistoryTableDataUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
    private val exportToCsvUseCase: ExportToCsvUseCase,
    private val importB3FileUseCase: ImportB3FileUseCase,
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

    internal fun loadInitialData() {
        val period = state.value.period.selected!!
        val filters = state.value.walletFilters
        val walletFilter = filters.toWalletHistoryFilterCriteria()

        viewModelScope.launch {
            val currentMonth = async { getCurrentYearMonthUseCase(Unit).getOrThrow() }
            val brokerages = async {
                getBrokeragesUseCase(GetBrokeragesUseCase.Param).getOrNull().orEmpty()
            }
            val tableRows = async {
                getHistoryTableDataUseCase(GetHistoryTableDataUseCase.Param(period, walletFilter))
                    .getOrNull() ?: emptyList()
            }

            val maturityMonths = maturityFilterMonthRange(currentMonth.await())
            val brokerageOptions =
                brokerages.await()
                    .sortedBy { it.name }
                    .map(WalletFiltersCatalog::brokerageOption)
            val walletFilterOptions =
                WalletFiltersCatalog.staticPanelOptions(maturityMonths, brokerageOptions)

            val availableBrokerageIds = brokerageOptions.map { it.id.id }.toSet()
            val selectedBrokerage =
                filters.selectedBrokerage?.takeIf { it.id in availableBrokerageIds }

            val tableData = tableRows.await().map { HoldingHistoryView(it) }

            val previousValue = tableData.sumOf { it.previousValue }
            val actualValue = tableData.sumOf { it.currentValue }
            val contributions = tableData.sumOf { it.totalContributions }
            val withdrawals = tableData.sumOf { it.totalWithdrawals }

            val (growth, growthPercent) = Growth.calculate(previousValue, actualValue, contributions, withdrawals)
            val (earnings, earningsPercent) = Appreciation.calculate(previousValue, actualValue, contributions, withdrawals)

            state.update {
                it.copy(
                    tableData = tableData,
                    walletFilterOptions = walletFilterOptions,
                    walletFilters = filters.copy(selectedBrokerage = selectedBrokerage),
                    summaryProperties = SummaryProperties(
                        previousValue = previousValue,
                        actualValue = actualValue,
                        contributions = contributions,
                        withdrawals = withdrawals,
                        growth = growth,
                        growthPercent = growthPercent,
                        earnings = earnings,
                        earningsPercent = earningsPercent,
                    ),
                )
            }
        }
    }
}

internal sealed interface HistoryIntent {
    data object LoadInitialData : HistoryIntent
    data object Sync : HistoryIntent
    data object ExportFixedIncomeCsv : HistoryIntent
    data object ImportB3File : HistoryIntent
    data class UpdateEntryValue(val entry: HoldingHistoryEntry, val value: Double) : HistoryIntent
    data class SelectPeriod(val period: YearMonth) : HistoryIntent
    data class WalletFiltersChanged(val filters: WalletFiltersUiState) : HistoryIntent
}
