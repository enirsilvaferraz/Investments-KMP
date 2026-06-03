# Tasks: 016-asset-taxonomy-refactor — Taxonomia de ativos

**Input**: Design documents from `/specs/016-asset-taxonomy-refactor/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/AssetTaxonomyContract.md`, `quickstart.md`

**Tests**: Atualizar testes existentes em `:domain:usecases` (princípio V) — `UpsertAssetUseCaseTest`, `GetHistoryTableDataUseCaseTest`, `WalletHistoryFilterTest`, `HoldingHistoryViewTest`, `MergeHistoryUseCaseTest`. `./gradlew` opcional para agentes (princípio IX).

**Organization**: Fase fundacional **sequencial** (entity — colisão `FixedIncomeAssetType`); onda paralela **D + U + P** após checkpoint; user stories por prioridade da spec.

## Execução por subagentes (paralelismo)

| Subagente | Tarefas | Quando |
|-----------|---------|--------|
| **E — Entity** | T003–T009 | Onda 0 — **sem** paralelo interno |
| **D — Database** | T010–T015 | Onda 1 — T010–T014 ∥ U/P; **T015 após T016** |
| **U — Use cases** | T016–T019 | Onda 1 — **T016 antes de T015**; T017–T019 ∥ D/P |
| **P — Presentation** | T020–T026 | Onda 1 — paralelo com D e U |
| **Integrador** | T027–T032 | Após onda 1 |

### Prompts (Task tool)

**E — Entity** (T003–T009):
```text
Feature 016-asset-taxonomy-refactor. Read specs/016-asset-taxonomy-refactor/contracts/AssetTaxonomyContract.md and research.md R1.
Order: AssetClass; YieldIndexer (delete FixedIncomeAssetType.kt indexador); FixedIncomeSubType file → FixedIncomeAssetType product; AssetType marker; Asset.assetClass, FixedIncomeAsset.indexer+type.
Update core/domain/entity/docs/DOMAIN.md. No Gradle. Minimal diff.
```

**D — Database** (T010–T015):
```text
Feature 016. AppDatabase v7, Migration6To7 RenameColumn only (atomic). T010–T014 parallel after T011. T015 repos/datasources getByAssetClass AFTER T016 ports merged.
```

**U — Use cases** (T016–T019):
```text
Feature 016. HistoryTableData indexer+type+assetClass, HoldingHistoryView, WalletHistoryFilter, GetHistoryTableDataUseCase, MergeHistoryUseCase, repository ports, AssetFileStoreImpl, TestDataFactory, jvmTests. No behavior change.
```

**P — Presentation** (T020–T026):
```text
Feature 016. asset-management AssetClass/YieldIndexer/product type; FieldLabels YieldIndexer+AssetClass; TableIcons; walletfilters+Formatters. Labels Indexador/Tipo RF.
```

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Paralelizável (ficheiros diferentes; requer **T009** concluído)
- **[Story]**: US1–US3 conforme `spec.md`
- **[SA-E]**, **[SA-D]**, **[SA-U]**, **[SA-P]**: Subagente recomendado

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Alinhar branch e contrato antes da onda 0.

- [x] T001 Confirmar branch `016-asset-taxonomy-refactor` e ler `specs/016-asset-taxonomy-refactor/contracts/AssetTaxonomyContract.md`
- [x] T002 [P] Revisar ordem de rename R1 em `specs/016-asset-taxonomy-refactor/research.md` e mapa em `specs/016-asset-taxonomy-refactor/data-model.md`

---

## Phase 2: Foundational (Blocking) — Subagente E

**Purpose**: Tipos de domínio estáveis. **Bloqueia** onda 1 (D/U/P). **Não marcar [P]** entre T003–T007 (mesmo pacote `assets`, colisão de nomes).

**Checkpoint**: `YieldIndexer`, `AssetClass`, produto `FixedIncomeAssetType`, `AssetType` compiláveis em `:domain:entity`; `DOMAIN.md` atualizado.

