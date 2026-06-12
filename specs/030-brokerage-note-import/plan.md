# Implementation Plan: Importação de Nota de Corretagem JSON

**Branch**: `030-brokerage-note-import` | **Date**: 2026-06-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/030-brokerage-note-import/spec.md`

## Summary

Integrar a nota de corretagem JSON já parseada pelo `BrokerageNoteJsonDataSource` ao domínio persistido: (1) enriquecer `AssetTransaction` com `allocatedFee` e derivar `netValue`; (2) introduzir `BrokerageNoteAsset` para carregar ticker na estrutura `BrokerageNote`; (3) implementar `ImportBrokerageNoteUseCase` que calcula o rateio via `NoteFeeAllocation`, resolve o `AssetHolding` por ticker (corretora Nubank fixo) e persiste cada transação; (4) migrar Room v10 → v11 com a nova coluna; (5) exibir "Valor Líq." no formulário de transações em `:features:asset-management`.

## Technical Context

**Language/Version**: Kotlin Multiplatform (commonMain); build-logic foundation plugins

**Primary Dependencies**: `kotlinx.datetime`, Room 3 (`androidx.room3`), Koin KSP annotations, Compose Multiplatform, `kotlinx.coroutines`

**Storage**: Room SQLite — tabela `asset_transactions` (migração v10 → v11, AutoMigration)

**Testing**: `:domain:usecases:jvmTest` — `ImportBrokerageNoteUseCaseTest` com MockK; padrão `GIVEN_WHEN_THEN`

**Target Platform**: KMP commonMain; Android principal; iOS/Desktop via shared code

**Project Type**: Monorepo KMP multi-módulo — camadas `:domain`, `:data`, `:features`

**Performance Goals**: Import de nota completa (até ~50 ativos) em < 5 s (SC-001)

**Constraints**: AutoMigration Room suficiente; `allocatedFee` com `defaultValue = "0"` para retrocompatibilidade; corretora fixa Nubank (id=2); sem UI de erro — falhas somente em console

**Scale/Scope**: ~6 módulos tocados; ~15 arquivos modificados/criados; 1 migração Room

## Constitution Check

*GATE: Verificado antes da Fase 0. Re-verificado após Fase 1.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I. SOLID/DRY | ✅ | `ImportBrokerageNoteUseCase` tem responsabilidade única; `netValue` derivado evita duplicação |
| II. Clean Architecture | ✅ | Fluxo: `:data:filestore` → `:domain:entity` → `:domain:usecases` → `:data:database`; `:features` não acessa `:data` |
| III. KMP First | ✅ | Tudo em `commonMain`; Room 3 KMP; sem `expect/actual` novos |
| IV. Plugins Foundation | ✅ | Nenhum plugin externo adicionado diretamente nos `build.gradle.kts` |
| V. Testes Obrigatórios | ✅ | `ImportBrokerageNoteUseCaseTest` obrigatório em `:domain:usecases:jvmTest` |
| VI. API Explícita | ✅ | `BrokerageNoteAsset` e `ImportBrokerageNoteUseCase` `public`; entidades Room e mappers `internal` |
| VII. Documentação Sincronizada | ✅ | `DOMAIN.md` atualizado; `AGENTS.md` atualizado |
| VIII. Idioma | ✅ | Docs pt-BR; código/testes em inglês |
| IX. Sem Build Automático | ✅ | — |
| X. Escopo Mínimo | ✅ | Diff mínimo; `LoadBrokerageNoteUseCase` substituído (não duplicado); sem funcionalidades fora da spec |

**Gate de escopo (princípio X)**: Plano toca exatamente os módulos necessários para a feature — nenhum refactor paralelo.

**Re-check pós-design (Fase 1)**: ✅ Todos os artefatos dentro dos módulos mapeados.

## Project Structure

### Documentation (this feature)

```text
specs/030-brokerage-note-import/
├── plan.md              ← este arquivo
├── spec.md
├── research.md          ← Phase 0
├── data-model.md        ← Phase 1
├── contracts/
│   └── kotlin-api.md    ← Phase 1
└── tasks.md             ← /speckit-tasks (não criado aqui)
```

### Source Code — módulos alterados

```text
core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/
├── transactions/
│   └── AssetTransaction.kt                  ← ALTERAR: + allocatedFee, + netValue (derivado)
└── brokeragenotes/
    ├── BrokerageNote.kt                     ← ALTERAR: assets: List<BrokerageNoteAsset>
    ├── BrokerageNoteAsset.kt                ← CRIAR: novo tipo (ticker + transaction)
    └── NoteFeeAllocation.kt                 ← ALTERAR: adaptar para BrokerageNoteAsset

core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
├── ImportBrokerageNoteUseCase.kt            ← CRIAR (substitui LoadBrokerageNoteUseCase.kt)
├── LoadBrokerageNoteUseCase.kt              ← REMOVER
├── repositories/
│   ├── AssetHoldingRepository.kt           ← ALTERAR: + getByTicker(ticker: String)
│   └── AssetTransactionRepository.kt       ← ALTERAR: + saveAll(entries) com semântica @Transaction
└── di/
    └── UseCaseModule.kt                     ← ALTERAR: trocar binding Load → Import

core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/
└── ImportBrokerageNoteUseCaseTest.kt        ← CRIAR (obrigatório — princípio V)

core/data/database/src/commonMain/kotlin/com/eferraz/database/
├── core/
│   └── AppDatabase.kt                       ← ALTERAR: version 11, + AutoMigration(10→11)
├── entities/transaction/
│   └── AssetTransactionEntity.kt            ← ALTERAR: + allocatedFee coluna
├── mappers/
│   └── TransactionMappers.kt                ← ALTERAR: mapear allocatedFee
└── (sem Migration spec — AutoMigration suficiente)

core/data/database/src/jvmTest/kotlin/com/eferraz/database/migrations/
└── Migration10To11Test.kt                   ← CRIAR: teste de migração v10 → v11

core/data/repositories/src/commonMain/kotlin/com/eferraz/repositories/
└── AssetHoldingRepositoryImpl.kt            ← ALTERAR: implementar getByTicker

core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/brokeragenote/
└── BrokerageNoteV2Parser.kt                 ← ALTERAR: mapear BrokerageNoteAsset com ticker

core/presentation/asset-management/src/commonMain/kotlin/com/eferraz/asset_management/
├── assets/
│   └── AssetManagementUiState.kt            ← ALTERAR: TransactionDraftUi + allocatedFee/netValue
└── transactions/
    └── TransactionManagementView.kt          ← ALTERAR: + coluna "Valor Líq."

core/domain/entity/docs/
└── DOMAIN.md                                ← ATUALIZAR: BrokerageNoteAsset, AssetTransaction.allocatedFee
```

## Complexity Tracking

> Nenhuma violação da Constitution detectada — seção vazia por conformidade total.
