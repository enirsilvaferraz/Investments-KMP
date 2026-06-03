# Contract: Posição com transações (017)

**Feature**: `017-holding-transactions` | **Phase**: 1 | **Date**: 2026-06-03

Contrato mínimo para implementação paralela por camada — **sem** novos módulos Gradle.

---

## Domínio (`:domain:entity`)

```kotlin
public data class AssetHolding(
    public val id: Long,
    public val asset: Asset,
    public val owner: Owner,
    public val brokerage: Brokerage,
    public val goal: FinancialGoal? = null,
    public val transactions: List<AssetTransaction> = emptyList(),
)

public sealed interface AssetTransaction {
    public val id: Long
    public val date: LocalDate
    public val type: TransactionType
    public val observations: String?
    public val totalValue: Double
    // SEM holding
}
```

Subclasses concretas: remover parâmetro/propriedade `holding`.

---

## Port de transações (`:domain:usecases`)

```kotlin
public interface AssetTransactionRepository {
    public suspend fun upsert(holding: AssetHolding, transaction: AssetTransaction): Long
    public suspend fun delete(holding: AssetHolding, id: Long)
    public suspend fun getById(holding: AssetHolding, id: Long): AssetTransaction?
}
```

**Proibido** em `:domain:usecases`: chamar listagens de transações (SC-007).

---

## Casos de uso de escrita

```kotlin
public data class Param(val holding: AssetHolding, val transaction: AssetTransaction)
// SaveTransactionUseCase

public data class Param(val holding: AssetHolding, val id: Long)
// DeleteTransactionUseCase
```

---

## Leitura (consumidores)

```kotlin
// Histórico mensal
val entry: HoldingHistoryEntry = ...
val allTx: List<AssetTransaction> = entry.holding.transactions

// Filtro por mês (consumidor — ex. tabela Histórico)
val monthTx = allTx.filter {
    it.date.year == referenceDate.year && it.date.month == referenceDate.month
}

// Posição isolada
val holding: AssetHolding = getAssetHoldingUseCase(...).getOrThrow()
val txs: List<AssetTransaction> = holding.transactions
```

---

## Hidratação (`:data:database` — internal)

Responsabilidade exclusiva da camada de dados via **Room `@Relation`** em `AssetHoldingWithDetails`:

```kotlin
@Relation(
    entity = AssetTransactionEntity::class,
    parentColumns = ["id"],
    entityColumns = ["holdingId"],
)
val transactions: List<TransactionWithDetails> = emptyList()
```

1. DAOs `@Transaction` (`getAllWithAsset`, `getByIdWithDetails`, …) devolvem holding + transações + subtipos numa leitura.
2. `HoldingMappers.toDomain` mapeia com `TransactionWithDetails.toDomain()` **sem** `AssetHolding` na transação.
3. Ordenar `date` ↑, `id` ↑ no mapper (FR-007).
4. `AssetHoldingDataSourceImpl` não chama `AssetTransactionDao` para montar posição.

`AssetTransactionDataSource` mantém listagens **internal** só para escrita/legado; use cases não listam transações pelo port.

---

## Remoções obrigatórias

| Símbolo | Acção |
|---------|--------|
| `GetTransactionsUseCase` | Ausente / não reintroduzir |
| `GetTransactionsByHoldingUseCase` | Apagar classe + Koin |
| `AssetTransaction.holding` | Apagar |
| Listagens em `AssetTransactionRepository` | Apagar do port |

---

## Verificação estática (pós-implementação)

```bash
# Zero referências (SC-006)
rg 'GetTransactionsByHoldingUseCase|GetTransactionsUseCase' core/

# Port sem listagens (SC-007)
rg 'getAllByHolding|getByReferenceDate' core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/

# Contrato de transação (SC-005)
rg 'val holding' core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/AssetTransaction.kt
```

Build/test Gradle: **sob pedido** (constituição IX).
