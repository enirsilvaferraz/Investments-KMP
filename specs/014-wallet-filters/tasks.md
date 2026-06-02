# Tasks: 014-wallet-filters — Filtros da carteira

**Input**: Design documents from `/specs/014-wallet-filters/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

**Tests**: Não há pedido explícito de TDD ou testes automatizados nesta feature. Focamos em implementação e previews. Testes podem ser acrescentados depois, se desejado.

**Organization**: Tasks organizadas por user story da spec (US1–US7), com fases de Setup / Fundacional mínimas e forte ênfase em **paralelismo** via `[P]`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (ficheiros diferentes, sem dependências diretas)
- **[Story]**: User story (US1, US2, …) conforme `spec.md`
- Descrições sempre com **caminho de ficheiro** quando aplicável

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar estrutura mínima de ficheiros para paralelizar trilhas.

- [X] T001 [P] Criar pasta de feature em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/`
- [X] T002 [P] Criar subpasta `sections/` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/sections/`
- [X] T003 [P] Criar pasta de primitivos em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Itens base que simplificam todas as trilhas; pequenos, para não bloquear o paralelismo.

- [X] T004 Definir contrato base de estado/eventos (`WalletFiltersEvent`, estado inicial e helpers) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersUiState.kt`

**Checkpoint**: Estrutura criada; trilhas A, B, C e D do plano podem iniciar em paralelo.

---

## Phase 3: User Story 1 - Visualizar painel de filtros no cartão (Priority: P1) 🎯 MVP

**Goal**: Renderizar o **painel de filtros** dentro de um `OutlinedCard` M3 Expressive em `composeApp`, com cabeçalho (título, ícone, Reset) e contentor scrollável, sem ainda ligar secções reais.

**Independent Test**: Abrir o `@Preview` principal do painel no `composeApp` e verificar cartão com título, ícone de filtro, botão Reset e placeholder para secções com rendering estável em light/dark.

### Implementation for User Story 1

- [X] T005 [P] [US1] Criar `WalletFiltersUiState` e `MaturitySelection` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersUiState.kt`
- [X] T006 [P] [US1] Implementar funções puras de derivação `deriveFilterOptions` e `deriveVisibleSections` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt`
- [X] T007 [P] [US1] Criar catálogo de dados de preview `WalletFiltersPreviewCatalog` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPreviewCatalog.kt`
- [X] T008 [US1] Implementar `WalletFiltersPanel` com card, cabeçalho (título, ícone `filter_list`, botão Reset) e slots para secções em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`
- [X] T009 [US1] Adicionar previews principais (`WalletFiltersPanelPreviewFull`, `WalletFiltersPanelPreviewDynamic`) com `AppThemeV2` light/dark em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Painel visível em preview dentro de um cartão; Reset volta o estado ao inicial (mesmo que ainda não existam secções reais).

---

## Phase 4: User Story 2 - Seleccionar classes e subtipos dependentes (Priority: P1)

**Goal**: Permitir multi-selecção de classes (Renda Fixa, Renda Variável, Fundos) e mostrar subcartões de subtipos por classe activa, com opções derivadas da carteira de demo.

**Independent Test**: No preview, activar/desactivar classes e observar aparecimento/desaparecimento de subcartões de subtipos, respeitando FR-018 (nenhuma secção sem opções).

### Implementation for User Story 2

- [X] T010 [P] [US2] Implementar `FilterToggleGroup`, `FilterToggleOption` e enum `FilterToggleSize` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt`
- [X] T011 [P] [US2] Extrair defaults (dimensões, espaçamentos, tipografia) para `FilterToggleGroupDefaults` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroupDefaults.kt`
- [X] T012 [P] [US2] Criar `FilterSectionHeader` (ícone + label uppercase) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterSectionHeader.kt`
- [X] T013 [P] [US2] Adicionar previews `FilterToggleGroup` (standard/compact, multi-selecção) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt`
- [X] T014 [P] [US2] Implementar modelo `FilterOption`, `FilterGroupKind`, `WalletFilterPortfolioItem` e `SubtypeSectionModel` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt` conforme `data-model.md`
- [X] T015 [P] [US2] Adicionar catálogo de carteira de demo com classes e subtipos representativos em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPreviewCatalog.kt`
- [X] T016 [US2] Implementar `ClassFilterSection` usando `FilterSectionHeader` e `FilterToggleGroup` size `Standard` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/sections/ClassFilterSection.kt`
- [X] T017 [US2] Implementar `SubtypeFilterSections` com subcartões por classe activa em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/sections/SubtypeFilterSections.kt`
- [X] T018 [US2] Ligar secções de classe e subtipos ao `WalletFiltersPanel` respeitando `visibleSections` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Classes e subtipos funcionam em preview; desactivar uma classe limpa e esconde os subtipos dessa classe.

