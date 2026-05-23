# Tasks: Importação de Dados da B3

**Input**: Design documents from `/specs/003-b3-import/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/XlsxImportContract.md, quickstart.md

**Tests**: `ImportB3FileUseCaseTest` com MockK — obrigatório (constituição princípio V)

**Organization**: Tarefas agrupadas por user story para implementação e teste independentes.

**Alinhamento crítico**: Sem `errorMessage`, Snackbar ou `DismissError` (FR-014, FR-016). US2 = filtro `.xlsx` no diálogo + rejeição com log no console em `B3ImportPortImpl`. Desktop-only para import real; Android/iOS bypass (ViewModel ignora `ImportB3File` ou botão sem efeito). FR-015 atómico: validar todas as guias antes de qualquer `println` de dados.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: User story da spec (US1, US2, US3)

## Path Conventions

Gradle `:domain:usecases` → `core/domain/usecases/` | `:data:filestore` → `core/data/filestore/` | `:features:composeApp` → `core/presentation/composeApp/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependências FileMapper-KMP e ligação do módulo `filestore` ao port de domínio.

- [ ] T001 Add FileMapper-KMP version `1.0.0` to `[versions]` and declare `filemapper-kmp` library alias under `[libraries]` in `build-logic/gradle/libs.versions.toml`
- [ ] T002 Add `implementation(libs.filemapper.kmp)`, `implementation(projects.domain.usecases)`, and verify `kotlinx-serialization-core` (or equivalent from catalog) in `commonMain` dependencies of `core/data/filestore/build.gradle.kts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Port de domínio e UseCase com timeout — **bloqueia** todas as user stories.

**⚠️ CRITICAL**: Nenhuma user story começa antes desta fase.

- [ ] T003 Create public `B3ImportPort` interface with `suspend fun importAndLog(): Result<Unit>` in `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/B3ImportPort.kt`
- [ ] T004 Create `ImportB3FileUseCase` extending `AppUseCase<Unit, Unit>` with `withTimeout(30_000L)` wrapping `port.importAndLog().getOrThrow()`; on `TimeoutCancellationException` print timeout message to stdout (`println`, FR-011/FR-011a) then rethrow in `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/services/ImportB3FileUseCase.kt` (depends on T003)

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Selecionar e Importar Arquivo XLSX da B3 (Priority: P1) 🎯 MVP

**Goal**: Botão de importação na `AssetHistoryScreen`, picker Desktop, leitura das cinco guias B3, saída tabular no console, spinner durante processamento.

**Independent Test**: Selecionar `posicao-*.xlsx` válido no Desktop e verificar no console da IDE os dados das guias B3 presentes; spinner substitui o botão durante o processamento e volta ao concluir.

### Tests for User Story 1

- [ ] T016 [US1] Create `ImportB3FileUseCaseTest` with MockK for `B3ImportPort` covering success, `Result.failure`, and `TimeoutCancellationException` (advance virtual time past 30s; verify timeout `println` when feasible) using `GIVEN_WHEN_THEN` naming in `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/services/ImportB3FileUseCaseTest.kt`

### Implementation for User Story 1

- [ ] T005 [P] [US1] Create internal `B3StockPosition` DTO with `@ColumnName` annotations per `data-model.md` in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3StockPosition.kt`
- [ ] T006 [P] [US1] Create internal `B3EtfPosition` DTO in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3EtfPosition.kt`
- [ ] T007 [P] [US1] Create internal `B3FundPosition` DTO in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3FundPosition.kt`
- [ ] T008 [P] [US1] Create internal `B3FixedIncomePosition` DTO in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3FixedIncomePosition.kt`
- [ ] T009 [P] [US1] Create internal `B3TreasuryPosition` DTO in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/B3TreasuryPosition.kt`
- [ ] T010 [US1] Implement `B3ImportPortImpl` in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/B3ImportPortImpl.kt`: `FileMapperPicker.pickFile(FileType.XLSX)`; **Fase A** parse/validação das cinco guias B3 presentes (`Acoes`, `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto`) via `importData<T>` sem `println` de dados; column set per `specs/003-b3-import/data-model.md`; **Fase B** log header → linhas → totais calculados; filtros blank/total rows; FR-010 guias vazias; FR-012 guias desconhecidas ignoradas; FR-013 zero guias B3 → sucesso silencioso; `EMPTY_FILE` for 0-byte/ilegível workbook; FR-015 falha atómica `MISSING_COLUMNS` (depends on T005–T009)
- [ ] T011 [US1] Register `singleOf(::B3ImportPortImpl).bind<B3ImportPort>()` in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/di/FileStoreModule.kt` (depends on T010)
- [ ] T012 [P] [US1] Add `isImporting: Boolean = false` to `HistoryState` in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryState.kt`
- [ ] T013 [US1] Add `data object ImportB3File : HistoryIntent` to sealed interface in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [ ] T014 [US1] Wire `ImportB3File` in `HistoryViewModel`: inject `ImportB3FileUseCase`; set `isImporting = true` before invoke; on completion set `isImporting = false` only — **no** `errorMessage` or Snackbar; on Android/iOS bypass (ignore intent or no-op before UseCase) in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` (depends on T004, T012, T013)
- [ ] T015 [US1] Add import `IconButton` left of export in `Actions` composable; show `CircularProgressIndicator` same size/position when `state.isImporting`; wire `onImportClick` → `HistoryIntent.ImportB3File` in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (depends on T012, T014)

