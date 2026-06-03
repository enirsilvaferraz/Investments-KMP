# Research: Reestruturação da taxonomia de ativos

**Feature**: `016-asset-taxonomy-refactor` | **Phase**: 0 | **Date**: 2026-06-02

---

## R1 — Ordem de renomeação Kotlin (colisão `FixedIncomeAssetType`)

**Decision**: Sequência em **um** subagente de entidade (não paralelizar ficheiros de `entity`):

1. `InvestmentCategory.kt` → `AssetClass.kt` (rename tipo + ficheiro).
2. Criar `YieldIndexer.kt` com membros do antigo `FixedIncomeAssetType`; **eliminar o ficheiro** `FixedIncomeAssetType.kt` (indexador) por completo — não deixar ficheiro parcial.
3. Renomear `FixedIncomeSubType.kt` → `FixedIncomeAssetType.kt` (enum de produto CDB, LCI, …) — só após passo 2.
4. Criar `AssetType.kt` (interface vazia); `FixedIncomeAssetType`, `VariableIncomeAssetType`, `InvestmentFundAssetType` implementam `AssetType`.
5. Atualizar `Asset.assetClass`, `FixedIncomeAsset.indexer` + `FixedIncomeAsset.type`.

**Rationale**: Dois tipos não podem chamar-se `FixedIncomeAssetType` ao mesmo tempo; indexador sai primeiro para libertar o nome do ficheiro.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| `typealias` temporário | Duplica semântica e atrasa remoção |
| Manter `FixedIncomeSubType` | Contradiz clarificação Q3 |

---

## R2 — Migração Room 6 → 7

**Decision**: `AppDatabase.version = 7` + `AutoMigration(from = 6, to = 7, spec = Migration6To7::class)` com anotações `@RenameColumn` (Room 3 / `androidx.room3.migration.AutoMigrationSpec`):

| Tabela | De | Para |
|--------|-----|------|
| `assets` | `category` | `asset_class` |
| `asset_transactions` | `category` | `asset_class` |
| `fixed_income_assets` | `type` | `indexer` |
| `fixed_income_assets` | `subType` | `type` |

Valores TEXT/enum persistidos **inalterados**. Índices em `category` recriados via schema export em `asset_class`.

**Rationale**: Alinha SQL ao vocabulário de domínio (clarificação Q1/Q4); evita `@ColumnInfo` com nomes legados.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| Só rename Kotlin | Contradiz spec/FR-004 |
| Migração SQL manual `Migration` | Mais código que `RenameColumn` quando suficiente |

**Nota**: Após alterar entidades Room, gerar `schemas/.../AppDatabase/7.json` — **só** quando utilizador ou tarefa pedir build (princípio IX).

**Falha de migração**: `Migration6To7` usa apenas `@RenameColumn` (sem reescrita de valores); se a migração falhar, Room não deve deixar a app num schema intermédio inválido — validar em dispositivo com DB v6 (quickstart §3).

---

## R3 — `AssetType` marcadora

**Decision**: `public interface AssetType` sem membros; três enums `enum class X : AssetType`.

**Rationale**: Clarificação Q5; rótulos já em `FieldLabels.kt` (`YieldIndexer.asLabel()`, `FixedIncomeAssetType.asLabel()` para produto).

**Alternatives considered**: Propriedade `code` comum — rejeitada (YAGNI, duplica persistência enum name).

---

## R4 — DTOs de histórico (`HistoryTableData`)

**Decision**: Renomear campos em `FixedIncomeHistoryTableData`:

- `type: FixedIncomeAssetType` (indexador legado) → `indexer: YieldIndexer`
- `subType: FixedIncomeSubType` → `type: FixedIncomeAssetType` (produto)
- `category: InvestmentCategory` → `assetClass: AssetClass`

**Rationale**: Mesma linguagem que `FixedIncomeAsset`; reduz confusão em filtros 015.

**Alternatives considered**: Manter nomes antigos nos DTOs — rejeitado (dívida imediata).

---

## R5 — Discriminador de transações alinhado a ativos

**Decision**: Unificar o valor persistido de fundos em `asset_transactions` de `FUNDS` para `INVESTMENT_FUND`, igual a `assets.category` / `AssetMappers.kt`. Introduzir `PersistedAssetClass` (ou constantes equivalentes) em `AssetMappers.kt` como **fonte única**; `TransactionMappers.kt` consome as mesmas constantes.

**Rationale**: Hoje `AssetMappers` grava `INVESTMENT_FUND` e `TransactionMappers` grava `FUNDS` — assimetria pré-existente que confunde índices e queries por `asset_class`. A migração 6→7 já toca `asset_transactions`; incluir `onPostMigrate`:

```sql
UPDATE asset_transactions SET category = 'INVESTMENT_FUND' WHERE category = 'FUNDS'
```

(ajustar nome da coluna após `@RenameColumn` → `asset_class` se o SQL correr pós-rename).

**Alternatives considered**: Manter `FUNDS` e só renomear coluna — rejeitado (dívida e divergência com `AssetClass.INVESTMENT_FUND`).

---

## R6 — Simplicidade e paralelismo

**Decision**: Uma onda bloqueante (entidade) + três subagentes paralelos (DB, use cases+testes, presentation) + naming no mesmo pacote que presentation ou subagente **N** dedicado.

**Rationale**: ~45 ficheiros `.kt`; find-replace mecânico após tipos estáveis; evita 5 subagentes com overlap.

**Alternatives considered**: Subagente por módulo Gradle (8+) — overhead de coordenação > ganho.
