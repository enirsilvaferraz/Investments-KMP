# Tasks: Redesenho do Dialog de Transações com Lista em Draft

**Input**: Design documents from `/specs/002-transaction-form-redesign/`

**Prerequisites**: plan.md (✓), spec.md (✓), research.md (✓), data-model.md (✓), contracts/transaction-form-dialog.md (✓), quickstart.md (✓)

**Tests**: NÃO solicitados explicitamente em `spec.md` e marcados como opcionais em `research.md §D8`. Como a constitution exige testes apenas em `:domain:usecases` e esta feature não toca o domínio, **nenhuma task de teste automatizado é gerada**. A validação é feita via `quickstart.md` (preview + run manual).

**Organization**: Tasks agrupadas por user story (US1–US5) para permitir implementação e validação manual independentes. Como o refator concentra-se em ~5 ficheiros do mesmo módulo (`:features:asset-management` → `transactions/`), a maioria dos passos toca os mesmos ficheiros (`TransactionManagementView.kt`, `TransactionManagementViewModel.kt`, `TransactionManagementUiState.kt`) e portanto **não são paralelizáveis entre si**. Quando há `[P]` significa que a task vive num ficheiro distinto sem conflitos com outras tasks em curso.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (ficheiro diferente, sem dependência aberta).
- **[Story]**: Mapeia a task para a user story do `spec.md` (US1–US5).
- Cada task inclui caminho absoluto relativo ao repositório.

## Path Conventions

- Módulo: `core/presentation/asset-management` (accessor: `projects.features.assetManagement`).
- Pacote base da feature: `com.eferraz.asset_management.transactions` (e `com.eferraz.asset_management.assets` para um único ajuste em US5).
- Source set: `src/commonMain/kotlin/...` (Compose Multiplatform — Android/iOS/Desktop).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Sem inicialização de projeto/dependências — o módulo `:features:asset-management` e o ViewModel já existem. Apenas confirma o ponto de partida.

- [x] T001 Validar baseline de build do módulo antes de qualquer alteração rodando `./gradlew :features:assetManagement:compileKotlinJvm` e `./gradlew :apps:umbrellaApp:compileKotlinJvm` a partir da raiz do repositório, registando que ambos terminam com `BUILD SUCCESSFUL` (referência para regressão posterior).

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Mudanças de estado/eventos/snapshot que são pré-requisito para qualquer User Story (todas as US dependem do `initialSnapshot`, do helper `matchesByPosition`, do novo `isDirty` e do `Save` com diff). Sem este bloco, US1–US5 não podem ser validadas.

**⚠️ CRITICAL**: Nenhum trabalho de user story (US1–US5) pode começar até esta fase estar completa.

- [x] T002 Adicionar campo `initialSnapshot: List<TransactionDraftUi> = emptyList()` ao data class `TransactionManagementUiState` no ficheiro `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementUiState.kt`, mantendo a anotação `@Immutable` e a visibilidade `internal` (conforme `data-model.md §TransactionManagementUiState`).

- [x] T003 Implementar o helper `internal fun TransactionDraftUi.matchesByPosition(other: TransactionDraftUi, category: InvestmentCategory): Boolean` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementUiState.kt`, comparando `dateDigits`, `type`, `totalValue` e — quando `category == InvestmentCategory.VARIABLE_INCOME` — também `quantity` e `unitPrice`. NÃO comparar `id`, `isNew`, `observations` nem `category` (conforme `data-model.md §matchesByPosition`).

- [x] T004 Adicionar a propriedade derivada `val isDirty: Boolean` em `TransactionManagementUiState` no ficheiro `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementUiState.kt`, com a fórmula: `transactions.size != initialSnapshot.size || transactions.indices.any { i -> !transactions[i].matchesByPosition(initialSnapshot[i], category) }` (cobre FR-010 e FR-011).

- [x] T005 Remover o evento `DraftTransactionObservationChanged` do `sealed class TransactionManagementEvents` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementEvents.kt` (FR-005a + `data-model.md §Eventos`). Sem substituição — não há mais UI para o campo.

