# Tasks: Cores semânticas de status no tema v2 (simplificado)

**Input**: `/specs/012-status-theme-colors/`

**Superfície mínima**: `StatusKind` + `StatusColorRoles` + `FixedStatusPalettes` + `MaterialTheme.statusColors(status)` — **sem** resolvedores nem DTOs intermédios.

---

## Phase 1: Setup

- [X] T001 Run baseline `./gradlew :features:design-system-v2:compileKotlinJvm` from repository root
- [X] T002 [P] Note Default colors from `summary/SummaryCardStatusColors.kt` for SC-006 calibration

---

## Phase 2: Tema (bloqueia tudo)

- [X] T003 Create `StatusKind`, `StatusColorRoles`, `LocalStatusColorRoles`, and `MaterialTheme.statusColors(status: StatusKind)` in `theme/StatusColors.kt`
- [X] T004 Implement `FixedStatusPalettes` (8 M3 roles × 5 kinds × light/dark) in `theme/FixedStatusPalettes.kt`
- [X] T005 Wire `LocalStatusColorRoles` provider in `theme/AppThemeV2.kt`

**Checkpoint**: `MaterialTheme.statusColors(StatusKind.Info)` works inside `AppThemeV2`

---

## Phase 3: US1 + US2 — Cartão (P1) 🎯 MVP

- [X] T006 [US1][US2] Add `public typealias SummaryCardStatus = StatusKind` and replace color resolution with inline `MaterialTheme.statusColors(status)` in `summary/SummaryCard.kt`
- [X] T007 [US2] Apply M3 roles inline (container, onContainer, onFixedVariant, fixed, onFixed) to card UI in `summary/SummaryCard.kt`
- [X] T008 [US1][US2] Delete `summary/SummaryCardStatusColors.kt`
- [X] T009 [US1][US2] Add private preview with four semantic statuses in `summary/SummaryCard.kt`

**Checkpoint**: `SummaryCard(status = StatusKind.Positive)` renders green — zero intermediate classes

---

## Phase 4: US5 — Swatches (P2)

- [X] T010 [P] [US5] Create swatches preview (8 M3 roles × 5 status) using `statusColors(status)` in `theme/StatusColorSwatches.kt`

---

## Phase 5: US4 — Catálogo (P2)

- [X] T011 [US4] Add `status: StatusKind` to catalog items + FR-010a mapping in `summary/SummaryCardCatalog.kt`
- [X] T012 [US4] Remove "Edge Case" entry and update catalog preview to pass `item.status` in `summary/SummaryCard.kt`

---

## Phase 6: US3 — Light/dark (P2)

- [X] T013 [US3] Calibrate Default + semantic tonal scales in `theme/FixedStatusPalettes.kt`
- [X] T014 [US3] Validate previews per `quickstart.md` checklist

---

## Phase 7: Polish

- [X] T015 [P] Verify no `import …summary` in `theme/` files
- [X] T016 [P] Audit public APIs in `design-system-v2` for stray `Color` params on status-related composables (SC-003, FR-007)
- [X] T017 [P] Update `AGENTS.md` — document `StatusKind`, `MaterialTheme.statusColors(status)` and consumption pattern for other components (FR-010, Princípio VII)
- [X] T018 Run `./gradlew :features:design-system-v2:compileKotlinJvm`

---

## Mapeamento inline (referência)

```kotlin
val roles = MaterialTheme.statusColors(status)
// fundo → roles.container | valor → roles.onContainer
// título/borda → roles.onFixedVariant | legenda → roles.onFixedVariant @ 75%
// badge → roles.fixed / roles.onFixed
```

## Removidos vs. design anterior

| Removido | Substituído por |
|----------|-----------------|
| `AppStatusColors` | `MaterialTheme.statusColors(status)` |
| `SummaryCardStatus` enum | `typealias = StatusKind` |
| `StatusColorPalette` | inline no composable |
| `SummaryCardStatusColors` | eliminado |
| `resolve()`, `toStatusKind()`, `toSummaryCardPalette()` | eliminados |

**Total: 18 tarefas** (vs. 24 anterior)
