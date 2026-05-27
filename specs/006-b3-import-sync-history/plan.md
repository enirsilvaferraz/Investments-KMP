# Implementation Plan: Sincronização de Histórico via Importação B3

**Branch**: `006-b3-import-sync-history` | **Date**: 2026-05-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-b3-import-sync-history/spec.md`

## Summary

Refatorar o fluxo de importação B3 para que o `B3ImportDataSource` retorne objetos de domínio `B3Record` (identificador + valor), e introduzir um novo `SyncB3HistoryUseCase` que correlaciona as posições importadas com o histórico do período corrente, substitui os valores e persiste as alterações. Cada atualização emite um evento de log progressivo com uma das quatro categorias definidas: `atualizado`, `ignorado`, `não registrado` e `identificador inexistente`.

## Technical Context

**Language/Version**: Kotlin 2.x — Kotlin Multiplatform (KMP)

**Primary Dependencies**: Koin (DI via anotações `@Factory`), `kotlinx.datetime`, `kotlinx.serialization`, FileMapper (leitura XLSX)

**Storage**: Room (via `HoldingHistoryRepository`) — banco local SQLite

**Testing**: JVM Tests (`./gradlew :core:domain:usecases:jvmTest`) — padrão `GIVEN_WHEN_THEN`, MockK para colaboradores

**Target Platform**: Android, iOS, Desktop (JVM) — commonMain

**Project Type**: Kotlin Multiplatform library (camadas domain + data)

**Performance Goals**: Processamento de até 200 posições sem degradação perceptível (sem limite de tempo imposto)

**Constraints**: Código compartilhado em `commonMain`; sem timeout imposto; isolamento de erro por posição (FR-011)

**Scale/Scope**: Portfólio pessoal — dezenas a algumas centenas de posições por importação

## Constitution Check

*GATE: Verificação antes do Phase 0. Re-check após Phase 1.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I — SOLID/DRY | ✅ | `B3ImportDataSource` tem responsabilidade única (retornar dados); `SyncB3HistoryUseCase` tem responsabilidade única (orquestrar sincronização). Log progressivo isolado no UseCase. |
| II — Clean Architecture | ✅ | `B3Record` e `B3SyncLogEvent` ficam em `:domain:usecases`. `B3Position` com seus novos métodos fica em `:data:filestore`. `:data` depende de `:domain:usecases` (já existente). |
| III — KMP First | ✅ | Toda lógica nova em `commonMain`. |
| IV — Plugins Foundation | ✅ | Nenhum módulo novo criado; apenas alterações em módulos existentes. |
| V — Testes Obrigatórios em UseCases | ⚠️ **GATE** | `SyncB3HistoryUseCase` é novo use case em `:domain:usecases` → DEVE ter testes unitários cobrindo as quatro categorias de log e o algoritmo de matching. |
| VI — API Explícita | ✅ | Toda nova declaração pública em `explicitApi()` deve ter modificador explícito. `B3Record` e `B3SyncLogEvent` são `public`; implementações internas ficam `internal`. |
| VII — Documentação Sincronizada | ✅ | Spec, plan e data-model.md atualizados neste PR. |
| VIII — Idioma e Convenções | ✅ | Código e identificadores em inglês; KDoc e comentários em inglês. |

**Resultado do gate**: PASS com ressalva — testes unitários de `SyncB3HistoryUseCase` são obrigatórios antes do merge (princípio V).

## Project Structure

### Documentation (this feature)

```text
specs/006-b3-import-sync-history/
├── plan.md              ← este arquivo
├── research.md          ← Phase 0
├── data-model.md        ← Phase 1
├── contracts/
│   ├── B3ImportDataSource.kt.md   ← interface atualizada
│   └── B3Position.kt.md           ← métodos adicionados
└── tasks.md             ← gerado por /speckit.tasks
```

### Source Code (arquivos modificados e criados)

```text
core/domain/usecases/src/commonMain/kotlin/com/eferraz/usecases/
├── entities/
│   └── B3Record.kt                    ← NOVO — objeto de domínio
├── repositories/
│   └── B3ImportDataSource.kt          ← MODIFICADO — novo método import()
└── services/
    ├── ImportB3FileUseCase.kt         ← MODIFICADO — orquestra import + sync
    └── SyncB3HistoryUseCase.kt        ← NOVO — correlação, atualização e log

core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/
├── dto/
│   ├── B3Position.kt                  ← MODIFICADO — +b3Identifier(), +b3Value()
│   ├── B3StockPosition.kt             ← MODIFICADO — implementa b3Identifier/b3Value
│   ├── B3EtfPosition.kt               ← MODIFICADO — implementa b3Identifier/b3Value
│   ├── B3FundPosition.kt              ← MODIFICADO — implementa b3Identifier/b3Value
│   ├── B3FixedIncomePosition.kt       ← MODIFICADO — implementa b3Identifier/b3Value
│   └── B3TreasuryPosition.kt          ← MODIFICADO — implementa b3Identifier/b3Value
└── B3ImportDataSourceImpl.kt          ← MODIFICADO — usa import() + build B3Record list

core/domain/usecases/src/jvmTest/kotlin/com/eferraz/usecases/services/
└── SyncB3HistoryUseCaseTest.kt        ← NOVO — testes obrigatórios (Princípio V)
```

**Structure Decision**: Monorepo KMP em camadas — sem módulo novo. Alterações concentradas nos módulos `:domain:usecases` e `:data:filestore` já existentes.

## Complexity Tracking

> Sem violações da constituição. Nenhuma justificativa de desvio necessária.
