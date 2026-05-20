# Contract: `TransactionFormDialog` (UI entrypoint)

**Feature**: 002-transaction-form-redesign
**Module**: `core/presentation/asset-management` (Gradle accessor: `projects.features.assetManagement`)
**Package**: `com.eferraz.asset_management.transactions`

Este contrato descreve a **superfície pública** do dialog redesenhado e o **comportamento observável** das interações. Não é uma API REST: é o contrato de Composables/ViewModel consumido pelo shell `:apps:umbrellaApp` (e qualquer outro chamador interno).

---

## 1. Composables públicos

### 1.1 `TransactionFormDialog`

```kotlin
@Composable
public fun TransactionFormDialog(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onDismiss: () -> Unit,
)
```

- **Pré-condições**: `holdingId` referencia uma holding existente. Comportamento para `holdingId == null` ou inválido está fora do escopo desta feature (Assumption do `spec.md`).
- **Comportamento**:
  - Renderiza o invólucro `AppContentDialog` com título **"Transações"** e botão de fechar (X) no topo.
  - Delega o corpo a `TransactionFormView`.
  - `onDismiss` é chamado:
    1. Quando o utilizador toca no X do `AppContentDialog`.
    2. Quando o save conclui com sucesso (via `onComplete` interno).
- **Pós-condição**: Em sucesso de save, os efeitos colaterais no banco (delete + upsert) foram aplicados. Em falha ou fecho via X, **nenhum** efeito colateral persistente foi aplicado a partir das edições não confirmadas.

### 1.2 `TransactionFormView`

```kotlin
@Composable
public fun TransactionFormView(
    modifier: Modifier = Modifier,
    holdingId: Long?,
    onComplete: () -> Unit,
)
```

- **Comportamento**:
  - Carrega o `TransactionManagementViewModel` via `koinViewModel`.
  - Dispara `ScreenEntered(holdingId)` em `LaunchedEffect(holdingId)`.
  - Observa `state.isCompleted` e invoca `onComplete()` quando `true`.
  - Renderiza, em ordem vertical:
    1. Tabela (cabeçalho + linhas) — sem `UiTableV3`.
    2. Linha de ações: botão "Adicionar" + botão "Salvar".
- **Observação**: a assinatura permanece compatível com a versão atual; nenhum chamador externo precisa mudar.

---

## 2. Layout da tabela (sem `UiTableV3`)

Estrutura:

```
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row { /* Header */ }
    rows.forEach { row -> Row { /* Cells */ } }
}
Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
    OutlinedButton("Adicionar") { onEvent(AddTransactionDraft) }
    Button("Salvar", enabled = state.isDirty && !state.isSaving) { onEvent(Save) }
}
```

Colunas exibidas por categoria:

| Categoria | Data | Transação | Quantidade | Valor Unit. | Valor Total | Ação X |
|---|---|---|---|---|---|---|
| `VARIABLE_INCOME` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| `FIXED_INCOME` | ✓ | ✓ | — | — | ✓ | ✓ |
| `INVESTMENT_FUND` | ✓ | ✓ | — | — | ✓ | ✓ |

Inputs **obrigatórios** por célula (espelhando o protótipo `NewTransactionsTable`):

- Data → `FormTextField` (`label = ""`, `value = row.dateDigits`, `errorMessage = if (row.dateError) "Inválido" else null`).
- Transação → `FormTextField` (`value = row.type.asLabel()`; `onValueChange` faz `TransactionType.entries.firstOrNull { it.asLabel().equals(raw, ignoreCase = true) }` e só dispara o evento se o parse acertar).
- Quantidade / Valor Unitário / Valor Total → `FormTextField` (`value = row.quantity` / `unitPrice` / `totalValue`; mensagens de erro derivadas das flags `quantityError`, `unitPriceError`, `totalValueError`).
- Ação X → `IconButton(Icons.Default.Close)` com `tint = MaterialTheme.colorScheme.error`.

> **NÃO** usar `TableInputDate` / `TableInputSelect` / `TableInputMoney`. O layout segue exatamente o protótipo `NewTransactionsTable` (`Row`/`Column`/`FormTextField`), reaproveitando o helper `FormTextField` já existente em `com.eferraz.asset_management.helpers`. Larguras das colunas: Data `Modifier.width(125.dp)`, Transação `Modifier.width(130.dp)`, Quantidade `Modifier.weight(.5f)`, Valor Unitário `Modifier.weight(1.1f)`, Valor Total `Modifier.weight(1.1f)`. Em `FIXED_INCOME`/`INVESTMENT_FUND` as colunas Quantidade e Valor Unitário são omitidas.

---

