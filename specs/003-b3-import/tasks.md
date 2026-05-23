# Tasks: Importação de Dados da B3

**Input**: Design documents from `/specs/003-b3-import/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/XlsxImportContract.md ✅ | quickstart.md ✅

**Tests**: `ImportB3FileUseCase` tem testes JVM requeridos pela constituição (princípio V).

**Organization**: Tarefas organizadas por user story para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências incompletas)
- **[Story]**: A qual user story a tarefa pertence (US1, US2, US3)
- Caminhos absolutos desde a raiz do repositório

---

## Phase 1: Setup (Infraestrutura Compartilhada)

**Purpose**: Adicionar a dependência FileMapper-KMP ao projeto — pré-requisito para toda a feature.

- [ ] T001 Add filemapper-kmp version `1.0.0` to `[versions]` section and declare `filemapper-kmp` library under `[libraries]` in `build-logic/gradle/libs.versions.toml`
- [ ] T002 Add `implementation(libs.filemapper.kmp)` and verify `kotlinx-serialization-core` is present in `commonMain` dependencies block of `core/data/filestore/build.gradle.kts`

---

## Phase 2: Foundational (Pré-requisitos Bloqueantes)

**Purpose**: Definir o port de domínio e o UseCase — toda implementação de UI e data layer depende dessas interfaces.

**⚠️ CRÍTICO**: Nenhuma user story pode começar enquanto esta fase não estiver completa.

- [ ] T003 Create `B3ImportPort` interface with `suspend fun importAndLog(): Result<Unit>` in `core/domain/usecases/src/commonMain/com/eferraz/usecases/repositories/B3ImportPort.kt` — visibility `public`, package `com.eferraz.usecases.repositories`
- [ ] T004 Create `ImportB3FileUseCase` extending `AppUseCase<Unit, Unit>` with `withTimeout(30_000L)` wrapping `port.importAndLog().getOrThrow()` in `core/domain/usecases/src/commonMain/com/eferraz/usecases/services/ImportB3FileUseCase.kt` — inject `B3ImportPort` + `CoroutineContext` via constructor

**Checkpoint**: Port e UseCase definidos — as User Stories podem prosseguir em paralelo.

---

## Phase 3: User Story 1 — Selecionar e Importar Arquivo XLSX da B3 (Priority: P1) 🎯 MVP

**Goal**: O investidor toca no botão de importação na `AssetHistoryScreen`, seleciona um arquivo XLSX exportado pela B3 e vê o conteúdo de todas as 5 guias impresso no console da IDE; um spinner substitui o botão durante o processamento.

**Independent Test**: Executar `./gradlew :apps:desktopApp:run`, navegar até a tela de histórico de ativos, clicar no botão de importação (à esquerda do botão de exportação), selecionar o arquivo `posicao-*.xlsx` e verificar que as 5 guias (Acoes, ETF, Fundo de Investimento, Renda Fixa, Tesouro Direto) aparecem no console com cabeçalhos, linhas e totais calculados; confirmar que o spinner aparece e o botão é restaurado ao concluir.

### Testes para User Story 1 (constituição — princípio V)

- [ ] T005 [P] [US1] Create `ImportB3FileUseCaseTest` with MockK and `kotlinx-coroutines-test`; follow GIVEN_WHEN_THEN naming (constitution V): (1) success — `importAndLog` returns `Result.success(Unit)` → UseCase returns success; (2) failure — `importAndLog` returns `Result.failure(IOException())` → UseCase returns failure; (3) timeout — use `StandardTestDispatcher` + `advanceTimeBy(30_001L)` to advance virtual time past the `withTimeout(30_000L)` boundary → `TimeoutCancellationException` propagated; (4) silent cancel — `importAndLog` returns `Result.success(Unit)` (simulating null picker path) → UseCase returns success silently — in `core/domain/usecases/src/jvmTest/com/eferraz/usecases/services/ImportB3FileUseCaseTest.kt`

### Implementação — DTOs de Arquivo (`:data:filestore`)

- [ ] T006 [P] [US1] Create `B3StockPosition` internal data class with `@Serializable` and all 14 `@ColumnName` fields (Produto → variationPercent) in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/dto/B3StockPosition.kt`
- [ ] T007 [P] [US1] Create `B3EtfPosition` internal data class with `@Serializable` and all 13 `@ColumnName` fields (Produto → updatedValue) in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/dto/B3EtfPosition.kt`
- [ ] T008 [P] [US1] Create `B3FundPosition` internal data class with `@Serializable` and all 14 `@ColumnName` fields (Produto → updatedValue, inclui administrator) in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/dto/B3FundPosition.kt`
- [ ] T009 [P] [US1] Create `B3FixedIncomePosition` internal data class with `@Serializable` and all 19 `@ColumnName` fields (Produto → closingValue) in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/dto/B3FixedIncomePosition.kt`
- [ ] T010 [P] [US1] Create `B3TreasuryPosition` internal data class with `@Serializable` and all 13 `@ColumnName` fields (Produto → updatedValue) in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/dto/B3TreasuryPosition.kt`

