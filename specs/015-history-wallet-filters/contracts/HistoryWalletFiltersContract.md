# Contract: Histórico + WalletFiltersPanel

**Feature**: `015-history-wallet-filters` | **Phase**: 1 | **Date**: 2026-06-02

---

## Visão geral

```
HistoryViewModel (StateFlow)
    → GetHistoryTableDataUseCase(Param + WalletHistoryFilterCriteria)
    → matchesWalletHistoryFilter (pure)
AssetHistoryScreen.Supporting
    → WalletFiltersPanel(options, state, onStateChange)
```

**Depende de**: 014 (`WalletFiltersPanel`, design-system-v2 `filter/*`).

---

## HistoryViewModel

| Responsabilidade | Contrato |
|------------------|----------|
| Estado de filtros | `HistoryState.walletFilters: WalletFiltersUiState` — fonte única |
| Opções do painel | `HistoryState.walletFilterOptions: WalletFiltersPanelOptions` — derivadas após dados do mês |
| Período | `SelectPeriod` repõe `walletFilters = defaultForHistory()` **antes** de `loadInitialData` |
| Filtros | Intent `WalletFiltersChanged(WalletFiltersUiState)` → atualiza state → reload |
| Reset painel | `WalletFiltersChanged(defaultForHistory())` |
| Legado | **Sem** `SelectCategory`, `SelectLiquidity`, `SelectGoal` |

---

## GetHistoryTableDataUseCase.Param

```kotlin
public data class Param(
    val referenceDate: YearMonth,
    val brokerage: Brokerage?,
    val walletFilter: WalletHistoryFilterCriteria,
)
```

| Regra | Detalhe |
|-------|---------|
| Corretora | `brokerage == null` → sem filtro de corretora |
| Carteira | Sempre passa `walletFilter` (pode ser “tudo inactivo” exceto default histórico) |
| Meta financeira | **Removida** do Param |
| Exclusão zero | **Removida**; liquidados só via `walletFilter.settled` |

---

## WalletHistoryFilterCriteria (domain)

Ver `data-model.md`. Função pública para testes:

```kotlin
public fun matchesWalletHistoryFilter(
    candidate: WalletHistoryFilterCandidate,
    criteria: WalletHistoryFilterCriteria,
): Boolean
```

`WalletHistoryFilterCandidate` — DTO mínimo com campos necessários ao match (construído dentro do use case a partir de `HoldingHistoryResult`).

---

## AssetHistoryScreen.Supporting

| Antes (015 remove) | Depois |
|--------------------|--------|
| 3× `SegmentedControl` (categoria, liquidez, meta) | **Ausentes** |
| `remember { PreviewCatalog }` | `state.walletFilterOptions` do ViewModel |
| `remember { UiState.initial() }` | `state.walletFilters` + `onIntent(WalletFiltersChanged)` |

**Mantém**: `WalletFiltersPanel` como única superfície classe/subtipo/liquidez/B3/liquidados/vencimento.

---

## Sumário

| Campo | Contrato |
|-------|----------|
| Agregação | **Apenas** linhas retornadas pelo use case após `walletFilter` |
| Transações globais do mês | **Não** usar para contributions/withdrawals do sumário |

---

## Testes (domain)

| ID | Cenário mínimo |
|----|----------------|
| T1 | Nenhum grupo activo (incl. liquidados vazio) → todas as linhas |
| T2 | Só «Não liquidado» → exclui liquidados |
| T3 | Uma classe |
| T4 | OR duas classes |
| T5 | AND classe + B3 |
| T6 | Liquidez só afecta RF |
| T7 | Vence até só RF |
| T8 | Sim+Não B3 → grupo inactivo |
| T9 | Critérios de `defaultForHistory()` / pós-reset → equivalente a T2 (exclui liquidados; distinto de T1) |
| — | OR subtipo (ex.: CDB ∪ LCI) — coberto na mesma suite (FR-004 / US3) |

---

## Não-objectivos (contrato)

- Novo módulo Gradle
- Persistência de filtros entre sessões
- Filtro por meta financeira
- Alterar API pública do design-system-v2
- `WalletFiltersSlotGrid` (não exigido)
