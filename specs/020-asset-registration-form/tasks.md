# Tasks: Cadastro de investimento — cards Ativo e Posicionamento

**Input**: Design documents from `/specs/020-asset-registration-form/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [research.md](./research.md), [contracts/ui-contracts.md](./contracts/ui-contracts.md), [quickstart.md](./quickstart.md)

**Tests**: `UpsertAssetUseCaseTest` (constitution princípio V + plano); sem TDD obrigatório na spec.

**Organization**: Tarefas agrupadas por user story; `[P]` quando ficheiros distintos e sem dependência de ordem no mesmo batch.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Paralelo (ficheiros diferentes)
- **[Story]**: US1–US5 nas fases de user story
- Caminhos a partir da raiz do monorepo (`core/...`)

---

## Phase 1: Setup

**Purpose**: Confirmar contexto antes de alterar código.

- [X] T001 Confirmar branch `021-asset-registration-form` e ler [plan.md](./plan.md) + [contracts/ui-contracts.md](./contracts/ui-contracts.md)

**Checkpoint**: Branch e contrato compreendidos.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domínio, Room 7→8 e mappers — **bloqueia todas as user stories**.

**⚠️ CRITICAL**: Nenhuma US até T008 concluída.

### Batch A — paralelo

- [X] T002 [P] Adicionar `incomeTaxExempt: Boolean = false` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/FixedIncomeAsset.kt`
- [X] T003 [P] Documentar `incomeTaxExempt` em `core/domain/entity/docs/DOMAIN.md` (§5 + ER `FixedIncomeAsset`)
- [X] T004 [P] Actualizar `createFixedIncomeAsset()` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/TestDataFactory.kt` com default `incomeTaxExempt = false`

### Batch B — sequencial

- [X] T005 Adicionar `@ColumnInfo(name = "income_tax_exempt") val incomeTaxExempt: Boolean = false` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/entities/assets/FixedIncomeAssetEntity.kt`
- [X] T006 Alterar `version = 8` e `AutoMigration(from = 7, to = 8)` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/core/AppDatabase.kt`
- [X] T007 Mapear `incomeTaxExempt` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/AssetMappers.kt` (`toDomain` + `toEntity`)
- [X] T008 Executar `./gradlew :data:database:compileKotlinJvm` (sob pedido) e commitar `core/data/database/schemas/com.eferraz.database.core.AppDatabase/8.json`

**Checkpoint**: Foundation ready — US1–US5 podem avançar.

---

## Phase 3: User Story 1 — Card ATIVO completo e funcional (Priority: P1) 🎯 MVP

**Goal**: Todos os campos visíveis do card ATIVO wired ao estado global, validação e build; tipo por classe; reset parcial ao trocar classe.

**Independent Test**: Percorrer cada campo editável em cadastro novo e edição; trocar classe preserva emissor/observações e limpa tipo/campos específicos; salvar válido persiste valores (spec US1).

### Implementation

- [X] T009 [US1] Adicionar `incomeTaxExempt: Boolean = false` e `partialResetForAssetClass(assetClass: AssetClass)` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt` (limpar tipo + campos por classe; manter `issuer`, `observations`; reset `incomeTaxExempt = false`)
- [X] T010 [US1] Aplicar `partialResetForAssetClass` no handler `AssetClassChanged` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T011 [US1] Dropdown **Tipo** com opções por `assetClass` (`FixedIncomeAssetType` / `VariableIncomeAssetType` / `InvestmentFundAssetType`) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T012 [P] [US1] Completar wiring de `VariableIncomeFields` (tipo, ticker, CNPJ, observações) ao estado/eventos em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T013 [US1] Remover campo Identificador B3 de `VariableIncomeFields` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt` (B3 só em RF — spec edge case)
- [X] T014 [P] [US1] Completar wiring de `FundFields` (tipo, liquidez, dias, vencimento) ao estado/eventos em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T015 [US1] Remover `FormCardActions` de persistência na secção ATIVO em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`

**Checkpoint**: Card ATIVO funcional end-to-end (Isento IR wired em US2).

---

## Phase 4: User Story 2 — Isento de IR em renda fixa (Priority: P1)

**Goal**: Campo Sim/Não persistido em RF; default "Não"; round-trip na edição; visível **somente** em RF (critérios US5 incluídos).

**Independent Test**: Novo RF → default Não → Sim → salvar → reabrir Sim; RV/Fundo sem campo; RF→RV sem isenção persistida (spec US2 + US5).

### Batch C — paralelo

- [X] T016 [P] [US2] Adicionar `IncomeTaxExemptChanged(val exempt: Boolean)` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementEvents.kt`

### Batch D — sequencial

- [X] T017 [US2] Tratar `IncomeTaxExemptChanged` e hidratar `incomeTaxExempt` em `Asset.toUiState()` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T018 [US2] Incluir `incomeTaxExempt` em `buildFixedIncomeAsset()`; garantir `buildVariableIncomeAsset()` / `buildFundAsset()` ignoram isenção em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementMap.kt`
- [X] T019 [US2] Ligar `SegmentedControl` Isento de IR ao estado (sem `remember` local; label "Isento de IR"; **apenas** em `FixedIncomeFields`) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T020 [US2] Estender `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/UpsertAssetUseCaseTest.kt` com RF + `incomeTaxExempt = true` e `false`

**Checkpoint**: Isento de IR persiste, recarrega e é exclusivo de RF.

---

## Phase 5: User Story 3 — Card Posicionamento funcional (Priority: P1)

**Goal**: Titular só leitura; corretora wired; validação obrigatória.

**Independent Test**: Titular visível não editável; corretora persiste após save; save sem corretora bloqueia com erro (spec US3).

- [X] T021 [US3] Substituir dropdown Titular por `FormTextField` `readOnly = true` com `ui.owner?.name` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T022 [US3] Remover `FormCardActions` na secção POSICIONAMENTO em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`

