# Data Model: Cartões de resumo

**Feature**: `011-summary-cards` | **Date**: 2026-05-29

## Entidades

### `SummaryCardStatus` (enum — extensível)

| Valor (esta entrega) | Significado |
|----------------------|-------------|
| `Default` | Neutro; única variante com cores implementadas |

Valores futuros (não no enum até feature dedicada): `Info`, `Warning`, `Error`, `Success`.

**Regras**:
- API do cartão mantém parâmetro `status`.
- Novos status = novo enum + ramo em `SummaryCardStatusColors.resolve`.

---

### `SummaryCardColors` (internal)

Cores resolvidas para um par `(status, ColorScheme)` — alinhadas ao **Outlined Card** M3:

| Campo | Default (M3 Expressive) |
|-------|---------------------------|
| `container` | `surface` (Outlined Card via `CardDefaults`) |
| `onContainer` | `onSurface` |
| `title` | `onSurfaceVariant` |
| `legend` | `onSurfaceVariant` |
| `outline` | `outlineVariant` |
| `badgeContainer` | `surfaceContainerHigh` |
| `badgeIcon` | `onSurfaceVariant` |
| `badgeOutline` | `outlineVariant` (opcional no slot decorativo) |

---

### `AppThemeV2` (tema)

| Propriedade | Valor |
|-------------|--------|
| `colorScheme` | `lightExpressiveColorScheme()` / `darkExpressiveColorScheme()` |
| `shapes` | `AppShapesV2` (medium **12.dp** para cartão) |
| `typography` | `Typography()` M3 |

---

### `SummaryCard` (parâmetros)

| Campo | Tipo | Obrigatório |
|-------|------|-------------|
| `title` | `String` | Sim |
| `value` | `String` | Sim |
| `legend` | `String?` | Não |
| `icon` | `ImageVector?` | Não |
| `status` | `SummaryCardStatus` | Sim (só `Default` nesta entrega) |
| `modifier` | `Modifier` | Não |

**Invariantes**: Outlined Card + CardDefaults; altura mínima uniforme (slots M3); tipografia/shapes/spacing só do tema Expressive; cores via resolvedor + defaults do cartão; sem interação; sem animação; **não** pixel-perfect vs protótipo.

---

## Relacionamentos

```text
AppThemeV2 ──► colorScheme (Expressive) + shapes (AppShapesV2) + typography
SummaryCardStatus.Default ──► SummaryCardStatusColors.resolve ──► SummaryCardColors
SummaryCard ──► OutlinedCard(CardDefaults) + conteúdo com colors resolvidas
```

## Fora do modelo (esta entrega)

- Paletas info/warning/error/success
- `AppColorSchemeV2` com múltiplas famílias
- Dynamic Color do sistema (integração app futura)
- Motion Expressive no cartão
