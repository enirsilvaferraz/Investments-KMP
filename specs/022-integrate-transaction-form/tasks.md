# Tasks: Integração do Formulário de Transações

**Input**: Design documents de `specs/022-integrate-transaction-form/`

**Princípio guia**: simplicidade de código e legibilidade; *menos é mais*.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode executar em paralelo (arquivos diferentes, sem dependências pendentes)
- **[Story]**: user story correspondente (US1, US2, US3)
- Caminhos absolutos omitidos para legibilidade — prefixo base: `core/`

---

## Phase 1: Setup

**Propósito**: sem setup extra — projeto existente. Apenas verificar pré-condições.

- [X] T001 Confirmar que `AssetTransactionDataSource.getAllByHolding(holding)` está acessível em `data/database/src/commonMain/kotlin/com/eferraz/database/datasources/AssetTransactionDataSource.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Propósito**: pilha de persistência atômica e modelos de UI. **Nenhuma user story pode começar antes desta fase.**

**⚠️ CRÍTICO**: fases 3–5 dependem desta fase estar completa.

- [X] T002 Adicionar método `suspend fun upsertWithTransactions(holding: AssetHolding)` à interface `domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetHoldingRepository.kt`
- [X] T003 Adicionar assinatura `suspend fun saveWithTransactions(assetHolding: AssetHolding)` à interface `data/database/src/commonMain/kotlin/com/eferraz/database/datasources/AssetHoldingDataSource.kt`
- [X] T004 Implementar `saveWithTransactions` em `data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/AssetHoldingDataSourceImpl.kt`: injetar `AssetTransactionDataSource`; anotar com `@Transaction`; lógica: `save(holding)` → `getAllByHolding` → diff → `delete(orphans)` → `save(each transaction)` (ver data-model.md §DataSource)
- [X] T005 Implementar delegação em `data/repositories/src/commonMain/kotlin/com/eferraz/repositories/AssetHoldingRepositoryImpl.kt`: `override suspend fun upsertWithTransactions(holding) = dataSource.saveWithTransactions(holding)`
- [X] T006 Criar `domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/SaveAssetWithTransactionsUseCase.kt`: `Param(holding: AssetHolding)`; `execute` chama `UpsertAssetUseCase(holding.asset)` → `assetHoldingRepository.upsertWithTransactions(holding.copy(asset = savedAsset))`; visibilidade `public` (ver data-model.md §Use case)
- [X] T007 [P] Criar `domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/SaveAssetWithTransactionsUseCaseTest.kt` com 4 cenários MockK: `GIVEN_valid_holding_WHEN_save_THEN_upserts_asset_and_holding`; `GIVEN_upsert_asset_fails_WHEN_save_THEN_holding_not_called`; `GIVEN_empty_transactions_WHEN_save_THEN_succeeds`; `GIVEN_holding_with_removed_transaction_ids_WHEN_save_THEN_upsertWithTransactions_receives_final_list` (cobre exclusão de órfãos via diff no DataSource — US3 cenário 3; princípio V — obrigatório)
- [X] T008 Migrar `TransactionDraftUi` + `hasAnyFieldError()` + `syncVariableIncomeTotal()` + companion para o final de `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt`; adicionar campos `val transactions: List<TransactionDraftUi> = emptyList()` e `val saveError: String? = null` a `AssetManagementUiState`; atualizar `partialResetForAssetClass` incluindo `transactions = emptyList(), saveError = null` (FR-011)
- [X] T009 [P] Adicionar 7 eventos de transação ao sealed interface em `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementEvents.kt`: `TransactionAdded`, `TransactionRemoved`, `TransactionDateChanged`, `TransactionTypeChanged`, `TransactionQuantityChanged`, `TransactionUnitPriceChanged`, `TransactionTotalValueChanged` (ver data-model.md §AssetManagementEvents)

**Checkpoint**: camada de dados atômica pronta + modelos de UI atualizados — implementação das user stories pode começar.

---

## Phase 3: User Story 1 — Salvar ativo + transações de forma atômica (Priority: P1) 🎯 MVP

**Goal**: o botão "Salvar" persiste ativo + posição + transações em uma única operação atômica; erros de validação bloqueiam o save e mantêm a tela aberta.

**Independent Test**: Abrir tela de novo investimento → preencher campos obrigatórios → adicionar 1 transação válida → clicar "Salvar" → verificar que ativo + posição + transação foram persistidos sem abrir diálogo auxiliar.

- [X] T010 [US1] Atualizar construtor de `AssetManagementViewModel` em `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`: injetar `SaveAssetWithTransactionsUseCase` e `GetCurrentDateUseCase`; remover `UpsertAssetHoldingUseCase`
- [X] T011 [US1] Substituir `onSave` em `AssetManagementViewModel.kt` pelo fluxo atômico: guard `isSaving`; `checkErrors(state)`; `transactions.any { hasAnyFieldError() }`; `isSaving = true`; mapear rascunhos → `domainTransactions`; `buildHolding().copy(transactions = domainTransactions)` → `SaveAssetWithTransactionsUseCase(Param(holding))`; sucesso: `isCompleted = true`; falha: `isSaving = false, saveError = error.message` (ver contracts/asset-management-viewmodel.md §Fluxo Save)
- [X] T012 [P] [US1] Registrar `SaveAssetWithTransactionsUseCase` com `@Factory` Koin em `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/di/AssetManagementModule.kt`
- [X] T013 [P] [US1] Atualizar botão "Salvar" em `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementScreen.kt`: `enabled = !ui.isSaving`; adicionar exibição de `ui.saveError` (Snackbar ou texto de erro na barra inferior)

**Checkpoint**: cadastro de novo ativo com transações funciona de ponta a ponta de forma atômica.

---

## Phase 4: User Story 2 — Gestão de múltiplas transações inline (Priority: P2)

**Goal**: o card de Transações na tela permite adicionar e remover transações sem abrir diálogos, com campos adaptados à `assetClass` selecionada e resposta visual imediata.

**Independent Test**: Na tela de gestão de ativos → clicar "Adicionar" 3 vezes → verificar 3 linhas na tabela com data pré-preenchida → remover a linha do meio → verificar que as outras 2 permanecem.

- [X] T014 [US2] Implementar handlers de eventos de transação em `AssetManagementViewModel.kt`: `TransactionAdded` (adiciona rascunho com data atual); `TransactionRemoved(index)` (remove da lista); `TransactionDateChanged`, `TransactionTypeChanged`, `TransactionQuantityChanged`, `TransactionUnitPriceChanged`, `TransactionTotalValueChanged` (atualizam item por index, `syncVariableIncomeTotal` para RV)
- [X] T015 [US2] Reescrever `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt` para conter **somente** `internal fun TransactionFormContent(transactions, assetClass, onAdd, onRemove, onDateChanged, onTypeChanged, onQuantityChanged, onUnitPriceChanged, onTotalValueChanged, modifier)` — sem ViewModel interno; extrair tabela + botão "Adicionar" do `TransactionFormView` existente; **deletar** `TransactionFormDialog` e `TransactionFormView` do mesmo arquivo; colunas condicionais por `assetClass` (FR-010)
- [X] T016 [US2] Substituir `TransactionFormView(holdingId = 1, onComplete = {})` por `TransactionFormContent(transactions = ui.transactions, assetClass = ui.assetClass, ...)` em `AssetManagementScreen.kt`, passando callbacks via `vm.dispatch`; remover qualquer `FormCardActions` duplicado de transações
- [X] T017 [P] [US2] Deletar `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt`
- [X] T018 [P] [US2] Deletar `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementEvents.kt`
- [X] T019 [P] [US2] Deletar `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementUiState.kt`

**Checkpoint**: card de transações funciona inline com add/remove/edição e campos ajustados por `assetClass`; arquivos obsoletos removidos.

---

## Phase 5: User Story 3 — Edição de investimento existente com transações carregadas (Priority: P3)

**Goal**: ao abrir a tela de edição de um holding existente, as transações previamente cadastradas aparecem no card, ordenadas por data, prontas para edição.

**Independent Test**: Selecionar um holding com transações cadastradas na tela de histórico → verificar que a tela de edição exibe as transações no card dentro de 1 s; editar uma linha e clicar "Salvar" → verificar persistência.

- [X] T020 [US3] No handler de `ScreenEntered(holdingId)` em `AssetManagementViewModel.kt`: após carregar holding via `GetAssetHoldingUseCase.ById`, mapear `holding.transactions.sortedBy { it.date }` → `TransactionDraftUi.fromDomain(tx, assetClass)` e popular `state.transactions` (ordenadas por data — US3 cenário 1; SC-003)
- [X] T021 [P] [US3] Atualizar `apps/umbrellaApp/src/commonMain/kotlin/com/eferraz/investments/App.kt`: substituir `backStack += TransactionManagementRouting(holdingId)` por `backStack += AssetManagementRouting(holdingId)` no callback `onTransactionManagerRequest`; remover `entry<TransactionManagementRouting> { ... }`; remover imports de `TransactionManagementRouting` e `TransactionFormDialog`
- [X] T022 [P] [US3] Atualizar `apps/umbrellaApp/src/commonMain/kotlin/com/eferraz/investments/AppRoutes.kt`: remover `subclass(TransactionManagementRouting::class, TransactionManagementRouting.serializer())` do `SerializersModule` e import correspondente
- [X] T023 [P] [US3] Deletar `presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/di/TransactionManagementRouting.kt`

**Checkpoint**: fluxo completo de edição funciona; rota legada removida; `onTransactionManagerRequest` do histórico abre `AssetManagementDialog`.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T024 [P] Verificar ausência de referências remanescentes aos símbolos deletados: buscar `TransactionManagementRouting`, `TransactionFormDialog`, `TransactionManagementViewModel`, `TransactionFormView` no projeto — confirmar zero ocorrências fora de git history
- [X] T025 [P] Verificar visibilidade explícita em todos os novos símbolos: `SaveAssetWithTransactionsUseCase` e `Param` → `public`; `TransactionDraftUi` → `internal`; `TransactionFormContent` → `internal`; `AssetManagementUiState` campos novos → sem modificador extra (já `internal` via data class)
- [X] T026 Sincronizar `AGENTS.md` se houver referência ao `TransactionFormDialog` ou `TransactionManagementRouting` como componente ativo

---

## Dependências e Ordem de Execução

### Dependências entre fases

- **Phase 1 (Setup)**: sem dependências — pode começar imediatamente
- **Phase 2 (Foundational)**: depende de Phase 1 — **bloqueia todas as user stories**
- **Phase 3 (US1)**: depende de Phase 2 completa (T002–T009)
- **Phase 4 (US2)**: depende de Phase 2 (T008–T009 para UiState/Events) e Phase 3 concluída (ViewModel base pronto)
- **Phase 5 (US3)**: depende de Phase 3 (ViewModel wiring) — T021/T022/T023 podem correr em paralelo com Phase 4
- **Phase 6 (Polish)**: depende de todas as user stories

### Dependências dentro das fases

```
T002 → T003 → T004 → T005 → T006   (cadeia de dados + use case, em sequência)
T007 [P] após T006                 (testes do use case)
T008 → T009 [P]                    (UiState antes dos Events)
T010 → T011                        (ViewModel wiring antes do save)
T012, T013 [P] após T010           (Koin e Screen independentes)
T014 → T015 → T016                 (handlers → composable → screen)
T017/T018/T019 [P] após T015       (deletes)
T020 → T021/T022/T023 [P]          (load impl antes da nav cleanup)
```

### Oportunidades de paralelismo

```bash
# Phase 2 — após T002-T006 em sequência:
Task T007: "Criar SaveAssetWithTransactionsUseCaseTest.kt"     # paralelo após T006
Task T008: "Migrar TransactionDraftUi → AssetManagementUiState.kt"
Task T009: "Adicionar eventos de transação ao AssetManagementEvents.kt"  # paralelo com T008

