# Data Model: Importação de Nota de Corretagem JSON

**Feature**: 030-brokerage-note-import  
**Date**: 2026-06-12

---

## Entidades alteradas

### `AssetTransaction` — `:domain:entity`

**Alteração**: novo campo `allocatedFee`.

```
AssetTransaction
├── id: Long
├── date: LocalDate
├── type: TransactionType
├── quantity: Double
├── unitPrice: Double
├── allocatedFee: Double = 0.0   ← NOVO (taxa proporcional alocada da nota)
└── (derived) grossValue: Double   = quantity × unitPrice   ← RENOMEADO (era totalValue)
└── (derived) netValue: Double     = grossValue ± allocatedFee (PURCHASE: +, SALE: −)
```

**Regras**:
- `allocatedFee` padrão `0.0` — retrocompatível com todas as transações manuais existentes.
- `grossValue` e `netValue` são derivados, nunca persistidos.
- **Impacto de renomeação**: todos os usos de `totalValue` no codebase devem ser atualizados para `grossValue` (mappers, UI, testes).

---

### `BrokerageNoteAsset` — `:domain:entity` (novo tipo)

**Novo tipo** no pacote `com.eferraz.entities.brokeragenotes`:

```
BrokerageNoteAsset
├── ticker: String         ← código de negociação (ex: "PETR4")
└── transaction: AssetTransaction
```

**Propósito**: carregar o ticker junto à transação dentro do contexto exclusivo de nota de corretagem, sem poluir `AssetTransaction` com dado específico de nota.

---

### `BrokerageNote` — `:domain:entity`

**Alteração**: `assets` muda de `List<AssetTransaction>` para `List<BrokerageNoteAsset>`.

```
BrokerageNote
├── totalVolumeTraded: Double
├── apportionableFees: Double
├── withheldTaxes: Double
├── netValue: Double
└── assets: List<BrokerageNoteAsset>   ← ALTERADO (era List<AssetTransaction>)
```

**Impacto em cascata**:
- `BrokerageNoteV2Parser`: mapear `asset.ticker` → `BrokerageNoteAsset`.
- `NoteFeeAllocation`: adaptar para extrair `it.transaction` onde antes usava `it` diretamente.

---

### `AssetTransactionEntity` — `:data:database` (Room v11)

**Alteração**: nova coluna `allocatedFee` com `defaultValue = "0"`.

```sql
-- Migração: v10 → v11 (AutoMigration)
ALTER TABLE asset_transactions
  ADD COLUMN allocatedFee REAL NOT NULL DEFAULT 0;
```

```
AssetTransactionEntity (tabela: asset_transactions)
├── id: Long  [PK autoGenerate]
├── holdingId: Long  [FK → AssetHoldingEntity.id, CASCADE]
├── transactionDate: LocalDate
├── type: TransactionType
├── quantity: Double  [default 1]
├── unitPrice: Double  [default 0]
└── allocatedFee: Double  [default 0]   ← NOVO
```

---

### `TransactionDraftUi` — `:features:asset-management`

**Alteração**: novos campos `allocatedFee` e `netValue` (somente leitura / derivado).

```
TransactionDraftUi
├── id: Long?
├── assetClass: AssetClass
├── isNew: Boolean
├── dateDigits: String
├── type: TransactionType
├── quantity: String
├── unitPrice: String
├── grossValue: String    (somente leitura — quantity × unitPrice)  ← RENOMEADO (era totalValue)
├── allocatedFee: String  (somente leitura — preenchido no import; "0.0" no manual)  ← NOVO
└── netValue: String      (somente leitura — grossValue ± allocatedFee)              ← NOVO
```

---

## Novo caso de uso

### `ImportBrokerageNoteUseCase` — `:domain:usecases`

**Substitui** o stub `LoadBrokerageNoteUseCase`.

**Parâmetro**: `Unit`  
**Retorno**: `Unit` (falhas logadas no console via `println`)