---

## Phase 5: User Story 3 - Filtrar por liquidez, B3 e liquidados com botões compactos (Priority: P1)

**Goal**: Adicionar secções de Liquidez, B3 informado e Liquidados com botões compactos multi-selecção e layout responsivo, derivando opções dos dados.

**Independent Test**: No preview completo, alternar liquidez, B3 e liquidados; verificar mudança de forma pill→quadrado e ocultação de secções sem opções válidas.

### Implementation for User Story 3

- [X] T019 [P] [US3] Estender derivação de opções para liquidez, B3 e liquidados em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt`
- [X] T020 [P] [US3] Implementar `LiquidityFilterSection` usando `FilterToggleGroup` size `Compact` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/sections/LiquidityFilterSection.kt`
- [X] T021 [P] [US3] Implementar `B3SettledFilterRow` compondo duas secções lado a lado (B3, Liquidados) com layout responsivo em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/sections/B3SettledFilterRow.kt`
- [X] T022 [US3] Integrar secções de liquidez, B3 e liquidados ao `WalletFiltersPanel` respeitando FR-018d (secção oculta se não houver Sim e Não) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Liquidez, B3 e Liquidados aparecem apenas quando há opções válidas; toggles compactos seguem padrão M3 Expressive.

---

## Phase 6: User Story 4 - Escolher vencimento até com opções derivadas dos dados (Priority: P2)

**Goal**: Implementar selector **Vence até** com menu M3 (Qualquer vencimento + meses reais da carteira), secção oculta quando não houver vencimentos.

**Independent Test**: No preview, ver menu com Qualquer vencimento + meses da carteira de demo; em dataset sem vencimentos, a secção não aparece.

### Implementation for User Story 4

- [X] T023 [P] [US4] Derivar lista de `YearMonth` distintos ordenados para vencimento em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt`
- [X] T024 [P] [US4] Implementar `MaturityFilterDropdown` com `ExposedDropdownMenuBox` e sentinel `MaturitySelection.Any` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/MaturityFilterDropdown.kt`
- [X] T025 [US4] Integrar selector de vencimento ao `WalletFiltersPanel` com ícone `calendar_month` e visibilidade condicionada à existência de meses em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Vence até funciona em preview, obedecendo FR-018e (ocultar se não houver vencimentos).

---

## Phase 7: User Story 5 - Rótulos abreviados com descrição ao focar (Priority: P2)

**Goal**: Suportar abreviação de rótulos e exibir descrição completa via tooltip/a11y.

**Independent Test**: No preview desktop, verificar que botões com texto abreviado mostram tooltip com rótulo completo; leitor de ecrã anuncia a descrição completa.

### Implementation for User Story 5

- [X] T026 [P] [US5] Garantir que `FilterOption` contém `shortLabel` e `fullLabel` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt`
- [X] T027 [P] [US5] Propagar `contentDescription` baseado em `fullLabel` para `FilterToggleOption` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt`
- [X] T028 [US5] Adicionar uso de tooltip M3 (plain tooltip) em pontos críticos do painel em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt` e secções em `sections/`

**Checkpoint**: Abreviações visuais mantêm descrição completa acessível e em tooltip.

---

## Phase 8: User Story 6 - Repor filtros e reflectir estado na interface (Priority: P2)

**Goal**: Garantir que o botão Reset limpa todos os critérios e reflecte o estado inicial em todas as secções.

**Independent Test**: Selecionar vários filtros, clicar Resetar e verificar estado inicial (nenhum toggle activo, subtipos ocultos, Vence até em Qualquer vencimento).

### Implementation for User Story 6

