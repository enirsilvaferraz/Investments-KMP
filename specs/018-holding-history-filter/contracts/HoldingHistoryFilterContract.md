# Contract: Filtro unificado de histórico (HoldingHistoryEntry + corretora)

**Feature**: `018-holding-history-filter` | **Phase**: 1 | **Date**: 2026-06-03

**Depende de**: `015-history-wallet-filters`, `017-holding-transactions`

---

## Visão geral

```
HistoryViewModel
  → toWalletHistoryFilterCriteria(selectedBrokerage)
  → GetHistoryTableDataUseCase(Param(referenceDate, walletFilter))
       → MergeHistoryUseCase
       → FilterHoldingHistoryEntriesUseCase(currentEntries, walletFilter)
       → map rows

FilterHoldingHistoryEntriesUseCase (reutilizável)
  → HoldingHistoryEntry.toWalletHistoryFilterCandidate()
  → matchesWalletHistoryFilter
```

**UI**: inalterada (FR-007). Segment de corretora continua a disparar `SelectBrokerage`; critério unificado só no domínio/VM.

---

## FilterHoldingHistoryEntriesUseCase

```kotlin
@Factory
public class FilterHoldingHistoryEntriesUseCase(
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<FilterHoldingHistoryEntriesUseCase.Param, List<HoldingHistoryEntry>>(context) {

    public data class Param(
        val entries: List<HoldingHistoryEntry>,
        val criteria: WalletHistoryFilterCriteria,
    )
}
```

| Regra | Detalhe |
|-------|---------|
| Entrada vazia | Retorna `emptyList()` |
| Ordem | Preserva ordem de `entries` |
| Avaliação | Só atributos do registo + `holding` (FR-004) |
| Liquidado | `endOfMonthValue * endOfMonthQuantity == 0.0` |

---

## WalletHistoryFilterCriteria (extensão)

```kotlin
public data class WalletHistoryFilterCriteria(
    // … campos 015 …
    val brokerageIds: Set<Long> = emptySet(),
)
```

| Grupo Corretora | Comportamento |
|-----------------|---------------|
| `brokerageIds` vazio | Inactivo — não exclui |
| 1 id (UI histórico) | Só essa corretora |
| 2+ ids (testes) | OR intra-grupo |
| Saturação (todos ids da lista) | Equivalente a inactivo |

**UI → domínio** (FR-006): `selected == null` → `emptySet()`; `selected.id` → `setOf(id)`.

---

## WalletHistoryFilterCandidate (extensão)

```kotlin
public data class WalletHistoryFilterCandidate(
    // … campos 015 …
    val brokerageId: Long,
)
```

---

## matchesWalletHistoryFilter

Assinatura inalterada; implementação acrescenta `matchesBrokerage` no final da cadeia AND.

Testes públicos em `WalletHistoryFilterTest.kt` (pacote `com.eferraz.usecases.screens`).

---

## GetHistoryTableDataUseCase.Param

```kotlin
public data class Param(
    val referenceDate: YearMonth,
    val walletFilter: WalletHistoryFilterCriteria,
)
```

| Removido | Substituído por |
|----------|-----------------|
| `brokerage: Brokerage?` | `walletFilter.brokerageIds` |
| `.filter { brokerage … }` | `FilterHoldingHistoryEntriesUseCase` |
| `.filter { matches… on HoldingHistoryResult }` | Filtro por `currentEntry` via UC |

**Proibido após merge**: segundo filtro paralelo em `HoldingHistoryResult` para critérios de carteira/corretora.

---

## HistoryViewModel

| Responsabilidade | Contrato |
|------------------|----------|
| Tabela + sumário | `GetHistoryTableDataUseCase.Param(period, walletFilters.toCriteria(brokerage.selected))` |
| Facetas | `Param(period, facetCriteriaForHistory(selected))` — **só** `brokerageIds` activo; painel inactivo (FR-009) |
| `SelectPeriod` | `defaultForHistory()` + `brokerage.selected = null` (FR-010) |
| `SelectBrokerage` | Toggle; reload; **sem** mudança de componentes |

---

## WalletFiltersToCriteria

```kotlin
internal fun WalletFiltersUiState.toWalletHistoryFilterCriteria(
    selectedBrokerage: Brokerage? = null,
): WalletHistoryFilterCriteria
```

Preenche todos os grupos do painel **e** `brokerageIds` a partir de `selectedBrokerage`.

```kotlin
internal fun facetCriteriaForHistory(selectedBrokerage: Brokerage?): WalletHistoryFilterCriteria
```

Critério de facetas: apenas `brokerageIds` preenchido; restantes grupos vazios/inactivos (sem `defaultForHistory()`).

---

## Critérios de aceite técnica (checklist implementação)

- [ ] `FilterHoldingHistoryEntriesUseCase` com `@Factory` (Koin automático)
- [ ] Testes corretora + regressão 015
- [ ] `GetHistoryTableDataUseCaseTest` / callers sem `brokerage` em `Param`
- [ ] `rg 'brokerage:' core/domain/usecases/.../GetHistoryTableDataUseCase` — só em comentários históricos ou zero
- [ ] Facetas com universo restrito por corretora sem `defaultForHistory()` no critério de facetas
- [ ] Mudança de período limpa corretora

---

## Fora de âmbito

- Multi-selecção de corretoras na UI
- Alterações em `WalletFiltersPanel` / design-system-v2
- Novos grupos no painel para corretora
- `WalletFiltersSlotGrid` / derivação WIP não relacionada
