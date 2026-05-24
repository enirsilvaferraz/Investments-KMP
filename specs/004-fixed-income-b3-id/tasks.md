# Tasks: Identificador B3 em Renda Fixa

**Input**: Design documents from `/specs/004-fixed-income-b3-id/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [research.md](./research.md), [contracts/FixedIncomeB3IdentifierContract.md](./contracts/FixedIncomeB3IdentifierContract.md), [quickstart.md](./quickstart.md)

**Tests**: Incluídos apenas onde a constitution (princípio V) e o plano exigem — `UpsertAssetUseCaseTest`; sem TDD obrigatório na spec.

**Organization**: Tarefas agrupadas por user story; **paralelismo maximizado** com batches `[P]` em ficheiros distintos sempre que não há dependência de ordem.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem depender de tarefas incompletas do mesmo batch)
- **[Story]**: US1, US2, US3 — apenas em fases de user story
- Caminhos absolutos ao repo: prefixo `core/...` a partir da raiz do monorepo

---

## Phase 1: Setup

**Purpose**: Confirmar contexto e alinhamento antes de tocar código.

- [x] T001 Confirmar branch `004-fixed-income-b3-id` e ler [plan.md](./plan.md) + [contracts/FixedIncomeB3IdentifierContract.md](./contracts/FixedIncomeB3IdentifierContract.md)

**Checkpoint**: Branch e contrato compreendidos — iniciar Phase 2.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domínio, persistência Room 5→6 e mappers — **bloqueia todas as user stories**.

**⚠️ CRITICAL**: Nenhuma US1/US2/US3 até T008 concluída.

### Batch A — paralelo (3 ficheiros independentes)

- [x] T002 [P] Adicionar `b3Identifier: String?` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/FixedIncomeAsset.kt`
- [x] T003 [P] Criar `B3IdentifierStatus` (sealed) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/B3IdentifierStatus.kt`
- [x] T004 [P] Atualizar `core/domain/entity/docs/DOMAIN.md` (§5 invariantes, §6.1 prosa, diagrama §9.1 `FixedIncomeAsset` + `b3Identifier` opcional)

### Batch B — sequencial após Batch A

- [x] T005 Adicionar `@ColumnInfo(name = "b3_identifier") val b3Identifier: String? = null` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/entities/assets/FixedIncomeAssetEntity.kt`
- [x] T006 Alterar `version = 6` e `AutoMigration(from = 5, to = 6)` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/core/AppDatabase.kt`
- [x] T007 Mapear `b3Identifier` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/AssetMappers.kt` (`toDomain` + `toEntity` com `trim()?.ifBlank { null }`)
- [x] T008 Executar `./gradlew :data:database:compileKotlinJvm` e commitar `core/data/database/schemas/com.eferraz.database.core.AppDatabase/6.json`

**Checkpoint**: Foundation ready — **US1, US2 e US3 podem avançar em paralelo** (módulos distintos).

---

## Phase 3: User Story 1 — Cadastro RF com Identificador B3 (Priority: P1) 🎯 MVP

**Goal**: Campo opcional no formulário de renda fixa, persistência com trim, RV/fundos inalterados.

**Independent Test**: Criar RF com identificador → reabrir com valor trimado; criar sem campo → salva vazio; RV não mostra campo (spec US1).

### Batch C — paralelo (estado/eventos/label)

- [x] T009 [P] [US1] Adicionar `b3Identifier: String?` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt`
- [x] T010 [P] [US1] Adicionar `B3IdentifierChanged(val value: String)` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementEvents.kt`
- [x] T011 [P] [US1] Adicionar label `Identificador B3` em `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/FieldLabels.kt`

### Batch D — sequencial (mesmo fluxo cadastro)

- [x] T012 [US1] Tratar `B3IdentifierChanged` e hidratar `b3Identifier` em `Asset.toUiState()` / edição em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [x] T013 [US1] Incluir `b3Identifier = b3Identifier?.trim()?.ifBlank { null }` em `buildFixedIncomeAsset()` e mapear em `toUiState()` para `FixedIncomeAsset` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementMap.kt`
- [x] T014 [US1] Adicionar `FormTextField` "Identificador B3" em `FixedIncomeFields` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt` (apenas `FIXED_INCOME`)

### Teste (constitution V)

