# Tasks: 015-history-wallet-filters — Filtros da carteira no histórico

**Input**: Design documents from `/specs/015-history-wallet-filters/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/HistoryWalletFiltersContract.md`, `quickstart.md`

**Tests**: Obrigatórios (FR-014, SC-005) — `WalletHistoryFilterTest.kt` com **≥9** cenários GIVEN_WHEN_THEN (T1–T9 do contrato) na fase fundacional (Subagente **D**). `./gradlew` opcional para agentes (princípio IX).

**Organization**: Fase fundacional por **subagentes D + F** em paralelo; user stories por prioridade; integração no **Subagente H**.

## Execução por subagentes

| Subagente | Tarefas | Quando |
|-----------|---------|--------|
| **D — Domínio** | T003–T008 | Onda 1 — paralelo com F |
| **F — Filtros UI** | T009–T013 | Onda 1 — paralelo com D |
| **H — Histórico** | T014–T027 | Onda 2 — após **T008** e **T012** |

### Prompts (Task tool)

**D — Domínio** (T003–T008):
```text
Feature 015-history-wallet-filters. Read specs/015-history-wallet-filters/contracts/HistoryWalletFiltersContract.md and research.md.
Add WalletHistoryFilter.kt (Criteria, Candidate, matchesWalletHistoryFilter: OR/AND, RF-only liquidity/maturity, saturated Sim+Nao).
Add WalletHistoryFilterTest.kt with GIVEN_WHEN_THEN for contract T1–T9 (incl. defaultForHistory / reset criteria).
Update GetHistoryTableDataUseCase: Param(brokerage, walletFilter), remove zero exclusion and legacy category/liquidity/goal filters. No new UseCase class.
```

**F — Filtros UI** (T009–T013):
```text
Feature 015. WalletFiltersUiState.defaultForHistory() with selectedSettled={NO}; reset() equals default.
Implement deriveWalletFiltersPanelOptions(facets) respecting 014 FR-018d (hide B3/Settled unless Sim AND Nao in data). Merge/clean WalletFiltersDerivation.kt WIP. No WalletFiltersSlotGrid.
```

**H — Histórico** (T014–T027):
```text
Feature 015. HistoryViewModel: walletFilters, derive options after load (FR-012), map to criteria, remove legacy Segmenteds and intents in US1 MVP. Summary from filtered rows only (after T006–T007). See tasks.md phases 3–7.
```

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Paralelizável (ficheiros diferentes, sem dependência de tarefas incompletas da mesma trilha)
- **[Story]**: US1–US5 conforme `spec.md`
- **[SA-D]**, **[SA-F]**, **[SA-H]**: Subagente recomendado

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar dependências da 014 e alinhar contrato antes dos subagentes.

- [X] T001 Confirmar branch `015-history-wallet-filters` e ler `specs/015-history-wallet-filters/contracts/HistoryWalletFiltersContract.md`
- [X] T002 [P] Verificar que `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt` e `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt` existem (pré-requisito 014)

---

## Phase 2: Foundational (Blocking Prerequisites) — Subagentes D + F

**Purpose**: Lógica OR/AND testável no domínio + estado/derivação do painel. **Bloqueia todas as user stories.**

**Checkpoint**: `matchesWalletHistoryFilter` com testes T1–T9; `GetHistoryTableDataUseCase` aceita `walletFilter`; `defaultForHistory()` e `deriveWalletFiltersPanelOptions` (FR-018) disponíveis.

### Subagente D — Domínio

- [X] T003 [P] [SA-D] Criar `WalletHistoryFilterCriteria`, `WalletHistorySubtype` e `WalletHistoryFilterCandidate` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/WalletHistoryFilter.kt`
- [X] T004 [SA-D] Implementar `matchesWalletHistoryFilter` (OR intra-grupo, AND inter-grupo, RF-only liquidez/vencimento, grupo saturado Sim+Não) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/WalletHistoryFilter.kt`
- [X] T005 [SA-D] Adicionar `WalletHistoryFilterTest.kt` com GIVEN_WHEN_THEN para **T1–T9** do contrato (incl. OR subtipo, `defaultForHistory()`/reset — FR-014) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/screens/WalletHistoryFilterTest.kt` — **depende de T004**
- [X] T006 [SA-D] Refactor `GetHistoryTableDataUseCase.Param` para `referenceDate`, `brokerage`, `walletFilter` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt`
- [X] T007 [SA-D] Remover filtros legados `category`/`liquidity`/`goal`, remover skip `previousValue==0 && currentValue==0`, aplicar `matchesWalletHistoryFilter` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt`
- [X] T008 [SA-D] Construir `WalletHistoryFilterCandidate` a partir de `HoldingHistoryResult` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt` (sem dependência de composeApp)

