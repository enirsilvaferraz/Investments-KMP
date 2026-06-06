# Tasks: 024-portfolio-balancing — Balanceamento de carteira

**Input**: Design documents from `/specs/024-portfolio-balancing/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/PortfolioBalancingContract.md`, `quickstart.md`

**Tests**: Obrigatórios (FR-014, princípio V) — `PortfolioBalancingEngineTest`, `PortfolioBalancingPartitionTest`, `CalculatePortfolioBalancingUseCaseTest`. `./gradlew` opcional para agentes (princípio IX).

**Organization**: Fundação (modelos) → US1 Grupo Carteira Total → US2 grupos aninhados RF/RV → US3 botão histórico + log → Polish.

## Execução por ondas (plan.md)

| Onda | Tarefas | Quando |
|------|---------|--------|
| **Domínio base** | T003–T007 | US1 — modelos, catálogo G1, engine G1 |
| **US2 aninhado** | T008–T012 | Catálogo G2/G3, engine aninhado, partição |
| **US3 UI** | T013–T018 | UC autónomo, formatter, VM, ecrã |
| **Polish** | T019–T020 | Revisão contrato + quickstart |

### Prompts (Task tool)

**Domínio — US1 + US2** (T003–T012):
```text
Feature 024. Read specs/024-portfolio-balancing/contracts/PortfolioBalancingContract.md.
Create com.eferraz.usecases.balancing: PortfolioBalancingModels, PortfolioBalancingCatalog (FR-007 all 3 groups), PortfolioBalancingEngine (FR-004*), tests PortfolioBalancingEngineTest (FR-014) + PortfolioBalancingPartitionTest (FR-007a). No CreateHistoryUseCase. HASH11/IVVB11 constants.
```

**UC + Histórico** (T013–T018):
```text
Feature 024. CalculatePortfolioBalancingUseCase @Factory AppUseCase<Unit, PortfolioBalancingReport> — DateProvider + GetHoldingHistoriesUseCase only. FormatPortfolioBalancingReport. HistoryViewModel intent CalculatePortfolioBalancing → useCase(Unit) + println; AssetHistoryScreen IconButton Balance after B3 import, no loading state. CalculatePortfolioBalancingUseCaseTest with mocks.
```

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Paralelizável (ficheiros diferentes, sem dependência de tarefas incompletas da mesma trilha)
- **[Story]**: US1, US2, US3 conforme `spec.md`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar branch, contrato e dependências existentes.

- [X] T001 Confirmar branch `024-portfolio-balancing` e ler `specs/024-portfolio-balancing/contracts/PortfolioBalancingContract.md`
- [X] T002 [P] Verificar pré-requisitos em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/DateProvider.kt`, `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/cruds/GetHoldingHistoriesUseCase.kt` e taxonomia em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/assets/` — **critério de conclusão**: os três ficheiros existem e as assinaturas são compatíveis com as dependências do plano; se algum estiver ausente ou incompatível, reportar e bloquear T003

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Tipos de domínio partilhados por todas as user stories. **Bloqueia US1–US3.**

**Checkpoint**: `PortfolioBalancingModels.kt` compila com `TargetWeight`, ids, `PortfolioBalancingReport` / `ReportLine`.

- [X] T003 Criar `PortfolioBalancingModels.kt` com `TargetWeight`, `BalancingComponentId`, `BalancingGroupId`, `BalancingComponent`, `BalancingGroup`, `PortfolioBalancingReportLine`, `PortfolioBalancingReport` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingModels.kt`

**Checkpoint fundacional**: US1 pode iniciar (T004+).

---

## Phase 3: User Story 1 — Calcular desvios da alocação alvo da Carteira Total (Priority: P1) 🎯 MVP

**Goal**: Grupo 1 (5 componentes) com actual, peso alvo, ideal e desvio; partição sem double-count; previdência dinâmica; peso zero; exclusão de liquidados.

**Independent Test**: Fixtures sintéticas → `PortfolioBalancingEngine.calculate` devolve 5 linhas do Grupo 1 coerentes com FR-004a/005/006; soma actual = total carteira.

### Tests for User Story 1

- [X] T004 [P] [US1] Criar `PortfolioBalancingEngineTest.kt` com GIVEN_WHEN_THEN: peso fixo 50% / R$ 100k; previdência dinâmica desvio zero; Demais 0% ideal zero; liquidados excluídos; soma G1 = total em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt` — **depende de T003** (testes falham até T006)