- [x] T015 [US1] Estender `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/UpsertAssetUseCaseTest.kt` com RF + `b3Identifier` (round-trip no mock)

**Checkpoint**: US1 testável via cadastro desktop + `jvmTest` UpsertAsset.

---

## Phase 4: User Story 2 — Dados existentes após migração (Priority: P1)

**Goal**: Upgrade 5→6 sem perda de dados; RF legados com `b3_identifier` NULL.

**Independent Test**: App com DB v5 → atualizar → listar RF → cadastro com campo vazio → histórico amarelo até preencher (spec US2).

> **Nota**: Implementação técnica está em Phase 2 (T005–T008). Esta fase é **validação** e pode correr **em paralelo com Phase 3 e 5** após o checkpoint da Phase 2.

### Batch E — paralelo (build + manual)

- [x] T016 [P] [US2] Executar `./gradlew :domain:entity:compileKotlinJvm :data:database:compileKotlinJvm` e confirmar build verde pós-migração
- [x] T017 [P] [US2] Seguir secção "Teste manual — migração" em `specs/004-fixed-income-b3-id/quickstart.md` (DB v5 opcional → upgrade → RF legado campo vazio)

**Checkpoint**: Migração validada — dados legados intactos.

---

## Phase 5: User Story 3 — Status no Histórico (Priority: P1)

**Goal**: Coluna direita com ícones RF (azul/amarelo) e célula vazia RV/fundos.

**Independent Test**: Histórico misto — RF com/sem identificador vs RV/fundo sem ícone (spec US3).

### Batch F — paralelo (domínio histórico + composable UI)

- [x] T018 [P] [US3] Adicionar `b3Identifier: String?` em `FixedIncomeHistoryTableData` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/HistoryTableData.kt`
- [x] T019 [P] [US3] Criar `B3IdentifierStatusCell.kt` com `B3IdentifierStatus.BuildCell()` (TooltipBox + Info/Warning) em `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/B3IdentifierStatusCell.kt` (espelhar `TableIcons.kt`)

### Batch G — sequencial (use case → view → ecrã)

- [x] T020 [US3] Preencher `b3Identifier` a partir de `FixedIncomeAsset` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/screens/GetHistoryTableDataUseCase.kt`
- [x] T021 [US3] Adicionar `b3IdentifierStatus` e mapeamento `Informed` / `NotInformed` / `NotApplicable` no construtor `HoldingHistoryView(HistoryTableData)` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/HoldingHistoryView.kt`
- [x] T022 [US3] Adicionar coluna direita em `UiTableV3` com `when (row.b3IdentifierStatus)` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt`
- [x] T026 [US3] Adicionar testes jvmTest em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/GetHistoryTableDataUseCaseTest.kt` cobrindo: RF com `b3Identifier` não-nulo → `Informed(value)`; RF com `b3Identifier = null` → `NotInformed`; RV/fundo → `NotApplicable` (constitution V — alterações em `:domain:usecases` DEVEM incluir testes)

**Checkpoint**: US3 testável no ecrã Posicionamento no Período.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificação final multi-módulo e quickstart.

### Batch H — paralelo

- [x] T023 [P] Executar bloco "Verificação de build" em `specs/004-fixed-income-b3-id/quickstart.md` (`:domain:usecases:jvmTest`, `:presentation:asset-management:compileKotlinJvm`, `:features:composeApp:compileKotlinJvm`)
- [x] T024 [P] Executar secções "Teste manual — cadastro" e "Teste manual — histórico" em `specs/004-fixed-income-b3-id/quickstart.md`
- [x] T025 Revisar `core/domain/entity/docs/DOMAIN.md` — critério de pass: §5 contém `b3Identifier: String?` (opcional, exclusivo de `FixedIncomeAsset`, trim aplicado); §6.1/§9.1 inclui o campo no diagrama; **nenhuma menção a ícones, cores ou histórico** (regras de UI pertencem à spec/contrato, não ao DOMAIN)

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) ──► CHECKPOINT ──┬──► Phase 3 (US1) ──┐
                                        ├──► Phase 4 (US2) ──┼──► Phase 6 (Polish)
                                        └──► Phase 5 (US3) ──┘
```

- **Phase 2** bloqueia tudo.
- **Após T008**: US1, US2 e US3 **podem correr em paralelo** (equipas ou agentes separados).

