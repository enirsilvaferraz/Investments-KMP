# Data Model: Identificador B3 em Renda Fixa

**Feature**: `004-fixed-income-b3-id` | **Phase**: 1 | **Date**: 2026-05-24

---

## Decisões arquiteturais

| Decisão | Escolha |
|---------|---------|
| Persistência | `fixed_income_assets.b3_identifier` (`TEXT NULL`) |
| Domínio | `FixedIncomeAsset.b3Identifier: String?` apenas (não na interface `Asset`) |
| Migração | Room DB v5 → v6, `AutoMigration` |
| Normalização | `trim()` + `ifBlank { null }` ao salvar e ao avaliar “informado” |
| Histórico (view) | `HoldingHistoryView.b3IdentifierStatus: B3IdentifierStatus` |
| Histórico (UI) | Coluna única à direita; RF com ícones; RV/fundos com célula vazia |

---

## Entidade de domínio

### `FixedIncomeAsset` (`:domain:entity`)

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `b3Identifier` | `String?` | Não | Texto livre; valor persistido após `trim()`; `null` se vazio após trim |

Demais campos inalterados. `VariableIncomeAsset` e `InvestmentFundAsset` **não** ganham o atributo.

**Documentação**: manter [core/domain/entity/docs/DOMAIN.md](../../../core/domain/entity/docs/DOMAIN.md) alinhado (§5 invariantes, diagrama ER `FixedIncomeAsset` em §9.1) — ver checklist em [plan.md](./plan.md).

---

## Persistência Room (`:data:database`)

### `FixedIncomeAssetEntity`

```kotlin
@ColumnInfo(name = "b3_identifier")
val b3Identifier: String? = null
```

### Migração 5 → 6

| Item | Valor |
|------|--------|
| `AppDatabase.version` | `6` |
| `autoMigrations` | `AutoMigration(from = 5, to = 6)` |
| Efeito em dados legados | Todas as linhas RF existentes com `b3_identifier = NULL` |
| Schema export | `schemas/com.eferraz.database.core.AppDatabase/6.json` |

> **Checklist implementação**: após alterar entidades, compilar `:data:database` e commitar o JSON do schema gerado pelo Room.

### Mapeamento (`AssetMappers.kt`)

- `AssetWithDetails.toDomain()` → `FixedIncomeAsset(..., b3Identifier = fixedIncome.b3Identifier)`
- `FixedIncomeAsset.toEntity()` → `FixedIncomeAssetEntity(..., b3Identifier = b3Identifier?.trim()?.ifBlank { null })`

---

## Modelo de apresentação — cadastro

### `AssetManagementUiState`

| Campo | Tipo | Visível quando |
|-------|------|----------------|
| `b3Identifier` | `String?` | `category == FIXED_INCOME` (campo em `FixedIncomeFields`) |

### `AssetManagementEvents`

- `B3IdentifierChanged(value: String)` — espelha `ObservationsChanged`.

### Fluxo

```
UI (FormTextField) → ViewModel → UiState.b3Identifier
  → buildFixedIncomeAsset() → FixedIncomeAsset.b3Identifier
  → UpsertAssetUseCase → AssetRepository → Room
```

---

## Modelo de apresentação — histórico

### `B3IdentifierStatus` (novo, `:domain:usecases`)

```kotlin
public sealed interface B3IdentifierStatus {
    public data class Informed(val value: String) : B3IdentifierStatus
    public data object NotInformed : B3IdentifierStatus
    public data object NotApplicable : B3IdentifierStatus  // RV, fundos — célula vazia
}
```

### `FixedIncomeHistoryTableData`

Adicionar:

```kotlin
public val b3Identifier: String?  // null = não informado
```

Preenchido em `GetHistoryTableDataUseCase` a partir de `FixedIncomeAsset.b3Identifier`.

### `HoldingHistoryView`

| Campo | Tipo | Origem |
|-------|------|--------|
| `b3IdentifierStatus` | `B3IdentifierStatus` | `Informed` / `NotInformed` se RF; `NotApplicable` se RV ou fundo |

Construtor secundário `HoldingHistoryView(HistoryTableData)` atualizado.

---

## Diagrama de relações

```text
┌─────────────────────┐       1:1        ┌──────────────────────────┐
│     AssetEntity     │◄─────────────────│  FixedIncomeAssetEntity  │
│  (assets)           │                  │  + b3_identifier TEXT?   │
└─────────────────────┘                  └──────────────────────────┘
         │                                           │
         │ toDomain()                                │
         ▼                                           ▼
┌─────────────────────┐                  ┌──────────────────────────┐
│ FixedIncomeAsset    │                  │  b3Identifier: String?   │
│ (domain)            │                  └──────────────────────────┘
└─────────────────────┘
         │
         │ GetHistoryTableDataUseCase
         ▼
┌─────────────────────┐     map      ┌──────────────────────────┐
│ HoldingHistoryView  │─────────────►│ B3IdentifierStatus       │
└─────────────────────┘              │ Informed | NotInformed   │
                                     │ NotApplicable (RV/Fund)  │
                                     └──────────────────────────┘
```

---

## Validação

| Regra | Onde |
|-------|------|
| Campo opcional no save | `AssetManagementViewModel` — sem erro de campo |
| `trim()` + `ifBlank → null` | `buildFixedIncomeAsset()` / mapper `toEntity()` |
| RF only no formulário | `FixedIncomeFields` — campo não renderizado em RV/Fund |
| Coluna histórico vazia (não RF) | `AssetHistoryScreen.Table` — `when (status)` |

---

## Fora do modelo

- Export CSV de renda fixa (não incluir `b3Identifier` nesta entrega).
- Importação B3 (`003-b3-import`) — sem ligação automática ao campo.