- [x] T006 Atualizar `TransactionManagementViewModel.loadInitialState` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt` para, após obter a lista de `GetTransactionsByHoldingUseCase`, **ordenar por data ascendente** (usando `localDateFromIsoDateDigits` ou o `LocalDate` do domínio antes de mapear para `TransactionDraftUi`) e popular tanto `transactions` quanto `initialSnapshot` com a mesma lista resultante (FR-001, FR-002, `research.md §D5`).

- [x] T007 Remover o branch que trata `DraftTransactionObservationChanged` da função `onEvent`/`reduce` do `TransactionManagementViewModel` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt`, garantindo que o `when` continua exaustivo após a remoção do evento (depende de T005).

- [x] T008 Refatorar o tratamento de `DraftTransactionDeleteClicked` no `TransactionManagementViewModel` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt` para apenas **remover a linha da lista exibida** (`state.copy(transactions = transactions.filterIndexed { i, _ -> i != index })`) **sem** invocar `DeleteTransactionUseCase`. A exclusão real passa a ocorrer só no `Save` (FR-008, FR-009, `contract §3`).

- [x] T009 Reescrever `onSave` no `TransactionManagementViewModel` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt` conforme `contract §4` e `research.md §D3`:
  - Guard: ignorar quando `state.holding == null`, `state.isSaving`, ou `!state.isDirty`.
  - Marcar `isSaving = true`.
  - Calcular `removeIds = initialSnapshot.mapNotNull { it.id }.toSet() - transactions.mapNotNull { it.id }.toSet()`.
  - Calcular `upserts = transactions.mapNotNull { it.toDomainTransaction(holding, category) }`.
  - Dentro de `runCatching`, executar primeiro todos os `deleteTransactionUseCase(DeleteTransactionUseCase.Param(id)).getOrThrow()` e depois todos os `saveTransactionUseCase(SaveTransactionUseCase.Param(tx)).getOrThrow()`.
  - `onSuccess`: `state = state.copy(isSaving = false, isCompleted = true)`.
  - `onFailure`: `state = state.copy(isSaving = false)` (manter `transactions`/`initialSnapshot` intactos — FR-015).

**Checkpoint**: Estado + eventos + Save com diff funcionais. A partir daqui, US1–US5 podem progredir.

---

## Phase 3: User Story 1 - Visualizar e editar transações existentes de uma holding (Priority: P1) 🎯 MVP

**Goal**: Substituir o render baseado em `UiTableV3` por um `Column`/`Row` que exibe e permite editar as transações da holding, respeitando as colunas por categoria e a ordenação inicial por data.

> **Alinhamento 2026-05-19**: T012 foi revista após decisão de usar **`FormTextField`** (helper local, mesmo componente do protótipo `NewTransactionsTable`) em todas as células — **não** `TableInputDate`/`TableInputSelect`/`TableInputMoney`. Se a implementação anterior tiver usado os `TableInput*`, esta task volta a `[ ]` e deve ser reaplicada conforme o novo mapeamento; T011 e T013 continuam válidas (só as células mudam).

**Independent Test**: Abrir o dialog para uma holding com transações, conforme `quickstart.md §3 Cenário 1` e §2 (preview para `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND`). Verificar (a) carga ordenada, (b) colunas por categoria, (c) layout sem `UiTableV3`, (d) edição inline reflete em memória.

### Implementation for User Story 1

