# Tasks: Modelo unificado de transações de ativos

**Input**: Design documents de `specs/023-unify-asset-transaction/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Incluídos onde a constituição (princípio V) exige testes em `:domain:entity` e `:domain:usecases`. Execução Gradle só sob pedido (princípio IX).

**Princípio guia**: diff mínimo — tipo único, Room v10 achatado, formulário inline uniforme; legado `composeApp/transactions/` já removido.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode executar em paralelo (ficheiros diferentes, sem dependências pendentes)
- **[Story]**: user story correspondente (US1, US2, US3)
- Prefixo base omitido para legibilidade — caminhos relativos a `core/`

---

## Phase 1: Setup

**Propósito**: confirmar pré-condições antes de alterar domínio e Room.

- [x] T001 Confirmar branch `023-unify-asset-transaction` e artefactos de design em `specs/023-unify-asset-transaction/` (plan.md, data-model.md, contracts/)
- [x] T002 [P] Verificar que pacote legado `presentation/composeApp/.../features/transactions/` está removido e `apps/umbrellaApp/.../App.kt` redirecciona `onTransactionManagerRequest` → `AssetManagementRouting`

---

## Phase 2: Foundational — Domínio (Blocking)

**Propósito**: tipo único `AssetTransaction` — **bloqueia US3, US1 e US2**.

**⚠️ CRÍTICO**: nenhuma user story pode começar antes desta fase.

- [x] T003 Substituir `sealed interface AssetTransaction` por `data class` com `quantity`, `unitPrice` e `totalValue` derivado em `domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/AssetTransaction.kt`
- [x] T004 [P] Eliminar `domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/FixedIncomeTransaction.kt`, `VariableIncomeTransaction.kt` e `FundsTransaction.kt`
- [x] T005 Simplificar `calculateTransactionValue` para `transaction.totalValue` em `domain/entity/src/commonMain/kotlin/com/eferraz/entities/transactions/TransactionBalance.kt`
- [x] T006 [P] Actualizar `domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/AssetTransactionContractTest.kt` para tipo único (sem subtipos; regressão SC-005)
- [x] T007 [P] Actualizar `domain/entity/src/jvmTest/kotlin/com/eferraz/entities/transactions/TransactionBalanceTest.kt` para instanciar `AssetTransaction` unificado (RF/Fundos: qty=1, unitPrice=total; RV: qty×price)

**Checkpoint**: domínio compila com tipo único; testes `:domain:entity` escritos.

---

## Phase 3: User Story 3 — Persistência com esquema unificado (Priority: P2) 🎯

**Goal**: Room v10 com tabela `asset_transactions` achatada; migração 9→10; leitura/escrita sem subtipos nem colunas `observations`/`asset_class`.

**Independent Test**: Após migração, inspeccionar DB — cada linha tem `quantity` e `unitPrice`; tabelas satélite inexistentes; domínio expõe `totalValue` derivado (quickstart §2).

### Implementation for User Story 3

- [x] T008 [US3] Actualizar `data/database/src/commonMain/kotlin/com/eferraz/database/entities/transaction/AssetTransactionEntity.kt`: adicionar `quantity`, `unitPrice`; remover `observations`, `asset_class` e índice em `asset_class`
- [x] T009 [US3] Criar `data/database/src/commonMain/kotlin/com/eferraz/database/migrations/Migration9To10.kt` com `@DeleteTable` (3 satélites), `@DeleteColumn` (`observations`, `asset_class`) e `onPostMigrate` SQL (copiar RV/RF/Fundos → base; ver research.md R3)
- [x] T010 [US3] Actualizar `data/database/src/commonMain/kotlin/com/eferraz/database/core/AppDatabase.kt`: versão 10, `AutoMigration(9→10)`, remover entidades satélite do `@Database`
- [x] T011 [P] [US3] Eliminar `data/database/.../FixedIncomeTransactionEntity.kt`, `VariableIncomeTransactionEntity.kt`, `FundsTransactionEntity.kt`, `BaseTransactionEntity.kt` e `TransactionWithDetails.kt`
- [x] T012 [US3] Actualizar `data/database/src/commonMain/kotlin/com/eferraz/database/entities/holdings/AssetHoldingWithDetails.kt` e `data/database/.../mappers/HoldingMappers.kt` para `@Relation` directo a `AssetTransactionEntity` (sem wrapper polimórfico)
- [x] T013 [US3] Reescrever mappers planos em `data/database/src/commonMain/kotlin/com/eferraz/database/mappers/TransactionMappers.kt` (sem branching por subtipo; contrato `contracts/unified-transaction-domain.md`)
- [x] T014 [US3] Simplificar `data/database/src/commonMain/kotlin/com/eferraz/database/daos/AssetTransactionDao.kt` (CRUD só em `AssetTransactionEntity`)
- [x] T015 [US3] Actualizar `data/database/src/commonMain/kotlin/com/eferraz/database/datasources/impl/AssetTransactionDataSourceImpl.kt` e `AssetHoldingDataSourceImpl.kt` para tipo unificado
- [ ] T016 [P] [US3] Exportar schema Room v10 em `data/database/schemas/com.eferraz.database.core.AppDatabase/10.json` (artefacto de build — executar Gradle só se pedido ou em CI; pendente export manual)

**Checkpoint**: persistência unificada; migração legada RF/Fundos → qty=1, unitPrice=totalValue; leitura via holding devolve `AssetTransaction` único.

---

## Phase 4: User Story 1 — Cadastrar transação com campos uniformes (Priority: P1)

**Goal**: formulário inline com layout uniforme; total read-only e calculado; RF/Fundos qty=1 desabilitada; sem observações.

**Independent Test**: Cadastrar transação para RV, RF e Fundo; salvar; reabrir — total = qty × preço unitário (spec US1 cenários 1–4).

### Implementation for User Story 1

- [x] T017 [US1] Unificar `TransactionDraftUi` em `presentation/asset-management/.../assets/AssetManagementUiState.kt`: remover `observations`; `syncTotal()` (substitui `syncVariableIncomeTotal`); `toDomainTransaction()` → `AssetTransaction` único; remover erros qty/price/total; RF/Fundos default qty `"1"`
- [x] T018 [US1] Actualizar `presentation/asset-management/.../transactions/TransactionManagementView.kt`: valor total sempre `readOnly`; RF/Fundos qty=`"1"` read-only; RV qty inteira (`KeyboardType.Number`); remover edição directa de total (contrato `contracts/transaction-form-inline.md`)
- [x] T019 [US1] Actualizar handlers em `presentation/asset-management/.../assets/AssetManagementViewModel.kt`: `syncTotal()` em **todas** as classes; relaxar gate `hasAnyFieldError()` no save (sem bloqueio qty/price); novos rascunhos RF/Fundos com qty `"1"`
- [x] T020 [P] [US1] Remover ou tornar no-op `TransactionTotalValueChanged` em `AssetManagementEvents.kt` e wiring em `AssetManagementScreen.kt`
- [x] T021 [P] [US1] Actualizar previews em `TransactionManagementView.kt` (`TransactionFormPreviewProvider`) com qty/unitPrice/total coerentes por classe

**Checkpoint**: cadastro novo funciona para as três classes; total nunca editável; sem campo observações.

---

## Phase 5: User Story 2 — Editar e consultar transações existentes (Priority: P1)

**Goal**: transações migradas e RV legado exibem campos uniformes; edição persiste qty/price; histórico continua legível.

**Independent Test**: Abrir holding com transações pré-migração; RF/Fundos mostram qty=1 e unitPrice histórico; editar e salvar sem perda numérica (spec US2).

### Implementation for User Story 2

- [x] T022 [US2] Garantir `TransactionDraftUi.fromDomain()` em `AssetManagementUiState.kt` mapeia `tx.quantity`, `tx.unitPrice`, `tx.totalValue` para todas as classes (pós-migração RF/Fundos: qty=1)
- [x] T023 [US2] Verificar fluxo `ScreenEntered` em `AssetManagementViewModel.kt`: `holding.transactions` → `fromDomain` ordenado por data; save via `SaveAssetWithTransactionsUseCase` com lista unificada
- [x] T024 [P] [US2] Actualizar consumidores de leitura que ramificam subtipo: `presentation/composeApp/.../history/AssetHistoryScreen.kt` e `HistoryState.kt` se necessário (usar `totalValue` derivado; diff mínimo)
- [x] T025 [P] [US2] Actualizar `domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/SaveTransactionUseCaseTest.kt` para `AssetTransaction` unificado
- [x] T026 [P] [US2] Actualizar `domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/SaveAssetWithTransactionsUseCaseTest.kt` para `AssetTransaction` unificado
- [x] T027 [P] [US2] Actualizar `domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/MergeHistoryTransactionsTest.kt`, `screens/GetMonthSummaryUseCaseTest.kt` e `entities/HoldingHistoryRowTest.kt` (substituir factories de subtipos)

**Checkpoint**: edição de posição existente e listagem no histórico coerentes com modelo unificado.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Propósito**: documentação, limpeza residual e validação final.

- [x] T028 [P] Remover entradas órfãs de `TransactionForm`/`TransactionViewModel` em `presentation/composeApp/analysis/detekt-baseline.xml`
- [x] T029 [P] Actualizar §9.3 transações em `domain/entity/docs/DOMAIN.md` (tipo único; sem subtipos)
- [x] T030 [P] Actualizar secção transações, diagrama ER, DDL, índices e FKs em `docs/Modelagem do Banco de Dados.md` (Room v10)
- [x] T031 [P] Actualizar `AGENTS.md` se mencionar subtipos de `AssetTransaction`
- [x] T032 Validar checklist em `specs/023-unify-asset-transaction/quickstart.md` (cadastro, migração, docs)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: sem dependências
- **Phase 2 (Domínio)**: depende de Phase 1 — **bloqueia tudo**
- **Phase 3 (US3)**: depende de Phase 2
- **Phase 4 (US1)**: depende de Phase 3
- **Phase 5 (US2)**: depende de Phase 4 (UI base) — testes T025–T027 [P] podem correr após T013
- **Phase 6 (Polish)**: depende de Phases 4–5

### User Story Dependencies

| Story | Depende de | Notas |
|-------|------------|-------|
| US3 (P2) | Phase 2 | Pré-requisito técnico para US1/US2 |
| US1 (P1) | US3 | Formulário grava via persistência unificada |
| US2 (P1) | US1 + US3 | Edição/consulta reutiliza UI e migração |

### Within-Phase Order

```
T003 → T004 → T005          (domínio em sequência)
T006, T007 [P] após T005
T008 → T009 → T010          (entidade → migração → AppDatabase)
T011 [P] após T010
T012 → T013 → T014 → T015   (holding relation → mappers → DAO → datasource)
T017 → T018 → T019          (UiState → View → ViewModel)
T020, T021 [P] após T018
T022 → T023                 (fromDomain → load/save)
T024 [P] após T013
T025–T027 [P] após T003
T028–T031 [P] após código estável
T032 por último
```

### Parallel Opportunities

- **Phase 2**: T004 ∥ T006 ∥ T007 (após T003/T005)
- **Phase 3**: T011 ∥ T016 (após T010)
- **Phase 4**: T020 ∥ T021 (após T018)
- **Phase 5**: T024–T027 em paralelo (após domínio/persistência)
- **Phase 6**: T028–T031 em paralelo

---

## Parallel Example: User Story 3

```bash
# Após T010 (AppDatabase v10):
Task T011: Eliminar entidades satélite Room
Task T016: Exportar schema 10.json

