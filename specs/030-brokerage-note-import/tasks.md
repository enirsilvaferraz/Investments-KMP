# Tasks: Importação de Nota de Corretagem JSON

**Feature**: 030-brokerage-note-import  
**Date**: 2026-06-12  
**Input**: spec.md (US1 P1, US2 P2), plan.md, data-model.md, contracts/kotlin-api.md, research.md

## Format: `[ID] [P?] [Story?] Descrição com caminho de arquivo`

- **[P]**: Pode rodar em paralelo (arquivos distintos, sem dependência de tarefa incompleta)
- **[Story]**: Qual user story esta tarefa pertence (US1, US2)
- Setup e Foundational não levam label de story

---

## Phase 1: Setup

> Projeto existente — nenhuma inicialização necessária. Fase omitida.

---

## Phase 2: Foundational (Pré-requisitos Bloqueantes)

**Propósito**: Entidades de domínio que DEVEM estar concluídas antes de qualquer user story.

**⚠️ CRÍTICO**: Todas as US dependem dessas alterações de entidade.

- [X] T001 Alterar `AssetTransaction`: adicionar `allocatedFee: Double = 0.0`, renomear `totalValue` → `grossValue` (computed), adicionar `netValue` (computed) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/AssetTransaction.kt`
- [X] T002 [P] Criar `BrokerageNoteAsset` (novo tipo `ticker + transaction`) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNoteAsset.kt`
- [X] T003 Alterar `BrokerageNote.assets` de `List<AssetTransaction>` para `List<BrokerageNoteAsset>` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNote.kt`
- [X] T004 Atualizar usos de `totalValue` → `grossValue` nas camadas `:domain:entity` e `:domain:usecases` impactados pela renomeação em T001 — arquivos exatos: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt`, `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/TransactionBalance.kt`, `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt`, `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/CanonicalNoteFixtures.kt`, `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/TransactionBalanceTest.kt`, `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetGoalsMonitoringTableDataUseCase.kt`, `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/GoalsMonitoringTableData.kt`, `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` — **escopo desta tarefa: excluir camada `:features:asset-management` (coberta por T018/T019/T020)**

**Checkpoint**: Entidades de domínio prontas — implementação das user stories pode começar.

---

## Phase 3: User Story 1 — Importar nota e salvar transações com taxa (Priority: P1) 🎯 MVP

**Goal**: JSON de nota de corretagem é carregado, taxas proporcionais calculadas e transações persistidas atomicamente no `AssetHolding` correto (Nubank fixo).

**Independent Test**: Fornecer JSON válido de nota → verificar transações na listagem de posições com campo `allocatedFee` preenchido e `netValue` correto.

### Infraestrutura Room — US1

- [X] T005 [P] [US1] Adicionar coluna `allocatedFee` (REAL, defaultValue = "0") à `AssetTransactionEntity` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/entities/transaction/AssetTransactionEntity.kt`
- [X] T006 [US1] Atualizar `AppDatabase` para versão 11 com `AutoMigration(from = 10, to = 11)` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/core/AppDatabase.kt`
- [X] T007 [US1] Atualizar `TransactionMappers` para mapear `allocatedFee` (domínio ↔ entity) em `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/TransactionMappers.kt`

### Interfaces de Repositório — US1

- [X] T008 [P] [US1] Adicionar `getByTicker(ticker: String): AssetHolding?` à interface `AssetHoldingRepository` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetHoldingRepository.kt`
- [X] T009 [P] [US1] Adicionar `saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>)` à interface `AssetTransactionRepository` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetTransactionRepository.kt`

### Implementações de Dados — US1

- [X] T010 [US1] Implementar `getByTicker` em `AssetHoldingRepositoryImpl` (JOIN com tabela de assets, filtro `brokerage.id == 2`) em `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/AssetHoldingRepositoryImpl.kt`
- [X] T011 [US1] Implementar `saveAll` com `@Transaction` em `AssetTransactionDao` (rollback automático do lote em falha) em `core/data/database/src/commonMain/kotlin/com/eferraz/database/daos/AssetTransactionDao.kt`

### Parser e Alocação de Taxas — US1

- [X] T012 [US1] Adaptar `BrokerageNoteV2Parser` para mapear cada ativo como `BrokerageNoteAsset(ticker, transaction)` em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/BrokerageNoteV2Parser.kt`
- [X] T013 [US1] Adaptar `NoteFeeAllocation.calculate` para extrair `.transaction` de cada `BrokerageNoteAsset` (onde antes iterava `AssetTransaction` diretamente) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt`

### Caso de Uso — US1