### Implementação — Port e Koin (`:data:filestore`)

- [ ] T011 [US1] Implement `B3ImportPortImpl` in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/B3ImportPortImpl.kt`: `runCatching { val file = FileMapperPicker.pickFile(FileType.XLSX) ?: return@runCatching }`, then call `parseAndLog<T>(bytes, sheetName)` for all 5 sheets using `fileMapper.importData<T>(bytes, onSuccess, onFailed)`; implement the following private helpers inside the file: (a) `T::class.columnHeaders(): String` — derives column header string from `@ColumnName` annotations on the DTO; (b) `List<T>.computeTotals(): String` — sums numeric String fields (quantity, values) across data rows; (c) blank-row filter: all fields null/empty/`"-"`; (d) total-row filter: `product` field starts with `"Total"` or `"Subtotal"`; (e) **FR-010**: if `dataRows` is empty after filtering, print `"=== <sheetName> — sem dados ==="` instead of data lines; otherwise print header, data rows and computed totals (depends on T006–T010)
- [ ] T012 [US1] Register `B3ImportPortImpl` in `core/data/filestore/src/commonMain/com/eferraz/filestore/FileStoreModule.kt` via `singleOf(::B3ImportPortImpl).bind<B3ImportPort>()` (depends on T011)

### Implementação — Estado e UI (`:features:composeApp`)

- [ ] T013 [P] [US1] Add `val isImporting: Boolean = false` field to `HistoryState` data class in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryState.kt`
- [ ] T014 [P] [US1] Add `data object ImportB3File : HistoryIntent` to the sealed interface `HistoryIntent` in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryViewModel.kt`
- [ ] T015 [US1] Update `HistoryViewModel` to inject `ImportB3FileUseCase` via constructor and handle `ImportB3File` intent using the existing `StateFlow` backing field pattern (constitution VI): `_state.update { it.copy(isImporting = true) }` → launch `importB3FileUseCase(Unit)` → on complete (success or failure) `_state.update { it.copy(isImporting = false) }` in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryViewModel.kt` (depends on T004, T013, T014)
- [ ] T016 [US1] Update `AssetHistoryScreen` to add an import button (icon `FileUpload`, positioned to the left of the existing export button) inside the Actions section; when `state.isImporting` is true replace the button with a `CircularProgressIndicator` of the same size and position in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (depends on T013, T015)

**Checkpoint**: User Story 1 completa e testável de forma independente — o MVP está pronto.
> ⚠️ **FR-011 parcialmente coberto no MVP**: o timeout de 30 s é cancelado (T004), mas a mensagem de erro ao usuário só é exibida após US2 (T018). Se o produto exigir feedback de timeout já no MVP, incorporar o mapeamento de `TimeoutCancellationException` em T015.

---

## Phase 4: User Story 2 — Restrição de Tipo de Arquivo na Seleção (Priority: P2)

**Goal**: A caixa de diálogo nativa exibe apenas arquivos `.xlsx`; se o SO não suportar filtragem nativa e o usuário selecionar um arquivo de outro formato, o aplicativo rejeita e exibe mensagem informativa ao usuário.

**Independent Test**: (a) Abrir o diálogo e confirmar que apenas `.xlsx` fica disponível para seleção — comportamento garantido pelo `FileType.XLSX` do FileMapper-KMP. (b) Em SOs sem suporte nativo (fallback): tentar importar um arquivo `.csv` e verificar que uma mensagem de rejeição é exibida ao usuário e o botão é restaurado.

