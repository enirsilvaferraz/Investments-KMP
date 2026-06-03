# Tasks: 018-holding-history-filter — Filtragem unificada (incl. corretora)

**Input**: Design documents from `/specs/018-holding-history-filter/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/HoldingHistoryFilterContract.md`, `quickstart.md`

**Tests**: Obrigatórios (FR-008, SC-001/SC-002) — `FilterHoldingHistoryEntriesUseCaseTest.kt` + extensão `WalletHistoryFilterTest.kt` (corretora). `./gradlew` opcional para agentes (princípio IX).

**Organization**: Fase fundacional (critério corretora + UC + mapper) → US1 (pipeline tabela) → US2 (testes corretora/edge) → US3 (VM + remoção canal paralelo) → Polish.

## Execução por ondas (plan.md)

| Onda | Tarefas | Quando |
|------|---------|--------|
| **Domínio base** | T003–T007 | Antes de US1 — **T003 antes de T005** (candidato completo) |
| **US1** | T008–T011 | Pipeline tabela via UC |
| **US2** | T012–T014 | Testes corretora + edge cases |
| **US3** | T015–T019 | VM + `Param` unificado |
| **Polish** | T020–T023 | Documentação e SC-004 opcional |

### Prompts (Task tool)

**Domínio — fundacional + US1** (T003–T011):
```text
Feature 018. Read specs/018-holding-history-filter/contracts/HoldingHistoryFilterContract.md.
Order: extend WalletHistoryFilter (brokerageIds, brokerageId, matchesBrokerage) → HoldingHistoryEntry.toWalletHistoryFilterCandidate() → FilterHoldingHistoryEntriesUseCase @Factory → migrate GetHistoryTableDataUseCase to filter currentEntry via UC. Keep Param.brokerage until US3 (T017). Tests: FilterHoldingHistoryEntriesUseCaseTest (≥9 regressão 015) + fix GetHistoryTableDataUseCaseTest.
```

**Testes corretora + VM** (T012–T019):
```text
Feature 018. WalletHistoryFilterTest + FilterHoldingHistoryEntriesUseCaseTest brokerage/edge cases. WalletFiltersToCriteria(selectedBrokerage); HistoryViewModel facetCriteria (painel inactivo + brokerageIds); remove Param.brokerage and .filter { holding.brokerage == param.brokerage }; SelectPeriod clears brokerage. No AssetHistoryScreen layout changes.
```

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Paralelizável (ficheiros diferentes, sem dependência de tarefas incompletas da mesma trilha)
- **[Story]**: US1, US2, US3 conforme `spec.md`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar dependências 015/017 e contrato 018.

- [X] T001 Confirmar branch `018-holding-history-filter` e ler `specs/018-holding-history-filter/contracts/HoldingHistoryFilterContract.md`
- [X] T002 [P] Verificar pré-requisitos: `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/WalletHistoryFilter.kt`, `GetHistoryTableDataUseCase.kt`, `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` e `WalletFiltersToCriteria.kt` (feature 015)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Critério/candidato com corretora, mapper por entry, UC de filtragem. **Bloqueia US1–US3.**

**Checkpoint**: `FilterHoldingHistoryEntriesUseCase` testável; `brokerageId` no candidato antes do UC compilar (FR-003, FR-004).

