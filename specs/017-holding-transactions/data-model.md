# Data Model: 017-holding-transactions

**Feature**: `017-holding-transactions` | **Phase**: 1 | **Date**: 2026-06-03

---

## Grafo de leitura (canónico)

```text
HoldingHistoryEntry
    └── holding: AssetHolding
            └── transactions: List<AssetTransaction>   // lista completa persistida
```

**Escrita**: `Param(holding, transaction)` → persistência usa `holding.id` (FK `holdingId` inalterada no SQLite).

**Sem** `AssetTransaction.holding` no domínio público.

---

## Entidades de domínio

### `AssetHolding` (`com.eferraz.entities.holdings`)

| Campo | Tipo | Regras |
|-------|------|--------|
| `id` | `Long` | Invariante existente |
| `asset` | `Asset` | Invariante existente |
| `owner` | `Owner` | Invariante existente |
| `brokerage` | `Brokerage` | Invariante existente |
| `goal` | `FinancialGoal?` | Opcional |
| **`transactions`** | **`List<AssetTransaction>`** | **Default `emptyList()`**; em leituras do armazenamento = todas as transações da posição, ordenadas (FR-007) |

Construção manual em testes/factories: omitir `transactions` → lista vazia (FR-010).

### `AssetTransaction` (sealed, `com.eferraz.entities.transactions`)

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `Long` | |
| `date` | `LocalDate` | |
| `type` | `TransactionType` | |
| `observations` | `String?` | |
| `totalValue` | `Double` | |
| *(removido)* | ~~`holding`~~ | **Não expor** (FR-001a, SC-005) |

Subtipos: `FixedIncomeTransaction`, `VariableIncomeTransaction`, `FundsTransaction` — mesmos campos específicos atuais, sem `holding`.

### `HoldingHistoryEntry`

Sem alteração estrutural: continua `holding: AssetHolding`; após feature, `holding.transactions` preenchido em todas as leituras de repositório (FR-004, FR-005).

---

## Persistência (Room — sem migração de schema)

| Tabela | Ligação | Notas |
|--------|---------|-------|
| `asset_transactions` | `holdingId` FK | Mantida; domínio não espelha FK na transação |
| `asset_holdings` | — | Sem coluna de transações embutidas |

Hidratação em runtime via **`AssetHoldingWithDetails`** + `@Relation` → `TransactionWithDetails` (research R1, R7). Ordenação no mapper de domínio.

---

## Ports (`:domain:usecases`)

### `AssetTransactionRepository` (reduzido)

| Método | Semântica |
|--------|-----------|
| `upsert(holding, transaction)` | Grava/atualiza; usa `holding.id` |
| `delete(holding, id)` | Remove por id na posição |
| `getById(holding, id)` | Leitura pontual se necessário (formulários) |

### `AssetHoldingRepository` / `HoldingHistoryRepository`

Comportamento: todo `AssetHolding` devolvido já inclui `transactions` (FR-009).

### Casos de uso removidos

- `GetTransactionsUseCase`
- `GetTransactionsByHoldingUseCase`

### Casos de uso alterados

| Use case | `Param` |
|----------|---------|
| `SaveTransactionUseCase` | `(holding, transaction)` |
| `DeleteTransactionUseCase` | `(holding, id)` |

### Casos de uso simplificados (leitura)

| Use case | Mudança |
|----------|---------|
| `GetHistoryTableDataUseCase` | `holding.transactions` + filtro mês no próprio use case |
| `MergeHistoryUseCase` | Sem `AssetTransactionRepository`; filtro local no mês |
| `GetHoldingHistoriesUseCase` | Sem mudança de assinatura; dados via repo hidratado |

---

## Validação e invariantes

| ID | Regra |
|----|-------|
| INV-01 | `transactions` nunca `null` no modelo público |
| INV-02 | Lista em leitura = conjunto completo persistido para `holding.id` |
| INV-03 | Ordem: `date` ASC, `id` ASC em empate |
| INV-04 | Upsert/delete exige `holding` com `id` válido no parâmetro |
| INV-05 | Snapshot de histórico (`upsert` entry) ignora `transactions` no payload (FR-008) |

---

## Impacto em tipos derivados

- **`TransactionBalance.calculate(List<AssetTransaction>)`**: inalterado na assinatura; lista vem de `holding.transactions` (filtrada ou não no chamador).
- **`HistoryTableData` / `HoldingHistoryResult`**: passam a depender de `holding.transactions` hidratado upstream.
- **`DOMAIN.md`**: atualizar diagramas §3, §9.3 — relação unidirecional posição → transações (princípio VII).
