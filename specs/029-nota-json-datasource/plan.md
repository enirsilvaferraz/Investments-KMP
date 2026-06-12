# Implementation Plan: Importação de Nota de Corretagem via JSON

**Branch**: `029-nota-json-datasource` | **Date**: 2026-06-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/029-nota-json-datasource/spec.md`

**Note**: Gerado pelo workflow `/speckit.plan`. Artefatos de design: `research.md`, `data-model.md`, `contracts/`, `quickstart.md`. Tarefas em `tasks.md` (geradas por `/speckit-tasks` — não criadas aqui).

## Summary

Implementar parse do JSON de referência (`docs/nota2.json`) materializado como **constante Kotlin** em `:data:filestore`, com DTOs intermediários `@Serializable`, mapeamento explícito para `BrokerageNote` (tipo já existente em `:domain:entity`) e data source público no próprio módulo `filestore`. **Único módulo alterado**: `core/data/filestore`. Sem port em `:domain:usecases`, sem alterações em entity, features ou apps. Falhas de parse/mapeamento retornam `Result.failure`; validação contábil e rateio permanecem downstream (FR-009).

## Technical Context

**Language/Version**: Kotlin Multiplatform (commonMain), versão do build-logic do projeto

**Primary Dependencies**: `kotlinx.serialization` (já em `:data:filestore`); `kotlinx.datetime` (transitivo via `:domain:entity`); Koin (`@Factory`); coroutines (`Dispatchers.Default`)

**Storage**: Constante `String` em `Nota2JsonFixture`; sem `resources/`; sem persistência

**Testing**: `:data:filestore:jvmTest` — testes de mapeamento; padrão `GIVEN_WHEN_THEN`

**Target Platform**: KMP commonMain — **100% commonMain**, sem `expect/actual`

**Project Type**: Alteração isolada em `:data:filestore` (subpacote `brokeragenote/`)

**Performance Goals**: Parse de ~47 ativos em < 1 s (SC-002)

**Constraints**: **Diff limitado a `core/data/filestore`**; DTOs/mapper/fixture `internal`; interface data source `public`; `build.gradle.kts` inalterado salvo necessidade comprovada

**Scale/Scope**: 1 constante, ~6 DTOs, 1 mapper, 1 interface + 1 impl, ~1 ficheiro de testes — tudo em `:data:filestore`

## Constitution Check

*GATE: Verificado antes da Fase 0. Re-verificado após Fase 1.*

| Princípio | Status | Observação |
|-----------|--------|------------|
| I. SOLID/DRY | ✅ | Responsabilidades coesas no subpacote `brokeragenote/` |
| II. Clean Architecture | ✅ | `:data:filestore` → `:domain:entity` (dependência pré-existente); nenhum módulo novo ou alterado fora de `:data` |
| III. KMP First | ✅ | Tudo em `commonMain`; sem `expect/actual` |
| IV. Plugins Foundation | ✅ | Sem alteração de plugins Gradle |
| V. Testes Obrigatórios | ✅ | Testes em `:data:filestore:jvmTest` (camada alterada) |
| VI. API Explícita | ✅ | Interface data source `public`; resto `internal` |
| VII. Documentação Sincronizada | ✅ | Spec/plan/contratos alinhados; DOMAIN.md inalterado |
| VIII. Idioma | ✅ | Docs pt-BR; código em inglês |
| IX. Sem Build Automático | ✅ | — |
| X. Escopo Mínimo | ✅ | **Um único módulo**; sem port/usecase/DI noutros módulos |

**Gate de escopo (princípio X)**: Plano altera **exclusivamente** `core/data/filestore`.

**Re-check pós-design (Fase 1)**: ✅ Nenhum artefato fora de `:data:filestore` no diff de implementação.

## Project Structure

### Documentation (this feature)

```text
specs/029-nota-json-datasource/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/kotlin-api.md
└── tasks.md             # (/speckit-tasks)
```

### Source Code — **único módulo tocado**

```text
core/data/filestore/                       # :data:filestore — ÚNICO módulo alterado
├── build.gradle.kts                         # inalterado (deps já existentes)
└── src/
    ├── commonMain/kotlin/com/eferraz/filestore/brokeragenote/
    │   ├── BrokerageNoteJsonDataSource.kt       # interface public
    │   ├── BrokerageNoteJsonDataSourceImpl.kt   # internal, @Factory
    │   ├── BrokerageNoteJsonMapper.kt
    │   ├── Nota2JsonFixture.kt
    │   └── dto/
    │       ├── BrokerageNoteJsonDocument.kt
    │       ├── NoteMetadataJson.kt
    │       ├── NoteFinancialSummaryJson.kt
    │       ├── ApportionableFeesJson.kt
    │       ├── WithheldTaxesJson.kt
    │       └── NoteAssetJson.kt
    └── jvmTest/kotlin/com/eferraz/filestore/brokeragenote/
        └── BrokerageNoteJsonMapperTest.kt
```

**Módulos explicitamente fora do diff**: `:domain:entity`, `:domain:usecases`, `:features:*`, `:apps:*`, `FileStoreModule.kt` (scan Koin existente cobre `@Factory` no novo pacote — sem editar o ficheiro).

## Complexity Tracking

> Nenhuma violação da constituição. Port em `:domain:usecases` **deliberadamente omitido** para respeitar limite de escopo do utilizador; integração futura via feature separada.

## Phase 0 — Research (concluída)

Ver [research.md](research.md).

## Phase 1 — Design (concluída)

Ver [data-model.md](data-model.md), [contracts/kotlin-api.md](contracts/kotlin-api.md), [quickstart.md](quickstart.md).

## Próximo passo

Executar `/speckit.implement` para implementar as tarefas em [tasks.md](tasks.md).
