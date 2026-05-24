# Tasks: Destaque visual de investimentos liquidados no Histórico

**Input**: Design documents from `/specs/005-liquidated-history-style/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [research.md](./research.md), [contracts/LiquidatedHistoryRowContract.md](./contracts/LiquidatedHistoryRowContract.md), [quickstart.md](./quickstart.md)

**Tests**: Incluídos onde a constitution (princípio V) e o plano exigem — `HoldingHistoryViewTest` em `:domain:usecases`; sem TDD obrigatório na spec.

**Organization**: Tarefas agrupadas por user story; batches `[P]` em ficheiros distintos quando não há dependência de ordem.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem depender de tarefas incompletas do mesmo batch)
- **[Story]**: US1, US2 — apenas em fases de user story
- Caminhos a partir da raiz do monorepo: prefixo `core/...`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar contexto e alinhamento antes de tocar código.

- [X] T001 Confirmar branch `005-liquidated-history-style` e ler [plan.md](./plan.md), [spec.md](./spec.md) e [contracts/LiquidatedHistoryRowContract.md](./contracts/LiquidatedHistoryRowContract.md)

**Checkpoint**: Branch e contrato compreendidos — iniciar Phase 2.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Regra `isLiquidated` no domínio e cor muted centralizada — **bloqueia US1 e US2**.

**⚠️ CRITICAL**: Nenhuma alteração em `AssetHistoryScreen` até T004 concluída.

### Batch A — domínio (sequencial T002 → T003)

- [X] T002 Adicionar `public val isLiquidated: Boolean` com `get() = currentValue == 0.0` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/entities/HoldingHistoryView.kt` (fora do construtor primário; construtor `HistoryTableData` inalterado)

- [X] T003 Criar `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/entities/HoldingHistoryViewTest.kt` com casos contratuais: `currentValue == 0.0` → `true`; `100.0` → `false`; `-1.0` → `false` (nomes `GIVEN_…_WHEN_…_THEN_…`, pacote `com.eferraz.usecases.entities`); opcional: `GIVEN currentValue is negative zero THEN isLiquidated is true` (alinhar a `data-model.md`)

### Batch B — paralelo com T003 após T002

- [X] T004 [P] Adicionar `@Composable public fun historyMutedTextColor(): Color` (`Color.Gray.copy(alpha = 0.5f)`) em `core/presentation/design-system/src/commonMain/kotlin/com/eferraz/design_system/theme/Theme.kt`

**Checkpoint**: `./gradlew :domain:usecases:jvmTest --tests "com.eferraz.usecases.entities.HoldingHistoryViewTest"` verde — iniciar US1.

---

## Phase 3: User Story 1 — Reconhecer posições liquidadas na tabela de Histórico (Priority: P1) 🎯 MVP

**Goal**: Linhas com `valor atual == 0` exibem cinza muted nas colunas gerais; Valorização e Transações mantêm cores semânticas; ícones e tooltips inalterados.

**Independent Test**: Comparar linha com `currentValue == 0` vs linha ativa no ecrã Posicionamento no Período — cinza em Corretora/nome/observação/valores gerais; verde/vermelho preservados em Valorização/Transações quando aplicável; ícones com cores habituais (spec US1).

### Batch C — componente de entrada (bloqueia Valor Atual)

- [X] T005 [US1] Adicionar parâmetro opcional `textColor: Color? = null` na overload `TableInputMoney` privada em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/design_system/components/inputs/TableInputMoney.kt` — usar `textColor ?: (if (enabled) onSurface else onSurfaceVariant)` no `textStyle` do `BasicTextField`; propagar na overload `internal` pública

> **Nota**: `AssetHistoryScreen` importa `com.eferraz.presentation.design_system.components.inputs.TableInputMoney` (composeApp), não o homónimo em `:presentation:design-system`.

### Batch D — tabela de histórico (sequencial no mesmo ficheiro)

- [X] T006 [US1] Importar `historyMutedTextColor` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` e adicionar `@Composable` helper `historyRowTextColor(row: HoldingHistoryView): Color` que devolve `historyMutedTextColor()` se `row.isLiquidated`, senão `LocalContentColor.current`

- [X] T007 [US1] Adicionar `content` com `Text(..., color = historyRowTextColor(row))` nas colunas **Corretora**, **Display Name** e **Observação** em `AssetHistoryScreen.kt` (substituir render default sem cor condicional)

- [X] T008 [US1] Atualizar coluna **Valor Anterior** em `AssetHistoryScreen.kt`: `Text(it.previousValue.currencyFormat(), color = …)` com muted quando `row.isLiquidated`

- [X] T009 [US1] Na coluna **Valor Atual** em `AssetHistoryScreen.kt`, passar `textColor = historyMutedTextColor()` quando `rowData.isLiquidated` em `TableInputMoney` (manter `enabled = rowData.isCurrentValueEnabled()`)

- [X] T010 [US1] Revisar em `AssetHistoryScreen.kt`: (a) blocos `when` de **Transações** e **Valorização** **não** usam `isLiquidated`; (b) colunas de ícones e **B3** inalteradas; (c) **não** alterar `core/presentation/naming/src/commonMain/kotlin/com/eferraz/naming/B3IdentifierStatusCell.kt` (tooltips FR-003a)

**Checkpoint**: US1 testável manualmente (cenários A/C do [quickstart.md](./quickstart.md)); `jvmTest` HoldingHistoryView verde.

---

## Phase 4: User Story 2 — Consistência entre tipos de investimento no Histórico (Priority: P2)

