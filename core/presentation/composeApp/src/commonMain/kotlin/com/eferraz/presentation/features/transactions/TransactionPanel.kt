package com.eferraz.presentation.features.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.presentation.design_system.components.FormTextField
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TransactionPanel(
    modifier: Modifier = Modifier,
    selectedHolding: AssetHolding?,
    viewModel: TransactionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Carrega transações quando um holding é selecionado
    LaunchedEffect(selectedHolding) {
        if (selectedHolding != null) {
            viewModel.processIntent(TransactionIntent.LoadTransactions(selectedHolding))
        } else {
            viewModel.processIntent(TransactionIntent.ClearSelection)
        }
    }

    if (selectedHolding == null) {
        return
    }

    val asset = selectedHolding.asset
    val transactions = state.transactions.map { it.toTransactionRow() }

    Column(
        modifier = modifier
            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Cabeçalho
        Text(
            text = "Transações",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo Descrição do Ativo (somente leitura)
        FormTextField(
            label = "Descrição",
            value = asset.name,
            onValueChange = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Corretora (somente leitura)
        FormTextField(
            label = "Corretora",
            value = selectedHolding.brokerage.name,
            onValueChange = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Divider()

        Spacer(modifier = Modifier.height(24.dp))

        // Tabela de Transações
        TransactionTable(
            modifier = Modifier.fillMaxWidth(),
            transactions = transactions,
            asset = asset
        )

        Spacer(modifier = Modifier.height(24.dp))

        Divider()

        Spacer(modifier = Modifier.height(24.dp))

        // Formulário de Nova Transação
        TransactionForm(
            modifier = Modifier.fillMaxWidth(),
            formData = state.formData,
            asset = asset,
            validationErrors = state.validationErrors,
            onUpdateType = { type ->
                viewModel.processIntent(TransactionIntent.UpdateTransactionType(type))
            },
            onUpdateDate = { date ->
                viewModel.processIntent(TransactionIntent.UpdateDate(date))
            },
            onUpdateQuantity = { quantity ->
                viewModel.processIntent(TransactionIntent.UpdateQuantity(quantity))
            },
            onUpdateUnitPrice = { unitPrice ->
                viewModel.processIntent(TransactionIntent.UpdateUnitPrice(unitPrice))
            },
            onUpdateTotalValue = { totalValue ->
                viewModel.processIntent(TransactionIntent.UpdateTotalValue(totalValue))
            },
            onSave = {
                viewModel.processIntent(TransactionIntent.SaveTransaction)
            }
        )

        // Mensagem de sucesso
        state.successMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