- [x] T010 [US1] No ficheiro `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, **remover todas as referências a `UiTableV3` e `StableList`** (imports, parâmetros, construção de colunas) do Composable `TransactionTable` (ou equivalente atual), preparando-o para a reescrita em `Column`/`Row`. Garantir que nenhum import de `com.eferraz.design_system.table.UiTableV3` permanece (SC-006, FR-003).

- [x] T011 [US1] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, reescrever o body do Composable `TransactionTable` (que recebe `state: TransactionManagementUiState` e `onEvent: (TransactionManagementEvents) -> Unit`) como um `Column(verticalArrangement = Arrangement.spacedBy(8.dp))` contendo:
  1. Um `Row` de cabeçalho com células textuais `Data`, `Transação`, (`Quantidade`, `Unitário` quando `state.category == InvestmentCategory.VARIABLE_INCOME`), `Valor Total`, e uma célula vazia para a ação X.
  2. Para cada `(index, draft) in state.transactions.withIndex()`, um `Row` com os inputs do design-system (vide T012).
  3. Sem nenhum `LazyColumn`/grid — `Column` simples (conforme `research.md §D1`).
  Manter o pacote `com.eferraz.asset_management.transactions` e a visibilidade `private`/`internal` do Composable (FR-003, FR-004).

- [x] T012 [US1] Para cada `Row` de linha no `TransactionTable` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, ligar as células ao **`FormTextField`** (helper `com.eferraz.asset_management.helpers.FormTextField`, **mesmo** componente usado pelo protótipo `NewTransactionsTable`) e aos eventos correspondentes (`contract §2` + `data-model.md §Eventos` + `research.md §D1 / §D1.1 / §D1.2`). **NÃO usar `TableInputDate`, `TableInputSelect` nem `TableInputMoney`** — esses componentes do `:features:design-system` ficam fora do escopo desta feature. Mapeamento por célula:
  - Data → `FormTextField(label = "", value = draft.dateDigits, onValueChange = { onEvent(DraftTransactionDateChanged(index, it)) }, errorMessage = if (draft.dateError) "Inválido" else null)` com `Modifier.width(125.dp)`.
  - Transação → `FormTextField(label = "", value = draft.type.asLabel(), onValueChange = { raw -> TransactionType.entries.firstOrNull { it.asLabel().equals(raw, ignoreCase = true) }?.let { onEvent(DraftTransactionTypeChanged(index, it)) } }, errorMessage = null)` com `Modifier.width(130.dp)`.
  - Quantidade (apenas `VARIABLE_INCOME`) → `FormTextField(label = "", value = draft.quantity, onValueChange = { onEvent(DraftTransactionQuantityChanged(index, it)) }, errorMessage = if (draft.quantityError) "Inválido" else null)` com `Modifier.weight(.5f)`.
  - Valor Unitário (apenas `VARIABLE_INCOME`) → `FormTextField(label = "", value = draft.unitPrice, onValueChange = { onEvent(DraftTransactionUnitPriceChanged(index, it)) }, errorMessage = if (draft.unitPriceError) "Inválido" else null)` com `Modifier.weight(1.1f)`.
  - Valor Total → `FormTextField(label = "", value = draft.totalValue, onValueChange = { onEvent(DraftTransactionTotalValueChanged(index, it)) }, errorMessage = if (draft.totalValueError) "Inválido" else null)` com `Modifier.weight(1.1f)`.
  Garantir que **nenhuma célula expõe `observations`** (FR-005, FR-005a). Larguras/pesos espelham exatamente os de `NewTransactionsTable`.

- [x] T013 [US1] Atualizar/criar `TransactionFormViewPreview` no ficheiro `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt` para parametrizar por categoria (`FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND`) e por estado vazio, conforme `quickstart.md §2`. Cada preview deve renderizar `TransactionTable` com um `TransactionManagementUiState` mockado (transactions e initialSnapshot iguais ao abrir; sem linhas no caso vazio).

**Checkpoint**: Tabela renderiza, lista por categoria correta, edições mutam o `state.transactions` em memória, `UiTableV3` zerado no módulo. Verificar `quickstart §2` e §3 Cenário 1.

---

## Phase 4: User Story 2 - Adicionar uma nova transação como rascunho (Priority: P1)

**Goal**: Botão **Adicionar** abaixo da tabela acrescenta uma linha em branco com a data de hoje sem persistir; só é gravada no Save.

**Independent Test**: `quickstart.md §3 Cenário 3` — clicar em Adicionar, verificar nova linha no final, fechar sem salvar, reabrir e confirmar que nada foi persistido.

### Implementation for User Story 2

- [x] T014 [US2] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, **abaixo** do `Column` da tabela (e fora dele), adicionar um `Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End))` que contém um `OutlinedButton(onClick = { onEvent(AddTransactionDraft) })` com label `"Adicionar"`. Remover qualquer footer anterior do `UiTableV3` que disparava `AddTransactionDraft` (FR-006, `research.md §D6`).

- [x] T015 [US2] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementViewModel.kt`, garantir que o handler de `AddTransactionDraft` cria um `TransactionDraftUi(isNew = true, dateDigits = currentDate, category = state.value.category, type = TransactionType.<default>, quantity = "", unitPrice = "", totalValue = "", observations = "")` e o **acrescenta ao final** de `state.transactions` (não reordena, não toca `initialSnapshot`). `currentDate` deve vir de `GetCurrentDateUseCase` (já injetado) formatado para `dateDigits` ISO. (FR-006, FR-002, FR-005a).

