# Data Model: Cores semânticas de status no tema v2

**Feature**: `012-status-theme-colors` | **Date**: 2026-05-29

## Superfície mínima (3 tipos + 1 extensão)

| Artefacto | Pacote | Visibilidade | Função |
|-----------|--------|--------------|--------|
| `StatusKind` | `theme` | `public` enum | **Único** seletor de status (cartão, swatches, futuros consumidores) |
| `StatusColorRoles` | `theme` | `public` data class | 8 papéis M3 por estado |
| `FixedStatusPalettes` | `theme` | `internal` object | Hex/tonal light+dark — **única** fonte de valores concretos |
| `MaterialTheme.statusColors(status)` | `theme` | `public` extensão | Lookup: `StatusKind` → `StatusColorRoles` |

**Removidos** (complexidade desnecessária):

- ~~`AppStatusColors`~~ — substituído por `CompositionLocal` + extensão directa
- ~~`SummaryCardStatus`~~ (enum duplicado) — substituído por `typealias SummaryCardStatus = StatusKind` em `summary/` (compat 011)
- ~~`StatusColorPalette`~~ — cartão usa `StatusColorRoles` inline
- ~~`SummaryCardStatusColors`~~ — sem resolvedor; lookup no composable

---

## `StatusKind` (enum)

| Valor | Matiz |
|-------|-------|
| `Default` | Neutro |
| `Info` | Azul |
| `Warning` | Âmbar |
| `Positive` | Verde |
| `Negative` | Vermelho |

O argumento `status` do cartão **é** um `StatusKind` (via typealias `SummaryCardStatus`).

---

## `StatusColorRoles` (8 papéis M3)

| Papel M3 | Propriedade |
|----------|-------------|
| Info | `color` |
| On Info | `onColor` |
| Info Container | `container` |
| On Info Container | `onContainer` |
| Info Fixed | `fixed` |
| Info Fixed Dim | `fixedDim` |
| On Info Fixed | `onFixed` |
| On Info Fixed Variant | `onFixedVariant` |

---

## Fluxo (2 passos)

```text
SummaryCard(status: StatusKind)
  → val roles = MaterialTheme.statusColors(status)
  → aplica roles.* directamente no UI
```

Sem conversões intermédias, sem `resolve()`, sem `toStatusKind()`.

---

## Mapeamento cartão → `StatusColorRoles` (inline em `SummaryCard.kt`)

| UI | Propriedade |
|----|-------------|
| fundo do cartão | `container` |
| valor | `onContainer` |
| título, borda | `onFixedVariant` |
| legenda | `onFixedVariant` @ 75% |
| fundo badge | `fixed` |
| ícone badge | `onFixed` |

---

## `SummaryCardCatalogItem`

Campo `status: StatusKind` (ou `SummaryCardStatus` typealias) + mapeamento FR-010a.

---

## Invariantes

- **`status` enum decide a paleta** — sem inferência por conteúdo, sem `Color` na API.
- `theme/` não importa `summary/`.
- Paletas fixas — independentes do `ColorScheme`.
- Default calibrado à baseline 011 (SC-006).

## Fora do modelo

- Regras métrica → status
- Dynamic Color
- Integração Histórico