- [ ] T017 [P] [US2] Add `val errorMessage: String? = null` field to `HistoryState` data class in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryState.kt` (to surface rejection and error messages to UI)
- [ ] T018 [US2] Update `HistoryViewModel` to map `Result.failure(...)` from `ImportB3FileUseCase` to human-readable `errorMessage` in state — evaluate in this order: `FileMapperException(INVALID_FORMAT)` → "Apenas arquivos .xlsx são aceitos."; `FileMapperException(EMPTY_FILE)` → "O arquivo está vazio ou sem guias."; `TimeoutCancellationException` → "Processamento cancelado: tempo limite de 30 s excedido."; `java.nio.file.AccessDeniedException` or `SecurityException` → "Sem permissão para ler o arquivo selecionado."; outros (`IOException` etc.) → "Erro ao ler o arquivo. Verifique se ele não está corrompido." in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryViewModel.kt` (depends on T017)
- [ ] T022 [US2] Add `data object DismissError : HistoryIntent` and handle it in `HistoryViewModel` by setting `errorMessage = null`, ensuring re-showing the dialog after dismissal doesn't carry stale error state in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryViewModel.kt` (depends on T017)
- [ ] T019 [US2] Update `AssetHistoryScreen` to display `state.errorMessage` as a `Snackbar` (or equivalent Compose Desktop UI feedback) when non-null, and clear it after display via a `DismissError` intent in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (depends on T017, T018, T022)

**Checkpoint**: User Stories 1 e 2 devem funcionar de forma independente. `DismissError` já implementado — T019 pode exibir e limpar mensagens.

---

## Phase 5: User Story 3 — Cancelamento da Seleção de Arquivo (Priority: P3)

**Goal**: O investidor abre a caixa de diálogo e a fecha sem selecionar arquivo — o aplicativo permanece estável, sem erro, sem mudança de estado visível ao usuário além da restauração do botão.

**Independent Test**: Abrir a caixa de diálogo de seleção e cancelar ou fechar sem selecionar nenhum arquivo; verificar que o spinner desaparece, o botão de importação retorna e nenhuma mensagem de erro é exibida.

- [ ] T020 [US3] Verify and reinforce null-check in `B3ImportPortImpl.importAndLog()`: when `FileMapperPicker.pickFile(FileType.XLSX)` returns `null`, the function must execute `return@runCatching` (not throw), resulting in `Result.success(Unit)` — confirm in `core/data/filestore/src/commonMain/com/eferraz/filestore/b3/B3ImportPortImpl.kt`
- [ ] T021 [US3] Verify `HistoryViewModel` cancel path: on `Result.success(Unit)` (both real import and silent cancel), `isImporting` is reset to `false` and `errorMessage` remains `null` — no user-visible feedback — in `core/presentation/composeApp/src/commonMain/com/eferraz/presentation/features/history/HistoryViewModel.kt`

**Checkpoint**: Cancel path verificado — todas as 3 User Stories funcionam de forma independente.

---

## Phase Final: Polish & Cross-Cutting Concerns

**Purpose**: Verificação de build, testes automatizados e validação manual do quickstart.

- [ ] T023 [P] Run `./gradlew :domain:usecases:jvmTest` and confirm `ImportB3FileUseCaseTest` passes (success, failure, timeout, cancel scenarios)
- [ ] T024 [P] Run `./gradlew :domain:usecases:compileKotlinJvm :data:filestore:compileKotlinJvm` and fix any compilation errors
- [ ] T025 Run `./gradlew :features:composeApp:compileKotlinJvm` and fix any compilation errors (depends on T024)
- [ ] T026 Execute quickstart.md manual validation checklist: run `./gradlew :apps:desktopApp:run`, navegar para AssetHistoryScreen, testar os 5 cenários da tabela (arquivo válido, cancelamento, arquivo não-xlsx, arquivo corrompido, timeout simulado) e confirmar cada resultado esperado

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode iniciar imediatamente; T001 e T002 são independentes entre si [P]
- **Foundational (Phase 2)**: Depende da conclusão do Setup — **BLOQUEIA** todas as User Stories
  - T003 antes de T004
- **User Stories (Phase 3+)**: Todas dependem da conclusão da Phase 2
  - US1 → US2 → US3 em ordem de prioridade (ou em paralelo se houver capacidade)