- [x] T003 [SA-E] Renomear `InvestmentCategory.kt` → `AssetClass.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/`
- [x] T004 [SA-E] Criar `YieldIndexer.kt` (POST_FIXED, PRE_FIXED, INFLATION_LINKED) e **eliminar** `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/FixedIncomeAssetType.kt` (ficheiro do indexador legado) **antes** de T005
- [x] T005 [SA-E] Renomear ficheiro/conteúdo `FixedIncomeSubType.kt` → `FixedIncomeAssetType.kt` (produto CDB, LCI, …) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/`
- [x] T006 [SA-E] Criar `AssetType.kt` marcadora e fazer `FixedIncomeAssetType`, `VariableIncomeAssetType`, `InvestmentFundAssetType` implementarem `AssetType` nos respetivos ficheiros em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/`
- [x] T007 [SA-E] Atualizar `Asset.kt`, `FixedIncomeAsset.kt` (`assetClass`, `indexer`, `type`), `VariableIncomeAsset.kt`, `InvestmentFundAsset.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/`
- [x] T008 [P] [US3] [SA-E] Atualizar `core/domain/entity/docs/DOMAIN.md` (§2, §5, §6.5, §9.1) com AssetClass, YieldIndexer, AssetType, colunas `asset_class`/`indexer`/`type`
- [x] T009 [SA-E] Remover ficheiros obsoletos `InvestmentCategory.kt` / `FixedIncomeSubType.kt` se ainda existirem; garantir zero referências no módulo `entity`

**Checkpoint fundacional**: Iniciar **onda 1** (T010–T026 em paralelo).

---

## Phase 3: User Story 2 — Dados preservados (Priority: P1) — Subagente D

> **Nota de ordenação de fases**: Phase 3 trata **US2** (migração/persistência) antes da Phase 4 **US1** (UI) por dependência técnica (schema e ports), **não** por prioridade de negócio — US1 e US2 são ambos P1 na spec.

**Goal**: Migração Room 6→7 e persistência alinhada; registros legados intactos (FR-004, SC-001, SC-003).

**Independent Test**: DB v6 com RF/RV/fundo → após abrir app v7, contagens e valores enum iguais; colunas `asset_class`, `indexer`, `type` populadas (quickstart §3).

**Subagente**: **D** — paralelo com U e P após T009

- [x] T010 [US2] [SA-D] Criar `Migration6To7.kt` com `@RenameColumn` + `onPostMigrate`: `UPDATE asset_transactions SET asset_class = 'INVESTMENT_FUND' WHERE asset_class = 'FUNDS'` (R5/plan); KDoc: migração atómica 6→7 em `core/data/database/src/commonMain/kotlin/com/eferraz/database/migrations/Migration6To7.kt`
- [x] T011 [US2] [SA-D] Atualizar `AppDatabase.kt` para `version = 7` e `AutoMigration(from = 6, to = 7, spec = Migration6To7::class)` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/core/AppDatabase.kt`
- [x] T012 [P] [US2] [SA-D] Atualizar `AssetEntity.kt` (`assetClass`/`asset_class`), `FixedIncomeAssetEntity.kt` (`indexer`, `type`), `AssetTransactionEntity.kt` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/entities/`
- [x] T013 [P] [US2] [SA-D] `PersistedAssetClass` em `AssetMappers.kt`; `TransactionMappers.kt` usa as mesmas constantes (`INVESTMENT_FUND` para fundos, não `FUNDS`) em `core/data/database/src/commonMain/kotlin/com/eferraz/database/mappers/`
- [x] T014 [P] [US2] [SA-D] Atualizar queries `@Query` para `asset_class` em `core/data/database/src/commonMain/kotlin/com/eferraz/database/daos/AssetDao.kt` e `AssetHoldingDao.kt`
- [x] T015 [US2] [SA-D] Renomear `getByType`/`getByCategory` → `getByAssetClass` em `AssetDataSource.kt`, `AssetHoldingDataSource.kt`, impls e `core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/AssetRepositoryImpl.kt`, `AssetHoldingRepositoryImpl.kt` — **depende de T016** (ports primeiro, evita conflito de merge)

**Checkpoint US2**: Camada data/repositories alinhada ao contrato (schema v7).

---

## Phase 4: User Story 1 — Vocabulário de domínio na UI (Priority: P1) — Subagentes U + P

