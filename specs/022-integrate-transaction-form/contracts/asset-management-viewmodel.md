# Contrato: `AssetManagementViewModel` + Navegação (pós-feature 022)

Este documento descreve o contrato público interno (visibilidade `internal`) do ViewModel unificado após a integração de transações.

---

## Estado: `AssetManagementUiState`

**Arquivo único:** `assets/AssetManagementUiState.kt` — contém `AssetManagementUiState` + `TransactionDraftUi` + funções auxiliares (sem arquivo separado para `TransactionDraftUi`).

```kotlin
// ── AssetManagementUiState ──────────────────────────────────────────────────
internal data class AssetManagementUiState(
    // campos de ativo e posicionamento (existentes, sem alteração)
    val asset: Asset? = null,
    val issuers: List<Issuer> = emptyList(),
    val assetClass: AssetClass = AssetClass.FIXED_INCOME,
    // ...

    // controle de fluxo
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val saveError: String? = null,     // NOVO

    // transações (NOVO)
    val transactions: List<TransactionDraftUi> = emptyList(),
)

// ── TransactionDraftUi (migrado de TransactionManagementUiState.kt) ─────────
internal data class TransactionDraftUi(
    val id: Long? = null,
    val assetClass: AssetClass,
    val isNew: Boolean = false,
    val dateDigits: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val quantity: String = "",
    val unitPrice: String = "",
    val totalValue: String = "",
    val observations: String = "",
    // + propriedades derivadas: dateError, quantityError, unitPriceError, totalValueError
    // + companion: fromDomain(), toDomainTransaction()
)

internal fun TransactionDraftUi.hasAnyFieldError(): Boolean = ...
internal fun TransactionDraftUi.syncVariableIncomeTotal(): TransactionDraftUi = ...
```

**Sem `initialSnapshot`:** o cálculo de quais transações deletar é responsabilidade do DataSource (consulta o banco e faz o diff). O ViewModel só mantém o estado atual da tela.

**Invariantes:**
- `isSaving = true` → botão Salvar desabilitado (FR-005)
- `isCompleted = true` → tela fecha via `LaunchedEffect`
- `saveError != null` → exibir mensagem de erro; tela permanece aberta com dados preservados
- `partialResetForAssetClass(assetClass)` → limpa `transactions` e `saveError` (FR-011)

---

## Eventos: `AssetManagementEvents`

```kotlin
internal sealed interface AssetManagementEvents {

    // — Ciclo de vida (existente) —
    data class ScreenEntered(val holdingId: Long?) : AssetManagementEvents

    // — Campos do ativo (existentes) —
    data class AssetClassChanged(val assetClass: AssetClass) : AssetManagementEvents
    // ... demais eventos de ativo/posicionamento ...
    data object Save : AssetManagementEvents

    // — Transações (NOVO — migrado de TransactionManagementEvents) —
    data class TransactionAdded(val assetClass: AssetClass) : AssetManagementEvents
    data class TransactionRemoved(val index: Int) : AssetManagementEvents
    data class TransactionDateChanged(val index: Int, val digits: String) : AssetManagementEvents
    data class TransactionTypeChanged(val index: Int, val type: TransactionType) : AssetManagementEvents
    data class TransactionQuantityChanged(val index: Int, val value: String) : AssetManagementEvents
    data class TransactionUnitPriceChanged(val index: Int, val value: String) : AssetManagementEvents
    data class TransactionTotalValueChanged(val index: Int, val value: String) : AssetManagementEvents
}
```

---

## ViewModel

```kotlin
@KoinViewModel
internal class AssetManagementViewModel(
    private val getIssuersUseCase: GetIssuersUseCase,
    private val upsertAssetUseCase: UpsertAssetUseCase,          // mantido (usado internamente por SaveAssetWithTransactionsUseCase)
    private val getBrokeragesUseCase: GetBrokeragesUseCase,
    private val getAssetHoldingUseCase: GetAssetHoldingUseCase,
    private val upsertAssetHoldingUseCase: UpsertAssetHoldingUseCase,  // removido — substituído por saveAssetWithTransactions
    private val getOwnerUseCase: GetOwnerUseCase,
    private val saveAssetWithTransactionsUseCase: SaveAssetWithTransactionsUseCase,   // NOVO
    private val getCurrentDateUseCase: GetCurrentDateUseCase,                        // NOVO — para pré-preencher data
) : ViewModel()
```

**Nota:** `upsertAssetHoldingUseCase` é **removido** do ViewModel — a persistência de holding passa a ser responsabilidade de `SaveAssetWithTransactionsUseCase`.

---

## Composable stateless: `TransactionFormContent`

```kotlin
// Em TransactionManagementView.kt (pacote transactions/)
internal fun TransactionFormContent(
    transactions: List<TransactionDraftUi>,
    assetClass: AssetClass,
    onAdd: () -> Unit,
    onRemove: (index: Int) -> Unit,
    onDateChanged: (index: Int, digits: String) -> Unit,
    onTypeChanged: (index: Int, type: TransactionType) -> Unit,
    onQuantityChanged: (index: Int, value: String) -> Unit,
    onUnitPriceChanged: (index: Int, value: String) -> Unit,
    onTotalValueChanged: (index: Int, value: String) -> Unit,
    modifier: Modifier = Modifier,
)
```

Sem ViewModel interno. `TransactionFormView` e `TransactionFormDialog` são **deletados** nesta feature — `TransactionFormContent` é o único composable remanescente no pacote `transactions/`.

---

## Fluxo `ScreenEntered(holdingId)`

| holdingId | Comportamento |
|---|---|
| `null` (novo) | Carrega issuers, brokerages, owner; `transactions = []` |
| `id: Long` (edição) | Carrega holding via `GetAssetHoldingUseCase.ById` → popula ativo + posição; mapeia `holding.transactions` para `TransactionDraftUi` → `transactions = [...]` |

---

## Navegação — `onTransactionManagerRequest` (histórico)

```kotlin
// App.kt — antes
onTransactionManagerRequest = { holdingId ->
    backStack += TransactionManagementRouting(holdingId = holdingId)
}

// App.kt — depois (feature 022)
onTransactionManagerRequest = { holdingId ->
    backStack += AssetManagementRouting(holdingId = holdingId)
}
```

`TransactionManagementRouting` e `entry<TransactionManagementRouting>` são removidos de `App.kt` e `AppRoutes.kt`.

---

## Fluxo `Save`

1. `isSaving` → ignorar (guard)
2. `checkErrors(state)` → se erros de ativo: atualizar estado com erros, retornar
3. `transactions.any { it.hasAnyFieldError() }` → se erros de transação: retornar (sem save)
4. `isSaving = true`, `saveError = null`
5. `domainTransactions = transactions.mapNotNull { it.toDomainTransaction(assetClass) }`
6. `SaveAssetWithTransactionsUseCase(Param(holding.copy(transactions = domainTransactions)))`
   - Use case extrai `holding.asset` internamente para o upsert
   - DataSource calcula internamente quais transações deletar (diff com o banco)
7. Sucesso → `isCompleted = true`
8. Falha → `isSaving = false`, `saveError = error.message`
