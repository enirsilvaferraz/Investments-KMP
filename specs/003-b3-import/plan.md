# Implementation Plan: Importação de Dados da B3

**Branch**: `003-b3-import` | **Date**: 2026-05-23 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-b3-import/spec.md`

**Note**: Gerado pelo workflow `/speckit.plan`. Artefatos de design: `research.md`, `data-model.md`, `contracts/`, `quickstart.md`. Tarefas em `tasks.md` (geradas por `/speckit-tasks` — não regeneradas aqui).

## Summary

Implementar importação de posição B3 a partir de arquivo XLSX local na **AssetHistoryScreen** (Desktop): botão à esquerda do export, diálogo nativo filtrando `.xlsx`, leitura das **cinco guias B3** pelo nome exato (`Acoes`, `ETF`, `Fundo de Investimento`, `Renda Fixa`, `Tesouro Direto`), saída tabular no **console da IDE** (`println`), spinner durante processamento, timeout de 30 s, falha atómica em `MISSING_COLUMNS` (sem saída parcial). **Android/iOS**: bypass (botão pode existir na UI compartilhada, sem ação — `ImportB3File` não dispara o fluxo). **Sem** Snackbar, `errorMessage` na UI ou persistência nesta fase.

Abordagem técnica: **FileMapper-KMP 1.0.0** (`FileMapperPicker` + `importData`) em `:data:filestore`; port único **`B3ImportPort`** no domínio; **`ImportB3FileUseCase`** com `withTimeout(30_000L)` e `Dispatchers.Default`.

## Technical Context

**Language/Version**: Kotlin 2.3.21 (KMP), Compose Multiplatform

**Primary Dependencies**: FileMapper-KMP `io.github.mamon-aburawi:filemapper-kmp:1.0.0`; kotlinx.serialization (DTOs B3); Koin; coroutines

**Storage**: N/A nesta fase (sem persistência; apenas log no console)

**Testing**: `./gradlew :domain:usecases:jvmTest` — `ImportB3FileUseCase` com MockK em `B3ImportPort`; padrão `GIVEN_WHEN_THEN`

**Target Platform**: **Desktop (JVM)** — implementação completa (picker, parse, log). **Android / iOS** — bypass (sem picker, sem parse, sem log; intent ignorado ou botão sem `onClick` efetivo)

**Project Type**: KMP monorepo (apps + features + domain + data)

**Performance Goals**: Importação percebida &lt; 30 s (SC-001); parsing em `Dispatchers.Default`; cancelamento via `withTimeout(30_000L)` (FR-011)

**Constraints**: Console-only para sucesso e erro (FR-014, FR-016); cinco nomes de guia exatos; guias desconhecidas ignoradas sem log (FR-012); arquivo sem guias B3 conhecidas → sucesso silencioso sem console (FR-013); falha atómica FR-015; grafo de módulos Clean Architecture inalterado

**Scale/Scope**: Uma tela (`AssetHistoryScreen` / `HoldingHistoryScreen`), um UseCase, um port, cinco DTOs internos em `filestore`, ~8 ficheiros Kotlin novos/alterados na feature UI Desktop

## Constitution Check

*GATE: Deve passar antes da Phase 0. Revalidado após Phase 1 (design).*

| # | Princípio | Verificação para `003-b3-import` | Status |
|---|-----------|----------------------------------|--------|
| I | SOLID, DRY, KISS, YAGNI | Um port (`B3ImportPort`); DTOs e log só em `filestore`; UseCase só orquestra timeout; sem UI de erro especulativa | **APROVADO** |
| II | Clean Architecture | `:domain:usecases` define port; `:data:filestore` implementa; `:features:composeApp` chama UseCase; `:features` não depende de `:data` | **APROVADO** |
| III | KMP First | FileMapper-KMP em `commonMain`; bypass mobile sem `expect`/`actual` para import | **APROVADO** |
| IV | Plugins Foundation | Alterações em `build.gradle.kts` via plugins `foundation.*` existentes no módulo `filestore` | **APROVADO** |
| V | Testes em Use Cases | `ImportB3FileUseCase` com testes MockK obrigatórios (T005 / tasks.md) | **APROVADO** |
| VI | API Explícita | Port `public`; impl/DTOs `internal` em `filestore` | **APROVADO** |
| VII | Documentação sincronizada | `spec.md`, `plan.md`, `data-model.md`, contrato, `quickstart.md` alinhados neste PR de planeamento | **APROVADO** |
| VIII | Idioma e nomes | Docs pt-BR; código/DTOs em inglês (`B3StockPosition`, etc.) | **APROVADO** |

**Resultado do gate**: **APROVADO** — nenhuma violação que exija Complexity Tracking.

### Escopo por plataforma (fonte: spec §Assumptions, clarificações 2026-05-23)

| Camada | Desktop | Android / iOS |
|--------|---------|-----------------|
| Botão import na UI | Visível; dispara `ImportB3File` | Pode estar visível (layout compartilhado) ou oculto — **sem ação** (bypass) |
| `B3ImportPortImpl` | Executado via UseCase | Não invocado |
| Picker / parse / `println` | Sim | Não |
| Feedback erro/sucesso | Console IDE apenas | N/A |

> **Sincronização com `tasks.md`**: `tasks.md` regenerado e alinhado à `spec.md` (2026-05-23): console-only (FR-014/FR-016); US2 = T017–T018 no port; timeout no UseCase (FR-011a); sem Snackbar/`errorMessage`.

## Project Structure

### Documentation (this feature)

```text
specs/003-b3-import/
├── plan.md              # Este ficheiro
├── spec.md              # Fonte de verdade (requisitos)
├── research.md          # Phase 0 — decisões técnicas
├── data-model.md        # Phase 1 — DTOs, port, fluxo UI
├── quickstart.md        # Phase 1 — validação manual Desktop
├── contracts/
│   └── XlsxImportContract.md
├── checklists/
│   └── cross-artifact.md
└── tasks.md             # Phase 2 (/speckit-tasks) — alinhado à spec (2026-05-23)
```

### Source Code (repository root)

Gradle paths (`:domain:usecases`) → diretórios físicos sob `core/`:

```text
core/
├── apps/
│   └── desktopApp/                    # Entry Desktop — ./gradlew :apps:desktopApp:run
├── presentation/
│   └── composeApp/                    # :features:composeApp
│       └── src/commonMain/.../history/
│           ├── AssetHistoryScreen.kt  # Botão import + spinner (Desktop ativo)
│           ├── HistoryViewModel.kt    # isImporting; sem errorMessage nesta fase
│           ├── HistoryState.kt
│           └── HistoryIntent.kt       # ImportB3File
├── domain/
│   ├── entity/                        # :domain:entity — sem entidades B3 nesta fase
│   └── usecases/                      # :domain:usecases
│       └── repositories/B3ImportPort.kt
│       └── services/ImportB3FileUseCase.kt
│       └── jvmTest/.../ImportB3FileUseCaseTest.kt
└── data/
    └── filestore/                     # :data:filestore
        └── src/commonMain/.../b3/
            ├── B3ImportPortImpl.kt
            ├── dto/                   # B3StockPosition, B3EtfPosition, ...
            └── di/FileStoreModule.kt  # bind B3ImportPort
