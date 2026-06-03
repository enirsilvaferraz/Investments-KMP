# Tasks: 017-holding-transactions — Transações embutidas na posição

**Input**: Design documents from `/specs/017-holding-transactions/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/HoldingTransactionsContract.md`, `quickstart.md`

**Tests**: Atualizar/criar testes em `:domain:usecases` e `:domain:entity` (princípio V) — `MergeHistoryUseCaseTest`, `GetHistoryTableDataUseCaseTest`, `TransactionBalanceTest`, testes de Save/Delete se existirem. `./gradlew` opcional para agentes (princípio IX).

**Organization**: Fase fundacional **sequencial** (entity — breaking change); onda 1 **D ∥ U** após checkpoint; onda 2 **P1 ∥ P2** após onda 1; user stories por prioridade da spec.

## Execução por subagentes (paralelismo)

| Subagente | Tarefas | Quando |
|-----------|---------|--------|
| **E — Entity** | T003–T009 | Onda 0 — **sem** paralelo interno em `transactions/` |
| **D — Database** | T010–T016 | Onda 1 — **T010 (port U) antes de T011**; T011–T015 ∥ U após T010; T016 após hidratação |
| **U — Use cases** | T010, T017–T024 | Onda 1 — T010 port primeiro; T017–T024 ∥ D |
| **P1 — composeApp** | T033, T036 | Onda 2 — após T029–T030 (US4 escrita) |
| **P2 — asset-management** | T034–T035 | Onda 2 — ∥ P1 |
| **Integrador** | T037–T040 | Após onda 2 (polish + verificação) |

### Prompts (Task tool)

**E — Entity** (T003–T009):
```text
Feature 017-holding-transactions. Read specs/017-holding-transactions/contracts/HoldingTransactionsContract.md.
Add AssetHolding.transactions (default emptyList). Remove holding from AssetTransaction and FixedIncome/VariableIncome/FundsTransaction.
Update DOMAIN.md §3 and §9.3 (unidirectional holding → transactions). Fix TransactionBalanceTest. Minimal diff, no Gradle.
```

**D — Database** (T010–T016):
```text
Feature 017. After entity wave: internal hydrator in AssetHoldingDataSourceImpl (sort date↑ id↑), batch load in getAll (R7). TransactionMappers.toDomain without holding; toEntity uses holdingId param. HoldingHistoryDataSourceImpl uses hydrated holding. AssetTransactionRepositoryImpl only upsert/delete/getById. List methods stay internal on AssetTransactionDataSource only.
```

**U — Use cases** (T010, T017–T024):
```text
Feature 017. Slim AssetTransactionRepository port first. MergeHistoryUseCase: drop AssetTransactionRepository; filter holding.transactions for month. GetHistoryTableDataUseCase: drop GetTransactionsByHoldingUseCase; use result.holding.transactions. Delete GetTransactionsByHoldingUseCase.kt. Save/Delete Param(holding, …). Update jvmTests. No list calls from usecases.
```

**P1 / P2** (T033–T036):
```text
Feature 017. TransactionViewModel and TransactionManagementViewModel: load via GetAssetHoldingUseCase → holding.transactions. UiState builds drafts without holding inside transaction. Save/Delete pass Param(holding, transaction/id).
```

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Paralelizável (ficheiros diferentes; requer checkpoint anterior)
- **[Story]**: US1–US4 conforme `spec.md`
- **[SA-E]**, **[SA-D]**, **[SA-U]**, **[SA-P1]**, **[SA-P2]**: Subagente recomendado

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Alinhar branch, contrato e decisões antes da onda 0.

- [X] T001 Confirmar branch `017-holding-transactions` e ler `specs/017-holding-transactions/contracts/HoldingTransactionsContract.md`
- [X] T002 [P] Revisar hidratação R1/R7 e port mínimo R4 em `specs/017-holding-transactions/research.md` e `specs/017-holding-transactions/data-model.md`

---

## Phase 2: Foundational (Blocking) — Subagente E

**Purpose**: Modelo de domínio e documentação. **Bloqueia** onda 1 (D/U/P). **Não marcar [P]** entre T004–T007 (mesmo pacote `transactions`).

**Checkpoint**: `AssetHolding.transactions` existe; `AssetTransaction` sem `holding`; `DOMAIN.md` atualizado.

- [X] T003 [SA-E] Adicionar `transactions: List<AssetTransaction> = emptyList()` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/AssetHolding.kt`
- [X] T004 [SA-E] Remover `holding` de `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/AssetTransaction.kt`
- [X] T005 [SA-E] Remover parâmetro/propriedade `holding` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/FixedIncomeTransaction.kt`
- [X] T006 [SA-E] Remover parâmetro/propriedade `holding` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/VariableIncomeTransaction.kt`
- [X] T007 [SA-E] Remover parâmetro/propriedade `holding` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/FundsTransaction.kt`
- [X] T008 [P] [SA-E] Atualizar `core/domain/entity/docs/DOMAIN.md` (§3, §9.3, tabela de relações): grafo **posição → transações** sem seta inversa
- [X] T009 [SA-E] Atualizar `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/TransactionBalanceTest.kt` (mocks sem `transaction.holding`; lista passada diretamente a `calculate`)

