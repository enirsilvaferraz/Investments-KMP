# Research: Seletor de mês/ano

**Feature**: `013-date-selector` | **Date**: 2026-05-29

## Decisões Técnicas

### 1. Tipo de valor — `kotlinx.datetime.YearMonth`

**Decisão**: API pública genérica `<T>` **não** — tipo fixo `YearMonth` para `selected`, `options` e `onSelectionChange`.

**Rationale**: Constituição (III) recomenda `kotlinx.datetime`; o Histórico já usa `YearMonth` em `HistoryState` / `AssetHistoryScreen`; identidade mês+ano alinha com FR-008b. Formato de rótulo **"{Mês} de {Ano}"** fica no formatador interno ou em `itemLabel`.

**Alternativas consideradas**:
- Par `(month: Int, year: Int)` — rejeitado (menos type-safe; duplica domínio).
- `String` como valor — rejeitado na clarificação (híbrido com estruturado + `itemLabel`).

**Dependência**: `implementation(libs.kotlinx.datetime)` em `:features:design-system-v2`.

---

### 2. Gatilho em pílula — **não** `ExposedDropdownMenuBox` + `TextField`

**Decisão**: `Box` + superfície clicável compacta (`Surface` / `Row` com `RoundedCornerShape(percent = 50)` ou `CircleShape` em caps) + ícone `Icons.Outlined.CalendarMonth`; **sem** `TrailingIcon` / chevron.

**Rationale**: Protótipo (`App.tsx` linhas 770–781) é pílula compacta na barra de filtros, não campo de formulário com label. `AppDropdownField` (cadastro de ativos) usa `TextField` + label — referência **só para o menu flutuante**, não para o gatilho.

**Tokens M3 Expressive (gatilho)**:

| Aspeto | Token / API |
|--------|-------------|
| Fundo repouso | `surfaceContainerLow` (equivalente qualitativo a `#f1f3f8`) |
| Fundo hover/pressed | `surfaceContainerHigh` ou `secondaryContainer` atenuado |
| Ícone | `primary` ou `primary` fixo do protótipo (`#0061a4` → `colorScheme.primary`) |
| Texto | `onSurface` + `labelLarge` / `labelMedium` semibold |
| Forma | `RoundedCornerShape(percent = 50)` — pílula |
| Padding | horizontal `10.dp`, vertical `6.dp` — compacto; calibrar em `MonthYearSelectorDefaults` |
| Altura alvo | ~`32.dp` (protótipo `h-8`) |

**Alternativas consideradas**:
- Reutilizar `AppDropdownField` do v1 — rejeitado (spec: componente **novo** em v2; gatilho visual diferente).
- `ExposedDropdownMenuBox` com `TextField` readOnly — rejeitado (aparência de input, não pílula).

---

### 3. Menu flutuante — padrão visual do cadastro de ativos (v1), implementação nova em v2

**Decisão**: `DropdownMenu` Material3 ancorado ao `Box` do gatilho; itens via `DropdownMenuItem` com realce **`tertiaryContainer` / `onTertiaryContainer`** no seleccionado — espelhando `StableExposedDropdownMenuRow` em `AppDropdownField.kt`.

**Tokens menu**:

| Aspeto | Token / API |
|--------|-------------|
| Contentor | `DropdownMenu` — `shape = MaterialTheme.shapes.large`, `containerColor = surfaceContainerLow`, `shadowElevation = MenuDefaults.ShadowElevation` |
| Item seleccionado | `background(tertiaryContainer)` + `MenuDefaults.itemColors` com texto `onTertiaryContainer` |
| Espaçamento lista | `Column` + `spacedBy(4.dp)` + padding `4.dp` (mesmo padrão v1) |
| Largura mínima | `widthIn(min = triggerWidth)` ou `IntrinsicSize.Min` alinhado ao gatilho |

**Alternativas consideradas**:
- `ExposedDropdownMenu` — viable mas acoplado a anchor type; `DropdownMenu` + `Box` é mais simples para gatilho custom.
- Copiar ficheiro `AppDropdownField` para v2 — rejeitado (YAGNI; extrair só padrão de row/menu).

---

### 4. Lista longa e auto-scroll (FR-007a)