### Subagente F — Filtros UI

- [X] T009 [P] [SA-F] Adicionar `WalletFiltersUiState.defaultForHistory()` (`selectedSettled = { YesOrNo.NO }`, resto vazio, `maturitySelection = null`) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersUiState.kt`
- [X] T010 [SA-F] Alinhar `reset()` ao mesmo snapshot que `defaultForHistory()` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersUiState.kt`
- [X] T011 [SA-F] Implementar `deriveWalletFiltersPanelOptions(facets: List<WalletFilterHoldingFacet>)` com regras **FR-018/FR-018d** da 014 (ocultar B3/Liquidados sem Sim **e** Não nos dados) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFilters.kt`
- [X] T012 [SA-F] Limpar ou fundir WIP (`AssetClassKind`, `SubtypeKind`, etc.) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt` — uma única derivação compilável
- [X] T013 [P] [SA-F] (Opcional) Prune de selecções órfãs ao derivar em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFilters.kt`; senão ignorar IDs inválidos no match (T007)

**Checkpoint fundacional**: Subagente **H** pode iniciar (T014+).

---

## Phase 3: User Story 1 — Listagem por defeito (Priority: P1) 🎯 MVP

**Goal**: Histórico abre com «Não liquidado» activo; tabela só posições não liquidadas; Reset repõe default; **sem** controlos legados paralelos (FR-008/FR-009); opções derivadas do mês (FR-012).

**Independent Test**: Mix activo/liquidado → só activas; Reset repõe default; desactivar «Não liquidado» inclui liquidados; **ausência** de segmentados categoria/liquidez/meta; painel sem `PreviewCatalog` estático.

**Subagente**: **H** — **bloqueia US5** até T019–T021 (remoção legados + use case T006–T007)

### Implementation for User Story 1

- [X] T014 [P] [US1] [SA-H] Criar `WalletFiltersUiState.toWalletHistoryFilterCriteria()` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/WalletFiltersToCriteria.kt`
- [X] T015 [US1] [SA-H] Substituir `category`/`liquidity`/`goal` por `walletFilters` e `walletFilterOptions` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryState.kt`
- [X] T016 [US1] [SA-H] Em `loadInitialData`, usar `defaultForHistory()`, `toWalletHistoryFilterCriteria()`, e após merge derivar facetas → `deriveWalletFiltersPanelOptions` → `walletFilterOptions` (FR-012) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [X] T017 [US1] [SA-H] Adicionar `WalletFiltersChanged` e repor `defaultForHistory()` em `SelectPeriod` antes do reload em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [X] T018 [US1] [SA-H] Ligar `WalletFiltersPanel(options = state.walletFilterOptions, …)` ao VM; remover `remember` de `WalletFiltersPreviewCatalog` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt`
- [X] T019 [US1] [SA-H] Remover `SegmentedControl` de categoria, liquidez e meta de `Supporting()` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt`
- [X] T020 [US1] [SA-H] Remover `SelectCategory`, `SelectLiquidity`, `SelectGoal` e handlers em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [X] T021 [P] [US1] [SA-H] Remover `GetFinancialGoalsUseCase` e campos `goal` órfãos em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`

**Checkpoint**: MVP completo — FR-008/FR-009/FR-012 no ecrã; listagem por defeito e Reset (SC-004 no MVP).

---

## Phase 4: User Story 2 — Filtro por um único critério (Priority: P1)

**Goal**: Uma opção activa num grupo restringe a listagem.

**Independent Test**: Só «Renda Fixa»; só «Sim» B3; «Vence até» só afecta RF.

**Subagente**: **H**

### Implementation for User Story 2

- [X] T022 [US2] [SA-H] Recarregar tabela e `walletFilterOptions` em cada `WalletFiltersChanged` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [X] T023 [P] [US2] [SA-H] Actualizar `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/GetHistoryTableDataUseCaseTest.kt` para novo `Param` e critérios de `defaultForHistory()`

**Checkpoint**: Filtros simples reflectidos na tabela.

---

## Phase 5: User Story 3 — OR intra-grupo e AND inter-grupo (Priority: P1)

**Goal**: Combinações multi-grupo (domínio coberto em T005).

**Independent Test**: RF+RV OR; RF AND B3; CDB OR LCI; liquidez/vencimento só RF.

**Subagente**: **H** (testes já em T005)

### Implementation for User Story 3