```

**Structure Decision**: Feature transversal em camadas existentes — **nenhum subprojeto Gradle novo**. Dependência Maven `filemapper-kmp` apenas em `:data:filestore` `commonMain`. UI condicionada a Desktop via `expect`/`actual` mínimo, `Platform.isDesktop`, ou `onImportClick` nulo em mobile — detalhe de implementação, desde que mobile não abra picker nem altere estado além de ignorar o intent.

## Phase 0: Research

Concluída em [research.md](./research.md). Sem itens `NEEDS CLARIFICATION` pendentes.

Decisões principais: FileMapper-KMP (picker + parse); sem `JFileChooser`; `Dispatchers.Default`; `withTimeout(30_000L)` no UseCase com `println` de timeout em FR-011a; port único; DTOs `internal` em `filestore`; erros no console via `B3ImportPortImpl` (excepção: timeout no UseCase); parse atómico (validar todas as guias antes de qualquer `println` de dados).

## Phase 1: Design & Contracts

| Artefato | Estado |
|----------|--------|
| [data-model.md](./data-model.md) | Atualizado — sem referência a Snackbar; FR-015 atómico; bypass mobile |
| [contracts/XlsxImportContract.md](./contracts/XlsxImportContract.md) | Atualizado — UI console-only; fluxo parse em duas fases |
| [quickstart.md](./quickstart.md) | Atualizado — critérios manuais alinhados a FR/SC |

**Agent context**: `.cursor/rules/specify-rules.mdc` aponta para `specs/003-b3-import/plan.md` (marcadores `SPECKIT START/END`). Script `.specify/scripts/bash/update-agent-context.sh` **não existe** neste repositório — passo ignorado.

## Sincronização spec ↔ plan ↔ tasks

| Artefato | Estado (2026-05-23) |
|----------|---------------------|
| `spec.md` | Fonte de verdade — FR-014/FR-016 console-only; FR-011a timeout no UseCase; FR-015 → `data-model.md` |
| `tasks.md` | Alinhado — 24 tarefas; US2 = T017–T018 (port); sem UI de erro |
| `data-model.md`, `contracts/` | Alinhados — parse atómico; `EMPTY_FILE` vs FR-013 distinguidos |
| `checklists/cross-artifact.md` | CHK001, CHK002, CHK034, CHK036 resolvidos; G1/A1/I4/U1 remediados na spec |

Nenhum conflito bloqueante pendente entre `tasks.md` e `spec.md` para implementação.

## Complexity Tracking

> Não aplicável — gate constitucional aprovado sem exceções.
