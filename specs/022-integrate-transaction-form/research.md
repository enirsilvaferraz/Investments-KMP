# Research: Feature 022 — Integração do Formulário de Transações

**Princípio guia (input do utilizador):** simplicidade de código e legibilidade; *menos é mais*.

---

## 1. Atomicidade Room cross-DAOs + diff interno

**Decisão:** Adicionar `saveWithTransactions(holding)` a `AssetHoldingDataSourceImpl` anotado com `@Transaction`. Internamente o método:
1. Salva o holding (upsert).
2. Consulta as transações existentes no banco via `assetTransactionDataSource.getAllByHolding(holding)` — método já disponível na interface.
3. Calcula `toDelete = existingIds − incomingIds` (diff).
4. Deleta os orphans e faz upsert das transações recebidas.

Nenhuma informação de "o que deletar" precisa vir de fora — o DataSource consulta e decide sozinho.

**Racional:** Toda a responsabilidade de sincronização fica onde o dado existe (camada de dados). O chamador só diz "o estado final é este". `@Transaction` garante rollback total em falha. `getAllByHolding` já existe — sem novo método de leitura.

**Alternativas rejeitadas:**
- Calcular `toDeleteIds` no ViewModel + passar como parâmetro → vaza lógica de dados para a camada de apresentação; exige `initialSnapshot` no estado.
- Expor `withTransaction { }` wrapper → infraestrutura extra sem benefício adicional.
- Loop no ViewModel sem transação → sem rollback global.

---

## 2. Novo port no domínio (`AssetHoldingRepository`)

**Decisão:** Adicionar `suspend fun upsertWithTransactions(holding: AssetHolding)` à interface `AssetHoldingRepository` existente (`:domain:usecases`). O método recebe apenas o holding **com `transactions` populado** — sem `toDeleteIds`.

**Racional:** API mínima; o chamador declara o estado final desejado. A responsabilidade de calcular o diff fica inteiramente na implementação de dados.

**Alternativa rejeitada:** `upsertWithTransactions(holding, toDeleteIds)` → vaza detalhe de persistência (quais IDs deletar) para o domínio, que não deve saber como o banco gerencia orphans.

---

## 3. Use case de save unificado

**Decisão:** Criar `SaveAssetWithTransactionsUseCase` em `:domain:usecases` com `Param(holding: AssetHolding)` — parâmetro único. `AssetHolding` já contém `asset`, `transactions` e toda a informação necessária. Internamente:
1. `UpsertAssetUseCase(holding.asset)` → obtém `asset` com `id` populado.
2. `assetHoldingRepository.upsertWithTransactions(holding.copy(asset = savedAsset))`.

**Racional:** Um único parâmetro — o estado final completo do holding. Sem redundância: o asset não precisa ser passado separadamente quando já está dentro do holding. API mínima.

**Alternativas rejeitadas:**
- `Param(asset, holding)` → `asset` é redundante com `holding.asset`; o chamador teria que manter dois campos sincronizados.
- `Param(asset, holding, toDeleteIds)` → máxima redundância; obriga o ViewModel a rastrear snapshot e calcular diffs.

---

## 4. Remoção de `TransactionManagementRouting` e limpeza do pacote `transactions/`

**Decisão:** Deletar `TransactionManagementRouting` e toda a pilha que só ela sustentava:
- `TransactionManagementRouting.kt` — deletado
- `TransactionFormDialog` — deletado (sem callers externos após remoção da rota)
- `TransactionManagementViewModel.kt` — deletado (sem callers)
- `TransactionManagementEvents.kt` — deletado (eventos migrados para `AssetManagementEvents`)
- `TransactionManagementUiState.kt` — deletado (`TransactionDraftUi` já movido para `assets/`)

**Redirecionamento:** `onTransactionManagerRequest` em `HoldingHistoryRoute` passa a chamar `backStack += AssetManagementRouting(holdingId = holdingId)` — a mesma rota usada para editar o holding. O `AssetManagementDialog` agora é o único ponto de entrada para gerir ativo + posição + transações.

**Em `App.kt`:** remover `entry<TransactionManagementRouting>`, import de `TransactionManagementRouting` e `TransactionFormDialog`.

**Em `AppRoutes.kt`:** remover `subclass(TransactionManagementRouting::class, ...)` do `SerializersModule` e import correspondente.

**Pacote `transactions/`:** fica apenas com `TransactionManagementView.kt` renomeado/reduzido para conter só `TransactionFormContent` (o composable stateless criado nesta feature).

**Racional:** Eliminar a rota redundante simplifica o grafo de navegação — um único diálogo para edição completa do holding. "Menos é mais": menos rotas, menos ViewModels, menos arquivos. O SC-005 original preocupava-se com regressão no `TransactionFormDialog`; ao delegar à `AssetManagementDialog` o mesmo fluxo, não há regressão funcional — o utilizador ainda consegue gerir transações a partir do histórico.

---

## 5. Localização de `TransactionDraftUi`

**Decisão:** Mover `TransactionDraftUi` e funções auxiliares (`hasAnyFieldError`, `syncVariableIncomeTotal`) **para dentro de `AssetManagementUiState.kt`** — sem arquivo separado. Como `TransactionManagementUiState.kt` será deletado (decisão §4), não há necessidade de compatibilidade retroativa.

**Racional:** `TransactionDraftUi` é o modelo de UI de transações do `AssetManagementViewModel`; vive naturalmente junto com `AssetManagementUiState`, que já declara os campos `transactions: List<TransactionDraftUi>`. Um arquivo a menos. "Menos é mais".

---

## 6. Composable de transações stateless

**Decisão:** Criar `TransactionFormContent` (composable stateless) em `TransactionManagementView.kt` — único arquivo remanescente do pacote `transactions/`. Recebe `transactions: List<TransactionDraftUi>`, `assetClass`, callbacks de evento — sem ViewModel interno. `AssetManagementScreen` passa estado e eventos do `AssetManagementViewModel` diretamente.

**Racional:** Elimina o `holdingId = 1` hardcoded; a tela fica com uma única fonte de estado. Com a remoção de `TransactionFormDialog`, `TransactionManagementView.kt` se torna um arquivo pequeno e focado.

---

## 7. Testes obrigatórios (princípio V)

**Decisão:** Criar `SaveAssetWithTransactionsUseCaseTest.kt` em `:domain:usecases:jvmTest` com MockK para `UpsertAssetUseCase` e `AssetHoldingRepository`.

Cenários mínimos:
- `GIVEN_valid_asset_and_transactions_WHEN_save_THEN_upserts_asset_and_holding_with_transactions`
- `GIVEN_upsert_asset_fails_WHEN_save_THEN_holding_not_called`
- `GIVEN_empty_transactions_WHEN_save_THEN_succeeds_with_empty_list`

---

## Resumo de decisões

| Área | Decisão |
|---|---|
| Atomicidade | `@Transaction` no `AssetHoldingDataSource` |
| Port | Novo método em `AssetHoldingRepository` (sem nova interface) |
| Use case | `SaveAssetWithTransactionsUseCase` — orquestração mínima |
| ViewModel | Mesclar no `AssetManagementViewModel`; manter `TransactionManagementViewModel` para histórico |
| `TransactionDraftUi` | Mover para pacote `assets/`; importar no pacote `transactions/` |
| UI | Composable stateless `TransactionFormContent` |