### Implementation for User Story 1

- [X] T005 [US1] Implementar Grupo 1 em `PortfolioBalancingCatalog.kt` (RF 50%, RV 40% exc. HASH11, Previdência dinâmica, Cripto HASH11 1%, Demais 0% fallback) com predicados mutuamente exclusivos em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt` — **depende de T003**
- [X] T006 [US1] Implementar `PortfolioBalancingEngine.calculate` para Grupo 1 (património activo, ideal fixo/dinâmico/zero, desvio) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt` — **depende de T005**
- [X] T007 [US1] Fazer passar cenários US1 em `PortfolioBalancingEngineTest.kt` — **depende de T004, T006**

**Checkpoint**: US1 testável via engine + catálogo G1; MVP de cálculo global.

---

## Phase 4: User Story 2 — Balanceamento aninhado RF e RV (Priority: P1)

**Goal**: Grupos 2 e 3 com ideal aninhado sobre pai no Grupo 1 (FR-004b); HASH11 só em Cripto; partição FR-007a nos 3 grupos.

**Independent Test**: RF actual zero com ideal Pós-fixados = 33,33% × ideal RF; HASH11 fora do Grupo 3; `PortfolioBalancingPartitionTest` sem overlap/lacuna.

### Tests for User Story 2

- [X] T008 [P] [US2] Acrescentar em `PortfolioBalancingEngineTest.kt`: RF actual zero + ideal aninhado; RF sobre-alocado ideal ainda do pai; IVVB11/FII/HASH11 classificação em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt` — **depende de T007**
- [X] T009 [P] [US2] Criar `PortfolioBalancingPartitionTest.kt` — cada posição activa em exactamente um componente por grupo (FR-007a) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingPartitionTest.kt` — **depende de T005**

### Implementation for User Story 2

- [X] T010 [US2] Acrescentar Grupos 2 (indexadores RF) e 3 (RV sem HASH11) ao catálogo em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt` — **depende de T005**
- [X] T011 [US2] Estender `PortfolioBalancingEngine` com ideais aninhados (FR-004b/004c), carteira zero (FR-013) e relatório completo 11 linhas em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt` — **depende de T006, T010**
- [X] T012 [US2] Fazer passar testes US2 e partição em `PortfolioBalancingEngineTest.kt` e `PortfolioBalancingPartitionTest.kt` — **depende de T008, T009, T011**

**Checkpoint**: Motor completo (3 grupos, 11 componentes); domínio pronto para UC.

---

## Phase 5: User Story 3 — Acionar cálculo no histórico e ler log (Priority: P2)

**Goal**: Botão Balance ao lado do import B3; UC autónomo `Unit`; tabela formatada no log; botão sempre activo; erro visível no log.

**Independent Test**: Tocar botão no histórico → consola com tabela 3 secções, 5 colunas, sem coluna de regra; toques repetidos permitidos.

### Tests for User Story 3

- [X] T013 [P] [US3] Criar `CalculatePortfolioBalancingUseCaseTest.kt` — mock `DateProvider` + `GetHoldingHistoriesUseCase`; verificar orquestração sem `CreateHistoryUseCase`; incluir cenário de **falha do repositório** (US3 AC5 / FR-014): `GetHoldingHistoriesUseCase` lança excepção → UC propaga ou captura e devolve resultado de erro compreensível em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/CalculatePortfolioBalancingUseCaseTest.kt` — **depende de T011**

### Implementation for User Story 3

- [X] T014 [US3] Implementar `CalculatePortfolioBalancingUseCase` (`AppUseCase<Unit, PortfolioBalancingReport>`, `DateProvider` + `GetHoldingHistoriesUseCase` only) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/CalculatePortfolioBalancingUseCase.kt` — **depende de T011**
- [X] T015 [P] [US3] Implementar `formatPortfolioBalancingReport` (colunas nome/actual/peso/ideal/desvio, separadores por grupo) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/FormatPortfolioBalancingReport.kt` — **depende de T003**
- [X] T016 [US3] Adicionar `CalculatePortfolioBalancing` intent, inject UC, handler `calculatePortfolioBalancing()` com `useCase(Unit)` + `println(format(...))` / mensagem de erro em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/HistoryViewModel.kt` — **depende de T014, T015**
- [X] T017 [US3] Adicionar `IconButton` Balance após import B3 em `Actions` (sem loading state), wire `onBalancingClick`; usar ícone `Icons.Outlined.AccountBalance` (ou equivalente disponível no design-system-v2) em `core/presentation/composeApp/src/commonMain/kotlin/com/eferraz/presentation/features/history/AssetHistoryScreen.kt` — **depende de T016**
- [X] T018 [US3] Fazer passar `CalculatePortfolioBalancingUseCaseTest.kt` (inclui cenário de erro FR-014/US3-AC5) — **depende de T013, T014**

**Checkpoint**: Feature completa end-to-end (log no histórico).

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validação final e conformidade.

- [X] T019 [P] Rever implementação contra `specs/024-portfolio-balancing/contracts/PortfolioBalancingContract.md` (sem `CreateHistoryUseCase`, período `DateProvider`, sem `isBalancing`); **verificar SC-004**: adicionar um segundo componente de teste ao `PortfolioBalancingCatalog` num comentário ou num fixture de extensibilidade e confirmar que `PortfolioBalancingEngine` produz o resultado correcto **sem alterar nenhuma linha da classe engine** — evidência de aberto/fechado (SOLID)
- [X] T020 Executar cenários de `specs/024-portfolio-balancing/quickstart.md` (Gradle `./gradlew :domain:usecases:jvmTest --tests "*PortfolioBalancing*"` sob pedido do utilizador)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências
- **Foundational (Phase 2)**: Depende de Phase 1 — **bloqueia US1–US3**
- **US1 (Phase 3)**: Depende de Phase 2
- **US2 (Phase 4)**: Depende de US1 (catálogo G1 + engine base)
- **US3 (Phase 5)**: Depende de US2 (engine completo)
- **Polish (Phase 6)**: Depende de US3

### User Story Dependencies

```text
Phase 2 (Models) → US1 (G1) → US2 (G2/G3 + partição) → US3 (UC + UI)
```

- **US1**: Independente após fundação — MVP
- **US2**: Estende catálogo/engine de US1; testável via engine sem UI
- **US3**: Depende do engine; entrega fluxo utilizador

### Parallel Opportunities

| Grupo | Tarefas paralelas |
|-------|-------------------|
| Setup | T002 |
| US1 | T004 (testes) em paralelo com T005 após T003 |
| US2 | T008, T009 em paralelo após T007 |
| US3 | T015 após T003 (só precisa dos modelos); T013 após T011; ambas concluem antes de T016 |
| Polish | T019 |

### Parallel Example: User Story 2

```bash
# Após T007, em paralelo:
T008 — PortfolioBalancingEngineTest cenários aninhados
T009 — PortfolioBalancingPartitionTest
# Depois sequencial:
T010 → T011 → T012
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Phase 1–2: Setup + Models
2. Phase 3: US1 (catálogo G1 + engine + testes)
3. **STOP**: Validar engine com fixtures — desvios da Carteira Total correctos

### Incremental Delivery

1. US1 → relatório Grupo 1 correcto (domínio)
2. US2 → 11 componentes + ideais aninhados + partição
3. US3 → botão histórico + log formatado

### Parallel Team Strategy

- Dev A: US1 (T004–T007)
- Após US1: Dev A US2 engine; Dev B US3 formatter + UC (T013–T015) quando T011 pronto
- Dev B: UI (T016–T017) após T014

---

## Notes

- **Escopo mínimo** (princípio X): sem ecrã dedicado, sem persistência de pesos, sem `CreateHistoryUseCase`, sem `FilterHoldingHistoryUseCase`.
- Período: `DateProvider.getCurrentYearMonth()` — não o selector do histórico (ver `research.md` R5).
- `[P]` = ficheiros diferentes; `PortfolioBalancingEngine.kt` e `PortfolioBalancingCatalog.kt` são sequenciais dentro da mesma story.
- Commit após cada checkpoint; `./gradlew` só sob pedido (princípio IX).
