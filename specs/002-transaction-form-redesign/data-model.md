# Phase 1 — Data Model

**Feature**: 002-transaction-form-redesign
**Date**: 2026-05-19
**Scope**: estado **de UI** do dialog redesenhado. Não há alterações no modelo de domínio (`:domain:entity`) nem em entidades persistidas (`:data:database`).

---

## Entidades de UI (camada de apresentação)

### `TransactionManagementUiState`

Pacote: `com.eferraz.asset_management.transactions`
Visibilidade: `internal`
Imutabilidade: `@Immutable` (mantida).

| Campo | Tipo | Origem | Notas |
|---|---|---|---|
| `holding` | `AssetHolding?` | `GetAssetHoldingUseCase` | `null` enquanto carrega. |
| `transactions` | `List<TransactionDraftUi>` | Lista *editada* visível na UI | Lista corrente exibida; muta a cada edição/adição/remoção. |
| `initialSnapshot` | `List<TransactionDraftUi>` | `GetTransactionsByHoldingUseCase` (após ordenar por data ascendente) | **NOVO**. Cópia imutável usada para o diff. **Nunca** muta após `loadInitialState`. |
| `isSaving` | `Boolean` | `onSave` | `true` durante a chamada aos UseCases. |
| `isCompleted` | `Boolean` | `onSave` (sucesso) | Dispara o `LaunchedEffect` que invoca `onComplete`/`onDismiss`. |

**Propriedades derivadas**:

| Nome | Tipo | Fórmula |
|---|---|---|
| `category` | `InvestmentCategory` | `holding?.asset?.category ?: InvestmentCategory.FIXED_INCOME` (já existia). |
| `isDirty` | `Boolean` | `transactions.size != initialSnapshot.size` **ou** existe `i` tal que `!transactions[i].matchesByPosition(initialSnapshot[i], category)`. |

**Invariantes**:

- `initialSnapshot` é definido **apenas** dentro de `loadInitialState`. Demais transições (`addTransactionDraft`, `updateDraft`, `deleteDraft`) **não** alteram `initialSnapshot`.
- Quando `isSaving == true`, todos os eventos de edição/adição/remoção e o próprio `Save` são ignorados (`if (state.value.isSaving) return@launch` já presente em `onSave`; estender o guard às demais transições é recomendado mas não obrigatório).

---

### `TransactionDraftUi`

Pacote: `com.eferraz.asset_management.transactions`
Visibilidade: `internal`
Imutabilidade: `@Immutable` (mantida).

Sem mudanças no shape do data class. Removem-se entradas relacionadas a `observations` na **UI** (campo continua existindo internamente para preservar o round-trip):

| Campo | Tipo | Mudança vs. estado atual |
|---|---|---|
| `id` | `Long?` | sem mudança |
| `category` | `InvestmentCategory` | sem mudança |
| `isNew` | `Boolean` | sem mudança |
| `dateDigits` | `String` | sem mudança |
| `type` | `TransactionType` | sem mudança |
| `quantity` | `String` | sem mudança |
| `unitPrice` | `String` | sem mudança |
| `totalValue` | `String` | sem mudança |
| `observations` | `String` | **Não é exibido nem editável** (FR-005a). Preservado em `fromDomain` e copiado de volta em `toDomainTransaction`. |

**Helper novo** (em `TransactionManagementUiState.kt`):

```kotlin
internal fun TransactionDraftUi.matchesByPosition(
    other: TransactionDraftUi,
    category: InvestmentCategory,
): Boolean
```

Comparação:

- `dateDigits == other.dateDigits`
- `type == other.type`
- `totalValue == other.totalValue`
- Quando `category == InvestmentCategory.VARIABLE_INCOME`:
  - `quantity == other.quantity`
  - `unitPrice == other.unitPrice`
- **Não** compara: `id`, `isNew`, `observations`, `category`.

> O `category` é parâmetro porque a lista pode ter zero linhas (sem `firstOrNull`) e o diff continua a precisar saber se aplica regras de renda variável; em qualquer caso o `category` é único por holding.

---

## Entidades de domínio (sem alterações)