**Checkpoint**: User Story 1 fully functional on Desktop — MVP deliverable

---

## Phase 4: User Story 2 - Restrição de Tipo de Arquivo na Seleção (Priority: P2)

**Goal**: Diálogo nativo filtra `.xlsx`; fallback rejeita arquivo inválido com log no console apenas (sem UI).

**Independent Test**: (a) Abrir diálogo e confirmar filtro `.xlsx`. (b) Selecionar arquivo não-`.xlsx` (ou SO sem filtro) e verificar `INVALID_FORMAT` no console; botão restaurado; sem Snackbar.

### Implementation for User Story 2

- [ ] T017 [US2] Ensure `B3ImportPortImpl` calls `FileMapperPicker.pickFile(FileType.XLSX)` so native dialog restricts to `.xlsx` per FR-003 in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/B3ImportPortImpl.kt`
- [ ] T018 [US2] Add post-selection extension/MIME validation in `B3ImportPortImpl`: reject non-`.xlsx` with `println` reason `INVALID_FORMAT`, return `Result.failure(...)` — no UI message; ViewModel only clears `isImporting` in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/B3ImportPortImpl.kt` (depends on T010)

**Checkpoint**: File-type restriction independently verifiable per quickstart scenario "Arquivo não-xlsx"

---

## Phase 5: User Story 3 - Cancelamento da Seleção de Arquivo (Priority: P3)

**Goal**: Cancelar o picker retorna ao estado anterior sem erro, log ou spinner preso.

**Independent Test**: Abrir diálogo, cancelar sem selecionar — app estável, botão visível, sem log no console.

### Implementation for User Story 3

- [ ] T019 [US3] Ensure `B3ImportPortImpl.importAndLog()` returns `Result.success(Unit)` when `FileMapperPicker.pickFile` returns `null` — no `println`, no throw — in `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/B3ImportPortImpl.kt`
- [ ] T020 [US3] Ensure `HistoryViewModel` restores `isImporting = false` on cancel success path without error state in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` (depends on T014, T019)

**Checkpoint**: Cancel flow independently verifiable per FR-007 / SC-004

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Build, testes JVM e validação manual Desktop; documentar bypass mobile.

- [ ] T021 Run `./gradlew :domain:usecases:compileKotlinJvm :data:filestore:compileKotlinJvm :features:composeApp:compileKotlinJvm` and fix compile errors
- [ ] T022 Run `./gradlew :domain:usecases:jvmTest` and ensure `ImportB3FileUseCaseTest` passes
- [ ] T023 Execute manual validation scenarios from `specs/003-b3-import/quickstart.md` on Desktop (`./gradlew :apps:desktopApp:run`)
- [ ] T024 Document Android/iOS bypass in code comment or KDoc on `HistoryViewModel` import handler: button may remain visible but must not invoke picker/UseCase on non-Desktop in `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — iniciar imediatamente
- **Foundational (Phase 2)**: Depende de Setup — **bloqueia** todas as user stories
- **User Stories (Phase 3–5)**: Dependem de Foundational
  - US1 (P1) primeiro para MVP
  - US2 e US3 podem seguir sequencialmente ou em paralelo após US1 core (T010)
