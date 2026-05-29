# Tasks: Cartões de resumo da carteira

**Input**: Design documents from `/specs/011-summary-cards/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/SummaryCardContract.md, quickstart.md

**Tests**: Não solicitados na spec — validação via `compileKotlinJvm` e previews Compose.

**Organization**: Tarefas agrupadas por user story (US1 P1, **US4 P2**, **US3 P2** — US3 **após** US4). US2 (status semânticos) **fora do escopo**.

**Previews (constituição)**: Todos os `@Preview` ficam em `SummaryCard.kt`, funções `private`, no mesmo ficheiro do composable — **sem** `SummaryCardPreview.kt`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode executar em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: User story da spec (`US1`, `US3`, `US4`)

## Path Conventions

- Módulo: `core/presentation/design-system-v2/`
- Pacote: `com.eferraz.design_system_v2`
- Gradle: `:features:design-system-v2`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Criar módulo KMP `design-system-v2` independente do v1.

- [X] T001 Registar `:features:design-system-v2` em `settings.gradle.kts` com `projectDir = File(settingsDir, "core/presentation/design-system-v2")`
- [X] T002 Criar `core/presentation/design-system-v2/build.gradle.kts` com plugins `foundation.project`, `foundation.library.comp`, `foundation.library.koin` e dependência `libs.compose.material.icons.extended` (sem `:features:design-system`)
- [X] T003 Criar estrutura de pastas `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/theme/` e `.../summary/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Tema M3 Expressive — bloqueia todas as user stories.

**⚠️ CRITICAL**: Nenhuma story até concluir esta fase.

- [X] T004 [P] Implementar `lightExpressiveColorScheme()` e `darkExpressiveColorScheme()` com `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/theme/ExpressiveColorScheme.kt`
- [X] T005 [P] Implementar `AppShapesV2` (extraSmall 4.dp … extraLarge 32.dp; `medium` = 12.dp) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/theme/Shapes.kt`
- [X] T006 [P] Implementar `AppTypographyV2` (`Typography()` Material3) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/theme/Typography.kt`
- [X] T007 Implementar `AppThemeV2` aplicando colorScheme + shapes + typography em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/theme/AppThemeV2.kt` (depende de T004–T006)
- [X] T008 Executar `./gradlew :features:design-system-v2:compileKotlinJvm` e corrigir erros de compilação do módulo vazio com tema

**Checkpoint**: `AppThemeV2 { }` compila e pode envolver previews.

---

## Phase 3: User Story 1 — Exibir cartão de resumo padronizado (Priority: P1) 🎯 MVP

**Goal**: `SummaryCard` reutilizável com hierarquia M3 Expressive (OutlinedCard, tipografia, cores Default).

**Independent Test**: Preview `SummaryCard_Default_preview` dentro de `AppThemeV2` — rótulo, valor, legenda e ícone visíveis; cartão não clicável e sem animação.

### Implementation for User Story 1

- [X] T009 [P] [US1] Criar `SummaryCardStatus` enum só com `Default` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCardStatus.kt`
- [X] T010 [P] [US1] Criar `SummaryCardColors` + `SummaryCardStatusColors.resolve` mapeando `surface`, `onSurface`, `onSurfaceVariant`, `outlineVariant`, `surfaceContainerHigh` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCardStatusColors.kt`
- [X] T011 [P] [US1] Criar `SummaryCardDefaults` (padding 16.dp, spacing 8.dp, badge 40.dp, icon 24.dp, `shapes.medium`) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCardDefaults.kt`
- [X] T012 [US1] Implementar `SummaryCard` com `OutlinedCard(enabled=false)`, `CardDefaults.outlinedCardColors()`, `CardDefaults.outlinedCardBorder()`, tipografia `labelSmall`/`titleLarge`/`bodySmall` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCard.kt`
- [X] T013 [US1] Adicionar `@Preview` `private fun SummaryCard_Default_preview()` no **final** de `SummaryCard.kt`, envolvido em `AppThemeV2`
- [X] T014 [US1] Validar FR-006/FR-007: sem `clickable`, sem animação; `OutlinedCard` com elevação 0 em `SummaryCard.kt`

**Checkpoint**: Um cartão Default renderizável no preview — **MVP entregue**.

---

## Phase 4: User Story 4 — Parametrizar conteúdo e status sem expor cores (Priority: P2)

**Goal**: Slots reservados (legenda/ícone), altura mínima uniforme, truncamento, ícone decorativo a11y.

**Independent Test**: Previews lado a lado (completo vs sem legenda vs sem ícone vs sem ambos) — mesma altura mínima e alinhamento; ícone ignorado por leitor de tela.

### Implementation for User Story 4

- [X] T015 [US4] Implementar slot fixo de legenda (`bodySmall.lineHeight`) com texto invisível ou `Spacer` quando `legend == null` em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCard.kt`
- [X] T016 [US4] Implementar slot fixo de badge 40.dp (`CircleShape`, `surfaceContainerHigh`) com ícone 24.dp ou placeholder vazio quando `icon == null` em `SummaryCard.kt`
- [X] T017 [US4] Calcular e aplicar `heightIn(min = …)` em `SummaryCardDefaults.kt` + `SummaryCard.kt` (FR-004a; não usar 110.dp do mock)
- [X] T018 [US4] Aplicar `maxLines = 1` e `TextOverflow.Ellipsis` em título, valor e legenda sem sobrepor badge em `SummaryCard.kt`
- [X] T019 [US4] Marcar ícone decorativo: `contentDescription = null` + `Modifier.semantics { invisibleToUser() }` (ou equivalente) em `SummaryCard.kt`
- [X] T020 [US4] Adicionar `@Preview` `private fun SummaryCard_OptionalSlots_preview()` em `SummaryCard.kt`: `Row` com cartão completo, sem legenda, sem ícone e sem ambos (mesmo título/valor) — validação visual de SC-005b (alinhamento vertical idêntico)

