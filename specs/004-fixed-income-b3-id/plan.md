# Implementation Plan: Identificador B3 em Renda Fixa

**Branch**: `004-fixed-income-b3-id` | **Date**: 2026-05-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-fixed-income-b3-id/spec.md`  
**Nota de planeamento**: Migração obrigatória do banco (Room v5→v6). No histórico, RV e fundos exibem **coluna vazia** à direita (sem ícones), mantendo alinhamento com linhas de renda fixa.

## Summary

Adicionar campo opcional **Identificador B3** (`String?`, texto livre, paridade com observações) ao cadastro de **renda fixa**, persistido em `fixed_income_assets.b3_identifier` com **migração Room 5→6**. Na tela de histórico, nova coluna à direita: ícones com tooltip para RF (info azul / warning amarelo); **célula vazia** para renda variável e fundos.

Abordagem: estender `FixedIncomeAsset` + entidade Room + mappers + `AssetManagement*` (UI/VM) + `GetHistoryTableDataUseCase` / `HoldingHistoryView` + coluna em `AssetHistoryScreen` + composable `B3IdentifierStatusCell` em `:presentation:naming`; atualizar documentação canónica do domínio em `core/domain/entity/docs/DOMAIN.md` (princípio VII — documentação sincronizada).

## Technical Context

**Language/Version**: Kotlin 2.3.x (KMP), Compose Multiplatform

**Primary Dependencies**: Room 3 (`androidx.room3`), Koin, kotlinx.datetime

**Storage**: SQLite via `:data:database` — coluna `b3_identifier` em `fixed_income_assets`; `AutoMigration(5 → 6)`

**Testing**: `./gradlew :domain:usecases:jvmTest` — estender `UpsertAssetUseCaseTest` para RF + `b3Identifier`

**Target Platform**: Android, iOS, Desktop (JVM) — UI e DB em `commonMain`

**Project Type**: KMP monorepo (apps + features + domain + data)

**Performance Goals**: Sem metas novas; leitura/escrita de um campo texto por ativo RF

**Constraints**: Clean Architecture; `explicitApi()`; migração não destrutiva; grafo de módulos inalterado

**Scale/Scope**: ~12–15 ficheiros Kotlin; 1 migração de schema; sem novos módulos Gradle

## Constitution Check

*GATE: Deve passar antes da Phase 0. Revalidado após Phase 1.*

| # | Princípio | Verificação | Status |
|---|-----------|-------------|--------|
| I | SOLID, DRY, KISS, YAGNI | Campo só em RF; reutiliza `FormTextField`, `TooltipBox`, `UpsertAssetUseCase` | **APROVADO** |
| II | Clean Architecture | Entity em `:domain:entity`; Room em `:data:database`; UI em `:features:composeApp` / `:presentation:asset-management` | **APROVADO** |
| III | KMP First | Alterações em `commonMain` | **APROVADO** |
| IV | Plugins Foundation | Sem plugins novos em `build.gradle.kts` | **APROVADO** |
| V | Testes em Use Cases | Atualizar `UpsertAssetUseCaseTest` | **APROVADO** |
| VI | API Explícita | `b3Identifier` e `B3IdentifierStatus` `public` onde expostos; entities Room `internal` | **APROVADO** |
| VII | Documentação sincronizada | `spec.md`, `plan.md`, `data-model.md`, contrato, `quickstart.md`, `research.md`, **`core/domain/entity/docs/DOMAIN.md`** | **APROVADO** |
| VIII | Idioma | Docs pt-BR; código em inglês (`b3Identifier`) | **APROVADO** |

**Resultado do gate**: **APROVADO** — sem violações para Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/004-fixed-income-b3-id/
├── plan.md              # Este ficheiro
├── spec.md
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── FixedIncomeB3IdentifierContract.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (alterações previstas)

```text
core/
├── domain/
│   ├── entity/
│   │   ├── .../FixedIncomeAsset.kt             # + b3Identifier
│   │   └── docs/DOMAIN.md                      # ER §9.1 + invariantes §5
│   └── usecases/
│       ├── entities/
│       │   ├── HistoryTableData.kt             # + b3Identifier em FixedIncomeHistoryTableData
│       │   ├── HoldingHistoryView.kt           # + b3IdentifierStatus
│       │   └── B3IdentifierStatus.kt           # novo sealed
│       └── screens/GetHistoryTableDataUseCase.kt
├── data/
│   └── database/
│       ├── entities/assets/FixedIncomeAssetEntity.kt
│       ├── mappers/AssetMappers.kt
│       ├── core/AppDatabase.kt                 # version 6, AutoMigration 5→6
│       └── schemas/.../AppDatabase/6.json      # gerado pelo Room
├── presentation/
│   ├── asset-management/.../assets/
│   │   ├── AssetManagementScreen.kt            # FixedIncomeFields + FormTextField
│   │   ├── AssetManagementUiState.kt
│   │   ├── AssetManagementEvents.kt
│   │   ├── AssetManagementViewModel.kt
│   │   └── AssetManagementMap.kt
│   └── naming/.../B3IdentifierStatusCell.kt    # ícones + tooltip
└── presentation/composeApp/.../history/
    └── AssetHistoryScreen.kt                   # coluna direita UiTableV3