## 3. Eventos consumidos (contrato do ViewModel)

`internal sealed class TransactionManagementEvents`:

| Evento | Disparado por | Efeito esperado |
|---|---|---|
| `ScreenEntered(holdingId)` | `LaunchedEffect(holdingId)` na entrada | Carrega holding + transações; preenche `initialSnapshot`. |
| `AddTransactionDraft` | clique em "Adicionar" | Acrescenta linha em branco ao final, com `dateDigits = today`. |
| `DraftTransactionDateChanged(index, raw)` | edição da data | Atualiza `state.transactions[index].dateDigits`. |
| `DraftTransactionTypeChanged(index, type)` | seleção do tipo | Atualiza `state.transactions[index].type`. |
| `DraftTransactionQuantityChanged(index, value)` | edição (renda variável) | Atualiza `state.transactions[index].quantity`. |
| `DraftTransactionUnitPriceChanged(index, value)` | edição (renda variável) | Atualiza `state.transactions[index].unitPrice`. |
| `DraftTransactionTotalValueChanged(index, value)` | edição | Atualiza `state.transactions[index].totalValue`. |
| `DraftTransactionDeleteClicked(index)` | clique no X | Remove linha **da lista exibida**. **Não** chama `DeleteTransactionUseCase`. |
| `Save` | clique em "Salvar" | Persiste delete + upsert em sequência (vide §4). |

**Removidos**: `DraftTransactionObservationChanged` (não há UI correspondente).

---

## 4. Contrato do `Save`

Pré-condições:

- `state.holding != null`.
- `state.isSaving == false`.
- `state.isDirty == true`.

Execução (ordem):

1. `state.isSaving = true`.
2. Calcula `removeIds = initialSnapshot.mapNotNull { it.id }.toSet() - transactions.mapNotNull { it.id }.toSet()`.
3. Calcula `upserts = transactions.mapNotNull { it.toDomainTransaction(holding, category) }`.
4. Em sequência:
   - Para cada `id` em `removeIds`: `deleteTransactionUseCase(DeleteTransactionUseCase.Param(id)).getOrThrow()`.
   - Para cada `tx` em `upserts`: `saveTransactionUseCase(SaveTransactionUseCase.Param(tx)).getOrThrow()`.

Pós-condições:

- **Sucesso** → `state.isSaving = false`, `state.isCompleted = true`. Dialog fecha via `onComplete`/`onDismiss`.
- **Falha** (qualquer exceção lançada na sequência acima) → `state.isSaving = false`. `state.transactions` e `state.initialSnapshot` permanecem **inalterados** em relação ao estado pré-`Save`. Dialog permanece aberto. **Não** há mensagem de erro dedicada.

> Atomicidade: as operações são executadas individualmente nos repositórios (não há transação atómica no contrato atual de `AssetTransactionRepository`). Em caso de falha parcial, o estado persistido pode refletir parte das alterações; o utilizador pode acionar `Save` novamente — `removeIds` e `upserts` serão recalculados a partir do `initialSnapshot` original (que ainda corresponde ao banco do ponto de vista do dialog), permitindo a re-tentativa. Esta limitação é consciente (cf. `research.md` §D3 e `FR-015`).

---

## 5. Comportamento de fecho

- Botão X (`AppContentDialog`) → `onDismiss()`. Não há diálogo de confirmação.
- Quaisquer edições/adições/remoções em rascunho são descartadas.
- Comportamento idêntico ao `AssetManagementDialog`.

---

## 6. Critérios mensuráveis (mapeamento com Success Criteria)

| SC | Verificável por |
|---|---|
| SC-001 | Abrir dialog para holding com N>0 transações exibe as N linhas ordenadas por data ascendente. |
| SC-002 | `isDirty` lido em qualquer momento sem interação após `loadInitialState` deve ser `false`. |
| SC-003 | Qualquer alteração em qualquer linha/conjunto → `isDirty == true`. |
| SC-004 | Fechar via X sem `Save` → nenhuma chamada ocorre a `SaveTransactionUseCase`/`DeleteTransactionUseCase`. Verificável por mock. |
| SC-005 | Após `Save` bem-sucedido, `isCompleted` é setado em até 2 s (depende do banco; o dialog fecha imediatamente quando setado). |
| SC-006 | Buscar `UiTableV3` em `core/presentation/asset-management/.../transactions/*` retorna 0 ocorrências. |
| SC-007 | Em previews/testes manuais com `category ∈ {FIXED_INCOME, INVESTMENT_FUND}`, colunas Quantidade/Valor Unit. **não** aparecem. Com `VARIABLE_INCOME`, aparecem. |
