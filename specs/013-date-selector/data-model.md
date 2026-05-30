# Data Model: Seletor de mês/ano

**Feature**: `013-date-selector` | **Date**: 2026-05-29

## Entidades

### `YearMonth` (kotlinx.datetime — tipo de domínio UI)

| Campo | Tipo | Regras |
|-------|------|--------|
| `year` | `Int` | Quatro dígitos na apresentação |
| `month` | `Month` | 1–12 via enum `kotlinx.datetime.Month` |

**Identidade**: `YearMonth` equality nativa — base para item activo e callback.

**Apresentação predefinida**: `MonthYearLabelFormatter.format(YearMonth)` → `"{Mês} de {Ano}"` pt-BR.

---

### `MonthYearSelector` (parâmetros do composable)

| Campo | Tipo | Obrigatório | Contrato |
|-------|------|-------------|----------|
| `selected` | `YearMonth` | Sim | Período actual; gatilho mostra `itemLabel(selected)` |
| `options` | `List<YearMonth>` | Sim | Ordem preservada; vazia → gatilho desactivado |
| `onSelectionChange` | `(YearMonth) -> Unit` | Sim | Invocado após escolha; menu fecha antes |
| `modifier` | `Modifier` | Não | |
| `enabled` | `Boolean` | Não (default `true`) | AND com `options.isNotEmpty()` |
| `itemLabel` | `(YearMonth) -> String` | Não (default formatador) | Só afecta texto; não identidade |

**Invariantes**:
- Sem reordenação interna de `options`.
- Sem inferência de meses a partir de dados externos.
- Sem parâmetros `Color` na API pública.
- `selected` pode estar ausente de `options` — gatilho mostra rótulo; menu pode não realçar item.

---

### `MonthYearSelectorDefaults` (internal)

| Constante | Valor inicial | Uso |
|-----------|---------------|-----|
| `triggerHeight` | `32.dp` | Altura pílula |
| `triggerHorizontalPadding` | `10.dp` | |
| `iconSize` | `16.dp` | Calendário |
| `iconTextSpacing` | `6.dp` | |
| `menuMaxHeight` | `320.dp` | Scroll + auto-scroll |
| `menuItemSpacing` | `4.dp` | Entre rows |

Calibragem qualitativa vs protótipo — tokens M3, não px Tailwind.

---

### `MonthYearSelectorCatalog` (internal — preview)

| Campo | Descrição |
|-------|-----------|
| `defaultOptions` | Lista estática ~12 meses (ordem integrador simulada) |
| `defaultSelected` | Ex.: `YearMonth(2026, Month.MAY)` |
| `longListOptions` | 24 entradas para preview auto-scroll |
| `customLabelExample` | Entrada com label "Todos os meses" via `itemLabel` no preview |

---

## Transições de estado (UI)

```text
Closed ──(tap gatilho, effectiveEnabled)──► Open
Open ──(select item)──► Closed + onSelectionChange
Open ──(dismiss / tap outside)──► Closed (sem callback)
Open ──(LaunchedEffect)──► scrollToItem(selectedIndex)
```

| Estado | Gatilho | Menu |
|--------|---------|------|
| `effectiveEnabled = false` | Atenuado, sem click | Não abre |
| `Open` | Normal | Lista + realce `selected` |
| `selected ∉ options` | Rótulo de `selected` | Nenhum item realçado |

---

## Relacionamentos

```text
AppThemeV2 ──► MaterialTheme (cores/shapes menu + pílula)
MonthYearLabelFormatter ──► rótulo default
MonthYearSelector ──► DropdownMenu + LazyColumn
Integrador ──► selected, options, onSelectionChange [, itemLabel]
```

## Fora do modelo (esta entrega)

- Persistência de preferência de período
- Filtro de dados / ViewModel Histórico
- Locales ≠ pt-BR
- Selecção de dia ou intervalo