**Checkpoint**: FR-004b/004c/010/015 e SC-005b cobertos visualmente nos previews.

---

## Phase 5: User Story 3 — Validar oito modelos no catálogo (Priority: P2)

**⚠️ Bloqueio**: Iniciar **após** conclusão da Phase 4 (T015–T020). T021–T022 podem ser escritos em paralelo com US4, mas **T023 só após** slots estáveis.

**Goal**: Preview com 8 cartões estáticos (FR-008), todos `status = Default`, grade `spacedBy(8.dp)`.

**Independent Test**: Abrir `SummaryCard_Catalog8_preview` — exatamente 8 instâncias da tabela FR-008.

### Implementation for User Story 3

- [X] T021 [P] [US3] Criar dados estáticos FR-008 (título, valor, legenda) em `core/presentation/design-system-v2/src/commonMain/kotlin/com/eferraz/design_system_v2/summary/SummaryCardCatalog.kt`
- [X] T022 [P] [US3] Mapear ícones Material (`Icons.Outlined.*` extended) por cartão conforme `research.md` §7 em `SummaryCardCatalog.kt`
- [X] T023 [US3] Implementar `@Preview` `private fun SummaryCard_Catalog8_preview()` com `LazyVerticalGrid` 2 colunas e `spacedBy(8.dp)` em `SummaryCard.kt` (depende de T015–T020)

**Checkpoint**: SC-001a — oito modelos nomeados visíveis no catálogo.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificação final e conformidade com quickstart.

- [X] T024 Executar `./gradlew :features:design-system-v2:compileKotlinJvm` e resolver falhas
- [X] T025 [P] Adicionar previews light + dark em `SummaryCard.kt` (`@Preview(uiMode = …)` ou pares de previews) e validar **WCAG AA** (≥ 4,5:1) em título/valor/legenda sobre `surface` e ícone sobre `surfaceContainerHigh` nos dois temas — registar OK no checklist do quickstart (sem ferramenta automatizada nesta PR)
- [X] T026 Confirmar que **nenhum** ficheiro em `core/presentation/composeApp/.../history/` ou `AssetHistoryScreen` foi alterado (FR-012)
- [X] T027 Rever checklist em `specs/011-summary-cards/quickstart.md` e marcar itens concluídos na PR (incl. SC-005b e SC-006 se aplicável)
- [X] T028 [P] Atualizar `AGENTS.md` (módulo `:features:design-system-v2`, caminho `core/presentation/design-system-v2`) e confirmar que `.cursor/rules/specify-rules.mdc` aponta para `specs/011-summary-cards/plan.md` (Princípio VII)

