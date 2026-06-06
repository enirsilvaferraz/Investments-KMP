# Implementation Plan: Modelo unificado de transações de ativos

**Branch**: `023-unify-asset-transaction` | **Date**: 2026-06-06 | **Spec**: [spec.md](spec.md)

**Input**: Unificar subclasses de `AssetTransaction` num tipo único com `id`, `date`, `type`, `quantity`, `unitPrice` e `totalValue` derivado; remover observações; migrar Room; actualizar formulário inline da `AssetManagementScreen` (feature 022).

**Princípio guia**: diff mínimo — achatar persistência e domínio; UI uniforme em `:features:asset-management`. Legado `composeApp/transactions/` **já eliminado** no branch.

---

## Summary

A feature elimina o modelo polimórfico de transações (três subclasses de domínio + três tabelas satélite Room) em favor de um **`data class AssetTransaction`** único. O armazenamento passa a guardar apenas `quantity` e `unitPrice` na tabela `asset_transactions` (sem `observations`, sem `asset_class`); `totalValue` é calculado na leitura. A migração 9→10 converte dados legados (RF/Fundos: qty=1, unitPrice=totalValue antigo). O formulário inline em `:features:asset-management` passa a mostrar os mesmos campos para todas as classes de ativo, com regras de editabilidade por `AssetClass` (via `AssetHolding.asset`) e sem observações.

---

## Technical Context

**Language/Version**: Kotlin 2.x, Compose Multiplatform

**Primary Dependencies**: Room 3 (`AutoMigration` + `Migration9To10`), Koin, `kotlinx.datetime`

**Storage**: SQLite via Room — tabela `asset_transactions` achatada; versão **10**

**Testing**: MockK + `kotlin.test` em `:domain:entity:jvmTest` e `:domain:usecases:jvmTest`

**Target Platform**: Android, iOS, Desktop (`commonMain`)

**Project Type**: App KMP — camadas `:domain:entity`, `:domain:usecases`, `:data:database`, `:features:asset-management`

**Performance Goals**: Sem requisitos novos; migração one-shot na actualização da app

**Constraints**:
- Valor total nunca persistido (FR-007, FR-012)
- Sem validação bloqueante de qty/price (refinamento spec)
- UI de cadastro: **só** formulário inline `AssetManagementScreen` (FR-011)
- Sem arredondamento na gravação

**Scale/Scope**: ~22 ficheiros tocados, 9+ eliminados (incl. legado composeApp já removido), 1 migração Room, testes em entity + usecases

---

## Constitution Check

*Gate inicial e re-verificação pós-design (Phase 1)*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I — SOLID/DRY/KISS | ✅ | Tipo único elimina branching; mapper simplificado |
| II — Clean Architecture | ✅ | Domínio sem Room; `:features` sem `:data` |
| III — KMP First | ✅ | Alterações em `commonMain` |
| IV — Plugins Foundation | ✅ | Sem plugins novos |
| V — Testes em Use Cases | ✅ | Actualizar `SaveTransactionUseCaseTest`, `SaveAssetWithTransactionsUseCaseTest`, etc. |
| VI — API Explícita | ✅ | `AssetTransaction` `public`; entidades Room `internal` |
| VII — Docs Sincronizados | ✅ | `DOMAIN.md` §9.3 + `docs/Modelagem do Banco de Dados.md` no mesmo PR |
| VIII — Idioma | ✅ | Código EN; docs pt-BR |
| IX — Sem Build Automático | ✅ | Gradle só sob pedido |
| X — Escopo focado | ✅ | UI legada `composeApp/transactions/` removida; foco em domínio, Room e formulário inline |

**Gate de escopo (X)**: Não inclui card Resumo, IncomeTax, exclusão de transações, import B3. UI legada de transações em `composeApp` **já removida** (pré-implementação 023) — ver Fase D.

---

## Project Structure

### Documentation (esta feature)

```text
specs/023-unify-asset-transaction/
├── plan.md              ← este arquivo
├── research.md          ← Phase 0
├── data-model.md        ← Phase 1
├── quickstart.md        ← Phase 1
├── contracts/
│   ├── unified-transaction-domain.md
│   └── transaction-form-inline.md
└── tasks.md             ← /speckit-tasks (Phase 2)
```

### Source Code — ficheiros afectados

