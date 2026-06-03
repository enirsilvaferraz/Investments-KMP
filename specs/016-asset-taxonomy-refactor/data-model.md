# Data Model: Reestruturação da taxonomia de ativos

**Feature**: `016-asset-taxonomy-refactor` | **Phase**: 1 | **Date**: 2026-06-02

---

## Mapa conceitual

```text
Asset (sealed)
├── assetClass: AssetClass          # era category / InvestmentCategory
├── FixedIncomeAsset
│   ├── indexer: YieldIndexer       # era type / FixedIncomeAssetType (indexador)
│   └── type: FixedIncomeAssetType  # era subType / FixedIncomeSubType
├── VariableIncomeAsset
│   └── type: VariableIncomeAssetType : AssetType
└── InvestmentFundAsset
    └── type: InvestmentFundAssetType : AssetType

AssetType (interface marcadora, sem membros)
├── FixedIncomeAssetType   # produto RF
├── VariableIncomeAssetType
└── InvestmentFundAssetType

YieldIndexer (enum isolado, não implementa AssetType)
POST_FIXED | PRE_FIXED | INFLATION_LINKED
```

---

## Enums e contratos (`:domain:entity`)

| Tipo | Ficheiro | Valores (inalterados) |
|------|----------|------------------------|
| `AssetClass` | `AssetClass.kt` | `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND` |
| `YieldIndexer` | `YieldIndexer.kt` | `POST_FIXED`, `PRE_FIXED`, `INFLATION_LINKED` |
| `FixedIncomeAssetType` | `FixedIncomeAssetType.kt` | `CDB`, `LCI`, `LCA`, `LIG`, `DEBENTURE`, `SELIC`, `PRECATORIO` |
| `VariableIncomeAssetType` | (existente) + `AssetType` | `NATIONAL_STOCK`, … |
| `InvestmentFundAssetType` | (existente) + `AssetType` | `PENSION`, … |
| `AssetType` | `AssetType.kt` | (vazio) |

**Removidos**: `InvestmentCategory.kt`, `FixedIncomeSubType.kt`, indexador em `FixedIncomeAssetType.kt` antigo.

---

## `FixedIncomeAsset` (domínio)

| Propriedade | Tipo | Coluna Room |
|-------------|------|-------------|
| `assetClass` | `AssetClass` | `assets.asset_class` (via `Asset`) |
| `indexer` | `YieldIndexer` | `fixed_income_assets.indexer` |
| `type` | `FixedIncomeAssetType` | `fixed_income_assets.type` |
| Demais | inalterados | `expirationDate`, `contractedYield`, `b3Identifier`, … |

---

## Persistência Room (`:data:database`)

### `AssetEntity`

| Campo Kotlin | Coluna |
|--------------|--------|
| `assetClass` | `asset_class` |

### `FixedIncomeAssetEntity`

| Campo Kotlin | Coluna |
|--------------|--------|
| `indexer` | `indexer` |
| `type` | `type` |

### `AssetTransactionEntity`

| Campo Kotlin | Coluna |
|--------------|--------|
| `assetClass` | `asset_class` |

### Migração 6 → 7

Ver [research.md](./research.md) R2. `AppDatabase` + `Migration6To7`.

### Mapeamento (`AssetMappers.kt`)

- `toDomain()`: `indexer`, `type` (produto), `assetClass` em `Asset`.
- `toEntity()`: espelhar nomes; **sem** lógica nova.

---

## DTOs use case (`HistoryTableData`)

| Classe | Campo antigo | Campo novo |
|--------|--------------|------------|
| `HistoryTableData` | `category` | `assetClass` |
| `FixedIncomeHistoryTableData` | `type` (indexador) | `indexer: YieldIndexer` |
| `FixedIncomeHistoryTableData` | `subType` | `type: FixedIncomeAssetType` |

`HoldingHistoryView` / filtros 015: atualizar referências a `InvestmentCategory`, `FixedIncomeSubType`, indexador legado.

---

## Repositórios e ports

| API antiga | API nova |
|------------|----------|
| `getByType(category: InvestmentCategory)` | `getByAssetClass(assetClass: AssetClass)` |
| `getByCategory(category)` | `getByAssetClass(assetClass)` |

Preferir rename direto (sem overload deprecated) — refactor único na branch.

---

## Naming / UI (`FieldLabels.kt`)

| Função antiga | Função nova |
|---------------|-------------|
| `InvestmentCategory.asLabel()` | `AssetClass.asLabel()` |
| `FixedIncomeAssetType.asLabel()` (indexador) | `YieldIndexer.asLabel()` |
| `FixedIncomeSubType.asLabel()` | `FixedIncomeAssetType.asLabel()` (produto) |

Cadastro RF: rótulos **Indexador** e **Tipo** (produto) — strings em naming, não em `AssetType`.

---

## Documentação

[core/domain/entity/docs/DOMAIN.md](../../core/domain/entity/docs/DOMAIN.md): §2, §5, §6.5, §9.1 — substituir vocabulário; diagrama `FixedIncomeAsset` com `indexer` + `type`.

---

## Discriminador persistido (mappers)

| Fonte | Ficheiro | Valores TEXT |
|-------|----------|--------------|
| Ativos + transações | `AssetMappers.kt` → `PersistedAssetClass` | `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND` |
| Consumidor | `TransactionMappers.kt` | Mesmas constantes (não usar `FUNDS`) |

Migração 6→7: ver [research.md](./research.md) R5 (`FUNDS` → `INVESTMENT_FUND` em `asset_transactions`).

---

## Fora de escopo (data model)

- Novos membros de enum (produto ou indexador).
- Alterar regras de `UpsertAssetUseCase` além de tipos/campos renomeados.
