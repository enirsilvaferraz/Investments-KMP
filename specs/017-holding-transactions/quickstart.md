# Quickstart: 017-holding-transactions

**Branch**: `017-holding-transactions`

Validação manual e build **sob pedido** (constituição IX).

---

## Pré-requisitos

- Branch `017-holding-transactions` com implementação conforme [plan.md](./plan.md) e [contracts/HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md).
- Carteira de desenvolvimento com posições, transações e histórico mensal.

---

## 1. Verificação estática (sem Gradle)

```bash
rg 'GetTransactionsByHoldingUseCase|GetTransactionsUseCase' core/
rg 'getAllByHolding|getByReferenceDate' core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/
rg 'val holding' core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/AssetTransaction.kt
rg 'GetTransactionsByHolding' core/presentation/
```

Esperado: sem use cases removidos; port sem listagens; sem `holding` em `AssetTransaction`; apresentação sem `GetTransactionsByHolding`.

---

## 2. Testes use cases (sob pedido)

```bash
./gradlew :domain:usecases:jvmTest
```

Foco: `MergeHistoryUseCaseTest`, `GetHistoryTableDataUseCaseTest`, `CreateHistoryUseCaseTest`, `TransactionBalanceTest`.

---

## 3. Cenários manuais

| # | Passo | Esperado |
|---|--------|----------|
| 1 | Abrir **Histórico** num mês com posições e movimentações | Colunas de aportes/resgates coerentes; sem segunda consulta visível na UI |
| 2 | Posição **sem** transações no mês | Lista vazia; histórico sem erro |
| 3 | **Gestão de transações** de uma posição | Lista completa ordenada por data; salvar/apagar com posição no fluxo |
| 4 | Exportar / meta / B3 sync | Sem regressão em fluxos que usam `HoldingHistoryEntry` |

---

## 4. Documentação

- [x] `core/domain/entity/docs/DOMAIN.md` — diagrama posição → transações (sem seta inversa)
- [x] `AGENTS.md` — grafo de leitura se ainda referir use cases antigos

---

## Artefatos desta feature

| Ficheiro | Conteúdo |
|----------|----------|
| [spec.md](./spec.md) | Requisitos FR/SC |
| [research.md](./research.md) | Decisões R1–R10 |
| [data-model.md](./data-model.md) | Entidades e ports |
| [contracts/HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md) | API mínima |
| [plan.md](./plan.md) | Ondas e paralelismo |

Próximo passo: `/speckit.tasks` para `tasks.md`.
