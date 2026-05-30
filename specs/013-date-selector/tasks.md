# Tasks: Seletor de mês/ano

**Input**: Design documents from `/specs/013-date-selector/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/MonthYearSelectorContract.md, quickstart.md

**Tests**: Não solicitados na spec — validação via `compileKotlinJvm` e previews Compose.

**Organization**: Tarefas agrupadas por user story (US1 P1, US2 P1, **US4 P2**, **US3 P2** — US3 **após** US4). Ordem de entrega do plano: US1 → US2 → US4 → US3.

**Previews (constituição)**: Todos os `@Preview` ficam em `MonthYearSelector.kt`, funções `private`, no mesmo ficheiro do composable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode executar em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: User story da spec (`US1`, `US2`, `US3`, `US4`)

## Path Conventions

- Módulo: `core/presentation/design-system-v2/`
- Pacote: `com.eferraz.design_system_v2.dateselector`
- Gradle: `:features:design-system-v2`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependência `kotlinx-datetime` e pacote `dateselector/` no módulo v2 existente.

- [X] T001 Acrescentar `implementation(libs.kotlinx.datetime)` em `core/presentation/design-system-v2/build.gradle.kts`
- [X] T002 Criar pasta `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/dateselector/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Formatador pt-BR e dimensões partilhadas — bloqueia US1–US4.

**⚠️ CRITICAL**: Nenhuma user story até concluir esta fase.

- [X] T003 [P] Implementar `MonthYearLabelFormatter` com mapa pt-BR (Janeiro…Dezembro) e `format(YearMonth) → "{Mês} de {Ano}"` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/dateselector/MonthYearLabelFormatter.kt`
- [X] T004 [P] Implementar `MonthYearSelectorDefaults` (`triggerHeight`, padding pílula, `iconSize`, `menuMaxHeight`, `menuItemSpacing`) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/dateselector/MonthYearSelectorDefaults.kt`
- [X] T005 Executar `./gradlew :features:design-system-v2:compileKotlinJvm` após T003–T004 e corrigir erros

**Checkpoint**: Formatador e defaults compilam; prontos para o composable.

---

## Phase 3: User Story 1 — Identificar o período selecionado (Priority: P1) 🎯 MVP

**Goal**: Gatilho compacto em pílula com ícone de calendário, rótulo **"{Mês} de {Ano}"**, sem seta/chevron.

**Independent Test**: Preview `MonthYearSelector_Light_preview` (menu fechado) — vê pílula, ícone à esquerda, texto "Maio de 2026", **sem** indicador de expansão.

### Implementation for User Story 1