**Checkpoint fundacional**: Iniciar **onda 1** (T010–T024).

---

## Phase 3: User Story 1 — Histórico mensal com movimentações (Priority: P1) 🎯 MVP

**Goal**: Consulta por mês devolve **histórico → posição → transações** (lista completa, ordenada); tabela de Histórico usa uma leitura agregada (FR-004, FR-004a, SC-001, SC-002).

**Independent Test**: `GetHoldingHistoriesUseCase(ByReferenceDate)` ou `MergeHistoryUseCase` para mês com posição conhecida — `entry.holding.transactions.size` e ids iguais ao armazenamento; `GetHistoryTableDataUseCase` filtra mês localmente sem `GetTransactionsByHoldingUseCase`.

### Data — Subagente D (T011–T016)

- [X] T010 [US1] [SA-U] Reduzir `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetTransactionRepository.kt` a `upsert(holding, transaction)`, `delete(holding, id)`, `getById(holding, id)` — **bloqueia T011–T012**
- [X] T011 [US1] [SA-D] Adicionar `@Relation` `transactions: List<TransactionWithDetails>` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/entities/holdings/AssetHoldingWithDetails.kt` e queries `getByIdWithDetails` / `getByAssetIdWithDetails` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/daos/AssetHoldingDao.kt` (R1)
- [X] T012 [P] [US1] [SA-D] Criar `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/HoldingMappers.kt` (`AssetHoldingWithDetails.toDomain` + ordenação FR-007) e refatorar `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/AssetHoldingDataSourceImpl.kt` para usar só DAOs `@Transaction` com relação (sem `getAllByHoldingId` na hidratação)
- [X] T013 [P] [US1] [SA-D] Atualizar `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/TransactionMappers.kt`: `toDomain()` sem `AssetHolding`; `toEntity(holdingId: Long)` ou equivalente na escrita
- [X] T014 [US1] [SA-D] Garantir `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/HoldingHistoryDataSourceImpl.kt` devolve `HoldingHistoryEntry` com `holding` já hidratado (via `getById` ou helper partilhado)
- [X] T015 [P] [US1] [SA-D] Alinhar `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/AssetTransactionRepositoryImpl.kt` ao port T010 (sem delegar listagens públicas)
- [X] T016 [US1] [SA-D] Adicionar teste jvmTest ou helper de integração leve em `core/domain/usecases/src/jvmTest/kotlin/` que valida hidratação: posição com N transações → mesmos ids (SC-001) — **opcional executar Gradle**

### Use cases — Subagente U (T017–T021, paralelo com T011–T015 após T010)

- [X] T017 [P] [US1] [SA-U] Remover `AssetTransactionRepository` de `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/MergeHistoryUseCase.kt`; usar `holding.transactions.filter { it.date in monthRange }` para `TransactionBalance`/`Appreciation`
- [X] T018 [US1] [SA-U] Atualizar `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt`: remover `GetTransactionsByHoldingUseCase`; filtrar `result.holding.transactions` pelo `referenceDate` do param
- [X] T019 [P] [US1] [SA-U] Confirmar `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/cruds/GetHoldingHistoriesUseCase.kt` devolve entradas com posição hidratada (ajustar só se teste falhar)
- [X] T020 [P] [US1] [SA-U] Atualizar `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/MergeHistoryUseCaseTest.kt` (holdings com `transactions`; sem mock de listagem no repo de transações); se `mapByReferenceDate` falhar por igualdade de `AssetHolding`, chavear `Map` por `holding.id` em `MergeHistoryUseCase.kt`
- [X] T021 [P] [US1] [SA-U] Atualizar `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/GetHistoryTableDataUseCaseTest.kt` (dados via `holding.transactions` nos fixtures)

**Checkpoint US1**: Leitura de histórico/tabela sem segunda query de transações no domínio.

> **Compilação (US4)**: Após T009, `SaveTransactionUseCase` / `DeleteTransactionUseCase` deixam de compilar sem **T029–T030** e **T027**. Executar Phase 6 (T029–T032) logo após T010–T015, **antes** da Phase 7 (apresentação).

---

## Phase 4: User Story 2 — Consistência em outras consultas de histórico (Priority: P2)

**Goal**: `getByHoldingAndReferenceDate`, `getByGoalAndReferenceDate`, export e persistência de snapshot mantêm o mesmo contrato de posição hidratada (FR-005).