# Após T005 (TransactionBalance):
Task T006: AssetTransactionContractTest
Task T007: TransactionBalanceTest
```

---

## Parallel Example: User Story 2

```bash
# Após T013 (mappers):
Task T025: SaveTransactionUseCaseTest
Task T026: SaveAssetWithTransactionsUseCaseTest
Task T027: MergeHistoryTransactionsTest + outros
Task T024: AssetHistoryScreen (se necessário)
```

---

## Implementation Strategy

### MVP First (US3 + US1)

1. Phase 1–2: domínio unificado
2. Phase 3: Room v10 + migração (**US3**)
3. Phase 4: formulário inline (**US1**)
4. **Parar e validar**: cadastro novo para RV, RF e Fundo (quickstart §3)
5. Phase 5: edição legado (**US2**)
6. Phase 6: docs + polish

### Incremental Delivery

1. US3 → persistência correcta (mesmo sem UI final)
2. US1 → cadastro end-to-end (MVP utilizável)
3. US2 → continuidade dados legados + testes use cases
4. Polish → documentação SQL e DOMAIN sincronizados

---

## Notes

- **Escopo mínimo** (princípio X): não recriar `composeApp/transactions/`; não alterar card Resumo, IncomeTax ou exclusão.
- **Sem validação numérica** no formulário (spec refinamento) — só parse de data opcional em `toDomainTransaction`.
- **`asset_class`** não persiste em `asset_transactions` — classe via `AssetHolding.asset`.
- Commit após cada fase ou grupo lógico; `./gradlew` só quando pedido ou para export schema (T016).
