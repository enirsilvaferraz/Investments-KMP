# Quickstart: Feature 022 — Integração do Formulário de Transações

Guia rápido para implementar a feature do zero, seguindo a ordem de dependências.

---

## Ordem de implementação

```
1.  AssetManagementUiState.kt              ← absorver TransactionDraftUi + adicionar campos
2.  AssetHoldingRepository                 ← novo método no port
3.  AssetHoldingDataSource + Impl          ← @Transaction + diff interno
4.  AssetHoldingRepositoryImpl             ← delega ao DataSource
5.  SaveAssetWithTransactionsUseCase       ← use case atômico
6.  SaveAssetWithTransactionsUseCaseTest   ← testes obrigatórios
7.  AssetManagementUiState                 ← adicionar transactions + saveError
8.  AssetManagementEvents                  ← eventos de transação
9.  AssetManagementViewModel               ← lógica unificada + save
10. TransactionManagementView              ← substituir por TransactionFormContent stateless
11. AssetManagementScreen                  ← usar TransactionFormContent
12. Deletar arquivos obsoletos             ← TransactionManagement* + TransactionManagementRouting
13. App.kt + AppRoutes.kt                  ← redirecionar rota + remover entrada obsoleta
```

---

## Passo 1 — Unificar `TransactionDraftUi` em `AssetManagementUiState.kt`

Em `AssetManagementUiState.kt`, **adicionar ao final do arquivo** o conteúdo migrado de `TransactionManagementUiState.kt`:
- `data class TransactionDraftUi` (com todas as propriedades e companion)
- `fun TransactionDraftUi.hasAnyFieldError()`
- `fun TransactionDraftUi.syncVariableIncomeTotal()`

Também adicionar os novos campos em `AssetManagementUiState`:
- `val transactions: List<TransactionDraftUi> = emptyList()`
- `val saveError: String? = null`

Atualizar `partialResetForAssetClass` para incluir `transactions = emptyList(), saveError = null`.

Sem criar `TransactionDraftUi.kt` — tudo em um único arquivo.

---

## Passo 2 — Port `AssetHoldingRepository`

Em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetHoldingRepository.kt`, adicionar:

```kotlin
public suspend fun upsertWithTransactions(holding: AssetHolding)
```

Sem `toDeleteIds` — o contrato declara apenas o estado final desejado.

---

## Passo 3 — DataSource com `@Transaction` e diff interno

Em `AssetHoldingDataSource.kt`, adicionar:

```kotlin
public suspend fun saveWithTransactions(assetHolding: AssetHolding)
```

Em `AssetHoldingDataSourceImpl.kt`, injetar `AssetTransactionDataSource` e implementar:

```kotlin
@Transaction
override suspend fun saveWithTransactions(assetHolding: AssetHolding) {
    val holdingId = save(assetHolding)
    val existingIds = assetTransactionDataSource.getAllByHolding(assetHolding).map { it.id }.toSet()
    val incomingIds = assetHolding.transactions.map { it.id }.toSet()
    (existingIds - incomingIds).forEach { assetTransactionDataSource.delete(holdingId, it) }
    assetHolding.transactions.forEach {
        assetTransactionDataSource.save(assetHolding.copy(id = holdingId), it)
    }
}
```

O diff (quais IDs deletar) é calculado aqui, onde os dados residem.

---

## Passo 4 — `AssetHoldingRepositoryImpl`

Implementar o novo método delegando ao DataSource:

```kotlin
override suspend fun upsertWithTransactions(holding: AssetHolding) =
    dataSource.saveWithTransactions(holding)
```

---

## Passo 5 — `SaveAssetWithTransactionsUseCase`

Criar em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/SaveAssetWithTransactionsUseCase.kt`.

```kotlin
// Param único — holding já contém asset e transactions
data class Param(val holding: AssetHolding)
```

Ver assinatura completa em [data-model.md](data-model.md).

Registrar no módulo Koin de `:domain:usecases` (se houver `@Single`/`@Factory` automático via KSP, apenas anotar).