**Goal**: Mesma regra visual para RF, RV e fundo quando `currentValue == 0` no período.

**Independent Test**: No mesmo período, linha RF e linha RV com valor atual 0 — ambas muted nas colunas gerais; RF com B3 não informado mantém ícone amarelo (spec US2).

> **Nota**: A UI de US2 partilha `AssetHistoryScreen` com US1; esta fase cobre **testes de domínio por categoria** e **validação manual**.

### Batch E — paralelo

- [X] T011 [P] [US2] Estender `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/entities/HoldingHistoryViewTest.kt` com `HoldingHistoryView` construído a partir de `FixedIncomeHistoryTableData`, `VariableIncomeHistoryTableData` e `InvestmentFundHistoryTableData` com `currentValue = 0.0` → `isLiquidated == true` (dados inline, sem `TestDataFactory` novo)

- [X] T012 [P] [US2] Executar checklist US2 em [quickstart.md](./quickstart.md): RF/RV/fundo com valor atual 0; RF com B3 não informado (ícone amarelo); **hover no ícone B3 em linha liquidada** — texto do tooltip com estilo padrão (não muted) — validação manual documentada em quickstart.md (não executável em CI)

**Checkpoint**: US2 validado — tipos de investimento sem exceção visual na tabela.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: DRY, build multi-módulo e regressão.

### Batch F — paralelo

- [X] T012b [P] Confirmar marcador SPECKIT em `.cursor/rules/specify-rules.mdc` aponta para `specs/005-liquidated-history-style/plan.md` (atualizar só se desatualizado)

- [X] T013 [P] Executar `./gradlew :domain:usecases:jvmTest --tests "com.eferraz.usecases.entities.HoldingHistoryViewTest"` e `./gradlew :features:composeApp:compileKotlinJvm`

- [X] T014 [P] Substituir literais `Color.Gray.copy(alpha = .5f)` em colunas **Valorização** e **Transações** (“Adicionar”) por `historyMutedTextColor()` em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` (DRY com tom de referência)

- [X] T015 Executar [quickstart.md](./quickstart.md) completo: cenários A/B/C; transição 0↔positivo editando **Valor Atual** e confirmando atualização na **mesma sessão** via `HistoryIntent.UpdateEntryValue` → `HistoryViewModel.updateEntryValue` → `state.tableData` recomposto (FR-005, SC-004); `Summary` inalterado; opcional SC-003 (identificação visual < 5 s em até 20 linhas) — validação manual requerida pelo utilizador conforme quickstart.md

**Checkpoint**: Feature pronta para `/speckit-analyze` e `/speckit-implement` follow-up.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — início imediato
- **Foundational (Phase 2)**: Depende de Phase 1 — **bloqueia** US1 e US2
- **User Story 1 (Phase 3)**: Depende de Phase 2 (T002–T004)
- **User Story 2 (Phase 4)**: Depende de Phase 3 para validação UI; T011 pode começar após T003
- **Polish (Phase 5)**: Depende de Phase 3 e 4

### User Story Dependencies

- **US1 (P1)**: Após Foundational — sem dependência de US2
- **US2 (P2)**: Reutiliza implementação US1; testes T011 paralelos após T003; validação manual T012 após T007–T010

### Within Each User Story

- Domínio (`isLiquidated`) antes da UI
- `historyMutedTextColor()` antes de `AssetHistoryScreen`
- `TableInputMoney.textColor` antes da coluna Valor Atual
- Colunas gerais antes da revisão de precedência (T010)

### Parallel Opportunities

| Batch | Tarefas paralelas | Condição |
|-------|-------------------|----------|
| Phase 2 | T004 ∥ T003 | Após T002 |
| Phase 3 | — | T007–T009 no mesmo ficheiro — sequencial |
| Phase 4 | T011 ∥ T012 | Após T003 / após UI US1 |
| Phase 5 | T013 ∥ T014 | Após Phase 3 |

---

## Parallel Example: User Story 1

```bash
# Após Phase 2, sequência recomendada no ecrã:
# 1) T005 TableInputMoney.kt
# 2) T006–T010 AssetHistoryScreen.kt (uma PR ou commits lógicos)
```

---

## Parallel Example: User Story 2

```bash
# Em paralelo após HoldingHistoryViewTest base (T003):
Task T011: HoldingHistoryViewTest.kt — RF/RV/Fundo
Task T012: quickstart.md checklist manual (após T010)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: Setup (T001)
2. Phase 2: Foundational (T002–T004) — **obrigatório**
3. Phase 3: User Story 1 (T005–T010)
4. **STOP and VALIDATE**: quickstart cenários A e C + `jvmTest`
5. Demo se pronto

### Incremental Delivery

1. Setup + Foundational → regra e cor prontas
2. US1 → MVP visual na tabela
3. US2 → confiança multi-categoria + testes
4. Polish → DRY + build + checklist completo

### Suggested Command Order (from plan)

1. `HoldingHistoryView.isLiquidated` + `HoldingHistoryViewTest`
2. `historyMutedTextColor()` em design-system
3. `TableInputMoney` (composeApp) + `AssetHistoryScreen`
4. Quickstart manual

---

## Notes

- **Sem migração Room** nem alteração em `:domain:entity`
- **Filtro “só liquidados”**: fora do escopo — usar `filter { it.isLiquidated }` em feature futura
- **Escopo FR-007**: apenas linhas da tabela em `AssetHistoryScreen`; `Summary` inalterado
- Pacote de teste: `com.eferraz.usecases.entities` (espelha `HoldingHistoryView.kt`)