- [X] T003 [P] Acrescentar `brokerageIds` a `WalletHistoryFilterCriteria`, `brokerageId` a `WalletHistoryFilterCandidate` e `matchesBrokerage` (OR, vazio = inactivo, saturação) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/WalletHistoryFilter.kt`
- [X] T004 [P] Adicionar `HoldingHistoryEntry.toWalletHistoryFilterCandidate()` com `brokerageId = holding.brokerage.id` e `settled` via património do registo em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt` ou `HoldingHistoryFilterMappers.kt` no pacote `screens/` — **depende de T003**
- [X] T005 Implementar `FilterHoldingHistoryEntriesUseCase` com `@Factory` (`Param(entries, criteria)` → sublista ordenada) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/FilterHoldingHistoryEntriesUseCase.kt` — **depende de T004**
- [X] T006 [P] Criar `FilterHoldingHistoryEntriesUseCaseTest.kt` com GIVEN_WHEN_THEN: lista vazia; critérios totalmente inactivos; só «Não liquidado»; só «Renda Fixa» em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/screens/FilterHoldingHistoryEntriesUseCaseTest.kt` — **depende de T005**
- [X] T007 Refactor `HoldingHistoryResult.toWalletHistoryFilterCandidate()` para delegar a `currentEntry.toWalletHistoryFilterCandidate()` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt` — **depende de T004**

**Checkpoint fundacional**: US1 pode iniciar (T008+).

---

## Phase 3: User Story 1 — Filtrar registos por critérios da carteira (Priority: P1) 🎯 MVP

**Goal**: Tabela filtra **só `currentEntry`** via `FilterHoldingHistoryEntriesUseCase` para critérios do painel (FR-001, FR-011). Canal `Param.brokerage` pode permanecer até T017 (US3).

**Independent Test**: Lista fixa de `HoldingHistoryEntry` + critérios (não liquidado, RF, todos inactivos) → sublista via UC; tabela mapeia só pares cuja entrada actual passa.

### Tests for User Story 1

- [X] T008 [P] [US1] Adicionar em `FilterHoldingHistoryEntriesUseCaseTest.kt` cenários de regressão **015 T1–T9** (≥9 GIVEN_WHEN_THEN via entries) — **obrigatório**; não substituir por «confirmar» só em `WalletHistoryFilterTest.kt` — **depende de T006**

### Implementation for User Story 1

- [X] T009 [US1] Injetar `FilterHoldingHistoryEntriesUseCase` em `GetHistoryTableDataUseCase` e filtrar `results` pelas `currentEntry` que passam no UC (chave `holding.id`) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt` — **depende de T005, T007**
- [X] T010 [US1] Remover `.filter { matchesWalletHistoryFilter(it.toWalletHistoryFilterCandidate(), param.walletFilter) }` em `HoldingHistoryResult` no mesmo ficheiro (FR-005) — **depende de T009**
- [X] T011 [US1] Actualizar `GetHistoryTableDataUseCaseTest.kt` para pipeline com UC inject/mock em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/GetHistoryTableDataUseCaseTest.kt` — **depende de T009**

**Checkpoint**: FR-001/FR-011 satisfeitos no domínio; corretora unificada pendente US3 (T017).

---

## Phase 4: User Story 2 — Corretora no mesmo modelo de critérios (Priority: P1)

**Goal**: Cobertura de testes SC-001/FR-008 para corretora e edge cases de spec (saturação, id inexistente, património zero com valores negativos).

**Independent Test**: Critério `{idA}` → só A; `{idA,idB}` → OR; A + RF → AND; `brokerageIds` vazio → não filtra por corretora.

### Tests for User Story 2

- [X] T012 [P] [US2] Testes GIVEN_WHEN_THEN de corretora (isolada, OR duas, AND com classe, inactivo, id inexistente, **saturação** todos ids da lista) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/screens/WalletHistoryFilterTest.kt` — **depende de T003**
- [X] T013 [P] [US2] Cenários de corretora integrados no `FilterHoldingHistoryEntriesUseCaseTest.kt` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/screens/FilterHoldingHistoryEntriesUseCaseTest.kt` — **depende de T012**
- [X] T014 [US2] Teste edge: `endOfMonthValue`/`endOfMonthQuantity` negativos com produto zero → `settled == true` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/screens/FilterHoldingHistoryEntriesUseCaseTest.kt` — **depende de T006**

**Checkpoint**: SC-001 coberto por testes; implementação de match já em T003.

---

## Phase 5: User Story 3 — Comportamento inalterado na interface (Priority: P2)

**Goal**: Paridade UX (FR-007, FR-009, FR-010, FR-005 completo); remover canal paralelo de corretora.

**Independent Test**: Layout inalterado; corretora ≈ comportamento anterior; facetas por corretora; mudar mês limpa corretora + default.

### Implementation for User Story 3

