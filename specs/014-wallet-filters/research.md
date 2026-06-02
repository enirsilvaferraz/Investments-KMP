# Research: Filtros da carteira

**Feature**: `014-wallet-filters` | **Date**: 2026-06-02

**Diretriz do plano**: priorizar **paralelismo** — trilhas independentes em `:features:design-system-v2` vs `:features:composeApp`, e secções do painel desacopladas por contrato de estado.

---

## Decisões Técnicas

### 1. Localização — painel em `composeApp`, primitivos sob demanda em v2

**Decisão**: Widget composto `WalletFiltersPanel` em `core/presentation/composeApp/.../walletfilters/`; design-system-v2 recebe **apenas** primitivos genéricos reutilizáveis (toggle group multi-selecção, cabeçalho de secção).

**Rationale**: Clarificações FR-001a/b; espelha `SummaryGridWidget` + `SummaryCard`. Evita API monolítica `WalletFiltersPanel` no v2.

**Alternativas consideradas**:
- Painel inteiro no v2 — rejeitado (spec).
- Reutilizar só `SegmentedControl` v1 — rejeitado (semântica exclusiva; ver §2).

---

### 2. Toggles — novo `FilterToggleGroup` (v2), **não** `SegmentedControl`

**Decisão**: Primitivo M3 Expressive em v2 com `ToggleButton` + `ButtonGroupDefaults.connected*ButtonShapes()`, **multi-selecção** (`Set<T>` + `onToggle(T)`), semântica **`Role.Checkbox`**, tamanhos `Standard` (~40dp) e `Compact` (~32dp).

**Rationale**: `SegmentedControl` (v1) usa `selected: SegmentedControlChoice<T>?` único e `Role.RadioButton` — incompatível com FR-008a (multi-selecção em todas as secções de toggles). Histórico usa exclusividade por dimensão; carteira exige combinações livres.

**Morph pill → rectângulo**: mesmo padrão `connectedLeading/Middle/TrailingButtonShapes()` já validado em `SegmentedControl.kt` e `AssetHistoryScreen`.

**Alternativas consideradas**:
- Adaptar `SegmentedControl` com flag `multiSelect` — rejeitado (mudança breaking no v1; semântica a11y misturada).
- `FilterChip` M3 — rejeitado (spec FR-M3-005: toggles expressivos, não chips).
- Duplicar Row de `ToggleButton` só no composeApp — aceitável como fallback YAGNI, mas **recomendado extrair** para v2 pela repetição em ≥5 secções.

---

### 3. «Vence até» — composable no `composeApp`, padrão visual do `MonthYearSelector`

**Decisão**: `MaturityFilterDropdown` **interno** ao composeApp: `ExposedDropdownMenuBox` largura total, primeiro item **«Qualquer vencimento»** (`null` / sentinel), demais itens `YearMonth` derivados da carteira, ordenação cronológica, labels `"{Mês}/{Ano}"` (ex. Novembro/2027).

**Rationale**: `MonthYearSelector` exige `selected: YearMonth` sem opção «qualquer»; API v2 não deve crescer nesta feature (YAGNI). Reutilizar **padrão** (checkmark, `shape large`, tipografia) sem alterar contrato público de `MonthYearSelector`.

**Alternativas consideradas**:
- Estender `MonthYearSelector` com `nullable selected` — rejeitado (escopo 013 fechado; breaking para consumidores).
- Lista infinita de meses — rejeitado (FR-011: só meses presentes nos dados).

---

### 4. Estado — `WalletFiltersUiState` no widget, sem ViewModel

**Decisão**: `data class` imutável + funções puras `deriveFilterOptions(portfolio)` e `reduce(event)` no composeApp; `remember` no composable raiz e nos `@Preview`.

**Rationale**: Clarificação «sem ViewModel nesta entrega»; FR-015. Integração Histórico futura promove o mesmo modelo para `StateFlow` sem mudar derivação.

**Alternativas consideradas**:
- ViewModel + Koin agora — rejeitado (out of scope).
- Estado espalhado por secção — rejeitado (FR-015: ponto único + Reset).

