# AGENTS.md — Investments-KMP

Seguir as orientações de @../.specify/memory/constitution.md

## Módulos de apresentação (referência)

| Gradle                       | Caminho                                                                                                          |
|------------------------------|------------------------------------------------------------------------------------------------------------------|
| `:features:design-system`    | `core/presentation/design-system/`                                                                               |
| `:features:design-system-v2` | `core/presentation/design-system-v2/` — M3 Expressive; `SummaryCard`, `AppThemeV2` (feature `011-summary-cards`); cores semânticas via `StatusKind` + `MaterialTheme.statusColors(status)` (feature `012-status-theme-colors`) |
| `:features:composeApp`       | `core/presentation/composeApp/`                                                                                  |
| `:features:asset-management` | `core/presentation/asset-management/`                                                                            |

`design-system-v2` é biblioteca Compose (sem `*Contract.kt` / sem registo obrigatório em `umbrellaApp`, como o v1).

## Cores semânticas de status (design-system-v2)

- **`StatusKind`** (`theme/StatusColors.kt`): enum único — `Default`, `Info`, `Warning`, `Positive`, `Negative`.
- **`MaterialTheme.statusColors(status)`**: lookup de 8 papéis M3 (`StatusColorRoles`); requer `AppThemeV2`.
- **`SummaryCardStatus`**: `typealias` de `StatusKind` em `summary/SummaryCard.kt` (compat 011).
- Novos componentes com status: importar `StatusKind` do tema; obter cores com `MaterialTheme.statusColors(status)` — **sem** parâmetros `Color` na API pública.