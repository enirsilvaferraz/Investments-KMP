# Data Model: Feature 022 — Integração do Formulário de Transações

## Entidades existentes (sem alteração estrutural)

### `AssetTransaction` (domínio — `:domain:entity`)

```kotlin
sealed interface AssetTransaction {
    val id: Long          // 0L para novos
    val date: LocalDate
    val type: TransactionType   // PURCHASE | SALE
    val totalValue: Double
    val observations: String?
}

// Subtipos
data class FixedIncomeTransaction(id, date, type, totalValue, observations) : AssetTransaction
data class FundsTransaction(id, date, type, totalValue, observations) : AssetTransaction
data class VariableIncomeTransaction(id, date, type, quantity, unitPrice, observations) : AssetTransaction
//   totalValue = quantity * unitPrice (derivado)
```

### `AssetHolding` (domínio — `:domain:entity`)

```kotlin
data class AssetHolding(
    val id: Long,
    val asset: Asset,
    val owner: Owner,
    val brokerage: Brokerage,
    val goal: FinancialGoal? = null,
    val transactions: List<AssetTransaction> = emptyList(),  // ← inclui transações no save
)
```

---

## Modelo de UI — alterações e adições

### `TransactionDraftUi` — unificado em `AssetManagementUiState.kt`

**Arquivo:** `assets/AssetManagementUiState.kt` (sem arquivo separado)

Conteúdo extraído de `transactions/TransactionManagementUiState.kt` e adicionado ao final de `AssetManagementUiState.kt`, **sem alteração de campos**:

```kotlin
internal data class TransactionDraftUi(
    val id: Long? = null,
    val assetClass: AssetClass,
    val isNew: Boolean = false,
    val dateDigits: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val quantity: String = "",
    val unitPrice: String = "",
    val totalValue: String = "",
    val observations: String = "",
)
// + propriedades derivadas: dateError, quantityError, unitPriceError, totalValueError
// + companion: fromDomain(), fun toDomainTransaction()
// + fun hasAnyFieldError()
// + fun syncVariableIncomeTotal()
```

### `AssetManagementUiState` — campos adicionados

```kotlin
// Novos campos em AssetManagementUiState
val transactions: List<TransactionDraftUi> = emptyList(),
val saveError: String? = null,
```

**Sem `initialSnapshot`:** o diff de quais transações deletar é calculado internamente pelo DataSource (consulta o banco e compara com o que chegou). O ViewModel não precisa rastrear o estado anterior.

**Regras derivadas (sem novo campo):**
- `hasAnyTransactionFieldError: Boolean` — `transactions.any { it.hasAnyFieldError() }` — calculado na validação, não armazenado no estado.

**Alteração em `partialResetForAssetClass`:** adicionar `transactions = emptyList(), saveError = null` (FR-011).

### `AssetManagementEvents` — eventos de transação adicionados

```kotlin
// Eventos novos (migrados de TransactionManagementEvents)
data class TransactionAdded(val assetClass: AssetClass) : AssetManagementEvents
data class TransactionRemoved(val index: Int) : AssetManagementEvents
data class TransactionDateChanged(val index: Int, val digits: String) : AssetManagementEvents
data class TransactionTypeChanged(val index: Int, val type: TransactionType) : AssetManagementEvents
data class TransactionQuantityChanged(val index: Int, val value: String) : AssetManagementEvents
data class TransactionUnitPriceChanged(val index: Int, val value: String) : AssetManagementEvents
data class TransactionTotalValueChanged(val index: Int, val value: String) : AssetManagementEvents
```

---

## Port — alteração de interface

### `AssetHoldingRepository` (`:domain:usecases`)

```kotlin
// Novo método adicionado à interface existente
suspend fun upsertWithTransactions(holding: AssetHolding)
```

O holding passado **já contém** `transactions` com os registros a serem persistidos (mapeados de `TransactionDraftUi`). O método delega ao DataSource, que internamente consulta o banco, calcula o diff e sincroniza as transações numa única transação Room.

---

## Use case — novo

### `SaveAssetWithTransactionsUseCase` (`:domain:usecases`)

```kotlin
public class SaveAssetWithTransactionsUseCase(
    private val upsertAssetUseCase: UpsertAssetUseCase,
    private val assetHoldingRepository: AssetHoldingRepository,
) : AppUseCase<SaveAssetWithTransactionsUseCase.Param, Unit> {

    // Param único — AssetHolding já carrega asset e transactions
    public data class Param(val holding: AssetHolding)

    override suspend fun execute(param: Param) {
        val savedAsset = upsertAssetUseCase(UpsertAssetUseCase.Param(param.holding.asset))
        assetHoldingRepository.upsertWithTransactions(
            param.holding.copy(asset = savedAsset),
        )
    }
}
```

**Responsabilidade:** orquestrar asset → holding+transações. O diff é calculado pelo DataSource.

---

## Regras de validação (sem alteração de lógica — apenas novo ponto de chamada)

| Campo | Regra | Bloqueio |
|---|---|---|
| Emissor, corretora, tipo, etc. | `Validations.checkErrors(state)` (existente) | Sim — exibe erro por campo |
| Data da transação | `TransactionDraftUi.dateError` | Sim — bloqueia save |
| Quantidade (RV) | `TransactionDraftUi.quantityError` | Sim |
| Valor unitário (RV) | `TransactionDraftUi.unitPriceError` | Sim |
| Valor total (RF/Fundo) | `TransactionDraftUi.totalValueError` | Sim |
| Lista vazia de transações | — | Não — save com lista vazia é válido (Edge Case) |

---

## Mapeamento de estado para domínio (no ViewModel, no momento do save)

```kotlin
// No AssetManagementViewModel.onSave() — param único
val domainTransactions = state.transactions.mapNotNull { it.toDomainTransaction(state.assetClass) }
val holding = state.buildHolding().copy(transactions = domainTransactions)
saveAssetWithTransactionsUseCase(SaveAssetWithTransactionsUseCase.Param(holding))
// O use case extrai holding.asset internamente para o UpsertAssetUseCase
```

O diff de deleção é calculado no DataSource, não aqui.

---

## DataSource — novo método (`:data:database`)

### `AssetHoldingDataSource` — interface

```kotlin
// Novo método adicionado
public suspend fun saveWithTransactions(assetHolding: AssetHolding)
```

### `AssetHoldingDataSourceImpl` — implementação

```kotlin
@Transaction
override suspend fun saveWithTransactions(assetHolding: AssetHolding) {
    val holdingId = save(assetHolding)                         // upsert do holding
    val existingIds = assetTransactionDataSource
        .getAllByHolding(assetHolding)
        .map { it.id }
        .toSet()
    val incomingIds = assetHolding.transactions.map { it.id }.toSet()
    val toDelete = existingIds - incomingIds
    toDelete.forEach { assetTransactionDataSource.delete(holdingId, it) }
    assetHolding.transactions.forEach {
        assetTransactionDataSource.save(assetHolding.copy(id = holdingId), it)
    }
}
```

`AssetHoldingDataSourceImpl` injeta `AssetTransactionDataSource` (mesmo módulo `:data:database` — sem cruzamento de camada).