**Checkpoint**: Clique em Adicionar produz linha nova com data de hoje, lista cresce em memória, banco intocado até Save.

---

## Phase 5: User Story 3 - Excluir uma transação como rascunho (Priority: P1)

**Goal**: Botão **X** em cada linha remove a linha da lista exibida sem tocar no banco; a exclusão real só ocorre no Save.

**Independent Test**: `quickstart.md §3 Cenário 4` — clicar no X de uma linha existente, fechar sem salvar, reabrir e confirmar que a linha continua presente; salvar e confirmar exclusão definitiva.

### Implementation for User Story 3

- [x] T016 [US3] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, dentro do `Row` de cada linha do `TransactionTable`, adicionar um `IconButton(onClick = { onEvent(DraftTransactionDeleteClicked(index)) }) { Icon(Icons.Default.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error) }` como **última célula** do `Row`. Largura/alinhamento consistentes com o cabeçalho (T011). (FR-008, `contract §2`).

> A semântica do evento `DraftTransactionDeleteClicked` (sem chamada a `DeleteTransactionUseCase`) já foi implementada em T008.

**Checkpoint**: Clique no X retira a linha apenas da UI; reabrir o dialog sem Save restaura a linha; Save aplica a exclusão real no banco.

---

## Phase 6: User Story 4 - Botão Salvar habilitado apenas quando a lista difere da original (Priority: P1)

**Goal**: Botão **Salvar** abaixo da tabela, habilitado apenas quando `state.isDirty && !state.isSaving`; ao clicar, persiste o diff (delete + upsert) e fecha o dialog.

**Independent Test**: `quickstart.md §3 Cenários 2, 4 e 5` — abrir dialog (Salvar desabilitado), editar/adicionar/remover (Salvar habilita), reverter manualmente (Salvar desabilita), salvar com sucesso (dialog fecha em ≤ 2 s).

### Implementation for User Story 4

- [x] T017 [US4] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, no mesmo `Row` de ações abaixo da tabela (criado em T014), adicionar um `Button(onClick = { onEvent(Save) }, enabled = state.isDirty && !state.isSaving)` com label `"Salvar"`. Alinhar ao padrão `Arrangement.spacedBy(8.dp, Alignment.End)` (espelha `Actions(...)` de `AssetManagementScreen.kt`). (FR-007, FR-010, FR-011, `research.md §D6`).

> O handler `Save` no ViewModel (delete + upsert calculados a partir de `initialSnapshot`, com `onSuccess → isCompleted = true`, `onFailure → isSaving = false`) já foi implementado em T009. O fecho automático em sucesso depende do `LaunchedEffect(state.isCompleted)` em `TransactionFormView` (a confirmar na próxima task).

- [x] T018 [US4] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, garantir que o Composable público `TransactionFormView(modifier, holdingId, onComplete)` mantém um `LaunchedEffect(state.isCompleted)` que invoca `onComplete()` quando `state.isCompleted == true`, e um `LaunchedEffect(holdingId)` que dispara `onEvent(ScreenEntered(holdingId))` (conforme `contract §1.2`). A assinatura pública NÃO pode mudar (princípio VI — API explícita).

**Checkpoint**: Sem alterações, Salvar desabilitado; qualquer divergência (campo, adição, remoção) habilita; reverter desabilita; clique em Salvar aplica diff e fecha o dialog.

---

## Phase 7: User Story 5 - Padrão visual e estrutural alinhado ao dialog de Asset Management (Priority: P2)

**Goal**: O `TransactionFormDialog` usa o mesmo `AppContentDialog` que o `AssetManagementDialog`, com título "Transações", botão X no topo (descartando draft) e área de ações abaixo da tabela. Como cleanup, o protótipo `NewTransactionsTable` deixa de ser invocado.

**Independent Test**: `spec.md §User Story 5 / Acceptance Scenarios 1–2` + `quickstart.md §3 Cenário 7` — abrir o dialog de Transações lado a lado com o de Asset; confirmar mesmo container, título e bloco de ações; clicar no X descarta o rascunho e fecha sem persistir.

### Implementation for User Story 5

