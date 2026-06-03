# Implementation Plan: Transações embutidas na posição e no histórico mensal

**Branch**: `017-holding-transactions` | **Date**: 2026-06-03 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/017-holding-transactions/spec.md`  
**Diretriz do utilizador**: **minimalismo no código**, **facilidade de leitura** e **paralelismo** — um ponto de hidratação, grafo de leitura linear, ondas curtas por camada Gradle, sem adapters nem APIs duplicadas de listagem.

## Summary

Consolidar movimentações no agregado **posição**:

- `AssetHolding.transactions` (lista completa, ordenada, default vazia).
- Remover `holding` de `AssetTransaction` e use cases de leitura isolada (`GetTransactionsByHoldingUseCase`; `GetTransactionsUseCase` já removido).
- Hidratar transações **apenas** em `:data:database` via **`@Relation` Room** em `AssetHoldingWithDetails` ao carregar posição ou histórico.
- Escrita: `Param(holding, transaction)` em save/delete; port `AssetTransactionRepository` reduzido a persistência + `getById`.
- Consumidores (`GetHistoryTableDataUseCase`, VMs de transações) leem `holding.transactions` e filtram por mês **localmente** quando necessário.

Contrato único: [contracts/HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md). Decisões: [research.md](./research.md).

## Technical Context

**Language/Version**: Kotlin 2.x — KMP, Compose Multiplatform

**Primary Dependencies**: `:domain:entity`, `:domain:usecases`, `:data:database`, `:data:repositories`, Room 3, Koin, kotlinx.datetime

**Storage**: SQLite — **sem** migração de schema (`holdingId` mantido em `asset_transactions`)

**Testing**: Escrever/atualizar `jvmTest` em `:domain:usecases` e `entity` — **sem** `./gradlew` automático (princípio IX)

**Target Platform**: Android, iOS, Desktop — `commonMain`

**Project Type**: Refactor domínio → data → usecases → apresentação (leitura agregada)

**Performance Goals**: Uma query batelada de transações em `getAll()` (R7); eliminar segunda leitura em fluxos histórico+tabela (SC-002)

**Constraints**: Clean Architecture; `explicitApi()`; grafo **histórico → posição → transações**; KISS — sem “posição leve”

**Scale/Scope**: ~25–35 ficheiros `.kt` em `core/`; 0 módulos Gradle novos

## Constitution Check

*GATE: Deve passar antes da Phase 0. Revalidado após Phase 1.*

| # | Princípio | Verificação | Status |
|---|-----------|-------------|--------|
| I | SOLID, KISS, YAGNI | Um hidrator; port mínimo; sem use case de listagem | **APROVADO** |
| II | Clean Architecture | Entity → data hidrata → usecases → features | **APROVADO** |
| III | KMP First | `commonMain` / `jvmTest` | **APROVADO** |
| IV | Plugins Foundation | Sem alteração `build.gradle.kts` de plugins | **APROVADO** |
| V | Testes Use Cases | Atualizar MergeHistory, HistoryTable, Save/Delete, TransactionBalance | **APROVADO** |
| VI | API Explícita | `transactions` default público; helpers de hidratação `internal` | **APROVADO** |
| VII | Documentação | `DOMAIN.md` + artefactos `specs/017-*` | **APROVADO** |
| VIII | Idioma | Docs pt-BR; código inglês | **APROVADO** |
| IX | Validação | quickstart: build/test sob pedido | **APROVADO** |

**Resultado do gate (pós-design)**: **APROVADO** — Complexity Tracking vazio.

## Project Structure

### Documentation (this feature)

```text
specs/017-holding-transactions/
├── plan.md              # Este ficheiro
├── spec.md
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── HoldingTransactionsContract.md
└── tasks.md             # Phase 2 (/speckit.tasks)
```

### Source Code (alterações previstas)

```text
core/domain/entity/
├── holdings/AssetHolding.kt              # + transactions
├── transactions/AssetTransaction.kt      # - holding
├── transactions/*Transaction.kt            # - holding nos data classes
└── docs/DOMAIN.md

core/data/database/
├── entities/holdings/AssetHoldingWithDetails.kt     # + @Relation → transactions
├── daos/AssetHoldingDao.kt                          # getByIdWithDetails, getAllWithAsset (existentes + novos)
├── mappers/HoldingMappers.kt                          # toDomain(asset, goal) + ordenação FR-007
├── mappers/TransactionMappers.kt                    # toDomain() sem holding; toEntity(holdingId)
├── datasources/impl/AssetHoldingDataSourceImpl.kt   # só DAOs WithDetails (sem loop em AssetTransactionDao)
├── datasources/impl/HoldingHistoryDataSourceImpl.kt # holding via getByIdWithDetails
└── datasources/impl/AssetTransactionDataSourceImpl.kt # escrita: toEntity(holdingId)

core/data/repositories/
└── AssetTransactionRepositoryImpl.kt     # port reduzido

core/domain/usecases/
├── repositories/AssetTransactionRepository.kt
├── SaveTransactionUseCase.kt / DeleteTransactionUseCase.kt
├── MergeHistoryUseCase.kt
├── screens/GetHistoryTableDataUseCase.kt
├── cruds/GetHoldingHistoriesUseCase.kt   # já presente
├── (remover) GetTransactionsByHoldingUseCase.kt
└── jvmTest/...

core/presentation/
├── composeApp/.../transactions/TransactionViewModel.kt
├── composeApp/.../screens/GetHistoryTableDataUseCase consumers (se houver)
└── asset-management/.../TransactionManagementViewModel.kt
    TransactionManagementUiState.kt
```

**Structure Decision**: Refactor mecânico com **fronteira de paralelismo = camada Gradle** após onda 0 (entity). Leitura do código segue sempre `entry.holding.transactions` — sem atalhos por repositório de transações.

### Persistência Room (`@Relation`)

Reutilizar o padrão já usado em `AssetHoldingWithDetails` (asset, owner, brokerage, goal) e em `TransactionWithDetails` (subtipos RF/RV/fundo):

| De | Para | FK |
|----|------|-----|
| `AssetHoldingEntity.id` | `AssetTransactionEntity.holdingId` | já existe + índice |

```kotlin
// AssetHoldingWithDetails.kt — acrescentar
@Relation(
    entity = AssetTransactionEntity::class,
    parentColumns = ["id"],
    entityColumns = ["holdingId"],
)
val transactions: List<TransactionWithDetails> = emptyList()
```

- **Não** criar hydrator paralelo nem `getAllByHoldingId` em loop no `AssetHoldingDataSourceImpl`.
- Ordenação `date` ↑, `id` ↑ no **mapper** (`HoldingMappers`), não no SQL da relação.
- Detalhe: [research.md](./research.md) R1, R7.

## Estratégia de implementação (minimalismo + paralelismo)

### Onda 0 — Bloqueante (`:domain:entity` + `DOMAIN.md`)

Subagente único (não paralelizar ficheiros de entidade):

1. Adicionar `transactions: List<AssetTransaction> = emptyList()` em `AssetHolding`.
2. Remover `holding` de `AssetTransaction` e subclasses.
3. Atualizar `DOMAIN.md` (diagramas §3, §9.3).
4. Ajustar `TransactionBalanceTest` e factories em `entity`/`usecases` test helpers.

**Critério de saída**: projeto compila conceptualmente; contrato em [HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md) satisfeito no módulo entity.

### Onda 1 — Paralela (após onda 0)

| Subagente | Módulo | Tarefas mínimas |
|-----------|--------|-----------------|
| **D** | `:data:database` + `:data:repositories` | `@Relation` em `AssetHoldingWithDetails`; `HoldingMappers`; `toEntity(holdingId)`; port impl alinhado; histórico via `getById` hidratado |
| **U** | `:domain:usecases` | Port (T010) → Save/Delete `Param(holding, …)` (T029–T030) → remover `GetTransactionsByHoldingUseCase` (após migrar consumidores) → `MergeHistory` + `GetHistoryTableData` → testes jvmTest |

**Regra de leitura**: subagente **U** assume API de [contracts/HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md); não reintroduz listagens no port.

### Onda 2 — Paralela (após onda 1 + escrita US4)

**Pré-requisito**: `SaveTransactionUseCase` / `DeleteTransactionUseCase` com `Param(holding, …)` (tasks T029–T030).

| Subagente | Módulo | Tarefas mínimas |
|-----------|--------|-----------------|
| **P1** | `:features:composeApp` | `TransactionViewModel`; histórico se ainda referir listagem isolada |
| **P2** | `:features:asset-management` | `TransactionManagementViewModel` + `UiState` (holding no estado, transações sem `holding`) |

Cada VM: uma função de carga → `holding.transactions`; filtro por mês inline ou extraído **só** se duplicado entre 2+ ficheiros do mesmo módulo.

### Onda 3 — Verificação (humano ou sob pedido)

- `rg` conforme [quickstart.md](./quickstart.md).
- `./gradlew :domain:usecases:jvmTest` sob pedido.

## Ficheiros de alto impacto (ordem de leitura recomendada)

1. [contracts/HoldingTransactionsContract.md](./contracts/HoldingTransactionsContract.md)
2. `AssetHolding.kt` / `AssetTransaction.kt`
3. `AssetHoldingWithDetails.kt` + `AssetHoldingDataSourceImpl.kt` (Room `@Relation`)
4. `MergeHistoryUseCase.kt` + `GetHistoryTableDataUseCase.kt`
5. ViewModels de transações

## Riscos e mitigação

| Risco | Mitigação |
|-------|-----------|
| N+1 em `getAll()` | R7 — `@Relation` no mesmo `@Transaction` do DAO |
| Código legado `transaction.holding` | `rg '\.holding'` em `core/` após onda 0 |
| `Map<AssetHolding, …>` em `MergeHistory` | `AssetHolding` com `transactions` faz parte da igualdade — aceitável; se instável, chavear por `id` (só se testes falharem) |

## Complexity Tracking

> Vazio — nenhuma violação da constituição requer justificativa.
