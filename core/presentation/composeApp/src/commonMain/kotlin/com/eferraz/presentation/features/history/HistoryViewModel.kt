package com.eferraz.presentation.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentCategory
import com.eferraz.usecases.GetDataPeriodUseCase
import com.eferraz.usecases.GetHistoryTableDataUseCase
import com.eferraz.usecases.MergeHistoryUseCase
import com.eferraz.usecases.SyncVariableIncomeValuesUseCase
import com.eferraz.usecases.UpdateFixedIncomeAndFundsHistoryValueUseCase
import com.eferraz.usecases.entities.HistoryTableData
import com.eferraz.usecases.providers.DateProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class HistoryViewModel(
    dateProvider: DateProvider,
    private val getDataPeriodUseCase: GetDataPeriodUseCase,
    private val getHistoryTableDataUseCase: GetHistoryTableDataUseCase,
    private val mergeHistoryUseCase: MergeHistoryUseCase,
    private val updateFixedIncomeAndFundsHistoryValueUseCase: UpdateFixedIncomeAndFundsHistoryValueUseCase,
    private val updateVariableIncomeValues: SyncVariableIncomeValuesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(selectedPeriod = dateProvider.getCurrentYearMonth()))
    internal val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        processIntent(HistoryIntent.LoadInitialData(null))
    }

    internal fun processIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.SelectPeriod -> selectPeriod(intent.period)
            is HistoryIntent.UpdateEntryValue -> updateEntryValue(intent.entryId, intent.value)
            is HistoryIntent.SelectHolding -> selectHolding(intent.holding)
            is HistoryIntent.LoadInitialData -> loadPeriodData(intent.category)
            is HistoryIntent.Sync -> sync()
        }
    }

    private fun selectPeriod(period: YearMonth) {
        _state.value = _state.value.copy(selectedPeriod = period)
        processIntent(HistoryIntent.LoadInitialData(_state.value.currentCategory))
    }

    private fun selectHolding(holding: AssetHolding?) {
        _state.value = _state.value.copy(selectedHolding = holding)
    }

    private fun updateEntryValue(
        entryId: Long,
        value: Double,
    ) {
        viewModelScope.launch {
            val entry = _state.value.entryMap[entryId]
            if (entry != null) {
                updateFixedIncomeAndFundsHistoryValueUseCase(
                    UpdateFixedIncomeAndFundsHistoryValueUseCase.Params(entry = entry, endOfMonthValue = value)
                ).onSuccess {
                    processIntent(HistoryIntent.LoadInitialData(_state.value.currentCategory))
                }
            }
        }
    }

    private fun sync() {
        viewModelScope.launch {
            updateVariableIncomeValues(
                SyncVariableIncomeValuesUseCase.Param(_state.value.selectedPeriod)
            ).onSuccess {
                processIntent(HistoryIntent.LoadInitialData(_state.value.currentCategory))
            }
        }
    }

    internal fun loadPeriodData(category: InvestmentCategory?) {
        val period = _state.value.selectedPeriod

        viewModelScope.launch {

            val periods = async {
                getDataPeriodUseCase(Unit).getOrNull() ?: emptyList()
            }
            val tableData = async {
                getHistoryTableDataUseCase(GetHistoryTableDataUseCase.Param(period, category)).getOrNull() ?: emptyList()
            }

            // Buscar os resultados originais para manter referência aos entries
            val results = mergeHistoryUseCase(MergeHistoryUseCase.Param(period)).getOrNull() ?: emptyList()
            
            // Criar mapa de entryId para entry (filtrar entries sem ID)
            val entryMap = results
                .mapNotNull { result -> result.currentEntry.id?.let { id -> id to result.currentEntry } }
                .toMap()
            
            // Criar mapa de holdingId para holding
            val holdingMap = results.associate { it.holding.id to it.holding }

            _state.value = _state.value.copy(
                periods = periods.await(),
                tableData = tableData.await(),
                currentCategory = category,
                entryMap = entryMap,
                holdingMap = holdingMap
            )
        }
    }

    internal sealed interface HistoryIntent {
        data class SelectPeriod(val period: YearMonth) : HistoryIntent
        data class UpdateEntryValue(val entryId: Long, val value: Double) : HistoryIntent
        data class SelectHolding(val holding: AssetHolding?) : HistoryIntent
        data class LoadInitialData(val category: InvestmentCategory? = null) : HistoryIntent
        data object Sync : HistoryIntent
    }

    internal data class HistoryState(
        val selectedPeriod: YearMonth,
        val tableData: List<HistoryTableData> = emptyList(),
        val periods: List<YearMonth> = emptyList(),
        val selectedHolding: AssetHolding? = null,
        val currentCategory: InvestmentCategory? = null,
        val entryMap: Map<Long, HoldingHistoryEntry> = emptyMap(),
        val holdingMap: Map<Long, AssetHolding> = emptyMap(),
    )
}

