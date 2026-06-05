# Implementation Plan: Integração do Formulário de Transações

**Branch**: `022-integrate-transaction-form` | **Date**: 2026-06-05 | **Spec**: [spec.md](spec.md)

**Input**: Integrar o card de transações diretamente na `AssetManagementScreen`, removendo `TransactionFormDialog` como ponto de entrada da tela, unificando `TransactionManagementViewModel` no `AssetManagementViewModel` e salvando asset + holding + transações em um único botão salvar.

**Princípio guia**: simplicidade de código e legibilidade; *menos é mais*.

---

## Summary

A tela de gestão de ativos (`AssetManagementScreen`) precisa suportar a adição e edição de transações inline — sem diálogo auxiliar — e salvar tudo (ativo + posição + transações) numa única operação atômica. O plano cria um use case de save unificado com `@Transaction` Room, mescla a lógica de transações no `AssetManagementViewModel` existente, e extrai um composable stateless de transações que elimina o `holdingId` hardcoded atual.

---

## Technical Context

**Language/Version**: Kotlin 2.x, Compose Multiplatform

**Primary Dependencies**: Room 3 (atomicidade `@Transaction`), Koin, Lifecycle ViewModel KMP, `kotlinx.datetime`

**Storage**: Room (SQLite) — DAOs `AssetHoldingDao`, `AssetTransactionDao`, `AssetDao`

**Testing**: MockK + `kotlin.test` em `:domain:usecases:jvmTest`

**Target Platform**: Android, iOS, Desktop (commonMain)

**Project Type**: Aplicação KMP multiplatform — módulo `:features:asset-management` (apresentação)

**Performance Goals**: Tela carrega transações em < 1 s (SC-003); sem requisitos de throughput

**Constraints**: Sem diálogo auxiliar para transações na tela de gestão; save atômico obrigatório (FR-002)

**Scale/Scope**: Feature de UI — ~ 6 arquivos alterados, 2 criados, 2 novos testes

---

## Constitution Check

*Re-verificado após design (Phase 1)*

| Princípio | Status | Observação |
|---|---|---|
| I — SOLID/DRY/KISS | ✅ | Novo use case = responsabilidade única; composable stateless elimina duplicação de estado |
| II — Clean Architecture | ✅ | `:features` nunca depende de `:data`; `SaveAssetWithTransactionsUseCase` em `:domain:usecases` |
| III — KMP First | ✅ | Toda alteração em `commonMain` |
| IV — Plugins Foundation | ✅ | Nenhum plugin novo necessário |
| V — Testes em Use Cases | ✅ | `SaveAssetWithTransactionsUseCaseTest` obrigatório |
| VI — API Explícita | ✅ | Use case `public`; UiState/Events/ViewModel `internal`; composable stateless `internal` |
| VII — Docs Sincronizados | ✅ | `AGENTS.md` e `.specify/` atualizados pós-plan |
| VIII — Idioma | ✅ | Código em inglês; docs em pt-BR |
| IX — Sem Build Automático | ✅ | Não executar Gradle para validar |

---

## Project Structure

### Documentation (esta feature)

```text
specs/022-integrate-transaction-form/
├── plan.md          ← este arquivo
├── research.md      ← Phase 0
├── data-model.md    ← Phase 1
├── quickstart.md    ← Phase 1
├── contracts/
│   └── asset-management-viewmodel.md  ← Phase 1
└── tasks.md         ← gerado por /speckit-tasks (fora do plano)
```

### Source Code — arquivos afetados

```text
# Domínio — novo use case atômico
core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
└── SaveAssetWithTransactionsUseCase.kt           [CRIAR]

core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/
└── SaveAssetWithTransactionsUseCaseTest.kt       [CRIAR]

# Dados — novo método atômico com diff interno
core/data/database/src/commonMain/kotlin/com/eferraz/database/datasources/
├── AssetHoldingDataSource.kt                     [ALTERAR — novo método saveWithTransactions]
└── impl/AssetHoldingDataSourceImpl.kt            [ALTERAR — @Transaction + injetar AssetTransactionDataSource + diff]

# Port de domínio — novo método na interface
core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/repositories/
└── AssetHoldingRepository.kt                     [ALTERAR — novo método]

# Implementação do repositório
core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/
└── AssetHoldingRepositoryImpl.kt                 [ALTERAR — delegar ao DataSource]

# Apresentação — módulo :features:asset-management
core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── assets/
│   ├── AssetManagementUiState.kt                 [ALTERAR — absorver TransactionDraftUi + adicionar transactions + saveError]
│   ├── AssetManagementEvents.kt                  [ALTERAR — eventos de transação]
│   └── AssetManagementViewModel.kt               [ALTERAR — lógica de transações + save unificado]
├── transactions/
│   ├── TransactionManagementUiState.kt           [DELETAR — TransactionDraftUi migrado; restante desnecessário]
│   ├── TransactionManagementViewModel.kt         [DELETAR — sem callers após remoção da rota]
│   ├── TransactionManagementEvents.kt            [DELETAR — eventos migrados para AssetManagementEvents]
│   └── TransactionManagementView.kt              [ALTERAR — remover TransactionFormDialog; manter só TransactionFormContent]
├── di/
│   └── TransactionManagementRouting.kt           [DELETAR]
└── AssetManagementScreen.kt                      [ALTERAR — usar TransactionFormContent stateless]

# App shell — navegação
core/apps/umbrellaApp/src/commonMain/kotlin/com/eferraz/investments/
├── App.kt                                        [ALTERAR — remover entry<TransactionManagementRouting>; redirecionar onTransactionManagerRequest → AssetManagementRouting]
└── AppRoutes.kt                                  [ALTERAR — remover TransactionManagementRouting do SerializersModule]
```

