# Data Model: Filtros da carteira no histórico

**Feature**: `015-history-wallet-filters` | **Date**: 2026-06-02

## Escopo

- **Domínio**: critérios de filtro + função de match (testável).
- **Apresentação**: reutiliza modelos 014 (`WalletFiltersUiState`, `WalletFilterHoldingFacet`, `WalletFiltersPanelOptions`); ajustes de default/reset e derivação.
- **Sem** novos módulos Gradle; **sem** persistência.

---

## Domínio (`:domain:usecases`)

### `WalletHistoryFilterCriteria`

| Campo | Tipo | Semântica |
|-------|------|-----------|
| `categories` | `Set<InvestmentCategory>` | Grupo Classe; vazio = inactivo |
| `subtypes` | `Set<WalletHistorySubtype>` | Grupo Subtipo; OR intra-grupo |
| `liquidities` | `Set<Liquidity>` | Só RF; vazio = inactivo |
| `b3Informed` | `Set<Boolean>` | `true`=Sim, `false`=Não; ambos = inactivo |
| `settled` | `Set<Boolean>` | `true`=liquidado, `false`=não liquidado |
| `maturityUpTo` | `YearMonth?` | `null` = «Qualquer» (grupo inactivo) |

`WalletHistorySubtype` — sealed ou typealias que espelha subtipos entity (`FixedIncomeAssetType`, `VariableIncomeAssetType`, `InvestmentFundAssetType`) para match sem depender do composeApp.

### Regras de avaliação (`matchesWalletHistoryFilter`)

Para cada linha candidata (faceta de domínio derivada do asset + entradas do mês):

1. **Grupos inactivos** (set vazio; B3/Liquidados com 2 valores; `maturityUpTo == null`) não restringem.
2. **OR** dentro do mesmo grupo (ex.: RF ∪ RV em `categories`).
3. **AND** entre grupos activos.
4. **Liquidez / Vence até**: se grupo activo e asset **não** é RF → passa (transparente).
5. **Subtipo**: se classe RF desactivada, subtipos RF em `subtypes` são ignorados (purge já no UI state; match ignora órfãos).
6. **Vence até**: RF com vencimento **posterior** ao mês seleccionado → falha quando grupo activo.

### Transições de estado (histórico)

| Evento | Efeito no `WalletFiltersUiState` |
|--------|-----------------------------------|
| Abrir ecrã / `LoadInitialData` | `defaultForHistory()` |
| `SelectPeriod` | `defaultForHistory()` antes de recarregar |
| Alterar toggle / vencimento | `onStateChange` → recarga |
| Resetar (painel) | `defaultForHistory()` |
| Portfolio do mês actualizado | `deriveOptions` + prune selecções inválidas (opcional mínimo: ignorar IDs ausentes no match) |

---

## Apresentação (`composeApp`)

### Reutilizado da 014 (inalterado na forma)

- `WalletFiltersPanel`, `WalletFiltersPanelOptions`, `WalletFilterHoldingFacet`, `FilterOption`, mappers `HoldingHistoryView.toWalletFilterHoldingFacet()`.

### Alterado

| Artefacto | Mudança |
|-----------|---------|
| `WalletFiltersUiState.initial()` | Histórico usa `defaultForHistory()`; previews podem manter `initial()` vazio ou chamar default explícito |
| `WalletFiltersUiState.reset()` | Igual a `defaultForHistory()` para histórico |
| `deriveWalletFiltersPanelOptions` | Nova implementação única; remove WIP incompatível |

### `HistoryState` (simplificado)

| Removido | Substituído por |
|----------|-----------------|
| `category: Choice<InvestmentCategory>` | — |
| `liquidity: Choice<Liquidity>` | — |
| `goal: Choice<FinancialGoal>` | — |
| — | `walletFilters: WalletFiltersUiState` |
| — | `walletFilterOptions: WalletFiltersPanelOptions` (derivado após load) |

Mantém: `period`, `brokerage`, `tableData`, `summaryProperties`, …

---

## Fluxo de dados

```text
MergeHistory → map linhas → List<HistoryTableData> (sem exclusão zero implícita)
     ↑
     matchesWalletHistoryFilter(criteria)  ← WalletHistoryFilterCriteria
     ↑
HistoryViewModel: UiState → criteria; facetas → deriveOptions
     ↑
WalletFiltersPanel (hoisted state)
```

---

## Sumário (`SummaryProperties`)

| Campo | Fonte após 015 |
|-------|----------------|
| `previousValue`, `actualValue` | `sum` dos valores das linhas filtradas |
| `contributions`, `withdrawals` | `sum` de `totalContributions` / `totalWithdrawals` das linhas filtradas |
| `growth`, `earnings`, % | `Growth` / `Appreciation` sobre esses agregados |

---

## Entidades de negócio (spec)

| Spec | Implementação |
|------|----------------|
| Critério de filtro | Campos em `WalletHistoryFilterCriteria` + faceta |
| Grupo de filtros | Conjuntos no criteria; match por grupo |
| Linha de histórico | `HistoryTableData` / `HoldingHistoryView` |
| Faceta | `WalletFilterHoldingFacet` (presentation) + conversão para match domain |
