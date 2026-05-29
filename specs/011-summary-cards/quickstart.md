# Quickstart: Cartões de resumo (design-system-v2)

**Feature**: `011-summary-cards`

## Implementação

1. `settings.gradle.kts` — `include(":features:design-system-v2")`
2. `build.gradle.kts` — `foundation.project` + `foundation.library.comp` + `foundation.library.koin`; `compose-material-icons-extended`; **sem** `design-system` v1
3. Ficheiros principais:
   - `theme/AppThemeV2.kt`, `theme/ExpressiveColorScheme.kt`, `theme/Shapes.kt`, `theme/Typography.kt`
   - `summary/SummaryCard.kt` — composable + **todos** os `@Preview` (`private`)
   - `summary/SummaryCardCatalog.kt` — textos e ícones FR-008
   - `summary/SummaryCardDefaults.kt`, `SummaryCardStatus.kt`, `SummaryCardStatusColors.kt`

## Ordem sugerida

1. Setup + tema (`AppThemeV2`)
2. US1 — `SummaryCard` + `SummaryCard_Default_preview`
3. **US4** — slots reservados, truncamento, a11y, `SummaryCard_OptionalSlots_preview`
4. **US3** — `SummaryCardCatalog` + `SummaryCard_Catalog8_preview` (só após US4)
5. Polish — compilação, light/dark, docs

## Compilar

```bash
./gradlew :features:design-system-v2:compileKotlinJvm
```

## Checklist M3 Expressive

- [x] `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` em `AppThemeV2` / color schemes
- [x] `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()`
- [x] `AppShapesV2` com `medium` = 12.dp (cartão)
- [x] `OutlinedCard` + `CardDefaults.outlinedCardColors()` + `outlinedCardBorder()`
- [x] Fundo cartão = `surface` (não `surfaceContainerLow`)
- [x] `SummaryCardStatus` só com `Default`
- [x] Previews **somente** em `SummaryCard.kt`, funções `private`
- [x] Preview 8 cartões (FR-008), `spacedBy(8.dp)`, todos `Default`
- [x] `SummaryCard_OptionalSlots_preview_*`: 4 variantes (completo / sem legenda / sem ícone / sem ambos) — SC-005b
- [x] Sem hover/clique/animação no cartão
- [x] **Contraste WCAG AA** (≥ 4,5:1): revisado manualmente em previews light + dark (T025) — `onSurface` / `onSurfaceVariant` sobre `surface`; ícone `onSurfaceVariant` sobre `surfaceContainerHigh`; tokens M3 Expressive — **OK**
- [x] Sem paletas info/warning/error/success nesta PR
- [x] Nenhuma alteração em `AssetHistoryScreen` / Histórico (FR-012)
- [x] `AGENTS.md` menciona `:features:design-system-v2` (T028)

## Validação manual (opcional)

- [ ] **SC-006**: Com `SummaryCard_Catalog8_preview` aberto, identificar métrica (ex. aportes vs. retiradas) em &lt; 3 s sem ler legenda completa

## Próximo passo

Integrar `SummaryCard` nos ecrãs de carteira quando a feature de consumo estiver especificada.
