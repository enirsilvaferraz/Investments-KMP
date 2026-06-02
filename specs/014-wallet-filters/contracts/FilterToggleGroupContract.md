# Contract: FilterToggleGroup (design-system-v2)

**Feature**: `014-wallet-filters` | **Phase**: 1 | **Date**: 2026-06-02

---

## Visão geral

```
:features:design-system-v2  →  AppThemeV2 { FilterToggleGroup(...) }
:features:composeApp        →  compõe secções do painel (não expõe este contract ao utilizador final)
```

**Trilha paralela A** — pode implementar e compilar **sem** `WalletFiltersPanel`.

---

## API pública

### `FilterToggleSize`

```kotlin
public enum class FilterToggleSize {
    Standard,  // ~40dp — botões de classe
    Compact,   // ~32dp — liquidez, subtipos, B3, liquidados
}
```

### `FilterToggleGroup`

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun <T> FilterToggleGroup(
    options: List<FilterToggleOption<T>>,
    selectedIds: Set<T>,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: FilterToggleSize = FilterToggleSize.Compact,
    enabled: Boolean = true,
)
```

```kotlin
@Immutable
public data class FilterToggleOption<T>(
    val id: T,
    val label: String,
    val contentDescription: String = label,
)
```

| Parâmetro | Contrato |
|-----------|----------|
| `options` | Ordem preservada; `empty` → composable no-op (altura zero) |
| `selectedIds` | Multi-selecção; `id in selectedIds` → checked |
| `onToggle` | Invocado por mudança de checked; **toggle** (segundo toque desactiva) |
| `size` | `Standard` → `labelLarge`, altura alvo 40dp; `Compact` → `labelMedium`, ~32dp |
| Cores | `ToggleButtonDefaults.toggleButtonColors()` — **sem** `Color` na API |
| Formas | `ButtonGroupDefaults.connected*ButtonShapes()` por índice |

**Semântica a11y**: `Modifier.semantics { role = Role.Checkbox }` em cada toggle — **proibido** `Role.RadioButton`.

**Toque**: área efectiva ≥ **48dp** via padding / `minimumInteractiveComponentSize`.

**Quebra de linha**: `FlowRow` com grupos conectados **por linha contígua** (mesma regra FR-M3-009); quando uma linha quebra, reiniciar leading/trailing shapes por segmento.

---

## Tema

Requer **`AppThemeV2`**.

Previews **`private`** no mesmo ficheiro que o composable.

---

## Não faz parte deste contract

- Derivação de opções a partir de carteira.
- Tooltips (responsabilidade do composeApp ao redor do label).
- Cabeçalho de secção (contract separado).