---

## Passo 6 — Testes

Criar `SaveAssetWithTransactionsUseCaseTest.kt` com 3 cenários mínimos (ver [research.md](research.md) §7).

---

## Passos 7–9 — Presentation (ViewModel)

Seguir o contrato em [contracts/asset-management-viewmodel.md](contracts/asset-management-viewmodel.md).

Pontos-chave:
- `AssetManagementUiState`: já atualizado no Passo 1 (contém `TransactionDraftUi`, `transactions`, `saveError`)
- `AssetManagementEvents`: adicionar 7 eventos de transação
- `AssetManagementViewModel`: injetar `saveAssetWithTransactionsUseCase` + `getCurrentDateUseCase`; remover `upsertAssetHoldingUseCase`; implementar handlers de transação; substituir `onSave` pelo fluxo atômico sem cálculo de `toDeleteIds`

---

## Passo 10 — `TransactionManagementView.kt` → só `TransactionFormContent`

Reescrever `TransactionManagementView.kt` para conter **apenas** `TransactionFormContent` (stateless, `internal`). Remover `TransactionFormDialog`, `TransactionFormView` e `TransactionFormActions`.

---

## Passo 11 — `AssetManagementScreen`

Substituir:

```kotlin
// Antes
TransactionFormView(holdingId = 1, onComplete = {})

// Depois
TransactionFormContent(
    transactions = ui.transactions,
    assetClass = ui.assetClass,
    onAdd = { vm.dispatch(TransactionAdded(ui.assetClass)) },
    onRemove = { vm.dispatch(TransactionRemoved(it)) },
    onDateChanged = { i, d -> vm.dispatch(TransactionDateChanged(i, d)) },
    // ... demais callbacks
)
```

Atualizar botão Salvar: `enabled = !ui.isSaving`.

Adicionar exibição de `ui.saveError` (Snackbar ou texto de erro na barra inferior).

---

## Passo 12 — Deletar arquivos obsoletos

```
DELETE core/presentation/asset-management/.../transactions/TransactionManagementViewModel.kt
DELETE core/presentation/asset-management/.../transactions/TransactionManagementEvents.kt
DELETE core/presentation/asset-management/.../transactions/TransactionManagementUiState.kt
DELETE core/presentation/asset-management/.../di/TransactionManagementRouting.kt
```

Verificar que nenhum outro arquivo importa esses símbolos antes de deletar.

---

## Passo 13 — `App.kt` e `AppRoutes.kt`

**`App.kt`:**

```kotlin
// Remover entry<TransactionManagementRouting> { ... }

// Alterar callback em entry<HistoryRouting>:
onTransactionManagerRequest = { holdingId ->
    backStack += AssetManagementRouting(holdingId = holdingId)  // era TransactionManagementRouting
}

// Remover imports:
// import com.eferraz.asset_management.di.TransactionManagementRouting
// import com.eferraz.asset_management.transactions.TransactionFormDialog
```

**`AppRoutes.kt`:**

```kotlin
// Remover:
subclass(TransactionManagementRouting::class, TransactionManagementRouting.serializer())
// E import correspondente
```

---

## Verificação rápida (sem Gradle)

Após cada passo, revisar:
- [ ] Visibilidade explícita em todos os novos símbolos (`internal` para UI/ViewModel, `public` para use case e port)
- [ ] Nenhum import de `:data` em `:domain` ou `:features`
- [ ] `TransactionManagementRouting` removido de `App.kt` e `AppRoutes.kt`
- [ ] `onTransactionManagerRequest` no histórico abre `AssetManagementRouting`
- [ ] `TransactionManagementViewModel/Events/UiState` deletados — sem referências remanescentes
- [ ] `partialResetForAssetClass` limpa `transactions` e `saveError`
- [ ] Botão Salvar desabilitado só durante `isSaving`
- [ ] `AssetManagementUiState` **não tem `initialSnapshot`** — o diff está no DataSource