```

**Structure Decision**: Feature transversal domínio → dados → cadastro → histórico; sem novo subprojeto.

## Phase 0 — Research

Concluída em [research.md](./research.md). Sem `NEEDS CLARIFICATION` pendentes.

Destaques:
- Persistência em `fixed_income_assets.b3_identifier`
- Migração **5 → 6** obrigatória
- Coluna de histórico **sempre presente**; RV/fundos = célula vazia

## Phase 1 — Design & Contracts

| Artefacto | Caminho |
|-----------|---------|
| Modelo de dados | [data-model.md](./data-model.md) |
| Contrato UI/dados | [contracts/FixedIncomeB3IdentifierContract.md](./contracts/FixedIncomeB3IdentifierContract.md) |
| Validação manual | [quickstart.md](./quickstart.md) |
| Domínio canónico | [core/domain/entity/docs/DOMAIN.md](../../core/domain/entity/docs/DOMAIN.md) |

### Documentação `DOMAIN.md` (obrigatório)

Atualizar o documento canónico do módulo `:domain:entity` em paralelo à alteração de `FixedIncomeAsset.kt`:

| Secção | Alteração |
|--------|-----------|
| **§5 Invariantes** | Nota em RF: `b3Identifier` opcional (`String?`), exclusivo de `FixedIncomeAsset`; texto livre; valor persistido após `trim()`; `null` se vazio após trim |
| **§6.1 / §9.1** | Diagrama Mermaid `FixedIncomeAsset`: acrescentar `String b3Identifier "opcional"` |
| **§6.1** (prosa) | Uma linha: identificador B3 para conciliação manual com posição B3; sem validação de formato na entidade |

Não duplicar regras de UI (ícones do histórico) — isso permanece na spec/contrato; `DOMAIN.md` descreve apenas o modelo de domínio.

### Migração de banco (checklist implementação)

1. `FixedIncomeAssetEntity`: `@ColumnInfo(name = "b3_identifier") val b3Identifier: String? = null`
2. `AppDatabase`: `version = 6`, `AutoMigration(from = 5, to = 6)`
3. `./gradlew :data:database:compileKotlinJvm`
4. Commitar `schemas/com.eferraz.database.core.AppDatabase/6.json`
5. Testar upgrade desde DB v5 (quickstart)

### Histórico — coluna vazia (RV / fundos)

Implementar **uma** coluna `UiTableDataColumn` para todas as linhas:

```kotlin
content = { row ->
    when (val status = row.b3IdentifierStatus) {
        is B3IdentifierStatus.Informed,
        B3IdentifierStatus.NotInformed -> status.BuildCell()
        B3IdentifierStatus.NotApplicable -> { /* Box vazio — sem ícone */ }
    }
}
```

## Phase 2 — Tasks

Gerado pelo comando `/speckit-tasks` (não incluído neste passo).

Ordem sugerida de implementação:
1. Domain entity + `DOMAIN.md` + `B3IdentifierStatus`
2. Room entity + migration + mappers + schema JSON
3. Asset management (UI state, events, map, screen)
4. History use case + `HoldingHistoryView`
5. `B3IdentifierStatusCell` + coluna `AssetHistoryScreen`
6. Testes `UpsertAssetUseCaseTest` + quickstart manual

## Complexity Tracking

> Não aplicável — gate aprovado sem exceções.
