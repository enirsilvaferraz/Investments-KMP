# Implementation Plan: Seletor de mês/ano

**Branch**: `013-date-selector` | **Date**: 2026-05-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/013-date-selector/spec.md`

**Diretriz**: Componente **novo** em `:features:design-system-v2`; gatilho **pílula** (protótipo); menu flutuante **equivalente** ao dropdown de cadastro de ativos; **sem** seta; API `YearMonth` + `itemLabel` opcional; **sem** integração Histórico nesta entrega.

## Summary

Adicionar `MonthYearSelector` ao design system v2 — gatilho compacto com ícone de calendário e rótulo **"{Mês} de {Ano}"**, menu `DropdownMenu` M3 com realce `tertiaryContainer`, lista rolável com auto-scroll, previews light/dark. Tipo `kotlinx.datetime.YearMonth`; formatador pt-BR interno; ordem e opções fornecidas pelo integrador.

## Technical Context

**Language/Version**: Kotlin 2.x — KMP

**Primary Dependencies**: Compose Multiplatform + Material3 Expressive (`foundation.library.comp`); `kotlinx-datetime`; `compose-material-icons-extended`; módulo existente `:features:design-system-v2`

**Storage**: N/A

**Testing**: `./gradlew :features:design-system-v2:compileKotlinJvm`; previews Compose light/dark + lista longa + lista vazia

**Target Platform**: Android, iOS, Desktop — `commonMain`

**Project Type**: Biblioteca Compose (design system v2)

**Performance Goals**: Abertura de menu imperceptível (< 1 frame scroll); lista até 24 itens rolável

**Constraints**: `explicitApi()`; `AppThemeV2` obrigatório; sem `Color` na API; sem `:features:design-system` v1; sem registo `umbrellaApp`; previews `private` no mesmo ficheiro do composable

**Scale/Scope**: ~4 ficheiros Kotlin novos em `dateselector/`; 1 linha Gradle (`kotlinx-datetime`); zero alterações em `composeApp`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I — SOLID/DRY | ✅ | Um composable; formatador central; defaults isolados. |
| II — Clean Architecture | ✅ | Só `:features:design-system-v2`; sem `:data`/`:domain`. |
| III — KMP First | ✅ | `commonMain`; `YearMonth` kotlinx.datetime. |
| IV — Plugins Foundation | ✅ | Sem novos plugins; só `implementation(libs.kotlinx.datetime)`. |
| V — Testes Use Cases | ✅ N/A |
| VI — API Explícita | ✅ | `public fun MonthYearSelector`; resto `internal`. |
| VII — Documentação | ✅ | Contract + quickstart; `AGENTS.md` na PR de implementação. |
| VIII — Idioma | ✅ | Docs pt-BR; API inglês (`MonthYearSelector`, `itemLabel`). |

**Resultado do gate (pré-design)**: PASS

**Re-check pós-design**: PASS — DS v2 isento de `*Contract.kt` e `umbrellaApp` (AGENTS.md); sem violações.

## Project Structure

### Documentation (this feature)

```text
specs/013-date-selector/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── MonthYearSelectorContract.md
└── tasks.md             # Phase 2 (/speckit.tasks)
```

### Source Code (repository root)

```text
core/presentation/design-system-v2/
├── build.gradle.kts                      ← + kotlinx-datetime
└── src/commonMain/kotlin/com/eferraz/design_system_v2/
    └── dateselector/
        ├── MonthYearSelector.kt          ← composable + @Preview (private)
        ├── MonthYearSelectorDefaults.kt  ← dimensões pílula/menu (internal)
        ├── MonthYearLabelFormatter.kt    ← pt-BR default label (internal)
        └── MonthYearSelectorCatalog.kt   ← dados preview (internal)
```

**Structure Decision**: Pacote `dateselector/` paralelo a `summary/` e `theme/`. Gatilho custom (pílula) + `DropdownMenu` — **não** reutilizar `AppDropdownField` v1; copiar **apenas** padrão visual de row/menu (realce lavanda).

**Previews (constituição)**: `@Preview` apenas em `MonthYearSelector.kt`, visibilidade `private`.

**Ordem de entrega**: US1 (gatilho) → US2 (menu + scroll) → US4 (API edge cases) → US3 (catálogo light/dark).

## Phase 0: Research

Concluído — ver [research.md](research.md).

**Decisões-chave**:
- `YearMonth` + `itemLabel` opcional (clarificação híbrida)
- Gatilho pílula (`Surface`/`Row`), **não** `TextField` + `ExposedDropdownMenuBox`
- Menu `DropdownMenu` + `LazyColumn` + auto-scroll
- Lista vazia → `effectiveEnabled = false`
- Ordem integrador; previews light + dark

## Phase 1: Design & Contracts

Concluído:
- [data-model.md](data-model.md)
- [contracts/MonthYearSelectorContract.md](contracts/MonthYearSelectorContract.md)
- [quickstart.md](quickstart.md)

## Fora do escopo (esta entrega)

- Integração `AssetHistoryScreen` / substituir `SegmentedControl` de período
- ViewModels, persistência, filtro de dados
- Locales ≠ pt-BR
- Seletor de dia / intervalo / navegação por ano separada
- Alterar `AppDropdownField` (design-system v1)

## Complexity Tracking

| Item | Nota |
|------|------|
| `kotlinx-datetime` no DS v2 | Primeira dependência de data no v2; alinhada à constituição III |
| Duplicar padrão menu v1 | Consciente — evita dependência de `:features:design-system` |
| `LazyColumn` dentro de `DropdownMenu` | Necessário para SC-005 + auto-scroll |

## Artefactos gerados

| Artefacto | Caminho |
|-----------|---------|
| Plano | `specs/013-date-selector/plan.md` |
| Research | `specs/013-date-selector/research.md` |
| Data model | `specs/013-date-selector/data-model.md` |
| Contract | `specs/013-date-selector/contracts/MonthYearSelectorContract.md` |
| Quickstart | `specs/013-date-selector/quickstart.md` |

**Próximo comando sugerido**: `/speckit.tasks`
