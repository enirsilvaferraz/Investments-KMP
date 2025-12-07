package com.eferraz.presentation.design_system.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.eferraz.presentation.design_system.theme.successContainerLight
import com.eferraz.presentation.design_system.theme.successLight
import com.eferraz.presentation.design_system.theme.warningContainerLight
import com.eferraz.presentation.design_system.theme.warningLight
import kotlinx.coroutines.delay

/**
 * Estado visual do componente AutoComplete
 */
internal enum class AutoCompleteState {
    /** Campo vazio ou em edição (sem correspondência verificada) */
    NORMAL,

    /** O nome digitado corresponde exatamente a um item existente na lista */
    SUCCESS,

    /** O nome digitado não corresponde a nenhum item existente na lista */
    WARNING
}

/**
 * Componente AutoComplete que permite digitação livre com sugestões.
 *
 * @param value Valor atual do campo
 * @param onValueChange Callback chamado quando o valor muda
 * @param suggestions Lista de sugestões disponíveis
 * @param label Label do campo
 * @param placeholder Placeholder do campo (opcional)
 * @param enabled Se o campo está habilitado
 * @param modifier Modifier para customização
 * @param onItemCreated Callback opcional chamado quando um novo item precisa ser criado (estado Warning)
 * @param debounceDelay Delay em milissegundos para validação (padrão: 400ms)
 * @param maxSuggestions Número máximo de sugestões a exibir (padrão: 15)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AutoComplete(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onItemCreated: ((String) -> Unit)? = null,
    debounceDelay: Long = 400,
    maxSuggestions: Int = 15,
) {
    var expanded by remember { mutableStateOf(false) }
    var validationState by remember(value) { mutableStateOf(AutoCompleteState.NORMAL) }
    var hasFocus by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Filtra sugestões baseado no texto digitado (case-insensitive, busca parcial)
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) emptyList()
        else suggestions.filter { it.contains(value, ignoreCase = true) }.take(maxSuggestions)
    }

    // Validação com debounce
    LaunchedEffect(value) {
        if (value.isBlank()) {
            validationState = AutoCompleteState.NORMAL
            expanded = false
            return@LaunchedEffect
        }

        // Debounce
        delay(debounceDelay)

        // Verifica se ainda é o valor atual (evita race conditions)
        if (value.isBlank()) {
            validationState = AutoCompleteState.NORMAL
            return@LaunchedEffect
        }

        // Valida correspondência exata (case-insensitive, ignorando espaços no início/fim)
        val trimmedValue = value.trim()
        val matches = suggestions.any {
            it.trim().equals(trimmedValue, ignoreCase = true)
        }

        validationState = if (matches) {
            AutoCompleteState.SUCCESS
        } else {
            AutoCompleteState.WARNING
        }
    }

    // Controla expansão do menu baseado em foco e sugestões filtradas
    LaunchedEffect(hasFocus, filteredSuggestions, value) {
        expanded = hasFocus && filteredSuggestions.isNotEmpty() && value.isNotBlank()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        // Campo de entrada
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // Mostra sugestões imediatamente ao digitar se houver correspondências
                if (newValue.isNotBlank()) {
                    val newFiltered = suggestions
                        .filter { it.contains(newValue, ignoreCase = true) }
                        .take(maxSuggestions)
                    if (newFiltered.isNotEmpty() && hasFocus) {
                        expanded = true
                    }
                } else {
                    expanded = false
                }
            },
            label = { Text(label) },
            placeholder = if (placeholder.isNotBlank()) { { Text(placeholder) } } else null,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                    if (!focusState.isFocused) {
                        expanded = false
                    }
                },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when (validationState) {
                    AutoCompleteState.SUCCESS -> successLight
                    AutoCompleteState.WARNING -> warningLight
                    AutoCompleteState.NORMAL -> MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = when (validationState) {
                    AutoCompleteState.SUCCESS -> successLight.copy(alpha = 0.7f)
                    AutoCompleteState.WARNING -> warningLight.copy(alpha = 0.7f)
                    AutoCompleteState.NORMAL -> MaterialTheme.colorScheme.outline
                },
                focusedContainerColor = when (validationState) {
                    AutoCompleteState.SUCCESS -> successContainerLight.copy(alpha = 0.1f)
                    AutoCompleteState.WARNING -> warningContainerLight.copy(alpha = 0.1f)
                    AutoCompleteState.NORMAL -> MaterialTheme.colorScheme.surface
                },
                unfocusedContainerColor = when (validationState) {
                    AutoCompleteState.SUCCESS -> successContainerLight.copy(alpha = 0.05f)
                    AutoCompleteState.WARNING -> warningContainerLight.copy(alpha = 0.05f)
                    AutoCompleteState.NORMAL -> MaterialTheme.colorScheme.surface
                },
            ),
            shape = MaterialTheme.shapes.medium,
        )

        // Lista de sugestões
        if (filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                            keyboardController?.hide()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

/**
 * Função auxiliar para validar e criar novo item quando necessário.
 * Deve ser chamada antes de salvar o formulário.
 *
 * @param value Valor atual do campo
 * @param suggestions Lista de sugestões existentes
 * @param onItemCreated Callback para criar o novo item
 * @return true se o valor é válido e pode ser salvo, false caso contrário
 */
internal fun validateAndCreateItemIfNeeded(
    value: String,
    suggestions: List<String>,
    onItemCreated: (String) -> Unit,
): Boolean {
    val trimmedValue = value.trim()

    // Valida se o nome não está vazio
    if (trimmedValue.isBlank()) {
        return false
    }

    // Verifica se já existe
    val exists = suggestions.any {
        it.trim().equals(trimmedValue, ignoreCase = true)
    }

    // Se não existe, cria o novo item
    if (!exists) {
        onItemCreated(trimmedValue)
    }

    return true
}