# Phase 3 — após T011:
Task T012: "Registrar Koin SaveAssetWithTransactionsUseCase"    # paralelo
Task T013: "Atualizar botão Salvar + saveError na Screen"       # paralelo

# Phase 4 — após T015:
Task T017: "Deletar TransactionManagementViewModel.kt"          # paralelo
Task T018: "Deletar TransactionManagementEvents.kt"             # paralelo
Task T019: "Deletar TransactionManagementUiState.kt"            # paralelo

# Phase 5 — após T020:
Task T021: "Atualizar App.kt"                                   # paralelo
Task T022: "Atualizar AppRoutes.kt"                             # paralelo
Task T023: "Deletar TransactionManagementRouting.kt"            # paralelo
```

---

## Estratégia de Implementação

### MVP (apenas User Story 1)

1. Completar Phase 2 — Foundational (T001–T009)
2. Completar Phase 3 — US1 (T010–T013)
3. **PARAR e VALIDAR**: testa cadastro de novo ativo com transação atômica
4. Continuar para US2 e US3 se o MVP funcionar

### Entrega Incremental

1. Phase 2 → cadeia de dados atômica pronta
2. Phase 3 (US1) → save funcionando → validar SC-001 e SC-004
3. Phase 4 (US2) → card inline com add/remove → validar SC-002
4. Phase 5 (US3) → carregamento de existentes + nav redirect → validar SC-003 e SC-005
5. Phase 6 → polish e verificações finais

---

## Notas

- Testes (`SaveAssetWithTransactionsUseCaseTest`) são **obrigatórios** por constituição (princípio V) — não pular
- Não executar `./gradlew` para validar após cada tarefa (princípio IX) — revisão de código é suficiente
- Tasks de delete (T017–T019, T023) só executar após confirmar zero referências nos arquivos que ficam
- `[P]` = arquivos diferentes, sem dependência de task pendente no mesmo arquivo
