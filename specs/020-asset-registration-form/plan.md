# Implementation Plan: Cadastro de investimento — cards Ativo e Posicionamento

**Branch**: `021-asset-registration-form` | **Date**: 2026-06-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/020-asset-registration-form/spec.md`

**Diretriz**: **reuso**, **simplicidade**, **diff mínimo** — elementos fora do escopo (Transações, Resumo, Excluir, motor IncomeTax) **não** são alterados.

## Summary

Completar o wiring end-to-end dos cards **ATIVO** e **POSICIONAMENTO** no dialog existente (`:features:asset-management`), adicionando persistência de **Isento de IR** em renda fixa (`FixedIncomeAsset.incomeTaxExempt`, migração Room 7→8) e centralizando salvamento no botão **Salvar** da barra inferior (ex-Concluir), **sempre habilitado**. Reutiliza o fluxo sequencial asset → holding da feature **001** (`UpsertAssetUseCase` + `UpsertAssetHoldingUseCase`) e validações UI existentes (`Validations.kt`). **Sem** snapshot de comparação nem lógica `isDirty`/`canSave`. Sem novos módulos Gradle, sem novos UseCases.

## Technical Context

**Language/Version**: Kotlin 2.3+ (KMP), Compose Multiplatform

**Primary Dependencies**: Room 3, Koin, kotlinx.datetime, design-system v1 (`AppDropdownField`, `SegmentedControl`, `FormTextField`)

**Storage**: SQLite via `:data:database` — coluna `income_tax_exempt` em `fixed_income_assets`; `AutoMigration(7 → 8)`

**Testing**: Actualizar `UpsertAssetUseCaseTest` para RF + `incomeTaxExempt`; `./gradlew :domain:usecases:jvmTest` sob pedido (princípio IX)

**Target Platform**: Android, iOS, Desktop (JVM) — `commonMain`

**Project Type**: KMP monorepo (apps + features + domain + data)

**Performance Goals**: Dialog fecha em < 1s após salvamento (SC-009; `LaunchedEffect` já existente)

**Constraints**: Clean Architecture; `explicitApi()`; **não** alterar cards Transações/Resumo; botão Excluir permanece inactivo

**Scale/Scope**: ~10–14 ficheiros Kotlin; 1 migração; alterações concentradas em `:features:asset-management`, `:domain:entity`, `:data:database`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Princípio | Verificação | Status |
|---|-----------|-------------|--------|
| I | SOLID, DRY, KISS, YAGNI | Reutiliza UseCases 001, `SegmentedControl`; sem snapshot/`isDirty` | **APROVADO** |
| II | Clean Architecture | Entity + Room + UI; features não dependem de `:data` directamente | **APROVADO** |
| III | KMP First | Tudo em `commonMain` | **APROVADO** |
| IV | Plugins Foundation | Sem plugins novos | **APROVADO** |
| V | Testes em Use Cases | `UpsertAssetUseCaseTest` + entidade RF | **APROVADO** |
| VI | API Explícita | `incomeTaxExempt` public em entity; helpers de reset `internal` | **APROVADO** |
| VII | Documentação sincronizada | `DOMAIN.md` + artefactos spec 020 | **APROVADO** |
| VIII | Idioma | Docs pt-BR; código inglês (`incomeTaxExempt`) | **APROVADO** |
| IX | Validação sem build | Build só sob pedido / CI | **APROVADO** |

**Gate Result**: **APROVADO** — sem violações; Complexity Tracking vazio.

**Re-check pós-design**: **APROVADO** — contratos e data-model confirmam escopo mínimo; nenhuma dependência proibida.

## Project Structure

### Documentation (this feature)

```text
specs/020-asset-registration-form/
├── plan.md              # Este ficheiro
├── spec.md
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── ui-contracts.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (alterações previstas)

```text
core/
├── domain/
│   ├── entity/
│   │   ├── .../FixedIncomeAsset.kt             # + incomeTaxExempt: Boolean = false
│   │   └── docs/DOMAIN.md                      # ER + invariantes
│   └── usecases/
│       └── jvmTest/.../UpsertAssetUseCaseTest.kt
├── data/database/
│   ├── .../FixedIncomeAssetEntity.kt           # + incomeTaxExempt
│   ├── mappers/AssetMappers.kt                 # round-trip
│   ├── core/AppDatabase.kt                     # v8, AutoMigration 7→8
│   └── schemas/.../8.json                      # export Room
└── presentation/asset-management/
    └── .../assets/
        ├── AssetManagementViewModel.kt         # reset classe, IncomeTaxExemptChanged
        ├── AssetManagementUiState.kt           # + incomeTaxExempt
        ├── AssetManagementEvents.kt            # IncomeTaxExemptChanged
        ├── AssetManagementMap.kt               # build/toUiState RF
        └── AssetManagementScreen.kt            # wiring UI + barra Salvar (sempre enabled)
```

**Fora do diff intencional**:

```text
core/presentation/asset-management/.../transactions/*   # inalterado
AssetManagementScreen.kt — secções TRANSAÇÕES e RESUMO    # sem wiring novo
```

**Structure Decision**: Feature incremental no módulo `:features:asset-management` existente, espelhando o padrão da feature 004 (campo RF + migração) combinado com o wiring 001 (save único). Nenhum módulo Gradle novo.

## Complexity Tracking

> Nenhuma violação de constitution — tabela vazia.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0 — Research

Concluída em [research.md](./research.md). Decisões-chave:

- `Boolean incomeTaxExempt` + migração 7→8 (retrocompat `false`)
- Barra inferior como único save, botão **sempre habilitado**; `FormCardActions` dos cards desactivados
- **Sem** `AssetFormSnapshot` / `isDirty`
- Titular read-only via `FormTextField`; corrigir bug do dropdown Titular
- Reset parcial centralizado em `partialResetForAssetClass`
- Tipo dropdown dinâmico por `AssetClass`

## Phase 1 — Design

Concluída em:

- [data-model.md](./data-model.md)
- [contracts/ui-contracts.md](./contracts/ui-contracts.md)
- [quickstart.md](./quickstart.md)

## Implementation Notes (para tasks.md)

Ordem sugerida (menor risco):

1. **Domínio + Room** — `incomeTaxExempt`, mappers, migração 8, `DOMAIN.md`
2. **ViewModel + UiState** — T009 `incomeTaxExempt` + `partialResetForAssetClass`, `IncomeTaxExemptChanged`
3. **Map** — `buildFixedIncomeAsset` / `toUiState`; RV/Fundo ignoram isenção
4. **UI ATIVO** — tipo por classe; wire RV/Fundo; **remover B3 de RV**; remover `FormCardActions`
5. **UI POSICIONAMENTO** — titular RO; remover `FormCardActions`
6. **Barra inferior** — Concluir → Salvar, **sempre** `enabled = true`
7. **Validations** — confirmar FR-009/FR-012 (T027)
8. **Testes** — `UpsertAssetUseCaseTest`

**Não fazer nesta feature**: wiring Transações, valores Resumo, Excluir, integração IncomeTax 019.