- **Polish (Final Phase)**: Depende de todas as User Stories desejadas estarem completas

### User Story Dependencies

- **US1 (P1)**: Pode iniciar após Phase 2 — sem dependências em outras stories
- **US2 (P2)**: Pode iniciar após Phase 2 — compartilha `HistoryState` com US1 (T017 estende T013); T022 (DismissError) deve preceder T019
- **US3 (P3)**: Pode iniciar após Phase 2 — valida comportamento já implementado em US1 e US2 (T020/T021 são verificações, não novas implementações)

### Within Each User Story

- DTOs (T006–T010) antes de B3ImportPortImpl (T011)
- B3ImportPortImpl (T011) antes do registro Koin (T012)
- HistoryState/HistoryIntent (T013, T014) antes do ViewModel update (T015)
- ViewModel update (T015) antes da Screen update (T016)
- Teste (T005) pode ser escrito antes ou em paralelo com a implementação

### Parallel Opportunities

- T001 e T002 (Setup) podem rodar em paralelo
- T003 pode ser escrito enquanto T001/T002 estão em andamento
- T006–T010 (5 DTOs de US1) podem ser criados todos em paralelo
- T005 (teste do UseCase) pode ser escrito em paralelo com os DTOs
- T013 e T014 (HistoryState e HistoryIntent) são independentes entre si
- T023 e T024 (verificações de build) podem rodar em paralelo

---

## Parallel Example: User Story 1

```bash
# Criar todos os DTOs em paralelo (arquivos diferentes, sem dependências):
Task: "Create B3StockPosition DTO in .../b3/dto/B3StockPosition.kt"        # T006
Task: "Create B3EtfPosition DTO in .../b3/dto/B3EtfPosition.kt"            # T007
Task: "Create B3FundPosition DTO in .../b3/dto/B3FundPosition.kt"          # T008
Task: "Create B3FixedIncomePosition DTO in .../b3/dto/B3FixedIncomePosition.kt"  # T009
Task: "Create B3TreasuryPosition DTO in .../b3/dto/B3TreasuryPosition.kt"  # T010

# Em paralelo com os DTOs, escrever o teste do UseCase:
Task: "Create ImportB3FileUseCaseTest in .../services/ImportB3FileUseCaseTest.kt" # T005

# Após DTOs concluídos:
Task: "Implement B3ImportPortImpl in .../b3/B3ImportPortImpl.kt"            # T011

# Em paralelo, preparar o estado da UI:
Task: "Add isImporting to HistoryState.kt"                                  # T013
Task: "Add ImportB3File intent to HistoryViewModel.kt"                      # T014
```

---

## Implementation Strategy

### MVP First (User Story 1 — P1 Only)

1. Complete Phase 1: Setup (T001–T002)
2. Complete Phase 2: Foundational (T003–T004) — **CRÍTICO**
3. Complete Phase 3: User Story 1 (T005–T016)
4. **PARAR e VALIDAR**: Executar `./gradlew :domain:usecases:jvmTest` + teste manual no Desktop
5. Demo/validação com arquivo real `posicao-*.xlsx`

### Incremental Delivery

1. Setup + Foundational → base pronta
2. User Story 1 → teste independente → **MVP funcional** (botão + picker + log no console + spinner)
3. User Story 2 → rejections e mensagens de erro → experiência polida
4. User Story 3 → validação do cancel → qualidade completa
5. Polish → build verde + quickstart validado

### Parallel Team Strategy

Com múltiplos desenvolvedores (após Phase 2 completa):

- Dev A: T006–T012 (camada de dados — DTOs + B3ImportPortImpl + Koin)
- Dev B: T013–T016 (camada de UI — HistoryState + ViewModel + Screen)
- Dev C: T005 (testes do UseCase com MockK)

---

## Notes

- [P] = arquivos diferentes, sem dependências incompletas — seguro para paralelismo
- [Story] mapeia cada tarefa à sua user story para rastreabilidade
- Cada user story é implementável e testável de forma independente
- `FileMapper-KMP` é `commonMain` — zero código `expect`/`actual` e zero código de plataforma
- Android e iOS **não recebem** botão de importação nesta fase (scope = Desktop only)
- Commit após cada tarefa ou grupo lógico
- Parar em cada Checkpoint para validar a story de forma independente antes de avançar