**Independent Test**: Mesma posição consultada por mês global, por posição+mês e por meta+mês — listas `holding.transactions` idênticas (quickstart §3).

- [X] T022 [US2] [SA-D] Validar `getByGoalAndReferenceDate` e `getByHoldingAndReferenceDate` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/HoldingHistoryDataSourceImpl.kt` usam o mesmo caminho de hidratação que T014
- [X] T023 [P] [US2] [SA-U] Rever `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/services/ExportToCsvUseCase.kt` e `GetGoalHistoryUseCase.kt` — consumir `entry.holding` já com `transactions` se necessário para export
- [X] T024 [P] [US2] [SA-U] Atualizar testes afetados em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/services/ExportToCsvUseCaseTest.kt` e factories em `core/domain/usecases/src/jvmTest/kotlin/` (`TestDataFactory`, `holdingHistoryEntry`, `createAssetHolding` com `transactions` quando relevante)

**Checkpoint US2**: Todos os caminhos de `HoldingHistoryRepository` devolvem posição com lista completa.

---

## Phase 5: User Story 3 — Descontinuar leitura isolada de transações (Priority: P2)

**Goal**: Zero referências a `GetTransactionsByHoldingUseCase` / `GetTransactionsUseCase`; port sem listagens (FR-012, FR-013, SC-006, SC-007).

**Independent Test**: `rg 'GetTransactionsByHoldingUseCase|GetTransactionsUseCase' core/` sem resultados; `rg 'getAllByHolding|getByReferenceDate' core/domain/usecases/` sem resultados em repositório.

- [X] T025 [US3] [SA-U] Apagar `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/GetTransactionsByHoldingUseCase.kt` e referências Koin geradas — **após T018, T033 e T034** (consumidores já migrados)
- [X] T026 [US3] [SA-D] Remover métodos de listagem remanescentes em `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/AssetTransactionRepositoryImpl.kt` (complementa T015); listagens só em `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/AssetTransactionDataSource.kt` (internal)
- [X] T027 [P] [US3] [SA-D] Ajustar `core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/AssetTransactionDataSourceImpl.kt`: `upsert`/`delete`/`getById` recebem `holding`/`holdingId`; `toDomain` alinhado a T013
- [X] T028 [US3] [SA-U] Auditar `core/domain/usecases/` — nenhum use case chama `getAllByHolding`, `getAllByHoldingAndDateRange`, `getByReferenceDate` no port (incl. `SyncB3HistoryUseCase`, `CreateHistoryUseCase`, `CopyHistoryStrategy` se aplicável)

**Checkpoint US3**: API de leitura consolidada na posição.

---

## Phase 6: User Story 4 — Transação sem referência à posição (Priority: P3)

**Goal**: Escrita com `Param(holding, transaction)`; UI e estratégias não constroem `transaction.holding` (FR-008a, FR-011, SC-005).

**Independent Test**: Inspecionar entidade e chamadas Save/Delete — transação sem propriedade posição; persistência usa `holding.id` explícito.

- [X] T029 [US4] [SA-U] Alterar `SaveTransactionUseCase.Param` para `(holding, transaction)` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/SaveTransactionUseCase.kt` e delegar `upsert(holding, transaction)` ao repositório
- [X] T030 [US4] [SA-U] Alterar `DeleteTransactionUseCase.Param` para `(holding, id)` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/DeleteTransactionUseCase.kt`
- [X] T031 [P] [US4] [SA-U] Atualizar `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/holdings/CopyHistoryStrategy.kt` e `CreateHistoryUseCase.kt` se ainda instanciam transações com `holding =`
- [X] T032 [P] [US4] [SA-U] Adicionar ou atualizar testes jvmTest de Save/Delete em `core/domain/usecases/src/jvmTest/kotlin/` com `Param(holding, …)` se existirem; senão criar teste mínimo `SaveTransactionUseCaseTest` / `DeleteTransactionUseCaseTest`

**Checkpoint US4**: Grafo de escrita alinhado ao contrato.

---

## Phase 7: Presentation — Subagentes P1 ∥ P2 (após Phase 5–6)

**Purpose**: Migrar consumidores que injetavam `GetTransactionsByHoldingUseCase` (FR-014).

**Pré-requisito**: T029–T030 concluídos (Save/Delete com `Param(holding, …)`).

- [X] T033 [P] [US3] [SA-P1] Atualizar `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/transactions/TransactionViewModel.kt`: remover `GetTransactionsByHoldingUseCase`; carregar `GetAssetHoldingUseCase` → `holding.transactions`; `SaveTransactionUseCase.Param(holding, transaction)`
- [X] T034 [P] [US3] [SA-P2] Atualizar `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt`: lista de `resolved.transactions`; Save/Delete com posição no param
- [X] T035 [US4] [SA-P2] Atualizar `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementUiState.kt`: fabricar `FixedIncomeTransaction`/`VariableIncomeTransaction`/`FundsTransaction` **sem** `holding`; manter `holding` no estado da UI
- [X] T036 [P] [US3] [SA-P1] Atualizar `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/transactions/TransactionPanel.kt` / `TransactionIntent.kt` se propagam transações com `holding` embutido

