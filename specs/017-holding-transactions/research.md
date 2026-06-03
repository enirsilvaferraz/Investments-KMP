# Research: Transações embutidas na posição e no histórico mensal

**Feature**: `017-holding-transactions` | **Phase**: 0 | **Date**: 2026-06-03

**Diretriz**: código mínimo, grafo de leitura óbvio, paralelismo por camada Gradle após contrato único.

---

## R1 — Onde hidratar `transactions` (único ponto)

**Decision**: Hidratação **somente** em `:data:database`, via **`@Relation` Room** em `AssetHoldingWithDetails` (mesmo padrão de `asset`, `owner`, `brokerage`):

```kotlin
@Relation(
    entity = AssetTransactionEntity::class,
    parentColumns = ["id"],
    entityColumns = ["holdingId"],
)
val transactions: List<TransactionWithDetails> = emptyList()
```

Fluxo:

1. DAOs `@Transaction` devolvem `AssetHoldingWithDetails` (`getAllWithAsset`, `getByIdWithDetails`, etc.).
2. Mapper `AssetHoldingWithDetails.toDomain(asset, goal)` mapeia transações com `TransactionWithDetails.toDomain()` **sem** `holding` (R2).
3. Ordena **cronológica crescente** por `date`, desempate por `id` (FR-007), no mapper (Room não garante ordem na relação).
4. `AssetHoldingDataSourceImpl` usa só estes métodos; **sem** chamadas paralelas a `AssetTransactionDao.getAllByHoldingId` na hidratação de posição.

`HoldingHistoryDataSourceImpl.toModel()` continua via `assetHoldingDataSource.getById` (posição já vem com `transactions`).

**Rationale**: Reutiliza infraestrutura Room existente (`TransactionWithDetails`); uma query transacional por posição evita N+1 manual e dispensa “hydrator” ad hoc (KISS, FR-013a).

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| Hidrator + `getAllByHoldingId` em loop | Duplica o que `@Relation` já resolve; mais código |
| Use case que chama repo de transações após cada `getById` | Repete leitura e mantém API paralela |
| `GetTransactionsByHoldingUseCase` + merge manual | Contradiz FR-012 |
| Lazy `transactions` computada na entidade | Mistura domínio com persistência |

---

## R2 — Remover `holding` de `AssetTransaction`

**Decision**: Remover `val holding: AssetHolding` do sealed interface e das três implementações. Persistência mantém `holdingId` na entidade Room; `toEntity(holdingId: Long)` ou `toEntity(holding: AssetHolding)` só na camada de escrita recebe o id explicitamente.

**Rationale**: Grafo unidirecional histórico → posição → transações (spec FR-001a); elimina ciclos e simplifica leitura.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| `@Deprecated holding` | Viola SC-005 e aumenta ruído |
| `holdingId: Long` na transação de domínio | Duplica identificador já implícito na lista da posição |

---

## R3 — Ordenação da lista

**Decision**: Ordenar na hidratação (R1), não em cada ViewModel. Consumidores assumem lista já ordenada (FR-007).

**Alternatives considered**: Ordenar em `GetHistoryTableDataUseCase` / VMs — rejeitado (duplicação).

---

## R4 — Contrato `AssetTransactionRepository` (port mínimo)

**Decision**:

```kotlin
suspend fun upsert(holding: AssetHolding, transaction: AssetTransaction): Long
suspend fun delete(holding: AssetHolding, id: Long)
suspend fun getById(holding: AssetHolding, id: Long): AssetTransaction?
```

Remover do port público: `getAllByHolding`, `getAllByHoldingAndDateRange`, `getByReferenceDate`, `getByGoalAndReferenceDate`. Métodos equivalentes permanecem **internal** em `AssetTransactionDataSource` / DAO para a hidratação.

**Rationale**: Use cases não listam transações isoladamente (FR-013, SC-007).

---

## R5 — Casos de uso de escrita

**Decision**: `SaveTransactionUseCase.Param(holding, transaction)` e `DeleteTransactionUseCase.Param(holding, id)`; repositório usa `holding.id` no upsert/delete.

**Rationale**: Alinhado à clarificação Q2; transação sem referência à posição no domínio.

---

## R6 — `MergeHistoryUseCase` e métricas do mês

**Decision**: Remover injeção de `AssetTransactionRepository`. Para aportes/resgates do mês:

```kotlin
holding.transactions.filter { it.date in monthRange }
```

`TransactionBalance.calculate` sobre o subconjunto filtrado.

**Rationale**: Posição já traz lista completa; filtro por mês é responsabilidade do consumidor (FR-004a), mas o filtro mensal aqui é regra de negócio de `Appreciation`, não “segunda leitura”.

---

## R7 — Performance em `getAll()`

**Decision**: `AssetHoldingDao.getAllWithAsset()` (e variantes por classe/meta) passa a incluir a relação `transactions` no mesmo `@Transaction` Room — **sem** query separada por posição nem `groupBy` manual.

**Rationale**: Room resolve o grafo holding → transações → subtipos (RF/RV/fundo) numa transação DAO; atende FR-009 e SC-002 para `MergeHistoryUseCase`.

**Alternatives considered**:

| Alternativa | Rejeitada porque |
|-------------|------------------|
| Batch `getAllByHoldingIds` + merge em Kotlin | Mais código que estender `AssetHoldingWithDetails` |
| Hidratar só em `getById` | Viola FR-009 em `getAll` |

---

## R8 — Descontinuação de use cases

**Decision**: Remover `GetTransactionsByHoldingUseCase` (e qualquer vestígio de `GetTransactionsUseCase`). `GetHoldingHistoriesUseCase` mantém-se; enriquecimento vem do repositório de histórico.

**Estado do repo**: `GetTransactionsUseCase` já ausente no working tree; `GetHoldingHistoriesUseCase` adicionado — alinhar consumidores na onda de use cases.

---

## R9 — Consumidores de apresentação

**Decision**: Migrar para `holding.transactions` após `GetAssetHoldingUseCase` ou dados já no `HoldingHistoryResult` / `HistoryTableData`:

| Consumidor | Mudança |
|------------|---------|
| `GetHistoryTableDataUseCase` | Remover `GetTransactionsByHoldingUseCase`; filtrar `result.holding.transactions` por mês |
| `TransactionManagementViewModel` | Lista de `resolved.transactions` |
| `TransactionViewModel` | Idem |
| `TransactionManagementUiState` | Construir drafts sem `holding` dentro de cada transação |

**Rationale**: Uma leitura, código linear (SC-002).

---

## R10 — Testes e `TransactionBalance`

**Decision**: Atualizar `TransactionBalanceTest` e factories de teste: transações mock **sem** `holding`; quando o teste precisar de contexto de posição, usar `AssetHolding(transactions = list)`.

**Rationale**: Princípio V; contrato estável pós-R2.