- [X] T006 [US1] Criar esqueleto `public fun MonthYearSelector(...)` com parâmetros do contrato (`selected`, `options`, `onSelectionChange`, `modifier`, `enabled`, `itemLabel = MonthYearLabelFormatter::format`); calcular `effectiveEnabled = enabled && options.isNotEmpty()` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/dateselector/MonthYearSelector.kt`
- [X] T007 [US1] Implementar gatilho pílula: `Row`/`Surface` com `RoundedCornerShape(50%)`, fundo `surfaceContainerLow`, ícone `Icons.Outlined.CalendarMonth`, texto `itemLabel(selected)` com `TextOverflow.Ellipsis` e **sem** `TrailingIcon`/chevron em `MonthYearSelector.kt`
- [X] T008 [US1] Aplicar tokens M3 Expressive ao gatilho (tipografia semibold compacta, ícone `primary`, texto `onSurface`) conforme `research.md` §2 em `MonthYearSelector.kt`
- [X] T009 [US1] Adicionar `@Preview` `private fun MonthYearSelector_Light_preview()` no final de `MonthYearSelector.kt` envolvido em `AppThemeV2(darkTheme = false)` com `selected` estático e menu ainda fechado (FR-002, FR-003)

**Checkpoint**: Gatilho pílula reconhecível no preview — **MVP visual entregue**.

---

## Phase 4: User Story 2 — Escolher outro mês/ano no menu flutuante (Priority: P1)

**Goal**: Menu `DropdownMenu` M3, realce do item activo, seleccionar/fechar/dismiss, ordem integrador, auto-scroll em lista longa.

**Independent Test**: No preview interactivo — abrir menu → item seleccionado realçado (lavanda) → escolher outro mês → menu fecha e gatilho actualiza; segundo toque no gatilho fecha sem mudar selecção.

### Implementation for User Story 2

- [X] T010 [US2] Adicionar estado `expanded` e `Box` anchor; ao clicar gatilho com `effectiveEnabled`: se `!expanded` abrir menu, senão fechar **sem** `onSelectionChange` (US2 cenário 3) em `MonthYearSelector.kt`
- [X] T011 [US2] Implementar rows de menu com `DropdownMenuItem`, realce `tertiaryContainer`/`onTertiaryContainer` no item onde `option == selected` — padrão equivalente a `AppDropdownField.kt` — em `MonthYearSelector.kt`
- [X] T012 [US2] Configurar contentor do menu: `shape = MaterialTheme.shapes.large`, `containerColor = surfaceContainerLow`, `shadowElevation = MenuDefaults.ShadowElevation`, `Column`/`spacedBy(4.dp)` em `MonthYearSelector.kt`
- [X] T013 [US2] Invocar `onSelectionChange(option)`, fechar menu e actualizar gatilho ao seleccionar item em `MonthYearSelector.kt`
- [X] T014 [US2] Fechar menu em `onDismissRequest` (tap fora) sem alterar selecção em `MonthYearSelector.kt`
- [X] T015 [US2] Renderizar opções na ordem de `options` **sem** `sortedBy` (FR-008d) em `MonthYearSelector.kt`
- [X] T016 [US2] Substituir lista por `LazyColumn` com `Modifier.heightIn(max = MonthYearSelectorDefaults.menuMaxHeight)` dentro do menu em `MonthYearSelector.kt`
- [X] T017 [US2] Implementar auto-scroll: `LaunchedEffect(expanded)` + `listState.animateScrollToItem(indexOf(selected), scrollOffset = 0)` ao abrir (FR-007a; topo visível — `research.md` §4) em `MonthYearSelector.kt`

**Checkpoint**: Fluxo completo abrir → seleccionar → fechar funcional; toggle no gatilho; realce e ordem correctos.

---

## Phase 5: User Story 4 — Integrar o seletor com dados externos (Priority: P2)

**Goal**: API desacoplada — `itemLabel` opcional, lista vazia, `enabled` explícito, `selected` ausente da lista.

**Independent Test**: Previews com lista vazia (gatilho desactivado + rótulo visível) e `itemLabel` custom ("Todos os meses") — identidade por `YearMonth`, não por texto.

### Implementation for User Story 4

- [X] T018 [US4] Aplicar aparência atenuada ao gatilho quando `!effectiveEnabled` (lista vazia ou `enabled = false`); omitir `clickable` nesses casos (FR-008c, FR-009) em `MonthYearSelector.kt`
- [X] T019 [US4] Com `options` vazia: manter rótulo `itemLabel(selected)` no gatilho e **não** abrir menu em `MonthYearSelector.kt`
- [X] T020 [US4] Garantir `itemLabel` no gatilho e em cada row; comparar item activo por `YearMonth` equality, nunca por texto do rótulo (FR-008a, FR-008b) em `MonthYearSelector.kt`
- [X] T021 [US4] Quando `selected ∉ options`: gatilho mostra rótulo; menu aberto **sem** realce em nenhum item em `MonthYearSelector.kt`
- [X] T022 [US4] Reflectir alteração programática de `selected` via recomposição (sem interacção extra) em `MonthYearSelector.kt`

**Checkpoint**: Contrato híbrido e edge cases de integrador cobertos.

---

## Phase 6: User Story 3 — Validar o componente no catálogo do design system (Priority: P2)

**⚠️ Bloqueio**: Iniciar **após** Phase 5 (T018–T022). T023 pode ser escrito em paralelo com US4; previews T024–T029 dependem de `MonthYearSelectorCatalog`.

**Goal**: Catálogo estático + previews light/dark, lista longa, lista vazia e viewport estreito (FR-011, SC-005).

**Independent Test**: Abrir previews light e dark — interacção idêntica; `LongList` com auto-scroll; `EmptyOptions` com gatilho desactivado; `Narrow320` legível em 320 dp.

### Implementation for User Story 3

- [X] T023 [P] [US3] Criar `MonthYearSelectorCatalog` com `defaultOptions`, `defaultSelected`, `longListOptions` (~24 entradas, selected central) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/dateselector/MonthYearSelectorCatalog.kt`
- [X] T024 [US3] Actualizar `MonthYearSelector_Light_preview` para usar `MonthYearSelectorCatalog` com estado local `remember { mutableStateOf(...) }` e interacção completa em `MonthYearSelector.kt`
- [X] T025 [US3] Adicionar `@Preview` `private fun MonthYearSelector_Dark_preview()` com `AppThemeV2(darkTheme = true)` e mesmos dados do catálogo em `MonthYearSelector.kt`
- [X] T026 [P] [US3] Adicionar `@Preview` `private fun MonthYearSelector_LongList_preview()` com 24 opções e selected no meio (validar auto-scroll) em `MonthYearSelector.kt`
- [X] T027 [P] [US3] Adicionar `@Preview` `private fun MonthYearSelector_EmptyOptions_preview()` com `options = emptyList()` em `MonthYearSelector.kt`
- [X] T028 [P] [US3] Adicionar `@Preview` `private fun MonthYearSelector_CustomLabel_preview()` demonstrando `itemLabel` custom (ex. "Todos os meses") em `MonthYearSelector.kt`
- [X] T029 [P] [US3] Adicionar `@Preview(widthDp = 320)` `private fun MonthYearSelector_Narrow320_preview()` com lista longa — validar menu rolável e gatilho sem sobreposição ilegível (SC-005) em `MonthYearSelector.kt`