- `AssetTransaction` (sealed interface) e suas subclasses (`FixedIncomeTransaction`, `VariableIncomeTransaction`, `FundsTransaction`) permanecem **intactas**.
- `AssetHolding`, `Asset`, `InvestmentCategory`, `TransactionType` permanecem **intactos**.

---

## Eventos (`TransactionManagementEvents`)

Pacote: `com.eferraz.asset_management.transactions`
Visibilidade: `internal sealed class`

| Evento | Mantido | Mudança |
|---|---|---|
| `ScreenEntered(holdingId)` | sim | nenhuma |
| `Save` | sim | nenhuma |
| `AddTransactionDraft` | sim | continua acrescentando `TransactionDraftUi(isNew = true, dateDigits = currentDate, category = state.value.category)` ao final |
| `DraftTransactionDateChanged(index, raw)` | sim | nenhuma |
| `DraftTransactionTypeChanged(index, type)` | sim | nenhuma |
| `DraftTransactionQuantityChanged(index, value)` | sim | nenhuma |
| `DraftTransactionUnitPriceChanged(index, value)` | sim | nenhuma |
| `DraftTransactionTotalValueChanged(index, value)` | sim | nenhuma |
| `DraftTransactionDeleteClicked(index)` | sim | semântica muda: **não** chama `DeleteTransactionUseCase`; apenas remove da lista exibida. A exclusão no banco ocorre no `Save`. |
| `DraftTransactionObservationChanged(index, value)` | **remover** | sem UI correspondente; o campo deixa de ser editável. |

---

## Transições de estado

```
[Vazio]
   │ ScreenEntered(holdingId)
   ▼
loadInitialState:
   - resolve `holding` (GetAssetHoldingUseCase)
   - carrega transações (GetTransactionsByHoldingUseCase)
   - ordena por data ascendente
   - mapeia para `TransactionDraftUi.fromDomain`
   - `state = UiState(holding, transactions, initialSnapshot = transactions, isSaving=false, isCompleted=false)`

[Carregado]
   │ AddTransactionDraft
   │   state.transactions += blank(category, today)
   │
   │ DraftTransaction*Changed(index, ...)
   │   state.transactions[index] = state.transactions[index].copy(...)
   │
   │ DraftTransactionDeleteClicked(index)
   │   state.transactions = state.transactions.filterIndexed { i, _ -> i != index }
   │   (sem chamada a DeleteTransactionUseCase)
   ▼
[Editado]
   │ derivado: isDirty = !matchesByPosition(initialSnapshot, transactions, category)
   │
   │ Save (só quando isDirty && !isSaving)
   ▼
onSave (best effort, sem rollback):
   - isSaving = true
   - removeIds = initialSnapshot.mapNotNull{it.id}.toSet() - transactions.mapNotNull{it.id}.toSet()
   - upserts   = transactions.mapNotNull { it.toDomainTransaction(holding, category) }
   - runCatching {
       removeIds.forEach { deleteTransactionUseCase(Param(it)).getOrThrow() }
       upserts.forEach   { saveTransactionUseCase(Param(it)).getOrThrow() }
     }
   ├─ onSuccess → state = state.copy(isSaving = false, isCompleted = true)
   └─ onFailure → state = state.copy(isSaving = false)  // mantém transactions/initialSnapshot
```

Não há transição que **modifique** `initialSnapshot` fora de `loadInitialState`. Em sucesso de Save, o dialog fecha (via `onComplete`) e a próxima abertura recarrega o snapshot diretamente do banco.

---

## Validações / Regras

| Regra | Implementação |
|---|---|
| `dateError` | `localDateFromIsoDateDigits(dateDigits) == null` (já existe). |
| `quantityError` | `category == VARIABLE_INCOME && quantity.toDoubleOrNull() == null` (já existe). |
| `unitPriceError` | `category == VARIABLE_INCOME && unitPrice.toDoubleOrNull() == null` (já existe). |
| `totalValueError` | `category != VARIABLE_INCOME && totalValue.toDoubleOrNull() == null` (já existe). |
| Linha inválida no Save | `toDomainTransaction` retorna `null` → linha é silenciosamente ignorada (`mapNotNull`). Mesmo comportamento do estado atual. |
| Habilitação do Save | `state.isDirty && !state.isSaving`. |
