# Research: Filtragem de histórico por critérios unificados (incl. corretora)

**Feature**: `018-holding-history-filter` | **Date**: 2026-06-03

## Contexto

A feature **015** introduziu `WalletHistoryFilterCriteria`, `WalletHistoryFilterCandidate` e `matchesWalletHistoryFilter`, aplicados em `GetHistoryTableDataUseCase` sobre `HoldingHistoryResult.toWalletHistoryFilterCandidate()`. A corretora permaneceu num canal paralelo (`Param.brokerage` + `.filter { holding.brokerage == … }`). A spec **018** exige um caso de uso dedicado sobre `HoldingHistoryEntry`, corretora no mesmo critério, migração completa do pipeline da tabela e reset de corretora ao mudar período — **sem** alteração visual.

---

## Decisão 1: Caso de uso dedicado (não só função pura)

**Decision**: `FilterHoldingHistoryEntriesUseCase` em `:domain:usecases` (`AppUseCase<Param, List<HoldingHistoryEntry>>`), delegando a `matchesWalletHistoryFilter` após `HoldingHistoryEntry.toWalletHistoryFilterCandidate()`.

**Rationale**: FR-001 pede explicitamente um caso de uso reutilizável; alinha ao princípio V (testes no módulo usecases) e permite que `GetHistoryTableDataUseCase` injete o filtro sem duplicar loop.

**Alternatives considered**:
- **Só função top-level** `filterHoldingHistoryEntries(...)` — menos alinhada à spec e ao padrão Koin `@Factory` do projeto.
- **Filtrar dentro de `GetHistoryTableDataUseCase` sem UC** — viola FR-001 e dificulta testes isolados de corretora.

---

## Decisão 2: Corretora no critério por `brokerageIds: Set<Long>`

**Decision**: Acrescentar `brokerageIds: Set<Long> = emptySet()` a `WalletHistoryFilterCriteria` e `brokerageId: Long` a `WalletHistoryFilterCandidate`; novo `matchesBrokerage` com OR intra-grupo, AND com restantes, grupo inactivo se vazio.

**Rationale**: Identificador estável (`Brokerage.id`); UI histórico mapeia 0 ou 1 id (FR-006); domínio aceita vários ids para testes OR (US2 cenário 2).

**Alternatives considered**:
- **`Brokerage?` no critério** — acopla entity ao critério serializável; pior para testes com ids fictícios.
- **Manter `Param.brokerage`** — rejeitado por FR-005 (migração completa).

**Saturação**: Se todos os ids distintos presentes na lista de entrada estiverem em `brokerageIds`, tratar como grupo inactivo (paridade B3/Liquidados), cobrindo edge case da spec.

---

## Decisão 3: Candidato e «liquidado» só no mês do registo

**Decision**: `settled = (endOfMonthValue * endOfMonthQuantity) == 0.0` no `HoldingHistoryEntry` referenciado; **não** usar `previousEntry` para critérios (FR-004, FR-011).

**Rationale**: Clarificação da spec; `toWalletHistoryFilterCandidate()` em `HoldingHistoryResult` já usa `currentEntry` — extrair para `HoldingHistoryEntry` e reutilizar em ambos os caminhos.

**Alternatives considered**:
- **Comparar com mês anterior** — rejeitado (clarificação A na spec).

---

## Decisão 4: Pipeline `GetHistoryTableDataUseCase`

**Decision**:
1. `MergeHistoryUseCase` → lista `HoldingHistoryResult`.
2. Extrair `currentEntry` de cada par → `FilterHoldingHistoryEntriesUseCase`.
3. Construir `Set` de identidade de posição (ex. `holding.id`) das entradas que passam → filtrar `results` → `mapNotNull` para linhas (inalterado).

**Rationale**: FR-005/011; remove filtro paralelo de corretora e segundo `.filter` com candidato em `HoldingHistoryResult` sem passar pelo UC.

**Alternatives considered**:
- **Filtrar `results` antes de extrair entries** — equivalente se candidato for só de `currentEntry`; UC sobre entries mantém contrato FR-001.

---

## Decisão 5: Facetas vs listagem no `HistoryViewModel`

**Decision**:
- **Facetas**: `WalletHistoryFilterCriteria(brokerageIds = …)` com restantes grupos vazios/inactivos — **sem** `defaultForHistory()` (FR-009).
- **Tabela/sumário**: `walletFilters.toWalletHistoryFilterCriteria(brokerageSelected)` incluindo painel + corretora.
- **Período**: `selectPeriod` repõe `defaultForHistory()` **e** `brokerage.selected = null` (FR-010, comportamento novo).

**Rationale**: Hoje `facetCriteria = WalletHistoryFilterCriteria()` + `brokerage` separado; unificar num único `Param.walletFilter` elimina a segunda chamada com semântica duplicada.

**Alternatives considered**:
- **Manter dupla chamada ao UC da tabela para facetas** — possível mas frágil; preferir uma chamada com critério só-corretora ou reutilizar merge+filter internamente (VM continua com duas chamadas: facetas vs tabela, critérios distintos).

---

## Decisão 6: Mapeamento apresentação → critérios

**Decision**: Estender `WalletFiltersUiState.toWalletHistoryFilterCriteria(selectedBrokerage: Brokerage?)` (ou overload) para preencher `brokerageIds`.

**Rationale**: Um único ponto composeApp → domain; segment control inalterado (FR-007).

---

## Decisão 7: Testes e validação

**Decision**: Novo `FilterHoldingHistoryEntriesUseCaseTest.kt`; estender `WalletHistoryFilterTest` (corretora isolada, OR, AND com classe, saturado, inactivo); ajustar `GetHistoryTableDataUseCaseTest` se mockar `Param`; regressão dos 9+ cenários 015 via candidato/entries.

**Rationale**: Princípio V; princípio IX — sem `./gradlew` automático no plano/tasks.

---

## Decisão 8: UI e módulos

**Decision**: Zero alterações em `design-system-v2`, `WalletFiltersPanel`, `AssetHistoryScreen` layout; apenas `HistoryViewModel`, `WalletFiltersToCriteria.kt`, domínio.

**Rationale**: FR-007; reduz risco de regressão visual.

**WIP em git** (`WalletFiltersDerivation.kt`, `WalletFiltersSlotGrid.kt`): **fora de âmbito** 018 — não integrar no plano salvo conflito; implementação 018 não depende deles.

---

## Resumo de NEEDS CLARIFICATION

Nenhum — stack, dependências (015, 017) e comportamento estão definidos na spec e no código actual.
