# Quickstart: Seletor de mês/ano (design-system-v2)

**Feature**: `013-date-selector`

## Pré-requisitos

- Módulo `:features:design-system-v2` já registado (features 011/012)
- Branch `013-date-selector`

## Implementação

1. **`build.gradle.kts`** — acrescentar dependência:

```kotlin
implementation(libs.kotlinx.datetime)
```

2. **Ficheiros novos** (`core/presentation/design-system-v2/.../dateselector/`):

| Ficheiro | Responsabilidade |
|----------|------------------|
| `MonthYearSelector.kt` | Composable + **todos** `@Preview` (`private`) |
| `MonthYearSelectorDefaults.kt` | Dimensões pílula/menu (`internal`) |
| `MonthYearLabelFormatter.kt` | pt-BR `"{Mês} de {Ano}"` (`internal`) |
| `MonthYearSelectorCatalog.kt` | Dados estáticos preview (`internal`) |

3. **Sem alterações** em `composeApp`, `umbrellaApp`, `AssetHistoryScreen` (FR-010).

## Ordem sugerida

1. `MonthYearLabelFormatter` + `MonthYearSelectorDefaults`
2. **US1** — gatilho pílula (ícone, rótulo, sem seta) + preview light
3. **US2** — `DropdownMenu` + itens + realce + seleccionar/fechar + **toggle** (segundo toque no gatilho fecha)
4. **US2** — auto-scroll (`LazyColumn` + `LaunchedEffect`; item ao **topo** visível)
5. **US4** — `enabled`, lista vazia, `itemLabel` custom
6. **US3** — `MonthYearSelectorCatalog` + previews dark, long list, empty, **narrow 320 dp**

## Compilar

```bash
./gradlew :features:design-system-v2:compileKotlinJvm
```

## Checklist

- [x] `MonthYearSelector` API pública com `explicitApi`
- [x] Gatilho pílula + calendário, **sem** chevron
- [x] Menu estilo cadastro ativos (`tertiaryContainer` seleccionado)
- [x] `itemLabel` default pt-BR; identidade por `YearMonth`
- [x] Lista vazia → gatilho desactivado
- [x] Ordem `options` preservada
- [x] Auto-scroll ao abrir (lista longa; item ao topo visível)
- [x] Segundo toque no gatilho fecha menu sem alterar selecção
- [x] Previews light **e** dark (via `PreviewParameterProvider` — cenários em `MonthYearSelectorCatalog.previewScenarios`)
- [x] Preview `@Preview(widthDp = 320)` — menu rolável sem sobreposição ilegível (SC-005)
- [x] Revisão SC-003: menu row espelha `StableExposedDropdownMenuRow` (`tertiaryContainer`, `MenuDefaults`, `ExposedDropdownMenuDefaults.ItemContentPadding`); validação side-by-side recomendada na PR
- [x] Sem `Color` na API pública
- [x] Sem integração Histórico
- [x] `AGENTS.md` — mencionar `MonthYearSelector` (PR implementação)

## Uso (integrador futuro)

```kotlin
AppThemeV2 {
    MonthYearSelector(
        selected = selectedPeriod,
        options = availablePeriods,
        onSelectionChange = { selectedPeriod = it },
    )
}
```

Com label custom:

```kotlin
MonthYearSelector(
    selected = selected,
    options = options,
    onSelectionChange = onChange,
    itemLabel = { ym ->
        if (ym == sentinel) "Todos os meses"
        else MonthYearLabelFormatter.format(ym)
    },
)
```

## Próximo passo

`/speckit.tasks` — decompor tarefas de implementação.

Integração no Histórico (substituir `SegmentedControl` de período) — feature futura separada.
