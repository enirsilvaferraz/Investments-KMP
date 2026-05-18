# Implementation Plan: Dialog Unificado de Cadastro de Ativo + Holding

**Branch**: `001-asset-holding-dialog` | **Date**: 2026-05-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-asset-holding-dialog/spec.md`

## Summary

Unificar o cadastro de ativo (Asset) e holding em um único dialog em tela cheia, gerenciado por um `DialogViewModel` independente que controla abrir/fechar. O `AssetManagementViewModel` orquestra a persistência sequencial (asset → holding) e sinaliza conclusão ao `DialogViewModel`. O `HoldingManagementView.kt` será removido — o campo de corretora será incorporado inline no formulário de asset. A arquitetura permite futura adição de tela de transação ao mesmo dialog.

## Technical Context

**Language/Version**: Kotlin 2.3+ (com explicit backing fields), Kotlin Multiplatform

**Primary Dependencies**:
- Compose Multiplatform (UI)
- Navigation 3 (`androidx.navigation3`) com `DialogSceneStrategy`
- Koin (DI, annotations com `@KoinViewModel`, `@ComponentScan`)
- AndroidX Lifecycle ViewModel
- kotlinx.serialization (rotas)
- kotlinx.datetime (datas)

**Storage**: Room 3 (via `:data:database`), acessado indiretamente por UseCases/Repositories

**Testing**: MockK + kotlinx.coroutines.test, padrão `GIVEN_WHEN_THEN`, `./gradlew :domain:usecases:jvmTest`

**Target Platform**: Android, iOS, Desktop (JVM) — Compose Multiplatform

**Project Type**: mobile-app + desktop-app (monorepo KMP)

**Performance Goals**: Dialog fecha em < 1s após salvamento; edição carrega em < 2s

**Constraints**: Offline-capable (Room local); UI reativa via StateFlow

**Scale/Scope**: ~10 módulos Gradle, feature concentrada em `:features:asset-management` e `:features:composeApp`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Status | Evidência |
|-----------|--------|-----------|
| I. SOLID, DRY, Boas Práticas | PASS | S: DialogViewModel (ciclo de vida) separado de AssetManagementViewModel (persistência). O: dialog extensível para transações futuras. D: VMs dependem de UseCases (abstrações). DRY: campo de corretora reutilizado inline, sem duplicação. |
| II. Clean Architecture | PASS | Alterações concentradas em `:features:asset-management` (apresentação). UseCases existentes reutilizados sem modificação. Nenhuma dependência proibida adicionada. |
| III. KMP First | PASS | Todo código em `commonMain`. Nenhum código platform-specific necessário para esta feature. |
| IV. Plugins Foundation | PASS | Módulo `asset-management` já usa `foundation.project` + `foundation.library.comp` + `foundation.library.koin`. Sem novos plugins necessários. |
| V. Testes em UseCases | N/A | Nenhuma alteração em `:domain:usecases` prevista. UseCases existentes (`UpsertAssetUseCase`, `UpsertAssetHoldingUseCase`, `GetBrokeragesUseCase`) reutilizados sem modificação. |
| VI. API Explícita | PASS | Novas classes internas (`internal`). `AssetManagementRouting` já é `public` (contrato entre módulos). `AssetManagementScreen` será `public` como entrypoint do módulo. |
| VII. Documentação Sincronizada | PASS | Plano + spec + data-model atualizados neste ciclo. |
| VIII. Idioma e Convenções | PASS | Documentação em pt-BR. Código em inglês. Pacote `com.eferraz.asset_management`. |

**Gate Result**: PASS — nenhuma violação identificada.

## Project Structure

### Documentation (this feature)

```text
specs/001-asset-holding-dialog/
├── plan.md              # Este arquivo
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
core/presentation/asset-management/
└── src/commonMain/kotlin/com/eferraz/asset_management/
    ├── di/
    │   ├── AssetManagementModule.kt      # Koin module (existente)
    │   └── AssetManagementRouting.kt     # NavKey rota de dialog (existente)
    ├── assets/
    │   ├── AssetManagementViewModel.kt   # Orquestra persistência asset+holding (modificar)
    │   ├── AssetManagementScreen.kt      # Form view do asset (modificar — incorporar corretora)
    │   ├── AssetManagementUiState.kt     # Estado do formulário (modificar — adicionar campos holding)
    │   ├── AssetManagementEvents.kt      # Eventos (modificar — adicionar eventos holding)
    │   └── AssetManagementMap.kt         # Mapper UI↔Domain (existente)
    ├── holdings/
    │   ├── HoldingManagementViewModel.kt # Existente (remover após migração)
    │   ├── HoldingManagementView.kt      # Existente (remover — FR-007)
    │   ├── HoldingManagementUiState.kt   # Existente (remover após migração)
    │   └── HoldingManagementEvents.kt    # Existente (remover após migração)
    ├── dialog/
    │   └── DialogViewModel.kt            # NOVO — gerencia abrir/fechar dialog
    ├── AssetManagementEditContext.kt     # Contexto de edição (existente — staged)
    └── helpers/
        ├── FormTextField.kt              # Componente reutilizável (existente)
        ├── FormTwoColumnsRow.kt          # Componente reutilizável (existente)
        ├── FieldLabels.kt                # Labels dos campos (existente)
        └── Validations.kt               # Validações de UI (existente)

core/presentation/composeApp/
└── src/commonMain/kotlin/com/eferraz/presentation/
    └── App.kt                            # NavDisplay com DialogSceneStrategy (modificar — descomentar entry)
```

**Structure Decision**: Feature concentrada no módulo `:features:asset-management` existente. Novo subpacote `dialog/` para o `DialogViewModel`. Nenhum módulo Gradle novo necessário. A integração com o shell da app em `App.kt` (`:features:composeApp`) apenas descomenta o entry já preparado para `AssetManagementRouting`.

## Complexity Tracking

> Nenhuma violação de constitution identificada — tabela vazia.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |
