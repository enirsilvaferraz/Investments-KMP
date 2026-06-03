# Data Model: Filtragem de histórico unificada (incl. corretora)

**Feature**: `018-holding-history-filter` | **Date**: 2026-06-03

## Escopo

- **Domínio**: extensão de critérios/candidato/match; novo caso de uso sobre `HoldingHistoryEntry`; migração de `GetHistoryTableDataUseCase`.
- **Apresentação**: mapper critérios + `HistoryViewModel` (reset período/corretora, critérios de facetas).
- **Sem** novos módulos Gradle; **sem** persistência; **sem** alteração de UI 014/015.

---

## Entidades existentes (referência)

### `HoldingHistoryEntry` (`:domain:entity`)

| Campo | Uso no filtro |
|-------|----------------|
| `holding` | `asset`, `brokerage`, transações do mês (colunas, não critério) |
| `referenceDate` | Mês do snapshot avaliado |
| `endOfMonthValue`, `endOfMonthQuantity` | Património → `settled` |

### `Brokerage`

| Campo | Uso |
|-------|-----|
| `id: Long` | Valor em `brokerageIds` |
| `name` | Apenas UI (inalterada) |

### `HoldingHistoryResult` (usecases)

Par `previousEntry` + `currentEntry` + `holding` + métricas. **Critérios**: só `currentEntry` passa pelo filtro; `previousEntry` só para colunas da tabela.

---

## Domínio (`:domain:usecases`)

### `WalletHistoryFilterCriteria` (alterado)

| Campo | Tipo | Semântica |
|-------|------|-----------|
| `assetClasses` | `Set<AssetClass>` | Classe; vazio = inactivo |
| `subtypes` | `Set<WalletHistorySubtype>` | OR intra-grupo |
| `liquidities` | `Set<Liquidity>` | Só RF |
| `b3Informed` | `Set<Boolean>` | Sim/Não; size 2 = inactivo |
| `settled` | `Set<Boolean>` | Liquidado / não; size 2 = inactivo |
| `maturityUpTo` | `YearMonth?` | RF vencimento |
| **`brokerageIds`** | **`Set<Long>`** | **NOVO** — OR intra-grupo; vazio = inactivo |

`defaultForHistory()` — inalterado: `settled = { false }`, `brokerageIds` vazio.

**Critério só para facetas** (VM) — helper recomendado `facetCriteriaForHistory(selectedBrokerage)`:

```kotlin
WalletHistoryFilterCriteria(
    brokerageIds = selected?.id?.let(::setOf) ?: emptySet(),
)
```

Demais campos por defeito vazios/null → painel inactivo para derivação (FR-009). **Não** usar `defaultForHistory()` neste critério (excluiria liquidados do universo de facetas).

### `WalletHistoryFilterCandidate` (alterado)

| Campo | Tipo | Novo? |
|-------|------|-------|
| …campos 015… | … | — |
| **`brokerageId`** | **`Long`** | **Sim** — `holding.brokerage.id` |

### `matchesWalletHistoryFilter`

Ordem de avaliação (AND): classe → subtipo → liquidez → B3 → liquidados → vencimento → **corretora**.

**`matchesBrokerage`**:
- `brokerageIds.isEmpty()` → passa
- Saturação opcional: se conjunto de ids na lista de candidatos ⊆ `brokerageIds` → inactivo (edge spec)
- Senão: `candidate.brokerageId in brokerageIds`

### `FilterHoldingHistoryEntriesUseCase`

| Param | Tipo |
|-------|------|
| `entries` | `List<HoldingHistoryEntry>` |
| `criteria` | `WalletHistoryFilterCriteria` |

| Saída | Regra |
|-------|--------|
| `List<HoldingHistoryEntry>` | Sublista que passa em `matches`; ordem preservada; entrada vazia → vazia |

### `HoldingHistoryEntry.toWalletHistoryFilterCandidate()`

Mesma lógica que `HoldingHistoryResult.toWalletHistoryFilterCandidate()` hoje, lendo património e asset de `entry.holding` / valores do próprio entry.

`HoldingHistoryResult` pode delegar: `currentEntry.toWalletHistoryFilterCandidate()`.

### `GetHistoryTableDataUseCase.Param` (alterado)

```kotlin
public data class Param(
    val referenceDate: YearMonth,
    val walletFilter: WalletHistoryFilterCriteria,
)
```

| Removido | Motivo |
|----------|--------|
| `brokerage: Brokerage?` | FR-005 — unificado em `walletFilter.brokerageIds` |

---

## Apresentação (`composeApp`)

### `WalletFiltersToCriteria.kt`

| Função | Mudança |
|--------|---------|
| `toWalletHistoryFilterCriteria(selectedBrokerage: Brokerage?)` | Preenche `brokerageIds` (0 ou 1 elemento) |

### `HistoryViewModel`

| Evento | Efeito |
|--------|--------|
| `SelectPeriod` | `walletFilters = defaultForHistory()` **e** `brokerage.selected = null` |
| `loadInitialData` | Facetas: critério só-corretora; tabela: critério completo |
| Chamadas UC | `Param(period, walletFilter)` — **sem** `brokerage` |

### Inalterado (FR-007)

- `AssetHistoryScreen`, segment de corretora, `WalletFiltersPanel`, strings, layout.

---

## Fluxo de dados (pós-018)

```text
HistoryViewModel
  ├─ facetFilter = Criteria(brokerageIds only)
  └─ tableFilter = UiState.toCriteria(selectedBrokerage)

GetHistoryTableDataUseCase(Param(referenceDate, tableFilter))
  → MergeHistory
  → FilterHoldingHistoryEntriesUseCase(currentEntries, tableFilter)
  → filter HoldingHistoryResult by passing holdings
  → mapNotNull → HistoryTableData

FilterHoldingHistoryEntriesUseCase (reutilizável)
  → entry.toWalletHistoryFilterCandidate()
  → matchesWalletHistoryFilter
```

---

## Transições de estado

| Evento | `walletFilters` | `brokerage.selected` |
|--------|-----------------|----------------------|
| Abrir / LoadInitial | `defaultForHistory()` (init 015) | null ou seleção utilizador |
| SelectPeriod | `defaultForHistory()` | **null** (novo) |
| SelectBrokerage | inalterado | toggle 0/1 |
| WalletFiltersChanged | novo estado | inalterado |

---

## Testes (modelo mental)

| Área | Cenários mínimos |
|------|------------------|
| `matchesBrokerage` | inactivo; 1 id; 2 ids OR; AND com classe; id inexistente → vazio |
| `FilterHoldingHistoryEntriesUseCase` | lista vazia; 10+ heterogéneos (SC-001) |
| Regressão 015 | 9+ testes existentes via entry/candidate |
| `GetHistoryTableDataUseCase` | Param sem brokerage; integração com filter injetado |
