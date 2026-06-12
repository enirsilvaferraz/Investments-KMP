# Kotlin API Contracts: Importação de Nota de Corretagem JSON

**Feature**: 030-brokerage-note-import  
**Date**: 2026-06-12

---

## `:domain:entity` — Contratos alterados/novos

### `AssetTransaction` (alterado)

```kotlin
// package: com.eferraz.entities.transactions
public data class AssetTransaction(
    public val id: Long,
    public val date: LocalDate,
    public val type: TransactionType,
    public val quantity: Double,
    public val unitPrice: Double,
    public val allocatedFee: Double = 0.0,   // NOVO
) {
    public val grossValue: Double get() = quantity * unitPrice   // RENOMEADO (era totalValue)
    public val netValue: Double get() = when (type) {            // NOVO (derivado)
        TransactionType.PURCHASE -> grossValue + allocatedFee
        TransactionType.SALE     -> grossValue - allocatedFee
    }
}
```

### `BrokerageNoteAsset` (novo)

```kotlin
// package: com.eferraz.entities.brokeragenotes
public data class BrokerageNoteAsset(
    public val ticker: String,
    public val transaction: AssetTransaction,
)
```

### `BrokerageNote` (alterado)

```kotlin
// package: com.eferraz.entities.brokeragenotes
public data class BrokerageNote(
    val totalVolumeTraded: Double,
    val apportionableFees: Double,
    val withheldTaxes: Double,
    val netValue: Double,
    val assets: List<BrokerageNoteAsset>,   // ALTERADO: era List<AssetTransaction>
)
```

---

## `:domain:usecases` — Contratos alterados/novos

### `ImportBrokerageNoteUseCase` (substitui `LoadBrokerageNoteUseCase`)

```kotlin
// package: com.eferraz.usecases
@Factory
public class ImportBrokerageNoteUseCase(
    private val brokerageNoteRepository: BrokerageNoteRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, Unit>(dispatcher) {
    override suspend fun execute(param: Unit)
}
```

> **Nota**: injeta `AssetTransactionRepository` (não `SaveTransactionUseCase`) para garantir persistência atômica do lote via `saveAll` com `@Transaction` — chamar `SaveTransactionUseCase` N vezes não oferece rollback em falha parcial (ver research.md, Decisão 5).

**Contrato de comportamento**:
- Carrega nota via `BrokerageNoteRepository.loadNote()`.
- Calcula `NoteFeeAllocation`.
- Para cada `BrokerageNoteAsset`: busca holding por ticker; se não encontrado → loga e retorna sem persistir nenhuma transação.
- Acumula todos os pares `(AssetHolding, AssetTransaction)` e persiste o lote de uma vez via `assetTransactionRepository.saveAll(entries)`.
- Atomicidade garantida em dois níveis: early-return antes de qualquer insert se faltar holding; `@Transaction` no DAO para rollback automático em caso de falha de insert.

### `AssetHoldingRepository` (alterado)

```kotlin
// package: com.eferraz.usecases.repositories
public interface AssetHoldingRepository : AppCrudRepository<AssetHolding> {
    public suspend fun getByAssetId(assetId: Long): AssetHolding?
    public suspend fun getAllVariableIncomeAssets(): List<AssetHolding>
    public suspend fun getByAssetClass(assetClass: AssetClass): List<AssetHolding>
    public suspend fun getByGoalId(goalId: Long): List<AssetHolding>
    public suspend fun upsertWithTransactions(holding: AssetHolding)
    public suspend fun getByTicker(ticker: String): AssetHolding?   // NOVO
}
```

### `AssetTransactionRepository` (alterado)

```kotlin
// package: com.eferraz.usecases.repositories
public interface AssetTransactionRepository {
    public suspend fun upsert(holding: AssetHolding, transaction: AssetTransaction): Long
    public suspend fun delete(holding: AssetHolding, id: Long)
    public suspend fun getById(holding: AssetHolding, id: Long): AssetTransaction?
    public suspend fun saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>)  // NOVO
}
```

Implementado no DAO com `@Transaction` — garante rollback automático de todo o lote em caso de falha em qualquer insert.

---

## `:data:filestore` — Contratos alterados

### `BrokerageNoteV2Parser` (alterado)

```kotlin
// interno ao módulo — não é API pública
// BrokerageNote.assets muda para List<BrokerageNoteAsset>
internal object BrokerageNoteV2Parser {
    internal fun parse(note: BrokerageNoteDocument): BrokerageNote = BrokerageNote(
        // ...
        assets = note.assets.mapIndexed { index, asset ->
            BrokerageNoteAsset(
                ticker = asset.ticker,
                transaction = AssetTransaction(...)
            )
        },
    )
}
```

---

## `:data:database` — Migração Room

### Versão: 10 → 11

```kotlin
// AppDatabase.kt — adicionar à lista de autoMigrations:
AutoMigration(from = 10, to = 11)

// AssetTransactionEntity.kt — nova coluna:
@ColumnInfo(name = "allocatedFee", defaultValue = "0")
val allocatedFee: Double = 0.0,
```

---

## `:features:asset-management` — Contratos UI

### `TransactionDraftUi` (alterado)

```kotlin
internal data class TransactionDraftUi(
    // campos existentes...
    val allocatedFee: String = "0.0",   // NOVO — somente leitura
    val netValue: String = "",          // NOVO — derivado, somente leitura
)
```

### `TransactionManagementView` — nova coluna

Header e row adicionam coluna **"Valor Líq."** (somente leitura), após "Valor Total".