### User Story Dependencies

| Story | Depende de | Pode paralelizar com |
|-------|------------|----------------------|
| US1 | Phase 2 (T002, T005–T008) | US2 (validação), US3 (após T003) |
| US2 | Phase 2 (T005–T008) | US1, US3 |
| US3 | Phase 2 (T002, T003) | US1 (módulos `:presentation:asset-management` vs `:domain:usecases` + `:features:composeApp`) |

### Within Each Story

- Respeitar ordem **Batch A → B → C → D** (ou F → G) dentro da fase.
- Tarefas **sem [P]** no mesmo batch partilham ficheiro ou ordem lógica — não paralelizar.

---

## Parallel Execution Guide

### Após Phase 2 (máximo paralelismo — 3 streams)

| Stream | Tarefas | Módulos |
|--------|---------|---------|
| **Cadastro (US1)** | T009–T015 | `:presentation:asset-management`, `:domain:usecases` (teste) |
| **Migração (US2)** | T016–T017 | validação `:data:database` + manual |
| **Histórico (US3)** | T018–T022 | `:domain:usecases`, `:presentation:naming`, `:features:composeApp` |

```bash
# Exemplo: lançar Batch F enquanto US1 faz Batch C
# Agente/ dev A:
#   T018 HistoryTableData.kt
#   T019 B3IdentifierStatusCell.kt
# Agente/ dev B:
#   T009 UiState.kt
#   T010 Events.kt
#   T011 FieldLabels.kt
```

### Phase 2 — Batch A (3 tarefas simultâneas)

```text
T002 FixedIncomeAsset.kt     │  T003 B3IdentifierStatus.kt  │  T004 DOMAIN.md
```

### Phase 3 — Batch C (3 tarefas simultâneas)

```text
T009 UiState.kt  │  T010 Events.kt  │  T011 FieldLabels.kt
```

### Phase 5 — Batch F (2 tarefas simultâneas)

```text
T018 HistoryTableData.kt  │  T019 B3IdentifierStatusCell.kt
```

### Phase 6 — Batch H (2 tarefas simultâneas)

```text
T023 gradle verify  │  T024 quickstart manual
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1 + Phase 2 (obrigatório)
2. Phase 3 (US1) — **parar e validar** cadastro + `UpsertAssetUseCaseTest`
3. Demo: cadastro RF com/sem identificador

### Entrega incremental com paralelismo

1. **Foundation** (T001–T008) — ~1 sessão, Batch A paralelo
2. **Paralelo triplo**: US1 cadastro + US2 smoke migração + US3 início (T018–T019)
3. Fechar US3 (T020–T022) — pode sobrepor fim de US1 se cadastro já persiste
4. **Polish** (T023–T025)

### Ordem mínima sequencial (1 desenvolvedor)

`T001 → T002–T004 (paralelo se possível) → T005–T008 → T009–T015 → T016–T017 → T018–T022 → T023–T025`

---

## Notes

- **19 tarefas [P]** de **25** totais (~76% paralelizáveis em batches)
- US2 não duplica código de migração — valida T005–T008
- `B3IdentifierStatus` em `:domain:usecases` (não em `:domain:entity`) conforme data-model
- RV/fundos: `NotApplicable` + célula vazia — não omitir coluna
- Commitar schema Room `6.json` em T008 antes de paralelizar US1/US3

---

## Task Summary (for report)

| Métrica | Valor |
|---------|--------|
| **Total tasks** | 26 |
| **Parallelizable [P]** | 19 |
| **US1 tasks** | 7 (T009–T015) |
| **US2 tasks** | 2 (T016–T017) |
| **US3 tasks** | 6 (T018–T022, T026) |
| **Setup + Foundational + Polish** | 11 (T001–T008, T023–T025) |

### Independent Test Criteria

| Story | Critério |
|-------|-----------|
| US1 | Cadastro RF salva/reabre identificador trimado; vazio OK; RV sem campo |
| US2 | Upgrade v5→v6 sem crash; RF legado com campo vazio |
| US3 | Histórico: RF azul/amarelo; RV/fundo coluna vazia; reflete cadastro atual |

### Suggested MVP Scope

**Phase 2 + Phase 3 (US1)** — persistência e cadastro; histórico e migração manual podem seguir em paralelo na mesma sprint.