**Checkpoint**: Posicionamento wired; titular/corretora correctos.

---

## Phase 6: User Story 4 — Botão Salvar na barra inferior (Priority: P1)

**Goal**: Único ponto de persistência na barra inferior; **sempre habilitado**; dialog fecha no sucesso.

**Independent Test**: Salvar clicável em cadastro novo e edição; validação bloqueia inválido; double-tap ignorado no VM; sucesso fecha dialog; Excluir inactivo (spec US4).

- [X] T023 [US4] Renomear **Concluir → Salvar** e ligar `onClick` a `AssetManagementEvents.Save` na barra inferior em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T024 [US4] Manter botão Salvar **sempre** `enabled = true` (sem `isDirty`, `canSave` ou desactivar por `isSaving` na UI) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T025 [US4] Manter botão **Excluir** desabilitado (TODO existente) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`

**Checkpoint**: Persistência centralizada; guard `isSaving` só no ViewModel (`onSave`).

---

## Phase 7: User Story 5 — Isento de IR apenas em renda fixa (Priority: P2)

**Goal**: Coberto por T013, T018 e T019 — validação final.

**Independent Test**: Cenário 3 de [quickstart.md](./quickstart.md) (spec US5).

- [X] T026 [US5] Validar em [quickstart.md](./quickstart.md) §3 que Isento de IR ausente em RV/Fundo e não persistido ao trocar RF→RV antes de salvar

**Checkpoint**: US5 verificado via quickstart (sem tarefas de código adicionais).

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Verificação final; **não** alterar Transações/Resumo.

- [X] T027 [P] Confirmar `checkErros()` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/helpers/Validations.kt` cobre campos obrigatórios ATIVO + corretora (FR-009, FR-012) — ajustar só se regressão detectada
- [X] T028 [P] Rever secções TRANSAÇÕES e RESUMO em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt` — confirmar zero alteração funcional (FR-013)
- [X] T029 Executar todos os cenários manuais de [quickstart.md](./quickstart.md)

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) — BLOCKS ALL
    ↓
Phase 3 (US1) ──┬── Phase 4 (US2)
                ├── Phase 5 (US3) — paralelo após Phase 2
                └── Phase 6 (US4) — após US1/US3
    ↓
Phase 7 (US5) — validação quickstart após US2
    ↓
Phase 8 (Polish)
```

### User Story Dependencies

| Story | Depende de | Pode paralelizar com |
|-------|------------|----------------------|
| US1 | Phase 2 | — |
| US2 | Phase 2, T009 (UiState) | US3 após Phase 2 |
| US3 | Phase 2 | US1 (coordenar `AssetManagementScreen.kt`) |
| US4 | US1, US3 | US2 |
| US5 | US2 | T029 |

### Parallel Opportunities

**Phase 2 Batch A**: T002 + T003 + T004.

**Phase 3**: T012 + T014 (mesmo ficheiro — preferir commit único).

**Phase 4 Batch C**: T016 isolado; T017–T019 sequencial.

**Phase 8**: T027 + T028 em paralelo.

---

## Parallel Example: After Foundational

```bash
# Developer A — US1
T009 → T010 → T011 → T012 → T013 → T014 → T015

# Developer B — US3 (merge AssetManagementScreen.kt)
T021 → T022

# Developer C — US2 (após T009)
T016 → T017 → T018 → T019 → T020
```

---

## Implementation Strategy

### MVP First (cadastro RF completo)

1. Phase 1 + Phase 2 (obrigatório)
2. Phase 3 US1 — card ATIVO wired
3. Phase 4 US2 — Isento IR (P1; incluído no MVP)
4. Phase 5 US3 — corretora + titular
5. Phase 6 US4 — barra Salvar
6. **Validar**: quickstart §1–§5

### Entrega incremental

1. Foundation → US1 → US2 → US3 → US4 (**MVP**)
2. US5 → quickstart §3
3. Polish → T027–T029

### Fora do escopo (não criar tarefas)

- `TransactionFormView` / `TransactionManagement*`
- Secção RESUMO (valores mock)
- Botão Excluir funcional
- `AssetFormSnapshot` / `isDirty` / `canSave`
- Integração IncomeTax engine (019)

---

## Task Summary

| Phase | Tasks | Count |
|-------|-------|-------|
| Setup | T001 | 1 |
| Foundational | T002–T008 | 7 |
| US1 | T009–T015 | 7 |
| US2 | T016–T020 | 5 |
| US3 | T021–T022 | 2 |
| US4 | T023–T025 | 3 |
| US5 | T026 | 1 |
| Polish | T027–T029 | 3 |
| **Total** | | **29** |

**Suggested MVP scope**: T001–T025 (Setup + Foundational + US1–US4).