**Decisão**: Conteúdo do menu em `LazyColumn` com `Modifier.heightIn(max = MonthYearSelectorDefaults.menuMaxHeight)` (~`320.dp` ou 8–10 itens visíveis); `LazyListState` + `LaunchedEffect(expanded, selectedIndex)` com `animateScrollToItem(selectedIndex.coerceAtLeast(0), scrollOffset = 0)` quando `expanded == true` — item seleccionado alinhado ao **topo da área visível** do menu (não centrado).

**Rationale**: Clarificação Q4; SC-005. `DropdownMenuItem` dentro de `LazyColumn` é suportado no Compose M3. Top-aligned é previsível com `DropdownMenu` de altura limitada e evita ambiguidade “centrado vs topo” da spec.

**Alternativas consideradas**:
- `Column` + `verticalScroll` — aceitável para ≤12 itens; `LazyColumn` preferido para 24+ (SC-005).
- Scroll manual apenas — rejeitado (clarificação).

---

### 5. Formatação pt-BR predefinida

**Decisão**: `internal object MonthYearLabelFormatter` com mapa fixo `Month → String` (Janeiro…Dezembro) e função `format(yearMonth: YearMonth): String` → **"{Mês} de {Ano}"**.

**Rationale**: FR-002/FR-005; locales futuros fora do escopo. Evita depender de `Formatters.kt` do `composeApp` (camada errada; `internal` noutro módulo).

**Exemplo**: `YearMonth(2026, Month.MAY)` → `"Maio de 2026"`.

**Alternativas consideradas**:
- `kotlinx.datetime` format API com locale — verificar suporte KMP pt-BR; mapa fixo é previsível e testável.
- Delegar sempre ao integrador — rejeitado (default format é responsabilidade do componente per clarificação).

---

### 6. API `itemLabel` opcional

**Decisão**:

```kotlin
itemLabel: (YearMonth) -> String = MonthYearLabelFormatter::format
```

Comparação de item activo: `option == selected` (identidade `YearMonth`), **nunca** por texto.

**Rationale**: Clarificação Q1 (híbrido); mesmo padrão que `AppDropdownField.itemLabel`.

---

### 7. Estado desactivado e lista vazia (FR-008c, FR-009)

**Decisão**: `effectiveEnabled = enabled && options.isNotEmpty()`; quando falso, gatilho atenuado (`disabled` content alpha / cores disabled do M3) e `clickable` omitido.

**Rationale**: Clarificação Q2. Rótulo do `selected` continua visível mesmo com lista vazia.

---

### 8. Ordem das opções

**Decisão**: `options.forEach` na ordem recebida — **sem** `sortedBy`.

**Rationale**: Clarificação Q3; integrador controla ordem (ex.: cronologia descendente no Histórico futuro).

---

### 9. Preview / catálogo (FR-011)

**Decisão**: `MonthYearSelectorCatalog.kt` com ~12 `YearMonth` estáticos + um exemplo com `itemLabel` custom ("Todos os meses" num mês sentinela ou entrada dedicada no preview only). Previews **`private`** em `MonthYearSelector.kt`:

| Preview | Conteúdo |
|---------|----------|
| `MonthYearSelector_Light_preview` | `AppThemeV2(darkTheme = false)` |
| `MonthYearSelector_Dark_preview` | `AppThemeV2(darkTheme = true)` |
| `MonthYearSelector_EmptyOptions_preview` | lista vazia + selected |
| `MonthYearSelector_LongList_preview` | 24 itens, selected no meio (auto-scroll) |

**Rationale**: Clarificação Q5; constituição (previews no mesmo ficheiro do composable).

---

### 10. Acessibilidade

**Decisão**: Gatilho com `semantics { role = Role.DropdownList; stateDescription = itemLabel(selected) }`; ícone calendário `contentDescription = null` + `clearAndSetSemantics { }` ou `Modifier.semantics { invisibleToUser() }`.

**Rationale**: Edge case spec; ícone decorativo.

---

### 11. Verificação Gradle

**Comando**: `./gradlew :features:design-system-v2:compileKotlinJvm`

**Registo**: módulo já existe (011/012); **sem** alteração `settings.gradle.kts`; **sem** registo `umbrellaApp`.

---

### 12. Integração futura (fora desta feature)

`AssetHistoryScreen` usa hoje `SegmentedControl` para período (linha ~449). Substituição pelo `MonthYearSelector` fica para feature de integração Histórico — **não** alterar `composeApp` nesta entrega (FR-010).
