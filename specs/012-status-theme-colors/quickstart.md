# Quickstart: Cores semânticas de status (design-system-v2)

**Feature**: `012-status-theme-colors`

## Superfície mínima

| Tipo | Ficheiro |
|------|----------|
| `StatusKind` + `StatusColorRoles` + extensão | `theme/StatusColors.kt` |
| Valores hex | `theme/FixedStatusPalettes.kt` (internal) |
| Provider | `theme/AppThemeV2.kt` |
| Cartão | `summary/SummaryCard.kt` — `statusColors(status)` inline |
| Swatches | `theme/StatusColorSwatches.kt` |
| **Remover** | `summary/SummaryCardStatusColors.kt` |

## Ordem

1. `StatusColors.kt` + `FixedStatusPalettes.kt`
2. `AppThemeV2` — `LocalStatusColorRoles`
3. `SummaryCard.kt` — lookup + mapeamento inline; `typealias SummaryCardStatus = StatusKind`
4. Remover `SummaryCardStatusColors.kt`
5. Catálogo FR-010a + swatches + calibração light/dark

## Compilar

```bash
./gradlew :features:design-system-v2:compileKotlinJvm
```

## Checklist

- [ ] `MaterialTheme.statusColors(StatusKind.Info)` retorna 8 papéis M3
- [ ] `SummaryCard(status = …)` usa lookup directo — zero `resolve()`
- [ ] `typealias SummaryCardStatus = StatusKind` — compat 011
- [ ] Default calibrado SC-006
- [ ] Swatches 8×5
- [ ] Catálogo FR-010a
- [ ] `SummaryCardStatusColors.kt` removido
- [ ] Zero hex em `summary/`
- [ ] Auditoria SC-003: zero parâmetros `Color` na API pública alterada (`SummaryCard`, extensão `statusColors`)
- [ ] `AGENTS.md` menciona `StatusKind` e `MaterialTheme.statusColors(status)` para reutilização (FR-010)
- [ ] SC-002 validado visualmente em previews light/dark (sem teste automatizado de contraste)