- [X] T014 [US1] Criar `ImportBrokerageNoteUseCase` com fluxo completo: loadNote → calculate fees → getByTicker (early-return se null) → saveAll em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/ImportBrokerageNoteUseCase.kt`
- [X] T015 [US1] Verificar todos os usages de `LoadBrokerageNoteUseCase` no codebase (ViewModel, UI, outros módulos), remover `LoadBrokerageNoteUseCase.kt` e atualizar binding em `UseCaseModule` (trocar `Load` → `Import`) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/di/UseCaseModule.kt`

### Testes — US1

- [X] T016 [US1] Criar `ImportBrokerageNoteUseCaseTest` com MockK cobrindo: importação com sucesso, soma de taxas fecha com nota, ticker não encontrado (early-return sem persistência) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/ImportBrokerageNoteUseCaseTest.kt`
- [X] T017 [P] [US1] Criar `Migration10To11Test` seguindo padrão de `Migration9To10Test` (transações existentes preservadas, `allocatedFee = 0.0` nas linhas migradas, banco vazio migra sem erro) em `core/data/database/src/jvmTest/kotlin/com/eferraz/database/migrations/Migration10To11Test.kt`

**Checkpoint**: US1 completa — importação de nota persiste transações com taxa calculada e testável de forma independente.

---

## Phase 4: User Story 2 — Visualizar taxa e valor líquido no formulário (Priority: P2)

**Goal**: Formulário de cadastro/edição de transações exibe coluna "Valor Líq." calculada em tempo real.

**Independent Test**: Abrir formulário de transação existente → confirmar que "Valor Líq." é exibido corretamente (compra: grossValue + taxa; venda: grossValue − taxa; manual: grossValue == netValue).

### UI State — US2

- [X] T018 [US2] Alterar `TransactionDraftUi`: adicionar `allocatedFee: String = "0.0"` e `netValue: String = ""` (somente leitura); renomear `totalValue` → `grossValue` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt`
- [X] T019 [US2] Atualizar `syncTotal()` para calcular `netValue = grossValue ± allocatedFee` (respeitando direção compra/venda) e preencher o campo no `TransactionDraftUi` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`

### View — US2

- [X] T020 [US2] Adicionar coluna "Valor Líq." (somente leitura, após "Valor Total") ao header e às linhas de `TransactionManagementView` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`

**Checkpoint**: US2 completa — formulário de transações exibe valor líquido em 100% das transações.

---

## Phase 5: Polish & Cross-Cutting

- [X] T021 Atualizar `DOMAIN.md` com `BrokerageNoteAsset` (novo tipo) e `AssetTransaction.allocatedFee` (novo campo) em `core/domain/entity/docs/DOMAIN.md`
- [X] T022 [P] Atualizar `AGENTS.md`: registrar `ImportBrokerageNoteUseCase` na seção de módulos (substitui `LoadBrokerageNoteUseCase`) e confirmar grafo de módulos tocados pela feature em `AGENTS.md`

---

## Dependencies (ordem de conclusão das stories)

```
T001 ──────────────────────────────────────────────────────────► T004
T002 ─► T003 ─────────────────────────────────────────────────► T012, T013

Phase 2 (T001–T004) deve completar antes de Phase 3 e Phase 4

Phase 3 — US1:
  T005 ─► T006           (AppDatabase precisa da entity anotada)
  T001 + T005 ─► T007    (TransactionMappers depende de domínio + entity Room)
  T008 ─► T010
  T009 ─► T011
  T012, T013 ─► T014 ─► T015
  T014 ─► T016
  T005 + T006 ─► T017

Phase 4 — US2 (pode começar após Phase 2):
  T018 ─► T019 ─► T020

Phase 5 (após todas as fases):
  T021
```

## Parallelism por story

### Phase 2 — Foundational
| Grupo | Tarefas paralelas |
|-------|------------------|
| A     | T001, T002       |
| B     | T003, T004 (após A) |

### Phase 3 — US1
| Grupo | Tarefas paralelas |
|-------|------------------|
| A     | T005, T008, T009 |
| B     | T006 (após T005), T010 (após T008), T011 (após T009) |
| C     | T007 (após T001+T005), T012 (após T003), T013 (após T003) |
| D     | T014 (após T008+T009+T012+T013) |
| E     | T015 (após T014) |
| F     | T016, T017 (após T014+T005+T006) |

### Phase 4 — US2 (paralela com Phase 3 após Phase 2)
| Grupo | Tarefas paralelas |
|-------|------------------|
| A     | T018              |
| B     | T019 (após T018)  |
| C     | T020 (após T019)  |

## Implementation Strategy

**MVP (US1 only)**: Completar Phase 2 + Phase 3 → importação funcional com taxa calculada e persistida.

**Full delivery**: MVP + Phase 4 → visualização de "Valor Líq." no formulário de transações.

**Total**: 22 tarefas | **US1**: 13 tarefas | **US2**: 3 tarefas | **Foundational**: 4 tarefas | **Polish**: 2 tarefas