```text
# Domínio — tipo único
core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/
├── AssetTransaction.kt                    [ALTERAR — data class único]
├── FixedIncomeTransaction.kt              [DELETAR]
├── VariableIncomeTransaction.kt           [DELETAR]
├── FundsTransaction.kt                      [DELETAR]
└── TransactionBalance.kt                  [ALTERAR — totalValue directo]

core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/
├── AssetTransactionContractTest.kt        [ALTERAR]
└── TransactionBalanceTest.kt                [ALTERAR]

core/domain/entity/docs/DOMAIN.md            [ALTERAR — §9.3]
docs/Modelagem do Banco de Dados.md          [ALTERAR — secção transações, diagrama ER, DDL, índices e FKs]

# Dados — schema achatado + migração
core/data/database/src/commonMain/kotlin/com/eferraz/database/
├── core/AppDatabase.kt                      [ALTERAR — v10, entities, autoMigration]
├── entities/transaction/
│   ├── AssetTransactionEntity.kt            [ALTERAR — qty, unitPrice; drop observations + asset_class]
│   ├── FixedIncomeTransactionEntity.kt      [DELETAR]
│   ├── VariableIncomeTransactionEntity.kt   [DELETAR]
│   ├── FundsTransactionEntity.kt            [DELETAR]
│   ├── TransactionWithDetails.kt            [DELETAR ou simplificar]
│   └── BaseTransactionEntity.kt             [DELETAR se órfão]
├── mappers/TransactionMappers.kt            [ALTERAR — mapper plano]
├── daos/AssetTransactionDao.kt              [ALTERAR — CRUD simplificado]
├── datasources/impl/AssetTransactionDataSourceImpl.kt  [ALTERAR]
└── migrations/Migration9To10.kt               [CRIAR]

core/data/database/schemas/.../10.json       [CRIAR — export schema]

# Use cases — testes
core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/
├── SaveTransactionUseCaseTest.kt            [ALTERAR]
├── SaveAssetWithTransactionsUseCaseTest.kt  [ALTERAR]
├── MergeHistoryTransactionsTest.kt          [ALTERAR]
└── … (outros que instanciam subtipos)       [ALTERAR]

# Apresentação — formulário inline (escopo UI, feature 022)
core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── assets/
│   ├── AssetManagementUiState.kt            [ALTERAR — TransactionDraftUi unificado]
│   ├── AssetManagementViewModel.kt          [ALTERAR — syncTotal, validação]
│   ├── AssetManagementEvents.kt             [ALTERAR — opcional: remover TotalValueChanged]
│   └── AssetManagementScreen.kt             [ALTERAR — wiring se evento removido]
└── transactions/TransactionManagementView.kt [ALTERAR — readOnly uniforme, sem obs]

# Legado composeApp — JÁ REMOVIDO (pré-023)
# core/presentation/composeApp/.../features/transactions/
#   TransactionForm.kt, TransactionPanel.kt, TransactionRow.kt,
#   TransactionState.kt, TransactionTable.kt, TransactionViewModel.kt,
#   TransactionIntent.kt  → eliminados; histórico redirecciona para AssetManagementRouting (022)
core/presentation/composeApp/analysis/detekt-baseline.xml  [ALTERAR — remover entradas órfãs]
```

---

## Fases de implementação (referência para tasks.md)

### Fase A — Domínio

1. Substituir `AssetTransaction` por `data class` com `quantity`, `unitPrice`, `totalValue` derivado.
2. Remover três subclasses.
3. Simplificar `TransactionBalance.calculateTransactionValue`.
4. Actualizar testes `:domain:entity`.

### Fase B — Persistência e migração

1. Adicionar colunas a `AssetTransactionEntity`; remover `observations` e `asset_class` (e índice).
2. Criar `Migration9To10` (copiar dados satélite → base; drop tabelas e colunas legadas).
3. Bump `AppDatabase` para v10; export schema.
4. Simplificar DAO, mappers, data source.
5. Remover entidades satélite.

### Fase C — Formulário inline (feature 022)

1. `TransactionDraftUi`: remover `observations`; `fromDomain`/`toDomainTransaction` unificados; `syncTotal()`; remover erros qty/price/total.
2. `TransactionFormContent`: total sempre read-only; RF/Fundos qty=1 read-only; RV qty inteira.
3. `AssetManagementViewModel`: `syncTotal` em todas as classes; relaxar `hasAnyFieldError` no save.
4. Actualizar previews.

### Fase D — Limpeza residual e documentação

1. ~~Ajuste mínimo legado `composeApp`~~ — **feito**: pacote `features/transactions/` removido; `App.kt` já redirecciona `onTransactionManagerRequest` → `AssetManagementRouting`.
2. Limpar referências órfãs (`detekt-baseline.xml` em `:features:composeApp`).
3. Actualizar testes `:domain:usecases`.
4. Actualizar `DOMAIN.md` (domínio) e `docs/Modelagem do Banco de Dados.md` (SQL Room v10: tabela `asset_transactions` achatada, remoção de tabelas satélite, colunas `observations` e `asset_class`, novos campos `quantity`/`unit_price`).

---

## Complexity Tracking

> Nenhuma violação de Constitution Check que exija justificação.

---

## Artefactos gerados

| Artefacto | Caminho |
|-----------|---------|
| Research | [research.md](research.md) |
| Data model | [data-model.md](data-model.md) |
| Quickstart | [quickstart.md](quickstart.md) |
| Contrato domínio | [contracts/unified-transaction-domain.md](contracts/unified-transaction-domain.md) |
| Contrato UI inline | [contracts/transaction-form-inline.md](contracts/transaction-form-inline.md) |

**Próximo passo**: `/speckit-tasks` para gerar `tasks.md`.