---

## Complexity Tracking

> Nenhuma violação de Constitution Check identificada.

---

## Decisões de design

### D1 — Atomicidade via `@Transaction` no DataSource com diff interno

Adicionar `saveWithTransactions(holding)` a `AssetHoldingDataSourceImpl` com `@Transaction`. O método:
1. Salva o holding.
2. Consulta IDs existentes via `AssetTransactionDataSource.getAllByHolding(holding)` (já disponível).
3. Calcula `toDelete = existingIds − holding.transactions.ids` e deleta orphans.
4. Faz upsert das transações recebidas em `holding.transactions`.

O chamador só declara o estado final. O DataSource é responsável pelo diff — nenhum ID de deleção precisa vir de fora.

### D2 — `SaveAssetWithTransactionsUseCase` mínimo

```kotlin
// Param único — AssetHolding já contém asset e transactions
data class Param(val holding: AssetHolding)
```

Fluxo interno: `UpsertAssetUseCase(holding.asset)` → `assetHoldingRepository.upsertWithTransactions(holding.copy(asset = savedAsset))`.

Sem lógica de diff, sem parâmetros redundantes — o holding é o estado final completo.

### D3 — ViewModel unificado sem `isDirty` e sem `initialSnapshot`

`AssetManagementUiState` ganha apenas `transactions: List<TransactionDraftUi>` — **sem** `initialSnapshot` e **sem** `isDirty`. O ViewModel não rastreia o que existia antes; essa responsabilidade pertence ao DataSource. O botão "Salvar" fica sempre habilitado exceto durante `isSaving` (FR-005).

### D4 — Remoção de `TransactionManagementRouting` e limpeza completa

`TransactionManagementRouting` é deletada. O `onTransactionManagerRequest` da tela de histórico é redirecionado para `AssetManagementRouting(holdingId)` — o mesmo diálogo usado para editar o holding. Consequências em cascata:

- `TransactionFormDialog`: sem callers externos → **deletado**
- `TransactionManagementViewModel`, `TransactionManagementEvents`, `TransactionManagementUiState`: sem uso → **deletados**
- `App.kt`: remove `entry<TransactionManagementRouting>` e muda callback
- `AppRoutes.kt`: remove `TransactionManagementRouting` do `SerializersModule`
- Pacote `transactions/`: fica apenas com `TransactionManagementView.kt` contendo só `TransactionFormContent`

### D5 — `TransactionDraftUi` unificado em `AssetManagementUiState.kt`

`TransactionDraftUi` e funções auxiliares (`hasAnyFieldError`, `syncVariableIncomeTotal`) são declarados diretamente em `AssetManagementUiState.kt` — sem arquivo separado. O estado de UI das transações vive junto com o estado da tela que o gerencia. Um arquivo a menos.

### D6 — `TransactionFormContent` stateless

Extrair a tabela de transações para `internal fun TransactionFormContent(...)` em `TransactionManagementView.kt`. Recebe `List<TransactionDraftUi>`, `AssetClass`, e callbacks explícitos. `TransactionFormDialog` e `TransactionFormView` são deletados do mesmo arquivo.

### D7 — `AssetManagementScreen` usa `TransactionFormContent`

Substituir `TransactionFormView(holdingId = 1)` por `TransactionFormContent(transactions = ui.transactions, assetClass = ui.assetClass, ...)`. Sem ViewModel aninhado.

---

## Fluxo de save alvo (pós-feature)

```
AssetManagementEvents.Save
  → validar ativo (checkErrors em Validations.kt)
  → validar transações (transactions.any { it.hasAnyFieldError() })
  → se erro: exibir erros, manter tela aberta
  → isSaving = true
  → SaveAssetWithTransactionsUseCase(Param(holding))
      → UpsertAssetUseCase(asset) → savedAsset
      → assetHoldingRepository.upsertWithTransactions(
            holding.copy(asset = savedAsset, transactions = domainTransactions)
        )
          → DataSource @Transaction:
              1. save(holding) → holdingId
              2. getAllByHolding → existingIds
              3. delete(orphans = existingIds − incomingIds)
              4. save(each incoming transaction)
          // rollback total em qualquer falha
  → sucesso: isCompleted = true → tela fecha
  → falha: isSaving = false, saveError = mensagem de erro
```

---

## Fora de escopo

- Card de Resumo (FR-012)
- Lógica de IncomeTax
- Excluir ativo/holding