**Checkpoint**: Ecrãs de transações e histórico funcionais com uma leitura de posição.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Documentação, verificação estática, limpeza.

- [X] T037 [P] Atualizar `AGENTS.md` se mencionar leitura isolada de transações — grafo **histórico → posição → transações**
- [X] T038 Executar verificações `rg` de `specs/017-holding-transactions/quickstart.md` §1 (SC-005, SC-006, SC-007)
- [X] T039 [P] Revisar `core/domain/usecases/src/jvmTest/kotlin/` — factories `createAssetHolding` / transações de teste sem `holding` na transação
- [X] T041 [P] Adicionar teste de regressão SC-005 em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/` que falha se `AssetTransaction` voltar a expor `holding` (reflexão ou compilação de fixture)
- [X] T040 (Opcional) `./gradlew :domain:usecases:jvmTest` — só se utilizador pedir validação (princípio IX)

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 (Setup)
    ↓
Phase 2 (Foundational — Entity)  ← BLOQUEIA tudo
    ↓
Phase 3 (US1 — hidratação + leitura)  ← MVP
    ↓
Phase 4 (US2)  ← pode sobrepor final da US1 (D)
    ↓
Phase 5 (US3) + Phase 6 (US4)  ← US4 escrita pode paralelizar com US3 após T010
    ↓
Phase 7 (Presentation)
    ↓
Phase 8 (Polish)
```

### User Story Dependencies

| Story | Depende de | Notas |
|-------|------------|-------|
| **US1** | Phase 2 | MVP — hidratação + MergeHistory + HistoryTable |
| **US2** | US1 (hidratação base) | Mesmo código de datasource; validação de caminhos |
| **US3** | US1 | Remoção de use case após consumidores de leitura migrados no domínio |
| **US4** | Phase 2 + T010, T027 | Modelo em Phase 2; escrita T029–T030 após port/datasource; UI em Phase 7 |

### Within Onda 1 (US1)

1. **T010** port (U) → **T011–T015** (D) + **T017–T021** (U) em paralelo
2. **T016** teste hidratação após T011–T014

### Parallel Opportunities

- **T002** ∥ leitura inicial
- **T008** ∥ T009 após T007
- **T012–T013** ∥ **T017–T019** (após T010 e Phase 2)
- **T020–T021** ∥ **T022–T024** (ficheiros de teste diferentes)
- **T033** ∥ **T034–T035** (módulos Gradle diferentes)
- **T037–T039** polish em paralelo

---

## Parallel Example: Onda 1 (após T009)

```bash
# Port primeiro (bloqueante):
T010 AssetTransactionRepository.kt

# Em paralelo:
T011–T015  # database hydrator
T017–T019  # MergeHistory + GetHistoryTableData + GetHoldingHistories

# Depois:
T020–T021  # jvmTests US1
```

---

## Parallel Example: Onda 2 (após T028)

```bash
T033  # composeApp TransactionViewModel
T034 + T035  # asset-management VM + UiState
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 + Phase 2 (Setup + Entity)
2. Phase 3 (US1) — hidratação + use cases de histórico/tabela
3. **STOP**: `rg` + testes jvmTest sob pedido
4. Demo: ecrã Histórico com aportes/resgates corretos

### Incremental Delivery

1. US1 → histórico mensal completo (MVP)
2. US2 → paridade em meta/posição/export
3. US4 (T029–T030) → escrita antes da apresentação
4. US3 (T025–T028) → remoção use case após migrar consumidores
5. Phase 7 → VMs
6. Phase 8 → polish

### Parallel Team Strategy

| Dev | Fase |
|-----|------|
| A | E: T003–T009 |
| B + C | Após T009: B=D (T011–T016), C=U (T010, T017–T021) |
| B + C | Após T024: B=P1 (T033), C=P2 (T034–T035) |

---

## Notes

- **Build Gradle**: tarefas com `./gradlew` são opcionais para agentes (constituição IX)
- **[P]**: ficheiros diferentes; evitar dois subagentes no mesmo `.kt`
- `GetTransactionsUseCase` já removido no working tree — não reintroduzir (T025 cobre `GetTransactionsByHoldingUseCase`)
- Filtro por mês de transação permanece no **consumidor** (`GetHistoryTableDataUseCase`, `MergeHistoryUseCase`), não na hidratação (FR-004a)
- Commit após cada checkpoint ou grupo lógico (opcional: `/speckit.git.commit`)