- [x] T019 [US5] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementView.kt`, garantir que o Composable público `TransactionFormDialog(modifier, holdingId, onDismiss)` é renderizado dentro de um `AppContentDialog` com `title = "Transações"` e `onDismissRequest = onDismiss` (X no topo). O corpo do `AppContentDialog` chama `TransactionFormView(holdingId = holdingId, onComplete = onDismiss)`, alinhado ao padrão estrutural usado por `AssetManagementDialog` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt` (FR-012, FR-013, FR-014).

- [x] T020 [P] [US5] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`, remover a chamada inline a `NewTransactionsTable()` dentro de `AssetFormView` (era apenas um placeholder estático). O Composable de Asset não deve renderizar lista de transações inline (`plan.md §Project Structure`).

- [x] T021 [P] [US5] Em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementScreen.kt`, apagar o ficheiro inteiro caso após T020 nenhuma referência a `NewTransactionsTable` ou ao Composable raiz desse ficheiro permaneça no módulo (verificar via busca por `NewTransactionsTable` em `core/presentation/asset-management`). Se ainda houver chamadores, manter apenas o estritamente referenciado e remover o restante. (`plan.md §Project Structure`, `quickstart.md §2`).

**Checkpoint**: Dialog de Transações estruturalmente igual ao de Asset; X descarta rascunho; placeholder removido do módulo.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Limpeza final e verificação cruzada com `quickstart.md`.

- [x] T022 Executar `./gradlew :features:assetManagement:compileKotlinJvm` e `./gradlew :apps:umbrellaApp:compileKotlinJvm` a partir da raiz e confirmar `BUILD SUCCESSFUL` em ambos (alinha com `research.md §D9` e `quickstart.md §1`).