**Goal**: Cadastro e ecrãs distinguem classe, indexador (RF) e tipo de produto (FR-007, SC-002).

**Independent Test**: Cadastrar CDB pós-fixado e ETF; reabrir; filtros histórico por classe RF (quickstart §4–5).

### Subagente U — Use cases (T016 antes de T015; depois paralelo com D, P)

- [x] T016 [US1] [SA-U] Renomear `getByCategory`/`category` → `getByAssetClass`/`assetClass` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/AssetRepository.kt` e `AssetHoldingRepository.kt` — **bloqueia T015**; executar no início da onda 1
- [x] T017 [P] [US1] [SA-U] Atualizar `HistoryTableData.kt` (`assetClass`, `indexer: YieldIndexer`, `type: FixedIncomeAssetType` produto) e `HoldingHistoryView.kt` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/`
- [x] T018 [P] [US1] [SA-U] Atualizar `GetHistoryTableDataUseCase.kt`, `MergeHistoryUseCase.kt`, `WalletHistoryFilter.kt` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/`
- [x] T019 [P] [US1] [SA-U] Atualizar `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/AssetFileStoreImpl.kt` (`YieldIndexer` + produto `FixedIncomeAssetType`); atualizar `TestDataFactory.kt`, `UpsertAssetUseCaseTest.kt`, `GetHistoryTableDataUseCaseTest.kt`, `WalletHistoryFilterTest.kt`, `HoldingHistoryViewTest.kt`, `MergeHistoryUseCaseTest.kt`, `GetHistoryMaturityMonthsUseCaseTest.kt`, `UpsertAssetHoldingUseCaseTest.kt` em `core/domain/usecases/src/jvmTest/kotlin/`

### Subagente P — Presentation (paralelo com D, U)

- [x] T020 [P] [US1] [SA-P] Atualizar `FieldLabels.kt` (`AssetClass.asLabel`, `YieldIndexer.asLabel`, produto `FixedIncomeAssetType.asLabel`) em `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/FieldLabels.kt`
- [x] T021 [P] [US1] [SA-P] Renomear `InvestmentCategory.BuildIcon()` → `AssetClass` em `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/TableIcons.kt`
- [x] T022 [P] [US1] [SA-P] Atualizar `AssetManagementUiState.kt`, `AssetManagementEvents.kt`, `AssetManagementMap.kt`, `AssetManagementViewModel.kt` para `AssetClass`, `YieldIndexer`, tipo produto em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/`
- [x] T023 [US1] [SA-P] Atualizar formulário RF (rótulos Indexador / Tipo) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [x] T024 [P] [US1] [SA-P] Atualizar `Validations.kt`, `ErrorMapping.kt` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/`
- [x] T025 [P] [US1] [SA-P] Atualizar `WalletFilters.kt`, `WalletFiltersUiState.kt`, `WalletFilterSubtype.kt`, `WalletFiltersPanel.kt`, `WalletFiltersPreviewCatalog.kt` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/`
- [x] T026 [P] [US1] [SA-P] Atualizar `Formatters.kt` e imports em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/helpers/Formatters.kt`; ajustar `TransactionManagementView.kt`, `TransactionManagementUiState.kt`, `TransactionManagementViewModel.kt` se referenciam categoria legada

**Checkpoint US1**: Cadastro e filtros compilam conceptualmente com novo vocabulário.

---

## Phase 5: User Story 3 — Documentação canônica (Priority: P2)

**Goal**: `DOMAIN.md` = fonte de verdade (FR-006, SC-004).

**Independent Test**: `rg InvestmentCategory|FixedIncomeSubType` em `DOMAIN.md` → zero; diagrama ER com `indexer` + `type`.

- [x] T027 [US3] Rever diff de `core/domain/entity/docs/DOMAIN.md` (T008) contra código final; corrigir lacunas em §6.5 / §9.1 se necessário

**Nota**: T008 cobre implementação; T027 é revisão pós-onda 1.

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: Verificação global e validação manual.

- [x] T028 Executar `rg 'InvestmentCategory|FixedIncomeSubType' --glob '*.kt'` na **raiz do repositório** (inclui `core/` e import B3/sync se existir); expect **zero** em código de produção/teste (excluir `specs/`)
- [ ] T029 [P] (Opcional, sob pedido) Compilar `:domain:entity`, `:data:database`, `:domain:usecases` e gerar `core/data/database/schemas/.../AppDatabase/7.json` per `specs/016-asset-taxonomy-refactor/quickstart.md` §7
- [x] T030 Validar checklist em `specs/016-asset-taxonomy-refactor/quickstart.md` §3–6; **SC-003**: mesma carteira/período antes e depois — totais de histórico e quantidades **sem divergência** (validação manual ou T031) — código alinhado; SC-003 requer validação manual no dispositivo
- [ ] T031 [P] (Opcional) Adicionar teste JVM de regressão migração 6→7 com fixture mínima RF+RV+fundo em `core/domain/usecases/src/jvmTest/kotlin/` ou módulo database — cobre SC-003 de forma automatizada
- [x] T032 [P] Confirmar secção «Taxonomia de ativos» em `AGENTS.md` alinhada ao código entregue; ajustar bullets se a implementação divergir (princípio VII)

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 (Setup)
    → Phase 2 (Entity T003–T009)  ← BLOQUEANTE
        → T016 (ports U)  ← antes de T015
        → Phase 3 US2: T010–T014 ∥ T017–T019, T020–T026
        → T015 (repos D, após T016)
            → Phase 5 US3 (T027)
                → Phase 6 Polish (T028–T032)
```