- [X] T029 [P] [US6] Implementar função de redução `reduce(event: WalletFiltersEvent, state)` centralizando as transições de estado em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersUiState.kt`
- [X] T030 [US6] Ligar evento de Reset do cabeçalho ao reducer para limpar todas as selecções em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Reset consistente em todas as secções; nenhuma seleção residual.

---

## Phase 9: User Story 7 - Opções de filtro coerentes com os dados disponíveis (Priority: P2)

**Goal**: Aplicar a regra transversal FR-018/FR-012 garantindo que nenhuma secção aparece sem opções e que todas as opções são derivadas da carteira de demo.

**Independent Test**: Ajustar datasets de preview (ex.: remover Fundos, remover liquidez, forçar só Sim em B3) e confirmar que secções e botões sem correspondência desaparecem.

### Implementation for User Story 7

- [X] T031 [P] [US7] Ajustar derivação para aplicar todas as regras FR-018 em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersDerivation.kt`
- [X] T032 [P] [US7] Expandir `WalletFiltersPreviewCatalog` com cenários edge (carteira vazia, sem Fundos, liquidez parcial, B3/Liquidados sem variação) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPreviewCatalog.kt`
- [X] T033 [US7] Criar previews adicionais focados em edge cases (incluindo tema escuro) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

**Checkpoint**: Nenhuma secção é renderizada sem opções seleccionáveis; SC-009 atendido.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Ajustes finais, consistência com M3 Expressive e constituição.

- [X] T034 [P] Revisar tipografia, cores e formas do painel e dos primitivos em `design-system-v2` e `walletfilters` para garantir uso exclusivo de tokens `MaterialTheme` / `AppThemeV2`
- [X] T035 [P] Verificar estados de acessibilidade (role checkbox, foco visível, contraste) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt` e painel `walletfilters`
- [X] T036 [P] Actualizar `AGENTS.md` para mencionar `FilterToggleGroup` e o painel de filtros da carteira após criação dos primitivos em `AGENTS.md`
- [X] T037 Revisar `quickstart.md` desta feature com os caminhos finais e comandos opcionais em `specs/014-wallet-filters/quickstart.md`
- [X] T038 [P] Implementar matriz explícita de ícones por secção (FR-M3-017) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt` e ficheiros em `sections/`
- [X] T039 [P] Implementar conformidade de movimento reduzido (FR-M3-024) para transições de toggles em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/filter/FilterToggleGroup.kt`
- [X] T040 [P] Verificar explicitamente ausência de wiring no histórico (sem alterações em `AssetHistoryScreen`) e documentar integração futura em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/walletfilters/WalletFiltersPanel.kt`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode começar imediatamente.
- **Foundational (Phase 2)**: Depende de Phase 1; após T004, trilhas A–D podem iniciar.
- **User Stories (Phase 3–9)**: Dependem de Setup + Foundational mínimas, mas têm **muitas tarefas [P]** internas:
  - US1 (Phase 3) abre base para previews, mas US2–US4 podem avançar em paralelo nas partes de derivação/primordiais.
  - US5–US7 dependem de funcionalidades anteriores, mas podem ser iniciadas assim que as respectivas estruturas existirem.
- **Polish (Phase 10)**: Depende das user stories que forem consideradas dentro do MVP desta feature.

### User Story Dependencies

- **US1 (P1)**: Pode iniciar assim que T001–T003 forem criadas; recomendada como primeiro incremento visível (MVP).
- **US2 (P1)**: Depende de `WalletFiltersUiState`, derivação básica e painel (T005–T008), mas **primitivo v2 (T010–T013)** pode ser feito em paralelo antes.
- **US3 (P1)**: Depende da derivação e painel base; secções podem evoluir em paralelo com US2.
- **US4 (P2)**: Depende de derivação de vencimentos e painel; dropdown implementável em paralelo com secções de toggles.
- **US5–US7 (P2)**: Dependem de secções básicas; focam em refino (abreviações, reset robusto, consistência de dados).

### Within Each User Story

- Dentro de cada US, tasks marcadas com **[P]** podem ser atribuídas a diferentes agentes/desenvolvedores em paralelo, desde que respeitem:
  - Ficheiros distintos, ou merges triviais.
  - Dependências explícitas indicadas nas descrições.
- `WalletFiltersPanel` (T008, T018, T022, T025, T028, T030, T033) é o principal ponto de integração e deve ser ajustado em passos incrementais, evitando grandes refactors monolíticos.

### Parallel Opportunities

- Trilha A (primitivo v2) pode ser feita em paralelo com trilhas C e D (estado + painel base) desde o início.
- Secções individuais (US2–US3) podem ser desenvolvidas por pessoas diferentes em ficheiros separados de `sections/`.
- `MaturityFilterDropdown` (US4) pode evoluir em paralelo com toggles de US2/US3.
- Previews de edge cases (US7) podem ser adicionados em paralelo à refinação de estilos (Phase 10).

---

## Implementation Strategy

### MVP First (User Story 1 + parte de US2)

1. Completar Phase 1–2 (estrutura mínima).
2. Implementar US1 (Phase 3) até T009.
3. Acrescentar Classe (T016) + uso inicial de `FilterToggleGroup` (T010) para ter demo forte de filtros principais.

### Incremental Delivery

1. US1 + Classe (subconjunto de US2) → demo inicial do painel.
2. Completar Subtipos (US2) → demo com dependência Classe/Subtipos.
3. Adicionar Liquidez, B3, Liquidados (US3) → demo completo de toggles.
4. Adicionar Vence até (US4) → demo com comportamento de meses reais.
5. Aplicar refinamentos de abreviação, reset e consistência (US5–US7) quando necessário.

### Notas de Build

- Em linha com o princípio IX da constituição, tasks **não** exigem executar `./gradlew` por padrão.
- Se desejar validar compilação em algum ponto, usar task opcional (por conta do utilizador):  
  `./gradlew :features:design-system-v2:compileKotlinJvm :features:composeApp:compileKotlinJvm`