- [X] T024 [US3] [SA-H] Confirmar purge de subtipos via `WalletFiltersUiState.toggleClass` ao propagar `WalletFiltersChanged` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`

**Checkpoint**: Regras OR/AND validadas por `WalletHistoryFilterTest` (T005); UI propaga estado.

---

## Phase 6: User Story 4 — Painel unificado (Priority: P2)

**Goal**: Validar integração contínua após MVP (opções e filtros ao mudar período).

**Independent Test**: Mudar mês → default + opções do novo mês; alterar chip → tabela actualiza &lt; 1s percepção (SC-002 manual).

**Subagente**: **H**

### Implementation for User Story 4

- [X] T025 [US4] [SA-H] Verificar em `HistoryViewModel.kt` que `SelectPeriod` repõe filtros **e** recalcula `walletFilterOptions` para o novo mês (regressão FR-011a + FR-012)

**Checkpoint**: US4 = validação; implementação principal já em US1 (T016–T021).

---

## Phase 7: User Story 5 — Sumário coerente com tabela filtrada (Priority: P2)

**Goal**: Sumário só das linhas filtradas (FR-010a).

**Independent Test**: Filtro a metade da carteira → soma manual = sumário.

**Subagente**: **H** — **só após T007** (use case) **e T019–T021** (sem filtros legados)

### Implementation for User Story 5

- [X] T026 [US5] [SA-H] Agregar património/aportes/resgates só de `tableData` filtrada em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [X] T027 [US5] [SA-H] Calcular `growth`/`earnings` via `Growth`/`Appreciation` sobre agregados filtrados (não `GetTransactionsUseCase` global) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`

**Checkpoint**: SC-003.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T028 [P] Revisar chamadas a `GetHistoryTableDataUseCase.Param` no repo (grep) e actualizar assinatura
- [X] T029 [P] Executar checklist `specs/015-history-wallet-filters/quickstart.md` + cenários manuais SC-001 (US1–3) e SC-002 (percepção UI); `./gradlew` só se o utilizador pedir
- [X] T030 Actualizar `AGENTS.md` secção histórico/filtros se o wiring final divergir

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 → Phase 2 (D: T003→T004→T005; T006→T007→T008  ║  F: T009→T010→T011→T012)
    ↓
Phase 3 US1 MVP (T014–T021) — inclui remoção legados + FR-012
    ↓
Phase 4 US2 → Phase 5 US3 → Phase 6 US4 (validação) → Phase 7 US5 (após T007+T019–T021)
    ↓
Phase 8 Polish
```

### User Story Dependencies

| Story | Depende de | Independente quando |
|-------|------------|---------------------|
| US1 | Phase 2 | MVP sem legados + opções derivadas |
| US2 | US1 | Um critério na tabela |
| US3 | T005 + US1 | Testes unitários + UI |
| US4 | US1 | Smoke período/opções |
| US5 | **T007 + US1 (T019–T021)** | Sumário sem duplo filtro legado |

### Within Foundational (paralelo)

```text
Onda 1:
  D: T003 [P] → T004 → T005 → T006 → T007 → T008
  F: T009 [P] → T010 → T011 → T012 → T013 [P]
```

### Parallel Example: Subagente H (US1)

```bash
T014 WalletFiltersToCriteria.kt [P]
T015 HistoryState.kt
T016–T017 HistoryViewModel.kt (load + derive + intents)
T018 AssetHistoryScreen.kt
T019–T021 remover legados (mesmo sprint MVP)
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1–2: **D** ∥ **F**
2. Phase 3: **H** T014–T021 (não parar com segmentados ainda visíveis)
3. **STOP**: Validar US1 + SC-004
4. US2 → US3 → US4 (smoke) → US5 → Polish

### Incremental Delivery

| Entrega | Fases | Valor |
|---------|-------|-------|
| MVP | 1–3 | Default, painel único, opções do mês, sem legados |
| P1 completo | +4–5 | Filtro simples + OR/AND |
| P2 | +6–7 | Validação período + sumário |
| Release | +8 | Grep Param + checklist |

### Parallel Team Strategy

1. **Agente D**: T003–T008
2. **Agente F**: T009–T013 (paralelo)
3. **Agente H**: T014–T027 após fundacional

---

## Notes

- **Não criar** `WalletFiltersSlotGrid.kt`
- **T005** cobre toda a suite de filtros (não duplicar fase US3 com segunda tarefa de testes)
- **US5** nunca antes de **T007** e remoção de legados (**T019–T021**)
- **SC-002**: validação manual em T029 (sem tarefa de performance automatizada)
- **Corretora**: `SegmentedControl` em `subMainPane` inalterado
