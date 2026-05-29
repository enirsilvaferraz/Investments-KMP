# Implementation Plan: Cartões de resumo da carteira

**Branch**: `011-summary-cards` | **Date**: 2026-05-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/011-summary-cards/spec.md`

**Diretriz do utilizador**: módulo `:features:design-system-v2`; **M3 Expressive** (colorScheme + shapes + typography + OutlinedCard/CardDefaults, espaçamento 4/8 dp); protótipo só como **ideia** de hierarquia — **não** pixel-perfect; **apenas `Default`**; código extensível para outros status depois.

## Summary

Criar `:features:design-system-v2` com `SummaryCard` em **OutlinedCard** + `CardDefaults`, `SummaryCardStatus.Default`, resolvedor de cores extensível, **`AppThemeV2`** com `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` + **`AppShapesV2`**, preview 8 exemplos (todos `Default`). Sem Histórico; sem hover/animação/motion no cartão; sem paletas info/warning/error/success.

## Technical Context

**Language/Version**: Kotlin 2.x — KMP

**Primary Dependencies**: Compose Multiplatform + Material3 (`foundation.library.comp`), `compose-material-icons-extended`, plugins `foundation.project` + `foundation.library.koin` — **sem** `:features:design-system`

**Storage**: N/A

**Testing**: `./gradlew :features:design-system-v2:compileKotlinJvm`; previews Compose (8 cartões layout + 1 Default isolado)

**Target Platform**: Android, iOS, Desktop — `commonMain`

**Project Type**: Biblioteca Compose (design system v2)

**Constraints**: `explicitApi()`; `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` no tema; sem `Color` na API; sem px do protótipo; Outlined Card (surface + outlineVariant); shapes Expressive; altura mínima uniforme; enum/resolver extensível

**Scale/Scope**: ~8–9 ficheiros Kotlin; previews `private` em `SummaryCard.kt`; catálogo FR-008 em `SummaryCardCatalog.kt`; zero alterações em `AssetHistoryScreen`

## Constitution Check

| Princípio | Status | Observação |
|-----------|--------|------------|
| I — SOLID/DRY | ✅ | Um resolvedor central; um composable; extensão por novo ramo `when`. |
| II — Clean Architecture | ✅ | Só `:features:design-system-v2`; sem `:data`/`:domain`. |
| III — KMP First | ✅ | `commonMain`. |
| IV — Plugins Foundation | ✅ | `foundation.*`. |
| V — Testes Use Cases | ✅ N/A |
| VI — API Explícita | ✅ |
| VII — Documentação | ✅ | T028: `AGENTS.md` + regra `specify-rules.mdc` na PR |
| VIII — Idioma | ✅ |

**Resultado do gate**: PASS

## Project Structure

```text
core/presentation/design-system-v2/
└── src/commonMain/kotlin/com/eferraz/design_system_v2/
    ├── theme/
    │   ├── AppThemeV2.kt                 ← MaterialTheme + OptIn Expressive
    │   ├── ExpressiveColorScheme.kt      ← light/dark ExpressiveColorScheme
    │   ├── Shapes.kt                     ← AppShapesV2 (medium 12.dp cartão)
    │   └── Typography.kt
    └── summary/
        ├── SummaryCard.kt                ← composable + todos os @Preview (private)
        ├── SummaryCardCatalog.kt         ← dados estáticos FR-008 + ImageVectors
        ├── SummaryCardDefaults.kt        ← padding/spacing/minHeight do tema M3
        ├── SummaryCardStatus.kt
        └── SummaryCardStatusColors.kt    ← SummaryCardColors + resolve(...)
```

**Structure Decision**: M3 Expressive-first — `OutlinedCard` + `CardDefaults`, `AppThemeV2` com colorScheme/shapes Expressive. Protótipo informa hierarquia apenas. Cartão estático = exceção à motion Expressive (documentada na spec).

**Previews (constituição)**: `@Preview` apenas em `SummaryCard.kt`, visibilidade `private`, no mesmo ficheiro do composable — alinhado a `AppScreenPane.kt` e à restrição técnica do projeto.

**Ordem de entrega**: US1 → **US4** (slots + a11y) → **US3** (catálogo 8) — o preview `SummaryCard_Catalog8_preview` depende de layout estável (FR-004b/c).

## Fora do escopo (esta entrega)

- Paletas e cores para `Info`, `Warning`, `Error`, `Success`
- Integração Histórico
- Preview comparando cinco status lado a lado

## Complexity Tracking

| Item | Nota |
|------|------|
| `design-system-v2` | DS novo, independente do v1 |
| Escopo incremental de status | YAGNI — um status; API estável para acrescentar enum + resolvedor depois |
