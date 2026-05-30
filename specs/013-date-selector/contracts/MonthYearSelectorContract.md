# Contract: MonthYearSelector (design-system-v2)

**Feature**: `013-date-selector` | **Phase**: 1 | **Date**: 2026-05-29

---

## Visão geral

```
:features:design-system-v2  →  AppThemeV2 { MonthYearSelector(...) }
```

Sem `:features:design-system` v1. Sem `:features:composeApp` nesta entrega.

---

## API pública

### `MonthYearSelector`

```kotlin
@Composable
public fun MonthYearSelector(
    selected: YearMonth,
    options: List<YearMonth>,
    onSelectionChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemLabel: (YearMonth) -> String = MonthYearLabelFormatter::format,
)
```

| Parâmetro | Contrato |
|-----------|----------|
| `selected` | Valor estruturado; gatilho exibe `itemLabel(selected)` |
| `options` | Ordem do integrador preservada; `empty` → gatilho desactivado automaticamente |
| `onSelectionChange` | Chamado **uma vez** por seleção; identidade `YearMonth` |
| `enabled` | `false` impede abertura; combinado com `options.isNotEmpty()` |
| `itemLabel` | Opcional; default **"{Mês} de {Ano}"** pt-BR |
| Cores | Via `MaterialTheme` — **proibido** `Color` na API |

**Visibilidade**: `MonthYearLabelFormatter` — `internal` (formatador default).

---

## Gatilho (pílula)

| Aspeto | Especificação |
|--------|---------------|
| Layout | `Row`: ícone calendário + texto |
| Ícone | `Icons.Outlined.CalendarMonth` (ou `CalendarToday`) — **decorativo** (a11y) |
| Seta / chevron | **Proibido** (FR-003) |
| Forma | Pílula (`RoundedCornerShape(50%)`) |
| Fundo | `surfaceContainerLow` |
| Texto | `onSurface`, truncamento `TextOverflow.Ellipsis`, max 1 linha |
| Interacção | `clickable` / `combinedClickable` só se `enabled && options.isNotEmpty()` |

---

## Menu flutuante

| Aspeto | Especificação |
|--------|---------------|
| Componente | `DropdownMenu` M3 |
| Item | `DropdownMenuItem` — padrão visual **equivalente** a `AppDropdownField` |
| Seleccionado | Fundo `tertiaryContainer`, texto `onTertiaryContainer` |
| Lista | `LazyColumn` + `heightIn(max = menuMaxHeight)` |
| Auto-scroll | Ao abrir → `scrollToItem(indexOf(selected))` |
| Fecho | Seleccionar item; dismiss; tap fora; **segundo toque no gatilho** (toggle) |
| Ordem | Igual a `options` — sem sort |

---

## Formatação default

```kotlin
// internal — MonthYearLabelFormatter.kt
internal object MonthYearLabelFormatter {
    internal fun format(yearMonth: YearMonth): String
    // "Maio de 2026"
}
```

Capitalização: mês com inicial maiúscula; ano 4 dígitos.

---

## Tema

Requer **`AppThemeV2`** no preview e consumo:

```kotlin
AppThemeV2(darkTheme = false) { MonthYearSelector(...) }
AppThemeV2(darkTheme = true)  { MonthYearSelector(...) }
```

---

## Acessibilidade

| Elemento | Comportamento |
|----------|---------------|
| Gatilho | Semântica de lista dropdown; anuncia `itemLabel(selected)` |
| Ícone | Ignorado por leitor de ecrã |
| Itens menu | Texto de `itemLabel(option)` |

---

## Previews

Todos em **`MonthYearSelector.kt`**, visibilidade **`private`**.

| Preview | Conteúdo |
|---------|----------|
| `MonthYearSelector_Light_preview` | Catálogo default, tema claro |
| `MonthYearSelector_Dark_preview` | Catálogo default, tema escuro |
| `MonthYearSelector_LongList_preview` | 24 opções, selected central |
| `MonthYearSelector_Narrow320_preview` | `@Preview(widthDp = 320)` — SC-005 |
| `MonthYearSelector_EmptyOptions_preview` | Lista vazia |
| `MonthYearSelector_CustomLabel_preview` | `itemLabel` custom (ex. "Todos os meses") |

Dados estáticos: `MonthYearSelectorCatalog.kt` (sem previews neste ficheiro).

---

## Fora do contrato (esta entrega)

- Integração `AssetHistoryScreen` / `HoldingHistoryScreen`
- Registo `umbrellaApp`
- `*Contract.kt` (DS v2 isento)
- Opção fixa "Todos os meses" no componente

---

## Verificação

```bash
./gradlew :features:design-system-v2:compileKotlinJvm
```

Checklist manual: abrir menu → realce lavanda → seleccionar → fechar; repetir light/dark; lista 24 com auto-scroll.
