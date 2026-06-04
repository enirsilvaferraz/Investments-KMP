# Implementation Plan: Imposto de renda regressivo sobre rendimentos da posição

**Branch**: `019-holding-income-tax` | **Date**: 2026-06-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/019-holding-income-tax/spec.md`  
**Diretriz do utilizador**: **simplicidade**, **menos é mais** — **apenas** `IncomeTax.kt` + testes; sem `earliestPurchaseDate`, sem use case, sem UI, sem `:data`.

## Summary

Introduzir no domínio o cálculo de **IR regressivo** sobre lucro em reais, com base no tempo entre **data de compra** (parâmetro do chamador) e **data de referência**, seguindo o padrão de `Growth.kt`:

- **`IncomeTax`**: `taxRate` + `taxValue`, via `IncomeTax.calculate(profit, purchaseDate, referenceDate)`.

Tabela: 22,5% (≤180d) → 20% (≤360d) → 17,5% (≤720d) → 15% (>720d). Imposto zero se lucro ≤ 0.

Contrato: [contracts/IncomeTaxContract.md](./contracts/IncomeTaxContract.md). Decisões: [research.md](./research.md).

## Technical Context

**Language/Version**: Kotlin 2.x — KMP (`commonMain` + `jvmTest`)

**Primary Dependencies**: `:domain:entity` apenas; `kotlinx.datetime` (`LocalDate`, `daysUntil`)

**Storage**: N/A

**Testing**: `IncomeTaxTest.kt` em `entity/jvmTest` — **sem** `./gradlew` automático (princípio IX)

**Target Platform**: Domínio partilhado (Android, iOS, Desktop)

**Project Type**: Regra de negócio pura em entidade (padrão `Growth`)

**Constraints**: Clean Architecture (só `entity`); `explicitApi()`; KISS/YAGNI

**Scale/Scope**: **1** ficheiro `.kt` em `commonMain`, **1** ficheiro de teste; 0 alterações Room

## Constitution Check

*GATE: Deve passar antes da Phase 0. Revalidado após Phase 1.*

| # | Princípio | Verificação | Status |
|---|-----------|-------------|--------|
| I | SOLID, KISS, YAGNI | Uma classe; `when` de 4 faixas; sem extensão de transações | **APROVADO** |
| II | Clean Architecture | Só `:domain:entity` | **APROVADO** |
| III | KMP First | `commonMain` / `jvmTest` | **APROVADO** |
| IV | Plugins Foundation | Sem alteração `build.gradle.kts` | **APROVADO** |
| V | Testes | `IncomeTaxTest` em entity | **APROVADO** |
| VI | API Explícita | `public` em `IncomeTax`; lógica `private` no companion | **APROVADO** |
| VII | Documentação | `DOMAIN.md` (secção curta) | **APROVADO** |
| VIII | Idioma | Docs pt-BR; código/testes inglês | **APROVADO** |
| IX | Validação | Gradle sob pedido | **APROVADO** |

**Resultado do gate (pós-design)**: **APROVADO** — Complexity Tracking vazio.

## Project Structure

### Documentation (this feature)

```text
specs/019-holding-income-tax/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/IncomeTaxContract.md
└── tasks.md             # Phase 2 (/speckit.tasks)
```

### Source Code (entrega prevista)

```text
core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/
└── IncomeTax.kt

core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/
└── IncomeTaxTest.kt

core/domain/entity/docs/DOMAIN.md   # + secção IncomeTax
```

**Structure Decision**: Feature **100%** em `holdings/` — motor puro; `purchaseDate` sempre externa.

## Complexity Tracking

> Sem violações.

## Phase 0 — Research

Concluída em [research.md](./research.md): `daysUntil`, tabela em `when`, `earliestPurchaseDate` **fora de escopo** (R4).

## Phase 1 — Design

| Artefacto | Conteúdo |
|-----------|----------|
| [data-model.md](./data-model.md) | `IncomeTax` + faixas |
| [contracts/IncomeTaxContract.md](./contracts/IncomeTaxContract.md) | API única |
| [quickstart.md](./quickstart.md) | Validação mínima |

## Phase 2 — Implementação (referência para `/speckit.tasks`)

Ver [tasks.md](./tasks.md) (12 tarefas T001–T012, traceability FR/SC).

```text
1. IncomeTax.kt + IncomeTaxTest.kt (padrão Growth; test-patterns GIVEN/WHEN/THEN)
2. DOMAIN.md — parágrafo IR regressivo
```

**Fora de v1**: `earliestPurchaseDate`, histórico/UI, use cases de integração.

## Riscos e mitigação

| Risco | Mitigação |
|-------|-----------|
| Off-by-one em dias | Testes com pares `LocalDate` fixos (US2) |
| Confundir % com decimal | `taxRate` em percentual legível (22.5) |
| Acoplar a transações | Contrato proíbe imports de `AssetTransaction` |
