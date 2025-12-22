package com.eferraz.presentation.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeTransaction
import com.eferraz.entities.FundsTransaction
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.TransactionType
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeTransaction
import com.eferraz.usecases.GetTransactionsByHoldingUseCase
import com.eferraz.usecases.SaveTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.annotation.KoinViewModel
import kotlin.time.Clock

@KoinViewModel
internal class TransactionViewModel(
    private val getTransactionsByHoldingUseCase: GetTransactionsByHoldingUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state = _state.asStateFlow()

    fun processIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.LoadTransactions -> loadTransactions(intent.holding)
            is TransactionIntent.UpdateTransactionType -> updateTransactionType(intent.type)
            is TransactionIntent.UpdateDate -> updateDate(intent.date)
            is TransactionIntent.UpdateQuantity -> updateQuantity(intent.quantity)
            is TransactionIntent.UpdateUnitPrice -> updateUnitPrice(intent.unitPrice)
            is TransactionIntent.UpdateTotalValue -> updateTotalValue(intent.totalValue)
            is TransactionIntent.SaveTransaction -> saveTransaction()
            is TransactionIntent.ClearForm -> clearForm()
            is TransactionIntent.ClearSelection -> clearSelection()
        }
    }

    private fun loadTransactions(holding: AssetHolding) {
        _state.update { it.copy(selectedHolding = holding, isLoading = true) }
        viewModelScope.launch {
            getTransactionsByHoldingUseCase(GetTransactionsByHoldingUseCase.Param(holding))
                .onSuccess { transactions ->
                    _state.update {
                        it.copy(
                            transactions = transactions.sortedByDescending { it.date },
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun updateTransactionType(type: TransactionType?) {
        _state.update { state ->
            val newFormData = state.formData.copy(type = type)
            state.copy(
                formData = newFormData,
                validationErrors = validateFormData(newFormData, state.selectedHolding)
            )
        }
    }

    private fun updateDate(date: String) {
        _state.update { state ->
            val newFormData = state.formData.copy(date = date)
            state.copy(
                formData = newFormData,
                validationErrors = validateFormData(newFormData, state.selectedHolding)
            )
        }
    }

    private fun updateQuantity(quantity: String) {
        _state.update { state ->
            val newFormData = state.formData.copy(quantity = quantity)
            val updatedFormData = if (state.selectedHolding?.asset is VariableIncomeAsset) {
                // Calcula totalValue automaticamente para Renda Variável
                val qty = quantity.toDoubleOrNull() ?: 0.0
                val unitPrice = state.formData.unitPrice.toDoubleOrNull() ?: 0.0
                val totalValue = (qty * unitPrice)
                newFormData.copy(totalValue = if (totalValue > 0) totalValue.toString() else "")
            } else {
                newFormData
            }
            state.copy(
                formData = updatedFormData,
                validationErrors = validateFormData(updatedFormData, state.selectedHolding)
            )
        }
    }

    private fun updateUnitPrice(unitPrice: String) {
        _state.update { state ->
            val newFormData = state.formData.copy(unitPrice = unitPrice)
            val updatedFormData = if (state.selectedHolding?.asset is VariableIncomeAsset) {
                // Calcula totalValue automaticamente para Renda Variável
                val qty = state.formData.quantity.toDoubleOrNull() ?: 0.0
                val price = unitPrice.toDoubleOrNull() ?: 0.0
                val totalValue = (qty * price)
                newFormData.copy(totalValue = if (totalValue > 0) totalValue.toString() else "")
            } else {
                newFormData
            }
            state.copy(
                formData = updatedFormData,
                validationErrors = validateFormData(updatedFormData, state.selectedHolding)
            )
        }
    }

    private fun updateTotalValue(totalValue: String) {
        _state.update { state ->
            val newFormData = state.formData.copy(totalValue = totalValue)
            state.copy(
                formData = newFormData,
                validationErrors = validateFormData(newFormData, state.selectedHolding)
            )
        }
    }

    private fun saveTransaction() {
        val state = _state.value
        val holding = state.selectedHolding ?: return
        val formData = state.formData

        val errors = validateFormData(formData, holding)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            try {
                val transaction = createTransaction(holding, formData)
                saveTransactionUseCase(SaveTransactionUseCase.Param(transaction))
                    .onSuccess {
                        clearForm()
                        loadTransactions(holding) // Recarrega transações
                        _state.update { it.copy(successMessage = "Transação salva com sucesso!") }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(successMessage = "Erro ao salvar: ${e.message}") }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(successMessage = "Erro ao salvar: ${e.message}") }
            }
        }
    }

    private fun createTransaction(
        holding: AssetHolding,
        formData: TransactionFormData
    ): AssetTransaction {

        val date =LocalDate.parse(formData.date) //parseDate(formData.date) ?: throw IllegalArgumentException("Data inválida")
        val type = formData.type ?: throw IllegalArgumentException("Tipo de transação obrigatório")

        return when (holding.asset) {
            is VariableIncomeAsset -> {
                val quantity = formData.quantity.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Quantidade inválida")
                val unitPrice = formData.unitPrice.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Preço unitário inválido")

                VariableIncomeTransaction(
                    id = 0,
                    holding = holding,
                    date = date,
                    type = type,
                    quantity = quantity,
                    unitPrice = unitPrice
                )
            }

            is FixedIncomeAsset -> {
                val totalValue = formData.totalValue.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Valor total inválido")

                FixedIncomeTransaction(
                    id = 0,
                    holding = holding,
                    date = date,
                    type = type,
                    totalValue = totalValue
                )
            }

            is InvestmentFundAsset -> {
                val totalValue = formData.totalValue.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Valor total inválido")

                FundsTransaction(
                    id = 0,
                    holding = holding,
                    date = date,
                    type = type,
                    totalValue = totalValue
                )
            }
        }
    }

    private fun validateFormData(
        formData: TransactionFormData,
        holding: AssetHolding?
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (formData.type == null) {
            errors["type"] = "Campo obrigatório"
        }

        if (formData.date.isBlank()) {
            errors["date"] = "Campo obrigatório"
        } else {
            if (formData.date.length != 10) {
                errors["date"] = "Data inválida. Use o formato YYYY-MM-DD"
            }
        }

        when (holding?.asset) {
            is VariableIncomeAsset -> {
                if (formData.quantity.isBlank()) {
                    errors["quantity"] = "Campo obrigatório"
                } else {
                    val quantity = formData.quantity.toDoubleOrNull()
                    if (quantity == null || quantity <= 0) {
                        errors["quantity"] = "Quantidade deve ser maior que zero"
                    }
                }

                if (formData.unitPrice.isBlank()) {
                    errors["unitPrice"] = "Campo obrigatório"
                } else {
                    val unitPrice = formData.unitPrice.toDoubleOrNull()
                    if (unitPrice == null || unitPrice <= 0) {
                        errors["unitPrice"] = "Preço unitário deve ser maior que zero"
                    }
                }

                // Valida valor total calculado
                val qty = formData.quantity.toDoubleOrNull() ?: 0.0
                val price = formData.unitPrice.toDoubleOrNull() ?: 0.0
                val totalValue = qty * price
                if (totalValue <= 0) {
                    errors["totalValue"] = "Valor total deve ser maior que zero"
                }
            }

            is FixedIncomeAsset, is InvestmentFundAsset -> {
                if (formData.totalValue.isBlank()) {
                    errors["totalValue"] = "Campo obrigatório"
                } else {
                    val totalValue = formData.totalValue.toDoubleOrNull()
                    if (totalValue == null || totalValue <= 0) {
                        errors["totalValue"] = "Valor total deve ser maior que zero"
                    }
                }
            }

            null -> {
                // Sem holding selecionado, não valida campos específicos
            }
        }

        return errors
    }

    private fun parseDate(dateString: String): LocalDate? {
        if (dateString.length != 10) return null
        val parts = dateString.split("/")
        if (parts.size != 3) return null

        return try {
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            LocalDate(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    private fun clearForm() {
        _state.update { state ->
            state.copy(
                formData = TransactionFormData(),
                validationErrors = emptyMap(),
                successMessage = null
            )
        }
    }

    private fun clearSelection() {
        _state.update {
            TransactionState()
        }
    }
}

