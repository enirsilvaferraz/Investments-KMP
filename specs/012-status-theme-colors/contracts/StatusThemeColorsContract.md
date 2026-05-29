# Contract: Status theme colors (design-system-v2)

**Feature**: `012-status-theme-colors` | **Date**: 2026-05-29

---

## Visão geral

```
AppThemeV2
  └── LocalStatusColorRoles: (StatusKind) → StatusColorRoles

SummaryCard(status)
  └── MaterialTheme.statusColors(status) → StatusColorRoles → UI
```

**3 tipos públicos no tema** + **1 extensão**. Zero resolvedores.

---

## API — tema (`theme/StatusColors.kt`)

### `StatusKind`

```kotlin
public enum class StatusKind {
    Default, Info, Warning, Positive, Negative,
}
```

### `StatusColorRoles`

```kotlin
@Immutable
public data class StatusColorRoles(
    public val color: Color,
    public val onColor: Color,
    public val container: Color,
    public val onContainer: Color,
    public val fixed: Color,
    public val fixedDim: Color,
    public val onFixed: Color,
    public val onFixedVariant: Color,
)
```

### Lookup

```kotlin
internal val LocalStatusColorRoles =
    compositionLocalOf<(StatusKind) -> StatusColorRoles> { error("AppThemeV2 required") }

@Composable
public fun MaterialTheme.statusColors(status: StatusKind): StatusColorRoles =
    LocalStatusColorRoles.current(status)
```

`AppThemeV2` monta o provider a partir de `FixedStatusPalettes` (internal, light/dark).

---

## API — cartão

### Compatibilidade 011

```kotlin
// summary/ — typealias, não enum duplicado
public typealias SummaryCardStatus = StatusKind
```

### `SummaryCard`

```kotlin
@Composable
public fun SummaryCard(
    title: String,
    value: String,
    status: SummaryCardStatus,  // = StatusKind
    modifier: Modifier = Modifier,
    legend: String? = null,
    icon: ImageVector? = null,
)
```

Implementação mínima:

```kotlin
val roles = MaterialTheme.statusColors(status)

OutlinedCard(
    colors = CardDefaults.outlinedCardColors(containerColor = roles.container),
    border = BorderStroke(1.dp, roles.onFixedVariant),
) { /* title → roles.onFixedVariant; value → roles.onContainer; badge → roles.fixed/onFixed */ }
```

| Regra | Detalhe |
|-------|---------|
| Seletor | `status` enum — **único** input cromático |
| Proibido | `Color` na API; inferir status por conteúdo |
| Proibido | `SummaryCardStatusColors`, `StatusColorPalette`, `resolve()` |

---

## Ficheiros

| Ficheiro | Acção |
|----------|-------|
| `theme/StatusColors.kt` | **NOVO** — enum, data class, Local, extensão |
| `theme/FixedStatusPalettes.kt` | **NOVO** — valores hex (internal) |
| `theme/StatusColorSwatches.kt` | **NOVO** — preview |
| `theme/AppThemeV2.kt` | provider `LocalStatusColorRoles` |
| `summary/SummaryCard.kt` | lookup + mapeamento inline |
| `summary/SummaryCardCatalog.kt` | campo `status` |
| `summary/SummaryCardStatusColors.kt` | **REMOVER** |

---

## Catálogo FR-010a

| Cartão | `status` |
|--------|----------|
| Valor Anterior | `Info` |
| Valor Atual | `Default` |
| Aportes | `Default` |
| Retiradas | `Negative` |
| Crescimento | `Negative` |
| % Crescimento | `Warning` |
| Lucro | `Positive` |
| Valorização | `Positive` |

---

## Não contrato

- Classes wrapper (`AppStatusColors`, resolvedores, paletas intermédias)
- Dynamic Color
- Inferência automática de status
