package com.eferraz.presentation.features.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.TransactionType
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.design_system.components.DateTransformation
import com.eferraz.presentation.design_system.components.EnumDropdown
import com.eferraz.presentation.design_system.components.FormTextField
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.currencyToDouble

@Composable
internal fun TransactionForm(
    modifier: Modifier = Modifier,
    formData: TransactionFormData,
    asset: Asset?,
    validationErrors: Map<String, String>,
    onUpdateType: (TransactionType?) -> Unit,
    onUpdateDate: (String) -> Unit,
    onUpdateQuantity: (String) -> Unit,
    onUpdateUnitPrice: (String) -> Unit,
    onUpdateTotalValue: (String) -> Unit,
    onSave: () -> Unit,
) {
    val isVariableIncome = asset is VariableIncomeAsset
    val isFixedIncomeOrFunds = asset is FixedIncomeAsset || asset is InvestmentFundAsset

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Nova Transação",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // Tipo de Transação
            EnumDropdown(
                label = "Tipo",
                value = formData.type,
                options = TransactionType.entries,
                optionLabel = { type ->
                    when (type) {
                        TransactionType.PURCHASE -> "Compra"
                        TransactionType.SALE -> "Venda"
                        null -> ""
                    }
                },
                onValueChange = onUpdateType,
                errorMessage = validationErrors["type"],
                modifier = Modifier.weight(1f)
            )

            // Data
            FormTextField(
                label = "Data",
                value = formData.date,
                onValueChange = { newValue -> onUpdateDate(newValue) },
                placeholder = "YYYY-MM-DD",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                validationErrors = validationErrors,
                errorKey = "date",
//                visualTransformation = DateTransformation(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campos específicos para Renda Variável
        if (isVariableIncome) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                // Quantidade
                FormTextField(
                    label = "Quantidade",
                    value = formData.quantity,
                    onValueChange = { newValue ->
                        // Permite apenas números e vírgula/ponto
                        val filtered = newValue.filter { it.isDigit() || it == ',' || it == '.' }
                            .replace(',', '.')
                        onUpdateQuantity(filtered)
                    },
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    validationErrors = validationErrors,
                    errorKey = "quantity",
                    modifier = Modifier.weight(1f)
                )

                // Preço Unitário
                FormTextField(
                    label = "Preço Unitário",
                    value = formData.unitPrice,
                    onValueChange = { newValue ->
                        // Permite apenas números e vírgula/ponto
                        val filtered = newValue.filter { it.isDigit() || it == ',' || it == '.' }
                            .replace(',', '.')
                        onUpdateUnitPrice(filtered)
                    },
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    validationErrors = validationErrors,
                    errorKey = "unitPrice",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Valor Total (desabilitado, calculado automaticamente)
            val qty = formData.quantity.toDoubleOrNull() ?: 0.0
            val price = formData.unitPrice.toDoubleOrNull() ?: 0.0
            val calculatedTotal = qty * price

            FormTextField(
                label = "Valor Total",
                value = calculatedTotal.currencyFormat(),
                enabled = false,
                onValueChange = { },
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                validationErrors = validationErrors,
                errorKey = "amount",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Campo Valor Total para Renda Fixa e Fundos
        if (isFixedIncomeOrFunds) {

            val totalValue = formData.totalValue

            FormTextField(
                label = "Valor Total",
                value = totalValue,
                onValueChange = { newValue -> onUpdateTotalValue(newValue) },
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                validationErrors = validationErrors,
                errorKey = "totalValue",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Salvar
        val isFormValid = validationErrors.isEmpty() &&
                formData.type != null &&
                formData.date.isNotBlank() &&
                ((isVariableIncome && formData.quantity.isNotBlank() && formData.unitPrice.isNotBlank()) ||
                        (isFixedIncomeOrFunds && formData.totalValue.isNotBlank()))

        Button(
            onClick = onSave,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }
}