---

### 5. Derivação de opções — modelo de preview, sem domínio

**Decisão**: `WalletFilterPortfolioItem` (composeApp, `internal`) com dimensões necessárias à spec (classe, subtipo, liquidez, B3, liquidado, vencimento); catálogos estáticos em `WalletFiltersPreviewCatalog`.

**Rationale**: FR-017 — sem use cases nem repositórios. Alinha com entidades reais (`Liquidity`, `B3IdentifierStatus`) como **referência** para nomes, sem dependência obrigatória de `:domain:entity` se YAGNI — **opcional** `implementation(projects.domain.entity)` só se reutilizar enums; caso contrário enums espelhados no preview.

**Recomendação**: reutilizar `Liquidity` e tipos de `:domain:entity` onde já existem no classpath do composeApp (`usecases` → entity transitivo) para evitar duplicação.

---

### 6. Tooltips e abreviaturas

**Decisão**: `FilterOption` com `shortLabel` + `fullLabel`; primitivo `FilterToggleGroup` aceita `contentDescription` por item; tooltip M3 (`TooltipBox` / plain) no composeApp quando `shortLabel != fullLabel`.

**Rationale**: FR-013, FR-M3-019. Desktop preview prioriza hover; a11y via `contentDescription` = `fullLabel`.

---

### 7. Layout responsivo B3 + Liquidados

**Decisão**: `Row` com `Modifier.weight(1f)` em `WindowWidthSizeClass.Expanded` (ou `BoxWithConstraints` / largura ≥ breakpoint ~600dp); empilhar em estreito.

**Rationale**: FR-008, user story 3 cenário 2. Sem nova dependência; padrão Compose existente no projeto.

---

### 8. Validação e build (constituição IX)

**Decisão**: Previews como critério de aceite; `./gradlew` apenas sob pedido do utilizador ou CI.

**Rationale**: Princípio IX; testes de domínio N/A (sem use cases).

---

## Paralelismo de implementação (síntese)

| Trilha | Módulo | Pode começar | Depende de |
|--------|--------|--------------|------------|
| **A** | v2 — `FilterToggleGroup` + previews | Imediato | — |
| **B** | v2 — `FilterSectionHeader` (opcional) | Imediato | — |
| **C** | composeApp — modelo + derivação pura | Imediato | Contrato `data-model.md` |
| **D** | composeApp — shell cartão + reset | Imediato | — |
| **E1–E5** | composeApp — secções UI | Após **A** (ou stub) | **C** para opções reais |
| **F** | composeApp — `MaturityFilterDropdown` | Paralelo a **E*** | **C** |
| **G** | composeApp — previews light/dark/dinâmico | Após **D+E+F** | Tudo integrado |

**E1–E5** entre si: **paralelo** (Classe, Subtipos, Liquidez, B3/Liquidados, Vence até) desde que partilhem `WalletFiltersUiState` e callbacks definidos em **C**.

---

## Referências no repositório

| Necessidade | Ficheiro |
|-------------|----------|
| Toggle morph (referência) | `design-system/.../SegmentedControl.kt` |
| Composição feature + v2 | `composeApp/.../SummaryGridWidget.kt` |
| Dropdown mês/ano | `design-system-v2/.../MonthYearSelector.kt` |
| Cartão M3 | `design-system-v2/.../SummaryCard.kt` |
| Dimensões liquidez/B3 (futuro) | `entities/assets/Liquidity.kt`, `HoldingHistoryView` |

---

## Riscos mitigados

| Risco | Mitigação |
|-------|-----------|
| Reintroduzir exclusividade via `SegmentedControl` | Proibir import v1 no pacote `walletfilters`; contract exige `Set` |
| Secções órfãs sem opções | `deriveVisibleSections()` centralizado (FR-018) |
| Cores hex do mockup | Tokens `MaterialTheme` + `AppThemeV2` apenas |
| Build lento bloqueando agente | IX — validação por preview |
