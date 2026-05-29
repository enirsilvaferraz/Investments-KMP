# Contract: SummaryCard (design-system-v2)

**Feature**: `011-summary-cards` | **Phase**: 1 | **Date**: 2026-05-29

---

## Visão geral

```
:features:design-system-v2  →  AppThemeV2 (M3 Expressive), SummaryCard(...)
```

Sem `:features:design-system`. **Esta entrega**: só `SummaryCardStatus.Default` + tema e cartão **M3 Expressive**.

---

## API pública

### `SummaryCardStatus`

```kotlin
public enum class SummaryCardStatus {
    Default,
}
```

Novos valores (`Info`, `Warning`, `Error`, `Success`) em features futuras — acrescentar ao enum e a `SummaryCardStatusColors.resolve`.

### `SummaryCard`

```kotlin
@Composable
public fun SummaryCard(
    title: String,
    value: String,
    status: SummaryCardStatus,
    modifier: Modifier = Modifier,
    legend: String? = null,
    icon: ImageVector? = null,
)
```

| Parâmetro | Contrato |
|-----------|----------|
| `status` | Nesta entrega: usar **`Default`** |
| Cores | Via tema Expressive + `CardDefaults` no cartão; texto/badge via `SummaryCardStatusColors.resolve` — **proibido** `Color` na API |

---

## Tema — M3 Expressive

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun AppThemeV2(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
)
```

| Token | Fonte |
|-------|--------|
| `colorScheme` | `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` |
| `shapes` | `AppShapesV2` (`medium` = 12.dp para cartão) |
| `typography` | `Typography()` Material3 |

Ficheiros: `theme/AppThemeV2.kt`, `theme/ExpressiveColorScheme.kt`, `theme/Shapes.kt`, `theme/Typography.kt`.

---

## Contentor — Outlined Card (M3 Components)

```kotlin
OutlinedCard(
    enabled = false,
    modifier = modifier.heightIn(min = SummaryCardDefaults.minHeight),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.outlinedCardColors(),
    border = CardDefaults.outlinedCardBorder(),
) { /* conteúdo */ }
```

| Aspeto | Token / API |
|--------|-------------|
| Fundo | `surface` (`CardDefaults.outlinedCardColors`) |
| Borda | `outlineVariant` (`CardDefaults.outlinedCardBorder`) |
| Elevação | **0** (sem sombra) |
| Forma | `shapes.medium` (12.dp) |

---

## Cores de conteúdo `Default`

| Elemento | `ColorScheme` |
|----------|---------------|
| Valor | `onSurface` |
| Título / legenda | `onSurfaceVariant` |
| Badge (slot ícone) | `surfaceContainerHigh` + ícone `onSurfaceVariant` |
| Badge forma | `CircleShape` (shape **full** M3) |
| Badge tamanho | contentor **40.dp**, ícone **24.dp** (decorativo, não `Badge` de notificação) |

```kotlin
internal object SummaryCardStatusColors {
    internal fun resolve(
        status: SummaryCardStatus,
        colorScheme: ColorScheme,
    ): SummaryCardColors
}
```

`SummaryCardColors.container` = `surface` (coerente com Outlined Card).

**Fora do escopo**: `resolve` para Info/Warning/Error/Success.

---

## Layout e tipografia

| Aspecto | Token / API M3 |
|---------|----------------|
| Padding interno | `16.dp` |
| Espaçamento vertical | `8.dp` entre blocos |
| Título | `labelSmall` + Bold + uppercase |
| Valor | `titleLarge` |
| Legenda | `bodySmall` |
| Altura | `heightIn(min = …)` — slots M3, **não** 110.dp do mock |
| Slots vazios | Reservados (legenda + badge) |
| Grade preview | `spacedBy(8.dp)` |

Protótipo: referência qualitativa apenas.

---

## Motion e interação

- **Sem** clique, hover, scale ou animação no cartão (FR-006/FR-007).
- Motion Expressive (springs) **não** aplicável a este componente; exceção documentada.

---

## Acessibilidade

- Ícone: `contentDescription = null` + `invisibleToUser()` (ou equivalente).
- Contraste texto: **≥ 4,5:1** (WCAG AA) em light/dark Expressive.
- Título = rótulo principal da métrica para leitores de tela.

---

## Previews

Todos em **`SummaryCard.kt`**, visibilidade **`private`** (constituição — mesmo ficheiro do composable).

| Preview | Conteúdo |
|---------|----------|
| `SummaryCard_Default_preview` | Um cartão completo |
| `SummaryCard_OptionalSlots_preview` | Quatro instâncias (completo / sem legenda / sem ícone / sem ambos) — SC-005b |
| `SummaryCard_Catalog8_preview` | 8 exemplos FR-008 via `SummaryCardCatalog`, **todos** `status = Default` |
| (light/dark) | Par de previews ou `uiMode` para WCAG AA (T025) |

```kotlin
// SummaryCard.kt (final do ficheiro)
@Preview
@Composable
private fun SummaryCard_Catalog8_preview() {
    AppThemeV2 {
        // LazyVerticalGrid 2 colunas, spacedBy(8.dp)
        // SummaryCardCatalog.items
    }
}
```

Dados estáticos FR-008: `SummaryCardCatalog.kt` (sem previews neste ficheiro).

---

## Verificação

```bash
./gradlew :features:design-system-v2:compileKotlinJvm
```