- [X] T015 [US3] Estender `toWalletHistoryFilterCriteria(selectedBrokerage: Brokerage?)` com `brokerageIds` e adicionar `facetCriteriaForHistory(selectedBrokerage)` (painel inactivo + só `brokerageIds`) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/WalletFiltersToCriteria.kt` — **depende de T003**
- [X] T016 [US3] Em `loadInitialData`, usar `facetCriteriaForHistory` para facetas e critério completo para tabela/sumário em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` — **depende de T015**
- [X] T017 [US3] Remover `brokerage` de `GetHistoryTableDataUseCase.Param`, remover `.filter { param.brokerage == null || it.holding.brokerage == param.brokerage }`, e actualizar chamadas VM com `walletFilter` unificado em `GetHistoryTableDataUseCase.kt` e `HistoryViewModel.kt` (FR-005, FR-006) — **depende de T016, T009**
- [X] T018 [US3] Em `selectPeriod`, repor `walletFilters = defaultForHistory()` **e** `brokerage.selected = null` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` (FR-010) — **depende de T016**
- [X] T019 [P] [US3] Verificar diff vazio de layout em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (FR-007)

**Checkpoint**: E2E manual conforme `quickstart.md` §3; SC-003 validável manualmente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentação, verificação estática, desempenho opcional.

- [X] T020 [P] Actualizar secção filtros/histórico em `AGENTS.md` (UC `FilterHoldingHistoryEntriesUseCase`, `brokerageIds`, facetas)
- [X] T021 Executar verificações `quickstart.md` §1 (`rg` sem `brokerage` em `Param`, UC presente)
- [ ] T022 [P] (Opcional SC-004) Teste de desempenho: filtrar 500 entries sintéticas em `FilterHoldingHistoryEntriesUseCaseTest.kt` com asserção de duração razoável em JVM — só se estável em CI
- [X] T023 (Opcional) `./gradlew :domain:usecases:jvmTest` sob pedido do utilizador

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup** → **Foundational (T003→T004→T005)** → **US1** → **US2** (testes, paralelo a US1 após T003) → **US3** → **Polish**

### User Story Dependencies

```text
T003–T007 (fundacional, corretora no match primeiro)
  → US1 T008–T011 (pipeline; Param.brokerage OK temporariamente)
  → US2 T012–T014 (testes; ∥ US1 após T003)
  → US3 T015–T019 (remove Param.brokerage — FR-005 completo)
```

### Parallel Opportunities

| Grupo | Tarefas |
|-------|---------|
| Setup | T002 após T001 |
| Foundational | T003 ∥ prep; T004 após T003; T006 após T005 |
| US1 | T008 ∥ T009 prep; T012 ∥ T009 após T003 |
| US2 | T012 ∥ T013 após T003 |
| Polish | T020 ∥ T022 |

---

## Implementation Strategy

### MVP First (User Story 1)

1. T001–T011: UC + pipeline tabela (critérios do painel via UC).
2. **Validar** FR-001/FR-011 antes de US3.
3. US2 testes + US3 VM para FR-005/FR-009/FR-010 completos.

### Nota FR-005 vs MVP

A migração **completa** (sem `Param.brokerage` nem filtro paralelo) fecha em **T017 (US3)**, não no checkpoint de US1 — alinhado ao entregável incremental do plano.

---

## FR Coverage

| Requisito | Task IDs | Notas |
|-----------|----------|-------|
| FR-001 | T005, T009 | UC dedicado |
| FR-002 | T003, T008 | Regras 015 |
| FR-003 | T003, T012–T013 | `brokerageIds` |
| FR-004 | T004, T014 | Património do registo |
| FR-005 | T009–T010, T017 | Completo em T017 |
| FR-006 | T015, T017 | Mapper VM |
| FR-007 | T019 | Sem diff UI |
| FR-008 | T008, T012–T014 | Testes |
| FR-009 | T015–T016 | `facetCriteriaForHistory` |
| FR-010 | T018 | Reset período |
| FR-011 | T009 | Só `currentEntry` |
| SC-001 | T012–T013 | 10+ registos heterogéneos nos testes |
| SC-002 | T008 | Regressão T1–T9 |
| SC-003 | T017–T019, quickstart §3 | Manual |
| SC-004 | T022 | Opcional |

---

## Task Summary

| Métrica | Valor |
|---------|--------|
| **Total de tarefas** | 23 |
| **US1** | 4 (T008–T011) |
| **US2** | 3 (T012–T014) |
| **US3** | 5 (T015–T019) |
| **Setup + Foundational + Polish** | 11 |
| **Com [P]** | 10 |
| **MVP sugerido** | T001–T011 |

---

## Notes

- **Koin**: `@Factory` em `FilterHoldingHistoryEntriesUseCase` — registo automático (contrato checklist).
- **Sem alteração visual**: não alterar `WalletFiltersPanel.kt` nem `design-system-v2`.
- **Facetas**: `facetCriteriaForHistory` — **não** `defaultForHistory()` (FR-009).