---

## Dependencies & Execution Order

### Phase Dependencies

| Fase | Depende de |
|------|------------|
| Setup (1) | — |
| Foundational (2) | Setup |
| US1 (3) | Foundational |
| US4 (4) | US1 (esqueleto do cartão) |
| US3 (5) | US4 (**obrigatório** para T023); T021–T022 opcionalmente após T012 |
| Polish (6) | US3, US4 |

### User Story Dependencies

| Story | Depende de | Notas |
|-------|------------|-------|
| **US1** (P1) | Foundational | MVP independente |
| **US4** (P2) | US1 | Refina layout e a11y no mesmo composable |
| **US3** (P2) | **US4** (T015–T020 antes de T023) | T021–T022 (dados) podem anteceder T023; preview catálogo **não** antes de slots estáveis |
| **US2** | — | **Fora do escopo** — sem tarefas |

### Within Each User Story

- Tema antes do componente
- `Status` + `Colors` + `Defaults` antes de `SummaryCard`
- Layout base antes de slots reservados (US4)
- Componente antes do catálogo de 8 (US3)

### Parallel Opportunities

- **Fase 2**: T004, T005, T006 em paralelo
- **Fase 3**: T009, T010, T011 em paralelo → depois T012
- **Fase 5**: T021, T022 em paralelo (dados) → T023 **só após** T020
- **US3 vs US4**: **Não** paralelizar T023 com US4 incompleta; dados FR-008 (T021–T022) podem avançar durante US4

---

## Parallel Example: User Story 1

```bash
# Paralelo (ficheiros distintos):
T009 SummaryCardStatus.kt
T010 SummaryCardStatusColors.kt
T011 SummaryCardDefaults.kt

# Sequencial:
T012 SummaryCard.kt  # integra T009–T011
T013 SummaryCard_Default_preview
```

---

## Parallel Example: User Story 3

```bash
# Paralelo:
T021 SummaryCardCatalog.kt      # textos FR-008
T022 SummaryCardCatalog.kt      # ImageVectors

# Sequencial:
T023 SummaryCard_Catalog8_preview
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1: Setup  
2. Phase 2: Foundational (`AppThemeV2`)  
3. Phase 3: US1 (`SummaryCard` + preview Default)  
4. **STOP**: `./gradlew :features:design-system-v2:compileKotlinJvm` + preview Default  

### Incremental Delivery

1. Setup + Foundational → tema Expressive  
2. US1 → cartão único (**MVP**)  
3. US4 → slots + truncamento + a11y  
4. US3 → catálogo 8 cartões  
5. Polish → compilação + light/dark + WCAG + FR-012 + docs (T028)  

### Suggested MVP Scope

**Fases 1–3 (T001–T014)** — entrega mínima: um `SummaryCard` Default com M3 Expressive e preview isolado.

---

## Task Summary

| Métrica | Valor |
|---------|-------|
| **Total de tarefas** | 28 |
| **Setup** | 3 |
| **Foundational** | 5 |
| **US1** | 6 |
| **US4** | 6 |
| **US3** | 3 |
| **Polish** | 5 |
| **Fora do escopo (US2)** | 0 tarefas |

### Independent Test Criteria

| Story | Critério |
|-------|----------|
| US1 | `SummaryCard_Default_preview` — hierarquia M3, sem interação/animação |
| US4 | `SummaryCard_OptionalSlots_preview` — altura mínima uniforme, alinhamento SC-005b, truncamento, ícone invisível a SR |
| US3 | `SummaryCard_Catalog8_preview` — 8 cartões FR-008, todos `Default` |

### Format Validation

- [x] Todas as tarefas usam `- [ ] [TaskID] [P?] [Story?] Description with file path`
- [x] IDs T001–T028 sequenciais
- [x] Labels `[US1]`/`[US4]`/`[US3]` apenas em fases de user story
- [x] Caminhos de ficheiro explícitos em cada tarefa
