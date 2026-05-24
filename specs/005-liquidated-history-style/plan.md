# Implementation Plan: Destaque visual de investimentos liquidados no Histórico

**Branch**: `005-liquidated-history-style` | **Date**: 2026-05-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/005-liquidated-history-style/spec.md`  
**Nota de planeamento**: Propriedade `isLiquidated` em `HoldingHistoryView` encapsula `currentValue == 0.0` (pedido explícito). Sem migração de banco. Colunas **Valorização** e **Transações** mantêm precedência de cor semântica sobre cinza de liquidado.

## Summary

Expor **liquidação** na view de histórico (`HoldingHistoryView.isLiquidated`) e aplicar **cinza muted** nas colunas gerais da tabela de posicionamento quando `valor atual == 0`, preservando cores por sinal em **Valorização** e **Transações**, ícones e tooltips inalterados.

Abordagem: propriedade derivada em `:domain:usecases` + testes unitários + helper de cor em `:presentation:design-system` + ajustes em `AssetHistoryScreen` e `TableInputMoney` com `textColor` opcional em **composeApp** (não no módulo design-system usado pelo Histórico).

## Technical Context

**Language/Version**: Kotlin 2.3.x (KMP), Compose Multiplatform

**Primary Dependencies**: Material3, design-system (`UiTableV3`), módulo `:domain:usecases`

**Storage**: N/A (estado derivado de `currentValue` já carregado pelo fluxo de histórico)

**Testing**: `./gradlew :domain:usecases:jvmTest` — `HoldingHistoryViewTest`; compilar `:features:composeApp:compileKotlinJvm`

**Target Platform**: Android, iOS, Desktop (JVM) — `commonMain`

**Project Type**: KMP monorepo (apps + features + domain + data)

**Performance Goals**: Sem metas novas; avaliação `isLiquidated` O(1) por linha

**Constraints**: Clean Architecture; `explicitApi()`; grafo de módulos inalterado; UI não reimplementa regra `== 0`

**Scale/Scope**: ~5–8 ficheiros Kotlin; sem novos módulos Gradle

## Constitution Check

*GATE: Deve passar antes da Phase 0. Revalidado após Phase 1.*

| # | Princípio | Verificação | Status |
|---|-----------|-------------|--------|
| I | SOLID, DRY, KISS, YAGNI | Regra única em `HoldingHistoryView`; cor muted centralizada; sem filtro nesta entrega | **APROVADO** |
| II | Clean Architecture | Regra na view de usecases; UI só consome `isLiquidated`; sem dependência `:features` → `:data` nova | **APROVADO** |
| III | KMP First | Alterações em `commonMain` | **APROVADO** |
| IV | Plugins Foundation | Sem plugins novos | **APROVADO** |
| V | Testes em Use Cases | `HoldingHistoryViewTest` em `:domain:usecases:jvmTest` | **APROVADO** |
| VI | API Explícita | `isLiquidated` e helpers `public` onde expostos | **APROVADO** |
| VII | Documentação sincronizada | `spec.md`, `plan.md`, `data-model.md`, contrato, `quickstart.md`, `research.md` | **APROVADO** |
| VIII | Idioma | Docs pt-BR; código `isLiquidated` em inglês | **APROVADO** |

**Resultado do gate**: **APROVADO** — sem violações para Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/005-liquidated-history-style/
├── plan.md              # Este ficheiro
├── spec.md
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── LiquidatedHistoryRowContract.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (alterações previstas)

```text
core/
├── domain/
│   └── usecases/
│       ├── entities/
│       │   └── HoldingHistoryView.kt          # + isLiquidated
│       └── src/jvmTest/kotlin/com/eferraz/usecases/entities/
│           └── HoldingHistoryViewTest.kt      # novo
├── presentation/
│   ├── design-system/src/commonMain/kotlin/com/eferraz/design_system/theme/
│   │   └── Theme.kt                           # + historyMutedTextColor()
│   └── composeApp/src/commonMain/kotlin/com/eferraz/presentation/
│       ├── features/history/AssetHistoryScreen.kt
│       └── design_system/components/inputs/
│           └── TableInputMoney.kt             # + textColor (usado pelo Histórico)
```

> **Nota**: O ecrã de Histórico importa `com.eferraz.presentation.design_system.components.inputs.TableInputMoney` (módulo **composeApp**), não o homónimo em `:presentation:design-system`.

**Structure Decision**: Domínio (`isLiquidated`) → design-system (cor muted) → composeApp (`TableInputMoney` + `AssetHistoryScreen`); filtro futuro reutiliza `isLiquidated` sem refactor da regra.

## Phase 0 — Research

Concluída em [research.md](./research.md). Sem `NEEDS CLARIFICATION` pendentes.

Destaques:
- `isLiquidated` como propriedade derivada em `HoldingHistoryView`
- Precedência Valorização/Transações > cinza liquidado
- `historyMutedTextColor()` centralizado
- `TableInputMoney.textColor` opcional para coluna Valor Atual

## Phase 1 — Design & Contracts

| Artefacto | Caminho |
|-----------|---------|
| Modelo de dados / view | [data-model.md](./data-model.md) |
| Contrato UI/dados | [contracts/LiquidatedHistoryRowContract.md](./contracts/LiquidatedHistoryRowContract.md) |
| Validação manual | [quickstart.md](./quickstart.md) |

### `HoldingHistoryView.isLiquidated` (obrigatório)

```kotlin
public val isLiquidated: Boolean
    get() = currentValue == 0.0
```

- **Não** adicionar ao construtor primário do `data class`.
- Construtor `HistoryTableData` permanece; getter deriva de `currentValue` já mapeado.
- UI: `if (row.isLiquidated) historyMutedTextColor() else …` apenas em colunas **fora** Valorização/Transações/ícones/B3.

### Cores por coluna (`AssetHistoryScreen`)

| Coluna | Ação |
|--------|------|
| Ícones, B3 | Sem mudança |
| Corretora, Display Name, Observação, Valor Anterior | `Text` com cor muted se `isLiquidated` |
| Valor Atual | `TableInputMoney(..., textColor = muted se liquidado)` |
| Transações, Valorização | Manter `when` existente — **ignorar** `isLiquidated` na escolha de cor |

### Agent context

Atualizar `.cursor/rules/specify-rules.mdc` (marcador SPECKIT) para `specs/005-liquidated-history-style/plan.md`.

## Phase 2 — Tasks

Gerado pelo comando `/speckit-tasks` (não incluído neste passo).

Ordem sugerida de implementação:

1. `HoldingHistoryView.isLiquidated` + `HoldingHistoryViewTest` (`:domain:usecases`)
2. `historyMutedTextColor()` em `:presentation:design-system` (`Theme.kt`)
3. `TableInputMoney.textColor` em `:features:composeApp` (`presentation/design_system/.../TableInputMoney.kt`)
4. `AssetHistoryScreen` — muted nas colunas gerais; não alterar `when` de Valorização/Transações
5. Quickstart manual (cenários A/B/C + tooltips + transição via ViewModel)

## Complexity Tracking

> Não aplicável — gate aprovado sem exceções.
