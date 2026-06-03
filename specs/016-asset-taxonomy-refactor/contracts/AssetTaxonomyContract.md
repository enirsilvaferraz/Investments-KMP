# Contract: Taxonomia de ativos (016)

**Feature**: `016-asset-taxonomy-refactor` | **Phase**: 1 | **Date**: 2026-06-02

Contrato mínimo para subagentes e revisão — **sem** novos módulos Gradle.

---

## Tipos públicos (`com.eferraz.entities.assets`)

```kotlin
public enum class AssetClass { FIXED_INCOME, VARIABLE_INCOME, INVESTMENT_FUND }

public enum class YieldIndexer { POST_FIXED, PRE_FIXED, INFLATION_LINKED }

public interface AssetType

public enum class FixedIncomeAssetType : AssetType {
    CDB, LCI, LCA, LIG, DEBENTURE, SELIC, PRECATORIO
}

// VariableIncomeAssetType, InvestmentFundAssetType : AssetType (existentes)
```

```kotlin
public sealed interface Asset {
    public val assetClass: AssetClass  // era category
    // id, issuer, observations
}

public data class FixedIncomeAsset(
    public val indexer: YieldIndexer,
    public val type: FixedIncomeAssetType,
    // ...
) : Asset {
    override val assetClass: AssetClass = AssetClass.FIXED_INCOME
}
```

---

## Persistência (Room v7)

| Tabela | Coluna | Tipo Kotlin |
|--------|--------|-------------|
| `assets` | `asset_class` | `String` / `AssetClass` em queries |
| `fixed_income_assets` | `indexer` | `YieldIndexer` |
| `fixed_income_assets` | `type` | `FixedIncomeAssetType` |
| `asset_transactions` | `asset_class` | `String` — `FIXED_INCOME`, `VARIABLE_INCOME`, `INVESTMENT_FUND` (igual a `assets`) |

Migração: `AutoMigration(6, 7, Migration6To7)` + `onPostMigrate`: `FUNDS` → `INVESTMENT_FUND` em `asset_transactions` (ver [research.md](../research.md) R5).

**Mappers**: `PersistedAssetClass` (ou equivalente) definido em `AssetMappers.kt`; `TransactionMappers.kt` reutiliza — não duplicar literais.

---

## Ports (`:domain:usecases`)

```kotlin
// AssetRepository / AssetHoldingRepository / DataSources
suspend fun getByAssetClass(assetClass: AssetClass): ...
```

**Proibido** após merge: imports de `InvestmentCategory`, `FixedIncomeSubType`, indexador via `FixedIncomeAssetType`.

---

## Presentation

| Área | Regra |
|------|--------|
| `AssetManagement*` | `AssetClassChanged`, `YieldIndexerChanged`, `FixedIncomeProductTypeChanged` (ou manter event names curtos alinhados ao VM); RF: dois dropdowns distintos |
| `FieldLabels` | `YieldIndexer.asLabel()`, `AssetClass.asLabel()`, produto via `FixedIncomeAssetType.asLabel()` |
| `WalletFilters` / 015 | Facetas usam `AssetClass`; subtipo RF = `FixedIncomeAssetType` |
| `TableIcons` | `AssetClass.BuildIcon()` (rename de `InvestmentCategory`) |

---

## Testes obrigatórios (escrever; executar sob pedido)

| Ficheiro | Mínimo |
|----------|--------|
| `UpsertAssetUseCaseTest` | RF com `indexer` + `type` persistidos |
| `GetHistoryTableDataUseCaseTest` | Linha RF expõe `indexer`/`type` renomeados |
| `WalletHistoryFilterTest` | Compila com `AssetClass` (ajuste imports) |
| `HoldingHistoryViewTest` | Idem |

---

## Checklist de done (subagente)

- [ ] Zero referências a `InvestmentCategory`, `FixedIncomeSubType` no `core/` (exceto specs históricas).
- [ ] `FixedIncomeAsset` sem propriedade `subType` nem `type` indexador legado.
- [ ] `DOMAIN.md` §9.1 atualizado.
- [ ] Queries SQL/Dao usam `asset_class`, não `category`.
