# Tasks: Dialog Unificado de Cadastro de Ativo + Holding

**Input**: Design documents de `/specs/001-asset-holding-dialog/`

**Pré-requisitos**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅, quickstart.md ✅

**Testes**: Não solicitados na spec — sem tarefas de teste.

**Organização**: Tarefas agrupadas por User Story para implementação e teste independentes.

## Formato: `[ID] [P?] [Story?] Descrição`

- **[P]**: Pode executar em paralelo (arquivos diferentes, sem dependências em tarefas incompletas)
- **[Story]**: User story correspondente (US1, US2, US3)
- Caminhos base: `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/`

---

## Phase 1: Setup

**Propósito**: Preparação do subpacote `dialog/` e ajuste do contexto de edição

- [X] T001 Criar diretório `dialog/` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/dialog/` (pasta vazia para o DialogViewModel)
- [X] T002 [P] Atualizar `AssetManagementEditContext` para substituir `editAssetId: Long?` por `holdingId: Long?` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/AssetManagementEditContext.kt`

---

## Phase 2: Foundational (Pré-requisitos Bloqueantes)

**Propósito**: Infraestrutura compartilhada que DEVE estar completa antes de qualquer User Story

**⚠️ CRÍTICO**: Nenhuma User Story pode começar enquanto esta fase não estiver completa

- [X] T003 Criar `DialogState` (data class com `isOpen: Boolean = true`, `holdingId: Long? = null`) e `DialogEvents` (sealed class com `Dismiss`) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/dialog/DialogViewModel.kt`
- [X] T004 Criar `DialogViewModel` interno com `StateFlow<DialogState>` usando explicit backing field (`field = MutableStateFlow(DialogState())`), handler de `Dismiss` que seta `isOpen = false`, e anotação `@KoinViewModel` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/dialog/DialogViewModel.kt`
- [X] T005 [P] Adicionar campos `brokerage: Brokerage?`, `brokerages: List<Brokerage>`, `brokerageError: String?`, `holdingId: Long?`, `owner: Owner?` ao `AssetManagementUiState` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementUiState.kt`
- [X] T006 [P] Adicionar `BrokerageChanged(brokerage: Brokerage)` ao sealed class `AssetManagementEvents` e modificar `ScreenEntered` para aceitar `holdingId: Long?` (substituindo `assetId`) em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementEvents.kt`

**Checkpoint**: Fundação pronta — implementação das User Stories pode começar

---

## Phase 3: User Story 1 + User Story 3 — Cadastrar novo investimento com holding (P1) 🎯 MVP

**Goal**: Usuário clica no FAB (+), dialog em tela cheia abre com formulário asset + corretora, salva ambos sequencialmente e o dialog fecha automaticamente. Responsabilidades separadas: `DialogViewModel` gerencia ciclo de vida, `AssetManagementViewModel` gerencia persistência.

**Independent Test**: Abrir o app, clicar no FAB (+), preencher todos os campos incluindo corretora, clicar em "Salvar" e verificar que ativo + holding foram persistidos no banco e o dialog fechou em até 1 segundo.

### Implementação — US1 + US3

- [X] T007 [P] [US1] [US3] Injetar `GetBrokeragesUseCase`, `GetAssetHoldingUseCase` e `UpsertAssetHoldingUseCase` no construtor do `AssetManagementViewModel` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T008 [US1] Implementar carregamento de corretoras via `GetBrokeragesUseCase` dentro de `loadInitialState()` (ou `init`) do `AssetManagementViewModel`, populando `state.brokerages` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T009 [US1] Implementar handler de `BrokerageChanged` no `AssetManagementViewModel` que atualiza `state.brokerage` e limpa `state.brokerageError` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T010 [US1] [US3] Refatorar `onSave()` no `AssetManagementViewModel` para persistência sequencial: (1) `upsertAssetUseCase(asset)` → (2) `upsertAssetHoldingUseCase(holding com newAsset + brokerage + owner)` → `state.isCompleted = true`; erro em asset → exibir erro, `isSaving = false`; asset salvo mas holding falhou → exibir erro holding, `isSaving = false` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T011 [US1] [US3] Refatorar `AssetManagementScreen.kt` em Composable público `AssetManagementScreen(holdingId: Long?, onDismiss: () -> Unit)` com `Scaffold` + `TopAppBar` (título dinâmico "Novo investimento" / "Editar investimento" + `IconButton` com `Icons.Default.Close`) e `LaunchedEffect(state.isCompleted)` que chama `onDismiss()` quando `isCompleted == true`; Composable interno `AssetFormView` mantém o conteúdo do formulário em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T012 [US1] Adicionar `StableExposedDropdown` para seleção de `Brokerage` (usando `state.brokerages`, `state.brokerage`, `state.brokerageError`) ao `AssetFormView`, disparando evento `BrokerageChanged`, com label `BROKERAGE_FIELD_LABEL` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`
- [X] T013 [US1] [US3] Verificar e atualizar `AssetManagementModule.kt` para que `DialogViewModel` seja registrado via `@ComponentScan` ou declaração explícita no módulo Koin em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/di/AssetManagementModule.kt`
- [X] T014 [US1] Descomentar e completar o entry `AssetManagementRouting` com `DialogSceneStrategy.dialog(DialogProperties(...))` e `AssetManagementScreen(holdingId = it.holdingId, onDismiss = { backStack.removeLastOrNull() })` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/App.kt`
- [X] T015 [US1] Remover `HoldingManagementView.kt` de `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/holdings/HoldingManagementView.kt` (FR-007: campo de corretora já incorporado inline)

**Checkpoint**: US1 + US3 completos — cadastro de novo investimento + holding funcional, dialog fecha automaticamente, responsabilidades separadas

---

## Phase 4: User Story 2 — Editar holding existente via dialog (P2)

**Goal**: Usuário acessa histórico de holdings, clica em editar; o dialog abre pré-populado com dados do ativo e corretora; usuário altera campos (exceto categoria) e salva.

**Independent Test**: Navegar ao histórico, clicar em editar um holding, verificar que o dialog abre com dados pré-populados, alterar a corretora, salvar e confirmar que a mudança foi persistida no banco.

### Implementação — US2

- [X] T016 [US2] Implementar carregamento do `AssetHolding` via `GetAssetHoldingUseCase(ById(holdingId))` no handler de `ScreenEntered(holdingId)` quando `holdingId != null` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T017 [US2] Pré-popular `AssetManagementUiState` com dados do `AssetHolding` carregado: mapear `asset` via `toUiState()` existente + preencher `brokerage`, `owner`, `holdingId` em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementViewModel.kt`
- [X] T018 [US2] Desabilitar campo de categoria no `AssetFormView` quando `state.holdingId != null` (modo edição), reutilizando a lógica `enabled = ui.asset == null` já existente no dropdown de Categoria em `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/assets/AssetManagementScreen.kt`

