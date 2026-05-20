# Implementation Plan: Redesenho do Dialog de Transações com Lista em Draft

**Branch**: `002-transaction-form-redesign` | **Date**: 2026-05-19 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-transaction-form-redesign/spec.md`

## Summary

Reescrever a renderização do `TransactionFormDialog` (`core/presentation/asset-management/.../transactions/TransactionManagementView.kt`) substituindo o componente `UiTableV3` por um layout `Column`+`Row` inspirado no protótipo `NewTransactionsTable` (no `TransactionManagementScreen.kt` do mesmo módulo). A lista passa a operar exclusivamente como **rascunho em memória**: adições, edições e remoções só atingem o banco quando o utilizador clica em **Salvar**. O botão **Salvar** fica abaixo da tabela e só é habilitado quando a lista exibida difere — por **pareamento posicional** sobre os campos editáveis aplicáveis à categoria (data, tipo, quantidade, valor unitário, valor total) — da snapshot inicial carregada do banco. O campo `observations` deixa de ser visível/editável e é preservado intacto no round-trip das transações existentes. O dialog mantém o invólucro `AppContentDialog` (mesmo padrão visual do `AssetManagementDialog`). Carregamento por holding já existe via `GetTransactionsByHoldingUseCase`; a ordenação por data ocorre exclusivamente no momento da carga inicial.

## Technical Context

**Language/Version**: Kotlin 2.3+ (com explicit backing fields), Kotlin Multiplatform

**Primary Dependencies**:
- Compose Multiplatform (UI)
- AndroidX Lifecycle ViewModel + `koinViewModel`
- Koin (DI; `@KoinViewModel`)
- kotlinx.datetime (datas)
- kotlinx.coroutines (Flow/`viewModelScope`)
- Componentes existentes: `AppContentDialog` do `:features:design-system`; `FormTextField` do `:features:asset-management/helpers` (mesmo input usado em `NewTransactionsTable`); `IconButton`/`Icons.Default.Close` do Material 3 para a ação de remover linha. **Não** serão usados `TableInputDate`/`TableInputSelect`/`TableInputMoney` — a nova tabela espelha o protótipo `NewTransactionsTable` (apenas `Row`/`Column`/`FormTextField`).

**Storage**: Room 3 (via `:data:database`), acessado indiretamente por `UpsertTransactionUseCase` (`SaveTransactionUseCase`), `DeleteTransactionUseCase` e `GetTransactionsByHoldingUseCase`. Sem alterações de schema.

**Testing**: MockK + kotlinx.coroutines.test, padrão `GIVEN_WHEN_THEN`, `./gradlew :domain:usecases:jvmTest`. Não há alterações em `:domain:usecases` previstas; testes do ViewModel ficam ao critério do módulo de apresentação (sem alteração de regras de domínio).

**Target Platform**: Android, iOS, Desktop (JVM) — Compose Multiplatform.

**Project Type**: mobile-app + desktop-app (monorepo KMP).

**Performance Goals**: Save conclui e fecha o dialog em até 2 s após persistência (SC-005). Render de até ~50 linhas sem queda perceptível de fluidez (60 fps no scroll vertical do dialog).

**Constraints**: Offline-capable (Room local). UI reativa via `StateFlow` com explicit backing field (`field = MutableStateFlow(...)`). Sem uso de `UiTableV3` (`SC-006`).

**Scale/Scope**: ~5 arquivos tocados em `:features:asset-management`. Nenhum módulo Gradle novo. Nenhuma alteração no shell `:apps:umbrellaApp`. Nenhuma alteração em `:domain` ou `:data`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Status | Evidência |
|-----------|--------|-----------|
| I. SOLID, DRY, Boas Práticas | PASS | S: `TransactionManagementViewModel` continua responsável apenas pelo estado/persistência do form; `TransactionTable` (Composable) cuida da renderização. O: o novo layout é independente do `UiTableV3` (substituição limpa, sem `if`s espalhados). D: VM depende dos UseCases (abstrações), não de repositórios concretos. KISS/YAGNI: cumprir só os 16 FRs declarados, sem reordenação automática, sem confirmações extras (FR-016). DRY: reaproveitar `FormTextField` e `AppContentDialog` já existentes, espelhando o protótipo `NewTransactionsTable` sem introduzir novos componentes. |
| II. Clean Architecture | PASS | Alterações restritas a `:features:asset-management` (apresentação). Nenhum acesso direto a `:data:*`; persistência continua via UseCases. Sem dependências adicionais entre camadas. |
| III. KMP First | PASS | Todo o código vive em `commonMain`. Nenhum `expect`/`actual` necessário (Compose Multiplatform + componentes já cross-platform). |
| IV. Plugins Foundation | PASS | `core/presentation/asset-management/build.gradle.kts` já aplica `foundation.project` + `foundation.library.comp` + `foundation.library.koin`. Sem novos plugins. |
| V. Testes em Use Cases | N/A | Nenhuma alteração em `:domain:usecases`. UseCases reutilizados sem modificação (`GetTransactionsByHoldingUseCase`, `SaveTransactionUseCase`, `DeleteTransactionUseCase`, `GetAssetHoldingUseCase`, `GetCurrentDateUseCase`). |
| VI. API Explícita | PASS | `TransactionFormDialog` e `TransactionFormView` continuam `public` (entrypoints do módulo já consumidos por `core/apps/umbrellaApp/.../App.kt`). Demais Composables e o ViewModel permanecem `internal`/`private`. |
| VII. Documentação Sincronizada | PASS | Este ciclo atualiza `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`. Não há alteração no modelo de domínio (`core/domain/entity/docs/DOMAIN.md` permanece intacto). `AGENTS.md` e `.specify/` mantidos coerentes. |
| VIII. Idioma e Convenções | PASS | Documentação em pt-BR. Código/identificadores em inglês. Pacote `com.eferraz.asset_management.transactions`. |

**Gate Result**: PASS — nenhuma violação identificada.

## Project Structure

### Documentation (this feature)

```text
specs/002-transaction-form-redesign/
├── plan.md              # Este arquivo
├── spec.md              # Spec (já existente)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (contratos UI internos)
│   └── transaction-form-dialog.md
├── checklists/
│   └── requirements.md  # Já existente
└── tasks.md             # Phase 2 output (/speckit-tasks — NÃO criado aqui)
```

### Source Code (repository root)

```text
core/presentation/asset-management/
└── src/commonMain/kotlin/com/eferraz/asset_management/
    └── transactions/
        ├── TransactionManagementView.kt       # MODIFICAR — substituir UiTableV3 por layout Row/Column;
        │                                       # mover botão "Adicionar" para fora da tabela; adicionar botão "Salvar";
        │                                       # remover dependências em UiTableV3/StableList; ocultar `observations`.
        ├── TransactionManagementViewModel.kt  # MODIFICAR — manter snapshot inicial (`InitialTransactionSnapshot`);
        │                                       # `deleteDraft` deixa de chamar `DeleteTransactionUseCase` imediatamente;
        │                                       # `onSave` calcula diff (delete removidos + upsert remanescentes/adicionados);
        │                                       # adicionar exposição de `isDirty` no estado.
        ├── TransactionManagementUiState.kt    # MODIFICAR — adicionar `initialSnapshot: List<TransactionDraftUi>`;
        │                                       # `TransactionDraftUi` continua imutável e ganha helper `comparableTo(other)`;
        │                                       # propriedade derivada `isDirty` no UiState.
        ├── TransactionManagementEvents.kt     # SEM MUDANÇA estrutural (eventos já cobrem os fluxos);
        │                                       # remover `DraftTransactionObservationChanged` (campo não é mais editável).
        └── TransactionManagementScreen.kt     # MODIFICAR/REMOVER — `NewTransactionsTable` (placeholder) deixa de ser
                                                # invocado por `AssetManagementScreen`; manter o ficheiro apenas se algum
                                                # outro local o referenciar, caso contrário remover. `AssetManagementScreen`
                                                # continua sem renderizar lista de transações inline.

core/presentation/asset-management/
└── src/commonMain/kotlin/com/eferraz/asset_management/
    └── assets/
        └── AssetManagementScreen.kt           # MODIFICAR — remover chamada a `NewTransactionsTable()` que hoje vive em
                                                # `AssetFormView` (era um placeholder estático e o dialog de transações é
                                                # independente do dialog de Asset; manter consistência).
```

**Structure Decision**: Refator concentrado no módulo `:features:asset-management`, subpacote `transactions/`. Nenhum módulo Gradle novo. Nenhuma alteração em `:apps:umbrellaApp`, `:domain:*` ou `:data:*`. O dialog mantém o invólucro `AppContentDialog` (`:features:design-system`), garantindo consistência com `AssetManagementDialog` por composição estrutural, não por herança de Composable.

## Complexity Tracking

> Nenhuma violação de constitution identificada — tabela vazia.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |
