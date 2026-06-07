---
description: "Task list for feature 025-balanceable-groups"
---

# Tasks: Grupos balanceáveis e não balanceáveis

**Input**: Design documents from `/specs/025-balanceable-groups/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/PortfolioBalancingContract.md, quickstart.md

**Tests**: Obrigatórios em `:domain:usecases` (princípio V da constitution; FR-013). Execução `./gradlew` só sob pedido (princípio IX).

**Organization**: Tarefas agrupadas por user story para implementação e validação independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências em tarefas incompletas)
- **[Story]**: User story da spec (US1–US4)
- Caminhos absolutos ao módulo `balancing/` em `core/domain/usecases/`

## Path Conventions

- **Domínio**: `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/`
- **Testes**: `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar contexto e inventário do refactor sobre a feature 024

- [X] T001 Revisar contrato e regras R1–R7 em `specs/025-balanceable-groups/contracts/PortfolioBalancingContract.md` e `specs/025-balanceable-groups/plan.md`
- [X] T002 [P] Auditar ficheiros actuais em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/` e testes em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/` para mapear remoções (BalancingGroup, BalancingWeightCalculator, peso normalizado)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Tipos e utilitários que bloqueiam todas as user stories

**⚠️ CRITICAL**: Nenhuma user story pode avançar até esta fase estar completa

- [X] T003 Criar `BalancingTreeNode.kt` com `id`, `displayName`, `targetWeight`, `matches`, `children` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingTreeNode.kt`
- [X] T004 [P] Criar `NonBalanceableAssetList.kt` como ponto único de configuração explícita em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/NonBalanceableAssetList.kt`
- [X] T005 [P] Adicionar `isNonBalanceable` e `isBalanceable` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingMatchers.kt` consultando `NonBalanceableAssetList`
- [X] T006 Criar `BalancingIdealCalculator.kt` com `computeIdeal(weight, actual, referenceBase)` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingIdealCalculator.kt`
- [X] T007 Refactor `PortfolioBalancingModels.kt` para `PortfolioBalancingReportSection` + `PortfolioBalancingReportLine` sem campos de peso normalizado em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingModels.kt`
- [X] T008 Actualizar ids de nó da árvore em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingGroupId.kt` (root, não balanceável, balanceável, fallbacks)

**Checkpoint**: Fundação pronta — implementação por user story pode começar

---

## Phase 3: User Story 1 - Separar ativos balanceáveis dos não balanceáveis (Priority: P1) 🎯 MVP (classificação)

**Goal**: Cada posição activa pertence a exactamente um universo (balanceável ou não balanceável) via lista explícita no catálogo

**Independent Test**: Carteira com RF, RV, FGTS e previdência — FGTS/previdência só no universo não balanceável; RF/RV só no balanceável; sem sobreposição

### Implementation for User Story 1

- [X] T009 [US1] Definir `PortfolioBalancingCatalog.root` (`carteiraTotalNode`) com filhos `nonBalanceableNode` e `balanceableNode` (`TargetWeight.Dynamic` nos filhos da raiz, FR-010) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt`
- [X] T010 [P] [US1] Adicionar filhos terminais `previdenciaNode`, `fgtsNode`, `demaisNonBalanceableNode` (Dynamic) sob `nonBalanceableNode` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt`
- [X] T011 [P] [US1] Registar entradas iniciais (previdência, FGTS) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/NonBalanceableAssetList.kt`
- [X] T012 [US1] Implementar `BalancingUniverseIndex.kt` com R1 (`byNodeId[filho] = byNodeId[pai].filter(matches)`) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingUniverseIndex.kt`
- [X] T013 [US1] Refactor `PortfolioBalancingCatalogValidator.kt` para `validateNode` recursivo com R7 (partição), FR-011 (Σ Fixed+Zero = 100% ± 0,01% por contentor sem Dynamic nos filhos directos) e FR-006 (rejeitar Dynamic na subárvore balanceável) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalogValidator.kt`

### Tests for User Story 1

- [X] T014 [P] [US1] Actualizar `PortfolioBalancingCatalogValidatorTest.kt` para validação recursiva da árvore em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalogValidatorTest.kt`
- [X] T015 [P] [US1] Actualizar `PortfolioBalancingPartitionTest.kt` para exclusividade balanceável/não balanceável em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingPartitionTest.kt`
- [X] T016 [P] [US1] Criar `BalancingUniverseIndexTest.kt` verificando filtragem progressiva R1 em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/BalancingUniverseIndexTest.kt`
- [X] T041 [P] [US1] Adicionar teste activos não balanceáveis ausentes da subárvore RF (FR-008, FR-014) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingPartitionTest.kt`

**Checkpoint**: Classificação e partição de universos correctas; omissão na lista = balanceável

---

## Phase 4: User Story 2 - Grupo balanceável com metas fixas sobre a base balanceável (Priority: P1)

**Goal**: Grupo balanceável contém só activos balanceáveis; pesos fixos aplicam-se sobre `balanceableBase`, não sobre o total da carteira

**Independent Test**: Total R$ 100k, R$ 20k não balanceáveis, R$ 80k balanceáveis — ideais RF/RV usam base R$ 80k

### Implementation for User Story 2

- [X] T017 [P] [US2] Adicionar filhos de `balanceableNode` (cripto, RF, RV, `demaisInvestimentosNode` com peso Zero) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt`
- [X] T018 [P] [US2] Adicionar subárvore RF (pré, pós, IPCA) e RV (acções, FIIs, internacional) como filhos terminais em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt`
- [X] T019 [US2] Implementar `computeAndEmitNode` com bootstrap `idealByNodeId[root.id] = totalPortfolioValue` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt`
- [X] T020 [US2] Propagar `referenceBase = idealByNodeId[pai.id]` para filhos de `balanceableNode` (R3–R4) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt`

### Tests for User Story 2

- [X] T021 [P] [US2] Adicionar teste de propagação `ideal(rf) = balanceableBase × pesoRF` em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`
- [X] T022 [P] [US2] Adicionar teste carteira só balanceável equivale ao grupo 1 anterior em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`
- [X] T042 [P] [US2] Adicionar teste validação FR-011 soma pesos fixos do grupo balanceável em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalogValidatorTest.kt`

**Checkpoint**: Metas fixas e fallback «Demais investimentos» (peso zero) sobre base balanceável

---

## Phase 5: User Story 3 - Grupo não balanceável com peso dinâmico (Priority: P1)

**Goal**: Componentes dinâmicos mostram ideal = actual e desvio zero; fallback «Demais não balanceáveis» cobre o complemento

**Independent Test**: Previdência R$ 15k + FGTS R$ 5k em total R$ 100k — pesos dinâmicos 15% e 5%, desvios zero

### Implementation for User Story 3

- [X] T023 [US3] Confirmar pesos Dynamic/Fixed/Zero conforme FR-006 nos nós não balanceáveis (detalhe) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingCatalog.kt`
- [X] T024 [US3] Garantir `computeIdeal` retorna `actual` para `TargetWeight.Dynamic` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/BalancingIdealCalculator.kt`
- [X] T025 [US3] Aplicar R5 `deviation = actual − ideal` (zero em dinâmicos) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt`

### Tests for User Story 3

- [X] T026 [P] [US3] Adicionar testes ideal=actual e desvio=0 para componentes Dynamic em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`
- [X] T027 [P] [US3] Adicionar teste fallback `demaisNonBalanceableNode` para posições não listadas em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingPartitionTest.kt`

**Checkpoint**: Universo não balanceável visível com peso dinâmico e partição exaustiva

---

## Phase 6: User Story 4 - Ordem de apresentação no log e grupo de proporção (Priority: P2)

**Goal**: Log segue pre-order DFS da árvore; 1.ª secção = Carteira Total (Balanceável + Não balanceável); cinco colunas sem peso normalizado

**Independent Test**: Log começa por Carteira Total com duas linhas que somam o total; segue não balanceável → balanceável → aninhados (RF, RV, …)

### Implementation for User Story 4

- [X] T028 [US4] Completar emissão R6 (secção por contentor, linhas = filhos directos, recursão DFS) em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngine.kt`
- [X] T029 [US4] Refactor `FormatPortfolioBalancingReport.kt` para `formatTreeReport` iterando `report.sections` sem reordenar em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/FormatPortfolioBalancingReport.kt`
- [X] T030 [US4] Remover coluna peso normalizado e campos associados de `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/FormatPortfolioBalancingReport.kt`
- [X] T031 [US4] Corrigir sinal do desvio para `actual − ideal` em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/FormatPortfolioBalancingReport.kt`
- [X] T032 [US4] Ligar `CalculatePortfolioBalancingUseCase.kt` ao novo motor e modelo de relatório em `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/CalculatePortfolioBalancingUseCase.kt`

### Tests for User Story 4

- [X] T033 [P] [US4] Adicionar teste ordem `sections` = pre-order da árvore (cenário Q2) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`
- [X] T034 [P] [US4] Adicionar teste 1.ª secção Carteira Total com 2 linhas + Total (cenário Q1) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`
- [X] T035 [P] [US4] Actualizar `CalculatePortfolioBalancingUseCaseTest.kt` para colunas nome/actual/peso/ideal/desvio em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/CalculatePortfolioBalancingUseCaseTest.kt`
- [X] T043 [P] [US4] Adicionar teste carteira total zero com relatório estrutural completo (FR-012) em `core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingEngineTest.kt`

**Checkpoint**: Relatório completo no log com ordem da árvore e formato de 5 colunas

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Remover legado 024, alinhar documentação e fechar escopo

- [X] T036 Remover `BalancingWeightCalculator.kt` e `BalancingGroupDefaults.kt` de `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/`
- [X] T037 Remover tipos `BalancingGroup` e `BalancingComponent` de `core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/balancing/PortfolioBalancingModels.kt` após migração completa
- [X] T038 [P] Rever coerência final spec/plan/tasks após implementação em `specs/025-balanceable-groups/spec.md` e `specs/025-balanceable-groups/plan.md`
- [X] T039 [P] Actualizar cenários e checklist em `specs/025-balanceable-groups/quickstart.md`
- [X] T040 Validar suite de testes conforme `specs/025-balanceable-groups/quickstart.md` (executar `./gradlew :domain:usecases:jvmTest --tests "com.eferraz.usecases.balancing.*"` apenas sob pedido)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode começar de imediato
- **Foundational (Phase 2)**: Depende de Setup — **bloqueia** todas as user stories
- **US1 (Phase 3)**: Depende de Foundational
- **US2 (Phase 4)**: Depende de US1 (catálogo + índice de universos)
- **US3 (Phase 5)**: Depende de US1; pode avançar em paralelo com US2 após T012
- **US4 (Phase 6)**: Depende de US2 e US3 (motor e catálogo completos)
- **Polish (Phase 7)**: Depende de US4

### User Story Dependencies

```text
Foundational → US1 → US2 ─┐
              └→ US3 ─────┼→ US4 → Polish
```

- **US1**: Classificação e índice — base para US2 e US3
- **US2** e **US3**: Podem progredir em paralelo após T012–T013 (mesmo ficheiro `PortfolioBalancingEngine.kt` — coordenar T019–T025)
- **US4**: Integra motor + formatter; requer cálculo completo

### Within Each User Story

- Catálogo / índice antes do motor
- Motor antes do formatter
- Testes após implementação da story (princípio V)

### Parallel Opportunities

- T004, T005 em Foundational
- T010, T011 em US1
- T014, T015, T016, T041 em US1 (testes)
- T017, T018 em US2 (catálogo)
- T021, T022, T042 em US2 (testes)
- T026, T027 em US3 (testes)
- T033, T034, T035, T043 em US4 (testes)
- T038, T039 em Polish

---

## Parallel Example: User Story 1

```bash
# Catálogo em paralelo (ficheiros diferentes):
T010: filhos terminais não balanceáveis em PortfolioBalancingCatalog.kt
T011: entradas NonBalanceableAssetList.kt

# Testes em paralelo após T012–T013:
T014: PortfolioBalancingCatalogValidatorTest.kt
T015: PortfolioBalancingPartitionTest.kt
T016: BalancingUniverseIndexTest.kt
```

---

## Implementation Strategy

### MVP First (US1 + fundação)

1. Completar Phase 1 + Phase 2
2. Completar Phase 3 (US1) — classificação e partição
3. **Parar e validar**: testes T014–T016 passam; universos exclusivos

### Incremental Delivery

1. Setup + Foundational → tipos prontos
2. US1 → classificação correcta (MVP mínimo de valor)
3. US2 → metas sobre base balanceável
4. US3 → visibilidade não balanceável com dinâmico
5. US4 → log DFS e formato final
6. Polish → remover legado e alinhar spec

### Suggested MVP Scope

**US1** (Phases 1–3): separação balanceável/não balanceável com catálogo em árvore e validação de partição — desbloqueia o resto sem distorcer metas.

### Ondas do plano (referência)

| Onda plano | Tarefas |
|------------|---------|
| Onda 1 | T003–T013, T017–T018 |
| Onda 2 | T012, T019–T025, T028–T032 |
| Onda 3 | T014–T016, T021–T027, T033–T035, T041–T043, T039–T040 |

---

## Notes

- **Escopo mínimo** (princípio X): sem UI de classificação, persistência, nem ecrã dedicado (fora de âmbito spec)
- **Sem ramos por `node.id`** no motor — apenas R1–R7 (contract)
- `PortfolioBalancingEngine.kt` é ponto de contenção entre US2–US4; evitar edições paralelas no mesmo ficheiro
- Commit após cada fase ou grupo lógico de tarefas
- Spec alinhada (análise 2026-06-07): 1.ª secção = **Carteira Total** (raiz); ver FR-009/FR-010 em `spec.md`
- SC-007 (performance < 5s): herança feature 024 — sem tarefa de build; validação manual sob pedido