### User Story Dependencies

| Story | Depende de | Paralelo com |
|-------|------------|--------------|
| US2 (migração) | T009, **T016** | US1 P; T015 após T016 |
| US1 (vocabulário UI) | T009 | T016 primeiro; depois ∥ D/P |
| US3 (docs) | T008 + onda 1 | T027 após T026 |

### Within Entity (T003–T007)

Ordem **obrigatória** (research R1): T003 → T004 → T005 → T006 → T007 — **sem [P]**.

---

## Parallel Examples

### Onda 1 — três subagentes (após T009)

```bash
# 1) Ports (U) — PRIMEIRO, bloqueia T015
T016

# 2) Paralelo (D + U + P)
# D: migração + entidades (sem T015 ainda)
T010 → T011 → T012 | T013 | T014

# U: resto use cases
T017 | T018 | T019

# P: presentation
T020 | T021 | T022 → T023 | T024 | T025 | T026

# 3) Repos (D) — após T016 mergeado
T015
```

### Paralelo seguro [P] após T011

T012, T013, T014 (database). **Não** paralelizar T015 com T016.

---

## Implementation Strategy

### MVP mínimo (validar cedo)

1. Phase 1 + Phase 2 (T001–T009) — entity estável  
2. **T016** → T010–T014 + T017–T019 + T020–T023 em paralelo → **T015**  
3. Cadastro RF (T023) — **parar e validar** quickstart §4  
4. T028–T032 polish  

### Entrega incremental

1. Fundação entity → desbloqueia paralelo  
2. US2 + US1 em paralelo (3 subagentes)  
3. US3 revisão DOMAIN  
4. Polish + SC-001–SC-004  

### Contagem

| Métrica | Valor |
|---------|-------|
| **Total tarefas** | 32 (T001–T032) |
| **Fundacional (sequencial)** | 7 (T003–T009) |
| **Paralelas [P] após T009** | 17 (+ T016→T015 sequencial) |
| **US1** | 11 (T016–T026) |
| **US2** | 6 (T010–T015) |
| **US3** | 2 (T008, T027) |

---

## Notes

- **Build Gradle**: opcional para agentes (princípio IX) — T029 só sob pedido ou CI  
- **Não** criar aliases `typealias InvestmentCategory = AssetClass` — rename direto  
- **FUNDS** → **INVESTMENT_FUND** em transações (R5): migração + `PersistedAssetClass` em T010/T013  
- Conflito de merge: priorizar ordem T003–T007 se dois agentes tocaram `entity`  
- **API repos**: T016 (ports) antes de T015 (impl) — não inverter  
- Commit sugerido após T009, após T016+T015, após T032