**Fluxo interno**:
```
1. brokerageNoteRepository.loadNote()
       → BrokerageNote (com assets: List<BrokerageNoteAsset>)
2. NoteFeeAllocation.calculate(note)
       → Map<AssetTransaction, Double>  (transação → netValue)
3. Para cada BrokerageNoteAsset em note.assets:
   a. assetHoldingRepository.getByTicker(ticker)
      → AssetHolding? (brokerage = Nubank id=2 filtrado na implementação)
      → Se null: println("Holding not found for ticker=$ticker"); return (sem persistir nada)
   b. Construir AssetTransaction com allocatedFee calculado
   c. Acumular par (holding, transaction) na lista entries
4. assetTransactionRepository.saveAll(entries)
       → persiste TODAS as transações em uma única transação de banco de dados (@Transaction)
       → se qualquer insert falhar: rollback automático, nenhuma transação é gravada
5. println("Import complete: ${entries.size} transactions saved")
```

**Atomicidade**: garantida em dois níveis:
- **Use case** (early-return): se qualquer ticker não for encontrado antes de persistir, a lista nunca chega ao banco.
- **Banco de dados** (`@Transaction`): o método `saveAll` executa todos os inserts em uma única transação Room; qualquer falha de insert faz rollback automático de todo o lote.

---

## Interfaces de repositório alteradas

### `AssetHoldingRepository` — `:domain:usecases`

**Novo método**:
```
getByTicker(ticker: String): AssetHolding?
```
Implementado em `:data:repositories` com JOIN entre `AssetHoldingEntity` e a tabela de assets, filtrando por `ticker` e `brokerage.id == 2`.

### `AssetTransactionRepository` — `:domain:usecases`

**Novo método**:
```
saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>)
```
Implementado no DAO com `@Transaction` — todos os inserts do lote em uma única transação SQLite. Rollback automático em caso de falha.

---

## Teste de migração Room v10 → v11

**Arquivo**: `core/data/database/src/jvmTest/kotlin/com/eferraz/database/migrations/Migration10To11Test.kt`

Segue o padrão de `Migration9To10Test` já existente no projeto.

**Cenários obrigatórios**:

| Caso | GIVEN | WHEN | THEN |
|------|-------|------|------|
| Transações existentes mantêm dados | Banco v10 com linhas em `asset_transactions` | Migrar para v11 | `quantity`, `unitPrice`, `type`, `transactionDate` preservados; `allocatedFee = 0.0` em todas as linhas |
| Nova coluna com valor padrão | Banco v10 com N linhas | Migrar para v11 | `SELECT allocatedFee FROM asset_transactions` retorna `0.0` para cada linha |
| Banco vazio migra sem erro | Banco v10 sem linhas | Migrar para v11 | Migração completa; `asset_transactions` existe com esquema v11 |

**Estrutura do teste** (baseada no padrão `Migration9To10Test`):
```kotlin
class Migration10To11Test {
    // BeforeTest: createTempFile, MigrationTestHelper(AppDatabase, BundledSQLiteDriver)
    // AfterTest: deleteIfExists
    //
    // Constantes: START_VERSION = 10, END_VERSION = 11
    // helper.createDatabase(10) → seed com linhas existentes → close
    // helper.runMigrationsAndValidate(version = 11) → assertar allocatedFee = 0.0
    //
    // Sem Migration spec customizada: AutoMigration; migrations = emptyList()
}
```

---

## Diagrama de fluxo de dados

```
JSON (fixture/file)
    └─► BrokerageNoteJsonDataSource.loadNote()
            └─► BrokerageNoteV2Parser.parse(doc)
                    └─► BrokerageNote { assets: List<BrokerageNoteAsset> }
                            │
                            ▼
                    NoteFeeAllocation.calculate(note)
                            │
                            ▼
                    ImportBrokerageNoteUseCase
                            │
                            ├─► AssetHoldingRepository.getByTicker(ticker)
                            │       └─► AssetHolding (Nubank)
                            │
                            └─► SaveTransactionUseCase(holding, transaction+fee)
                                    └─► Room: asset_transactions (v11)
```