**Checkpoint**: US1 + US2 + US3 completos — fluxos de criação e edição funcionais

---

## Phase 5: Polish & Cross-Cutting Concerns

**Propósito**: Limpeza de código legado e validação final

- [X] T019 [P] Remover `HoldingManagementViewModel.kt`, `HoldingManagementUiState.kt` e `HoldingManagementEvents.kt` de `core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/holdings/` (funcionalidade absorvida pelo AssetManagementViewModel)
- [X] T020 Validar compilação do módulo afetado: `./gradlew :features:asset-management:compileKotlinJvm`
- [X] T021 Validar integração com shell da app: `./gradlew :features:composeApp:compileKotlinJvm`

---

## Dependências & Ordem de Execução

### Dependências entre Fases

- **Setup (Phase 1)**: Sem dependências — pode iniciar imediatamente
- **Foundational (Phase 2)**: Depende do Setup — BLOQUEIA todas as User Stories
- **US1 + US3 (Phase 3)**: Depende do Foundational — MVP entregável
- **US2 (Phase 4)**: Depende do Foundational + pode iniciar após US1 (compartilha `AssetManagementViewModel.kt`)
- **Polish (Phase 5)**: Depende de todas as User Stories desejadas estarem completas

### Dependências entre Tarefas

- T004 → depende de T003 (mesmo arquivo)
- T007, T008, T009, T010 → dependem de T005 + T006
- T011, T012 → dependem de T007 (para eventos do ViewModel)
- T013, T014 → dependem de T004 (DialogViewModel precisa existir)
- T015 → pode ser feito a qualquer momento após T012 estar completo
- T016, T017 → dependem de T005 (campos novos no UiState) + T007 (injeção do UseCase)
- T018 → depende de T016, T017
- T019 → depende de T015 (Holding removido do View antes de limpar o subpacote)
- T020, T021 → dependem de todas as tarefas de implementação

### Oportunidades Paralelas

- T002 pode rodar em paralelo com T003–T006 (arquivo diferente)
- T005 e T006 podem rodar em paralelo (arquivos diferentes)
- T007 pode rodar em paralelo com T008, T009 (mesmo arquivo, seções independentes — merge necessário)
- T019 pode rodar em paralelo com T020 e T021
- US2 (T016–T018) pode rodar em paralelo com US1 se equipe tem múltiplos desenvolvedores, exceto em `AssetManagementViewModel.kt`

---

## Exemplo Paralelo: Phase 2 (Foundational)

```bash
# Lançar em paralelo (arquivos diferentes):
Task: T005 — AssetManagementUiState.kt (adicionar campos holding)
Task: T006 — AssetManagementEvents.kt (adicionar BrokerageChanged)
# Sequencial:
Task: T003 → T004 — DialogViewModel.kt (tipos → ViewModel)
```

---

## Estratégia de Implementação

### MVP Primeiro (US1 + US3 apenas)

1. Completar Phase 1: Setup (T001–T002)
2. Completar Phase 2: Foundational (T003–T006) — CRÍTICO
3. Completar Phase 3: US1 + US3 (T007–T015)
4. **PARAR E VALIDAR**: Testar fluxo de cadastro manualmente (quickstart.md passo 1–6)
5. Demo/entrega do MVP

### Entrega Incremental

1. Setup + Foundational → base pronta
2. US1 + US3 → criar investimento via dialog funcional → **Demo MVP**
3. US2 → editar holding via dialog → **Demo completo**
4. Polish → remoção de código legado + validação de compilação

---

## Notas

- `[P]` = arquivos diferentes, sem dependências pendentes entre si
- `[US1]`, `[US2]`, `[US3]` = rastreabilidade à User Story correspondente
- US3 (separação de responsabilidades) é arquitetural — suas tarefas são co-marcadas com `[US3]` na Phase 3
- Nenhum novo módulo Gradle necessário — toda a feature fica em `:features:asset-management` existente
- Owner padrão para novos holdings: investigar `HoldingManagementViewModel` antes de remover (T007/T016)
- Commit após cada tarefa ou grupo lógico concluído