**Checkpoint**: SC-004 e SC-005 verificáveis nos previews.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Acessibilidade, verificação final e documentação.

- [X] T030 [P] Semântica a11y no gatilho (`Role.DropdownList`, valor anunciado); ícone calendário decorativo (`contentDescription = null` + `invisibleToUser()`) em `MonthYearSelector.kt`
- [X] T031 Executar `./gradlew :features:design-system-v2:compileKotlinJvm` e resolver falhas
- [X] T032 Confirmar que **nenhum** ficheiro em `core/presentation/composeApp/` (incl. `AssetHistoryScreen.kt`) foi alterado (FR-010)
- [X] T033 [P] Actualizar `AGENTS.md` — mencionar `MonthYearSelector` em `:features:design-system-v2` (Princípio VII)
- [X] T034 Rever checklist em `specs/013-date-selector/quickstart.md` — incluir revisão SC-003 (consistência visual vs `AppDropdownField` side-by-side, escala ≥ 4) e marcar itens concluídos na PR

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — iniciar imediatamente
- **Foundational (Phase 2)**: Depende de Phase 1 — **bloqueia** todas as user stories
- **US1 (Phase 3)**: Depende de Phase 2
- **US2 (Phase 4)**: Depende de US1 (gatilho + `effectiveEnabled` em T006)
- **US4 (Phase 5)**: Depende de US2 (menu funcional)
- **US3 (Phase 6)**: Depende de US4 (edge cases + API estável)
- **Polish (Phase 7)**: Depende de US3 (ou US1+US2 se entrega parcial)

### User Story Dependencies

```text
Phase 1 → Phase 2 → US1 → US2 → US4 → US3 → Polish
```

- **US1**: Independente após Foundational — MVP (só gatilho); inclui `effectiveEnabled`
- **US2**: Extende US1 — menu interactivo + toggle no gatilho
- **US4**: Refina US2 — estados de integrador e styling disabled
- **US3**: Catálogo/previews — requer componente completo

### Parallel Opportunities

- **Phase 2**: T003 ∥ T004
- **Phase 6**: T023 ∥ (preparar enquanto US4); T026 ∥ T027 ∥ T028 ∥ T029 após T023–T025
- **Phase 7**: T030 ∥ T033

### Parallel Example: User Story 3

```bash
# Após T023 (catalog):
T026 → MonthYearSelector_LongList_preview
T027 → MonthYearSelector_EmptyOptions_preview
T028 → MonthYearSelector_CustomLabel_preview
T029 → MonthYearSelector_Narrow320_preview
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: Setup (T001–T002)
2. Phase 2: Foundational (T003–T005)
3. Phase 3: US1 (T006–T009)
4. **STOP and VALIDATE**: Preview light — pílula + rótulo + ícone, sem seta

### Incremental Delivery

1. Setup + Foundational → base pronta
2. US1 → gatilho pílula + `effectiveEnabled` (MVP visual)
3. US2 → menu + seleção + toggle + auto-scroll
4. US4 → styling disabled + edge cases `itemLabel`
5. US3 → catálogo light/dark + previews edge + narrow 320
6. Polish → compile, a11y, docs, SC-003 review

### Suggested MVP Scope

**User Story 1** (Phase 3) — gatilho pílula com período legível; suficiente para validação visual antes do menu.

---

## Notes

- Não reutilizar nem alterar `AppDropdownField` em `:features:design-system` — copiar **só** padrão visual de menu row
- Não registar em `umbrellaApp` (DS v2 puro)
- `explicitApi()`: `public` só em `MonthYearSelector`; resto `internal`
- **Remediação pós-analyse (2026-05-29)**: I1 — `effectiveEnabled` movido para T006; U1 — toggle fechar em T010; U2 — preview 320 dp em T029; U3 — SC-003 em T034
- Total: **34 tarefas** | US1: 4 | US2: 8 | US4: 5 | US3: 7 | Setup: 2 | Foundational: 3 | Polish: 5
