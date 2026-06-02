# Data Model: Filtros da carteira

**Feature**: `014-wallet-filters` | **Date**: 2026-06-02

## Escopo

Modelos **de apresentação** em `:features:composeApp` (pacote `walletfilters`). **Sem** persistência, **sem** `:domain:usecases` nesta entrega.

---

## Entidades

### `FilterOptionId` (typealias ou value class)

Identificador estável de uma opção (`String` ou enum sealed por grupo). Usado em `Set<FilterOptionId>` para multi-selecção.

---

### `FilterOption`

| Campo | Tipo | Regras |
|-------|------|--------|
| `id` | `FilterOptionId` | Único no âmbito do grupo |
| `shortLabel` | `String` | Exibido no toggle; max 1 linha |
| `fullLabel` | `String` | Tooltip + `contentDescription` |
| `group` | `FilterGroupKind` | Agrupa derivação e reset |

**Invariantes**:
- `fullLabel` não vazio; se `shortLabel` omitido na factory, default = `fullLabel`.
- Abreviatura só quando necessário para layout (FR-013).

---

### `FilterGroupKind` (enum)

| Valor | Selecção | Derivação |
|-------|----------|-----------|
| `AssetClass` | Multi toggle | Classes com ≥1 activo |
| `Subtype` | Multi toggle | Por classe activa; subtipos presentes nos dados |
| `Liquidity` | Multi toggle | Valores distintos na carteira |
| `B3Informed` | Multi toggle | Secção só se Sim **e** Não possíveis |
| `Settled` | Multi toggle | Idem B3 |
| `Maturity` | **Única** (menu) | Meses distintos; secção oculta se zero vencimentos |

---

### `WalletFilterPortfolioItem` (entrada da carteira injectada)

| Campo | Tipo | Uso na derivação |
|-------|------|------------------|
| `assetClass` | `AssetClassKind` | RF / RV / Fundos |
| `subtype` | `SubtypeKind` | Subcartões |
| `liquidity` | `Liquidity` (entity) ou espelho | Secção Liquidez |
| `b3Informed` | `Boolean` | B3 Sim/Não |
| `settled` | `Boolean` | Liquidados Sim/Não |
| `maturity` | `YearMonth?` | Vence até |

Catálogo estático de subtipos por classe: ver `WalletFiltersCatalog` (RF: CDB, LCI, …; RV: FII, …; Fundos: Ação, …) — **filtrar** pela presença nos itens.

---

### `WalletFiltersUiState`

| Campo | Tipo | Inicial |
|-------|------|---------|
| `selectedClassIds` | `Set<FilterOptionId>` | `emptySet()` |
| `selectedSubtypeIds` | `Set<FilterOptionId>` | `emptySet()` |
| `selectedLiquidityIds` | `Set<FilterOptionId>` | `emptySet()` |
| `selectedB3Ids` | `Set<FilterOptionId>` | `emptySet()` |
| `selectedSettledIds` | `Set<FilterOptionId>` | `emptySet()` |
| `maturitySelection` | `MaturitySelection` | `Any` |

```kotlin
sealed interface MaturitySelection {
    data object Any : MaturitySelection
    data class Month(val yearMonth: YearMonth) : MaturitySelection
}
```

**Invariantes**:
- Subtipos de classe desactivada **não** permanecem em `selectedSubtypeIds` (FR-006).
- `maturitySelection` ignorado na UI se secção oculta (sem vencimentos nos dados).

---

### `WalletFiltersDerivedUiModel` (saída de derivação)

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `visibleSections` | `Set<FilterGroupKind>` | FR-018 |
| `classOptions` | `List<FilterOption>` | |
| `subtypeSections` | `List<SubtypeSectionModel>` | Ordem RF → RV → Fundos |
| `liquidityOptions` | `List<FilterOption>` | |
| `b3Options` | `List<FilterOption>` | Vazio → secção oculta |
| `settledOptions` | `List<FilterOption>` | |
| `maturityOptions` | `List<YearMonth>` | Ordenado cronologicamente |

```kotlin
data class SubtypeSectionModel(
    val classKind: AssetClassKind,
    val title: String,
    val options: List<FilterOption>,
)
```

---

## Transições de estado (UI)

```text
ToggleOption(id)     → add/remove id no Set do grupo
DeselectClass(id)    → remove id + purge subtypes dessa classe
SelectMaturity(ym)   → maturitySelection = Month(ym)
SelectMaturityAny()  → maturitySelection = Any
Reset                → estado inicial + fechar menu se aberto
PortfolioUpdated     → re-derive options; prune selections inválidas
```

| Evento | Efeito |
|--------|--------|
| `Reset` | Todos os `Set` vazios; `MaturitySelection.Any`; subtipos ocultos |
| Classe off | Remove subcartão; limpa subtipos da classe |
| Portfolio sem Fundos | `classOptions` sem Fundos; nunca mostrar botão órfão |

---

## Regras de visibilidade (FR-018)

| Secção | Visível quando |
|--------|----------------|
| Classe | `classOptions.isNotEmpty()` |
| Subtipos | ≥1 classe activa **e** ≥1 subcartão com `options.isNotEmpty()` |
| Liquidez | `liquidityOptions.isNotEmpty()` |
| B3 | `b3Options` contém Sim **e** Não |
| Liquidados | idem |
| Vence até | `maturityOptions.isNotEmpty()` |

---

## Fora do modelo

- Semântica AND/OR de filtragem real na lista de histórico.
- Persistência de preferências.
- ViewModel / `StateFlow` (feature futura).
