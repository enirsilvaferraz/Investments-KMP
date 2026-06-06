# Contrato: Transação de ativo unificada (domínio + persistência)

Contrato interno/público do módulo `:domain:entity` e comportamento esperado da camada `:data:database`.

---

## Tipo canónico

```kotlin
// :domain:entity — public API
data class AssetTransaction(
    val id: Long,
    val date: LocalDate,
    val type: TransactionType,
    val quantity: Double,
    val unitPrice: Double,
) {
    val totalValue: Double get() = quantity * unitPrice
}
```

**Invariantes**:
- Não existe subtipo por `AssetClass`.
- Não existe propriedade `observations`.
- Não existe propriedade `holding` (regressão SC-005 de feature 017).

---

## Mapper `:data:database` ↔ domínio

### Escrita (`AssetTransaction` → `AssetTransactionEntity`)

```
AssetTransactionEntity(
    id = tx.id,
    holdingId = param.holdingId,
    transactionDate = tx.date,
    type = tx.type,
    quantity = tx.quantity,
    unitPrice = tx.unitPrice,
)
```

**Proibido**: gravar `totalValue`, `observations` ou `asset_class` — a classe do ativo vive em `assets` (via `holding.assetId`).

**Obtendo `AssetClass`**: sempre a partir de `AssetHolding.asset.assetClass` ou join `asset_transactions` → `asset_holdings` → `assets`.

### Leitura (`AssetTransactionEntity` → `AssetTransaction`)

```
AssetTransaction(
    id = entity.id,
    date = entity.transactionDate,
    type = entity.type,
    quantity = entity.quantity,
    unitPrice = entity.unitPrice,
)
// totalValue disponível via getter
```

**Proibido**: ramificar por `asset_class` ou tabelas satélite para construir subtipos.

---

## DAO `AssetTransactionDao`

**Contrato simplificado**:

| Método | Comportamento |
|--------|---------------|
| `save(entity: AssetTransactionEntity): Long` | Upsert linha única |
| `find(id): AssetTransactionEntity?` | Por PK |
| `getAllByHoldingId(holdingId): List<AssetTransactionEntity>` | Ordenado por data DESC |
| `deleteById(id)` | Cascade conforme FK existente |

Remover overloads `save(FixedIncomeTransactionEntity)`, etc., e `TransactionWithDetails` polimórfico.

---

## `TransactionBalance.calculate`

```kotlin
private fun calculateTransactionValue(transaction: AssetTransaction): Double =
    transaction.totalValue
```

Entrada: `List<AssetTransaction>` homogénea. Sem pattern matching por subtipo.

---

## Use cases afectados (actualizar testes)

| Use case | Impacto |
|----------|---------|
| `SaveTransactionUseCase` | Param continua `(holding, transaction)` com tipo único |
| `SaveAssetWithTransactionsUseCase` | Lista `List<AssetTransaction>` homogénea |
| `DeleteTransactionUseCase` | Sem alteração de assinatura |
| Consumidores de histórico/resumo | Substituir factories de subtipos nos testes |

---

## Migração (versão 10)

**Pré-condição**: app com DB v9.

**Pós-condição**:
- Todas as linhas em `asset_transactions` têm `quantity` e `unitPrice` não nulos.
- Tabelas satélite inexistentes.
- Colunas `observations` e `asset_class` inexistentes em `asset_transactions`.
- Leitura de domínio: `totalValue == quantity * unitPrice` para 100% dos registos (SC-001, SC-003).

**SQL de verificação manual** (quickstart):

```sql
SELECT COUNT(*) FROM asset_transactions WHERE quantity IS NULL OR unitPrice IS NULL;
-- esperado: 0

SELECT id, quantity, unitPrice, quantity * unitPrice AS derived
FROM asset_transactions LIMIT 20;
```
