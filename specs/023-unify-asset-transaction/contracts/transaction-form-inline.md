# Contrato: Formulário inline de transações (`AssetManagementScreen`)

Escopo **único** de UI conforme FR-011. Composable stateless existente: `TransactionFormContent`.

**Arquivos**: `TransactionManagementView.kt`, `AssetManagementUiState.kt`, `AssetManagementViewModel.kt`, `AssetManagementScreen.kt`

---

## API do composable (inalterada na superfície)

```kotlin
@Composable
internal fun TransactionFormContent(
    transactions: List<TransactionDraftUi>,
    assetClass: AssetClass,
    onAdd: () -> Unit,
    onRemove: (index: Int) -> Unit,
    onDateChanged: (index: Int, digits: String) -> Unit,
    onTypeChanged: (index: Int, type: TransactionType) -> Unit,
    onQuantityChanged: (index: Int, value: String) -> Unit,
    onUnitPriceChanged: (index: Int, value: String) -> Unit,
    onTotalValueChanged: (index: Int, value: String) -> Unit,  // no-op ou removível
    modifier: Modifier = Modifier,
)
```

---

## Layout uniforme (SC-004)

Cabeçalho e linhas **sempre** exibem: Data | Transação | Qtde | Valor Unit. | Valor Total.

Sem coluna ou campo de observações.

---

## Regras por `assetClass`

### `AssetClass.VARIABLE_INCOME`

| Campo | Estado |
|-------|--------|
| Quantidade | Editável; `KeyboardType.Number` (sem decimal) |
| Preço unitário | Editável |
| Valor total | `readOnly = true`; actualizado via `syncTotal()` no ViewModel |

### `AssetClass.FIXED_INCOME` | `AssetClass.INVESTMENT_FUND`

| Campo | Estado |
|-------|--------|
| Quantidade | Valor `"1"`; `readOnly = true` |
| Preço unitário | Editável |
| Valor total | `readOnly = true`; `1 × unitPrice` |

---

## ViewModel — fluxo de actualização

```
TransactionQuantityChanged / TransactionUnitPriceChanged
  → updateTransactionDraft
  → draft.syncTotal()   // TODAS as classes, não só RV
  → state.transactions[index] = draft
```

**Novos rascunhos** (`TransactionAdded`):
- RV: qty vazia, unitPrice vazio, total vazio
- RF/Fundos: qty `"1"`, unitPrice vazio, total vazio

**Carregamento** (`fromDomain`):
- Mapear `tx.quantity`, `tx.unitPrice`, `tx.totalValue.toString()`
- RF/Fundos legados migrados: qty=1, unitPrice=total histórico

---

## Save (`AssetManagementViewModel.onSave`)

1. `transactions.mapNotNull { it.toDomainTransaction(assetClass) }` → `List<AssetTransaction>`
2. `toDomainTransaction` constrói **sempre** `AssetTransaction(id, date, type, quantity, unitPrice)` — sem `when` por classe excepto forçar qty=1 na UI para RF/Fundos
3. Remover gate `hasAnyFieldError()` para erros de qty/price/total (manter só erros de ativo/posicionamento via `checkErros()`)
4. Delegar a `SaveAssetWithTransactionsUseCase` (feature 022) — sem alteração de assinatura do use case além do tipo de transação

---

## Fora do escopo (FR-011)

- ~~`TransactionFormDialog` / `TransactionViewModel` em `:features:composeApp`~~ — pacote `features/transactions/` **removido**; cadastro/edição só via `AssetManagementScreen`.
- Card Resumo, IncomeTax, Excluir transação

---

## Previews

Actualizar `TransactionFormPreviewProvider`:
- RF/Fundos: `quantity = "1"`, `unitPrice = "5000.00"`, `totalValue = "5000.00"`
- RV: inalterado (qty + unitPrice + total derivado)