- [x] T023 [P] Rodar uma busca textual em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/` por `UiTableV3` e por `StableList` e confirmar **zero ocorrências** (SC-006, FR-003). Documentar o resultado no PR (ex.: `rg UiTableV3 core/presentation/asset-management/.../transactions/`).

- [x] T024 [P] Rodar uma busca por `DraftTransactionObservationChanged` em todo `core/presentation/asset-management/` e confirmar **zero ocorrências** (FR-005a + `data-model.md §Eventos`).

- [x] T025 Validar manualmente todos os cenários da `quickstart.md §3 (Cenários 1–8)` e marcar a checklist `quickstart.md §4` ponto a ponto no PR. A `Cenário 8 — Falha de Save` é opcional (smoke test) mas recomendada para confirmar FR-015 (dialog não fecha, `isSaving` volta a `false`, draft preservado).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)** — T001: pré-requisito de qualquer trabalho (baseline).
- **Phase 2 (Foundational)** — T002–T009: **bloqueia** Phases 3–7. Ordem interna recomendada: T002 → T003 → T004 (estado + helper + isDirty); T005 → T007 (evento removido + handler removido, T007 depende de T005); T006 (ordenação + initialSnapshot, depende de T002); T008 (delete drift, depende de T002 e T005); T009 (Save com diff, depende de T002, T004 e T008).
- **Phase 3 (US1)** — T010 → T011 → T012 → T013: depende de Phase 2 (precisa do `isDirty` e dos eventos atuais). T010 prepara o terreno (remover `UiTableV3`); T011 reescreve a estrutura; T012 liga eventos; T013 atualiza preview.
- **Phase 4 (US2)** — T014 → T015: T014 depende de T011 (precisa do Composable já em `Column`/`Row`); T015 depende de T002 (estado de inicialização da nova linha) e é independente da Phase 3 no ViewModel mas amarra ao Composable atualizado.
- **Phase 5 (US3)** — T016: depende de T011 (estrutura `Row` por linha) e de T008 (semântica de delete já corrigida).
- **Phase 6 (US4)** — T017 → T018: T017 depende de T014 (Row de ações já existe) e de T004/T009 (estado + Save); T018 depende de T009 (`isCompleted`).
- **Phase 7 (US5)** — T019 depende de T018 (`onComplete`/`onDismiss` cabeados); T020 e T021 são independentes entre si e do dialog (podem rodar em paralelo, vide [P]).
- **Phase 8 (Polish)** — T022–T025: depende de todas as anteriores.

### User Story Dependencies

- **US1 (P1)**: depende apenas de Phase 2.
- **US2 (P1)**: depende de Phase 2 + T011 (Composable em `Column`/`Row`).
- **US3 (P1)**: depende de Phase 2 + T011.
- **US4 (P1)**: depende de Phase 2 + T014 (Row de ações).
- **US5 (P2)**: depende de US4 concluída (T018) para o `onComplete`; cleanups (T020/T021) são independentes.

### Within Each User Story

- O `TransactionFormDialog` e `TransactionFormView` continuam `public` (princípio VI). Demais Composables/funções permanecem `internal`/`private`.
- Composables alteram o mesmo ficheiro (`TransactionManagementView.kt`) → tasks da mesma fase **não** são paralelas entre si, salvo onde marcado `[P]` (ficheiros distintos).

### Parallel Opportunities

- T020 e T021 (US5) tocam ficheiros distintos (`AssetManagementScreen.kt` e `TransactionManagementScreen.kt`) e podem rodar em paralelo.
- T023 e T024 (Polish) são buscas independentes e podem rodar em paralelo.
- Dentro de Phase 2, T002/T003/T004 estão todos em `TransactionManagementUiState.kt` (mesmo ficheiro → sem `[P]`), e T005 em `TransactionManagementEvents.kt` pode rodar em paralelo com T002–T004 (ficheiro diferente, sem dependência aberta); o sistema atual não marca essa paralelização para preservar a ordem lógica de leitura — opcional.

---

## Parallel Example: User Story 5

```bash
# T020 e T021 podem ser executadas simultaneamente (ficheiros distintos):
Task: "Remover invocação de NewTransactionsTable() em core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt"
Task: "Apagar (ou reduzir) core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/transactions/TransactionManagementScreen.kt se sem chamadores"
```

## Parallel Example: Polish

```bash
Task: "Confirmar zero ocorrências de UiTableV3/StableList em core/presentation/asset-management/.../transactions/"
Task: "Confirmar zero ocorrências de DraftTransactionObservationChanged em core/presentation/asset-management/"
```

---

## Implementation Strategy

### MVP First (US1–US4 — todas P1)

1. **Phase 1 (T001)**: baseline de build OK.
2. **Phase 2 (T002–T009)**: estado + helper + isDirty + evento removido + ordenação + delete drift + Save com diff.
3. **Phase 3 (T010–T013, US1)**: tabela renderiza com `Column`/`Row` por categoria e edição funciona.
4. **Phase 4 (T014–T015, US2)**: botão Adicionar abaixo da tabela cria linha em branco.
5. **Phase 5 (T016, US3)**: botão X remove linha em rascunho.
6. **Phase 6 (T017–T018, US4)**: botão Salvar habilitado pelo `isDirty`, fluxo completo de persistência + fecho.
7. **STOP e VALIDATE**: rodar `quickstart.md §3 Cenários 1–5 e 7`. Se passar, é o MVP entregável.

### Incremental Delivery

- MVP (US1+US2+US3+US4): dialog redesenhado totalmente funcional. Já cumpre `SC-001..SC-006`.
- Acrescentar US5 (T019–T021): consistência visual com `AssetManagementDialog` e cleanup do placeholder. Cumpre `SC-007`.
- Phase 8 (T022–T025): verificação cruzada antes do merge.

### Parallel Team Strategy

Para múltiplos desenvolvedores no mesmo PR (caso raro, dado o escopo pequeno):

1. Dev A foca em Phase 2 (ViewModel/UiState/Events).
2. Dev B aguarda checkpoint da Phase 2 e ataca Phase 3 (View — `Column`/`Row`).
3. Dev C pega Phase 7 cleanup (T020/T021) em paralelo a Phase 6/7.
   - Nota: como a maior parte das tasks toca os mesmos 3 ficheiros, o paralelismo real é limitado; preferir um único responsável pela Phase 2 + Phase 3 para evitar merges trabalhosos.

---

## Notes

- `[P]` = ficheiro distinto + sem dependência aberta.
- `[Story]` = traceability com `spec.md` (US1–US5).
- Nenhuma task de teste automatizado (`research.md §D8` deixou opcional; `:domain:usecases` não é tocado).
- Validação manual via `quickstart.md`.
- Princípios I (SOLID/KISS), II (Clean Architecture), III (KMP First), IV (Foundation), VI (API Explícita), VII (docs sync), VIII (idioma) já cobertos pelo plan — esta lista apenas executa.
- Commit sugerido após cada Phase (1 commit por fase, ou agrupando US1–US4 em três commits — opcional via `speckit-git-commit`).
