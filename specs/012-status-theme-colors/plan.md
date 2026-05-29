# Implementation Plan: Cores semânticas de status no tema v2

**Branch**: `012-status-theme-colors` | **Date**: 2026-05-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/012-status-theme-colors/spec.md`

**Diretriz**: Superfície mínima — `StatusKind` + `StatusColorRoles` + `FixedStatusPalettes` + `MaterialTheme.statusColors(status)`; cartão aplica inline; **remover** `SummaryCardStatusColors.kt`.

## Summary

Estender `:features:design-system-v2` com lookup directo `MaterialTheme.statusColors(status: StatusKind) → StatusColorRoles` (8 papéis M3, fixos, independentes do `ColorScheme`). **3 tipos públicos**, zero resolvedores.

## Technical Context

**Language/Version**: Kotlin 2.x — KMP

**Primary Dependencies**: Compose Multiplatform + Material3 Expressive (`foundation.library.comp`); módulo existente `:features:design-system-v2`

**Storage**: N/A

**Testing**: `./gradlew :features:design-system-v2:compileKotlinJvm`; previews Compose light/dark (`StatusColorSwatches_preview`, `SummaryCard_Catalog8_preview`)

**Target Platform**: Android, iOS, Desktop — `commonMain`

**Project Type**: Biblioteca Compose (design system v2)

**Performance Goals**: N/A (resolução de cores em composição; custo desprezável)

**Constraints**: superfície mínima (sem `AppStatusColors`, `StatusColorPalette`, `resolve()`); `typealias SummaryCardStatus = StatusKind`; previews `private`

**Scale/Scope**: 2 ficheiros novos + 3 alterados + 1 removido (`SummaryCardStatusColors.kt`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I — SOLID/DRY | ✅ | 1 enum, 1 data class, 1 lookup; cartão inline. |
| II — Clean Architecture | ✅ | Só `:features:design-system-v2`. |
| III — KMP First | ✅ | `commonMain`. |
| IV — Plugins Foundation | ✅ | Sem novos plugins. |
| V — Testes Use Cases | ✅ N/A |
| VI — API Explícita | ✅ | `public`: `StatusKind`, `StatusColorRoles`, extensão `statusColors`. |
| VII — Documentação | ✅ | Contract + quickstart; `AGENTS.md` actualizar na PR de implementação (mencionar status colors). |
| VIII — Idioma | ✅ | Docs pt-BR; código inglês (`Info`, `Positive`, …). |

**Resultado do gate (pré-design)**: PASS

**Re-check pós-design**: PASS — sem violações; extensão Open/Closed da 011 (novo enum + factory, Default intacto).

## Project Structure

### Documentation (this feature)

```text
specs/012-status-theme-colors/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── StatusThemeColorsContract.md
└── tasks.md             # Phase 2
```

### Source Code (repository root)

```text
core/presentation/design-system-v2/
└── src/commonMain/kotlin/com/eferraz/design_system_v2/
    ├── theme/
    │   ├── AppThemeV2.kt                 ← + LocalStatusColorRoles
    │   ├── StatusColors.kt               ← StatusKind + StatusColorRoles + extensão
    │   ├── FixedStatusPalettes.kt        ← internal
    │   └── StatusColorSwatches.kt        ← preview
    └── summary/
        ├── SummaryCard.kt                ← statusColors(status) inline
        ├── SummaryCardCatalog.kt         ← + status FR-010a
        └── SummaryCardDefaults.kt
        # SummaryCardStatusColors.kt       ← REMOVER
```

**Structure Decision**: Lookup directo `statusColors(status)`; mapeamento cartão inline; `typealias SummaryCardStatus = StatusKind`.

## Phase 0: Research

Concluído — ver [research.md](research.md).

**Decisões-chave**:
- `AppStatusColors` + `CompositionLocal`; **`StatusKind`** no tema (pacote isolado)
- Lookup directo `MaterialTheme.statusColors(status)` — sem wrapper classes
- `typealias SummaryCardStatus = StatusKind` — sem enum duplicado
- **Cinco paletas fixas** em `FixedStatusPalettes` — zero leitura de status colours do `ColorScheme`
- Contentor tintado; catálogo FR-010a; preview amostras de cor (SC-004)

## Phase 1: Design & Contracts

Concluído:
- [data-model.md](data-model.md)
- [contracts/StatusThemeColorsContract.md](contracts/StatusThemeColorsContract.md)
- [quickstart.md](quickstart.md)

## Ordem de entrega (user stories)

1. **US1** — `StatusColors.kt` + `FixedStatusPalettes.kt` + `AppThemeV2`
2. **US2** — `SummaryCard.kt` lookup inline + `typealias`
3. **US5** — `StatusColorSwatches.kt`
4. **US4** — catálogo FR-010a
5. **US3** — calibração light/dark
6. **Remover** — `SummaryCardStatusColors.kt`

## Fora do escopo (esta entrega)

- Integração Histórico / telas produção
- Dynamic Color
- Hex custom / parâmetros `Color` na API
- Segundo componente de produção (chip/banner)
- Testes unitários automatizados de contraste

## Complexity Tracking

| Item | Nota |
|------|------|
| `StatusKind` + `SummaryCardStatus` | Dois enums 1:1 — fronteira de pacote |
| Paletas 100% fixas | Brand e neutros globais mudam sem afectar status; Default calibrado à baseline 011 |
| `CompositionLocal` para status | Padrão Compose |

## Artefactos gerados

| Artefacto | Caminho |
|-----------|---------|
| Plano | `specs/012-status-theme-colors/plan.md` |
| Research | `specs/012-status-theme-colors/research.md` |
| Data model | `specs/012-status-theme-colors/data-model.md` |
| Contract | `specs/012-status-theme-colors/contracts/StatusThemeColorsContract.md` |
| Quickstart | `specs/012-status-theme-colors/quickstart.md` |

**Próximo comando sugerido**: `/speckit.tasks`