- **Polish (Phase 6)**: Depende das user stories desejadas completas

### User Story Dependencies

- **User Story 1 (P1)**: Após Phase 2 — sem dependência de US2/US3
- **User Story 2 (P2)**: Após T010 (`B3ImportPortImpl` base) — testável via console sem UI
- **User Story 3 (P3)**: Após T014 (ViewModel) + T019 (port null path) — testável independentemente

### Within Each User Story

- DTOs (T005–T009) antes de `B3ImportPortImpl` (T010)
- Port impl + Koin antes de ViewModel wiring
- `HistoryState` / intent antes de ViewModel handler
- ViewModel antes de UI spinner/button

### Parallel Opportunities

- **Phase 1**: T001 → T002 (sequencial)
- **Phase 2**: T003 → T004 (sequencial — UseCase depende do port)
- **Phase 3**: T005, T006, T007, T008, T009, T012 em paralelo; T016 (teste) pode iniciar após T004 com mock do port
- **Phase 4–5**: T017–T018 e T019–T020 podem correr em paralelo se equipas separadas (ficheiros distintos após US1 base)
- **Phase 6**: T021 e T022 sequenciais; T023 após compile/test green

---

## Parallel Example: User Story 1

```bash
# DTOs em paralelo (após Phase 2):
Task T005: B3StockPosition.kt
Task T006: B3EtfPosition.kt
Task T007: B3FundPosition.kt
Task T008: B3FixedIncomePosition.kt
Task T009: B3TreasuryPosition.kt
Task T012: HistoryState.kt (isImporting)

# Depois, sequencial:
Task T010: B3ImportPortImpl.kt
Task T011: FileStoreModule.kt
Task T013 → T014 → T015: ViewModel + Screen
Task T016: ImportB3FileUseCaseTest.kt (pode iniciar após T004)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T002)
2. Complete Phase 2: Foundational (T003–T004)
3. Complete Phase 3: User Story 1 (T005–T016)
4. **STOP and VALIDATE**: `./gradlew :domain:usecases:jvmTest` + quickstart Desktop cenário "Arquivo válido B3"
5. Demo MVP

### Incremental Delivery

1. Setup + Foundational → port e UseCase prontos
2. US1 → import Desktop funcional (MVP)
3. US2 → reforço filtro/rejeição `.xlsx` (console-only)
4. US3 → cancelamento silencioso verificado
5. Polish → compile, jvmTest, quickstart completo

### Parallel Team Strategy

1. Equipa completa Setup + Foundational
2. Após Phase 2:
   - Dev A: DTOs + `B3ImportPortImpl` (US1)
   - Dev B: `ImportB3FileUseCaseTest` + ViewModel (US1)
   - Dev C: UI botão/spinner (US1, após T012/T013)
3. US2/US3 em sequência rápida ou paralelo nos ficheiros do port/ViewModel

---

## Notes

- **Sem Snackbar / `errorMessage` / `DismissError`** nesta entrega (FR-014, FR-016)
- Erros e sucesso comunicados **apenas** via `println` em `B3ImportPortImpl`
- **Desktop-only** para picker/parse/log; mobile bypass — botão pode existir sem ação
- FileMapper-KMP 1.0.0 só em `:data:filestore` — grafo Clean Architecture inalterado
- Parse **duas fases** (contrato): validar todas as guias antes de qualquer log de dados (FR-015)
- Guias desconhecidas: ignorar silenciosamente (FR-012); arquivo sem guias B3: sucesso silencioso (FR-013)
