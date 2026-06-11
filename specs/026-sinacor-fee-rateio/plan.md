# Implementation Plan: Rateio de Taxas de Nota de Corretagem SINACOR

**Branch**: `026-sinacor-fee-rateio` | **Date**: 2026-06-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/026-sinacor-fee-rateio/spec.md`

## Summary

Implementar o cálculo stateless de rateio proporcional de taxas de corretagem (emolumentos, liquidação, IR) entre os ativos de uma nota SINACOR mista (compra e venda). Aritmética inteira em centavos elimina erros de arredondamento IEEE-754. O ajuste residual de centavos vai ao ativo de maior volume. A validação de equação de fechamento contábil garante rastreabilidade. Sem persistência, sem UI, sem parsing de arquivos — tudo em `:domain:entity` como entidades puras.

## Technical Context

- **Language/Version**: Kotlin Multiplatform (commonMain) — versão do build-logic do projeto
- **Primary Dependencies**: Nenhuma — cálculo puro sem I/O ou frameworks
- **Storage**: N/A — stateless; a spec exclui persistência explicitamente
- **Testing**: kotlin.test + JVM target (`jvmTest` em `:domain:entity`)
- **Target Platform**: KMP commonMain (Android, iOS, Desktop)
- **Project Type**: Biblioteca de domínio (`:domain:entity`)
- **Performance Goals**: N/A — cálculo em memória, sub-milissegundo
- **Constraints**: Aritmética inteira em centavos (FR-005); comparação de fechamento em centavos inteiros (FR-009); nenhuma dependência de `BigDecimal` (não disponível em `commonMain`)
- **Scale/Scope**: Até ~N ativos por nota (N pequeno, tipicamente < 50); cálculo síncrono

## Constitution Check

*GATE: Verificado antes da Fase 0. Re-verificado após Fase 1.*

| Princípio | Status | Observação |
|-----------|--------|-----------|
| I. SOLID/DRY | ✅ | Responsabilidade única: cálculo de rateio. Sem duplicação de lógica. |
| II. Clean Architecture | ✅ | Tudo em `:domain:entity`; sem dependências de `:data` ou `:features`. |
| III. KMP First | ✅ | `commonMain` only; sem `expect`/`actual`; sem imports de plataforma. |
| IV. Plugins Foundation | ✅ | Módulo `:domain:entity` já existe; nenhum plugin adicional necessário. |
| V. Testes Obrigatórios | ✅ | Testes unitários em `jvmTest` são obrigatórios — todos os cenários da spec. |
| VI. API Explícita | ✅ | `explicitApi()` ativo; visibilidade explícita em todas as declarações. |
| VII. Documentação Sincronizada | ✅ | `DOMAIN.md` atualizado com pacote `brokeragenotes`. |
| VIII. Idioma | ✅ | Código em inglês; documentação em pt-BR. |
| IX. Sem Build Automático | ✅ | Nenhum `./gradlew` executado pelo agente. |
| X. Escopo Mínimo | ✅ | Apenas FR-001 a FR-012 e edge cases da spec; sem persistência, sem UI. |

**Gate de escopo (princípio X)**: O plano cobre apenas o que a spec pede. Nenhuma abstração prematura, nenhum refactor paralelo, nenhuma funcionalidade especulativa.

**Re-check pós-design (Fase 1)**: ✅ Todos os gates mantidos. O modelo de dados não introduz dependências externas nem viola o grafo de módulos.

## Project Structure

### Documentation (this feature)

```text
specs/026-sinacor-fee-rateio/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── kotlin-api.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code

```text
core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/
├── TradeType.kt               # enum BUY | SELL
├── BrokerageNoteFees.kt       # data class (emoluments, settlement, incomeTax); total derived
├── NoteAsset.kt               # data class (ticker, tradeType, quantity, unitPrice); grossValue derived
├── BrokerageNote.kt           # data class (date, netValue, fees, assets)
├── AssetFeeAllocation.kt      # data class output per asset (internal constructor)
└── NoteFeeAllocation.kt       # data class output + companion object { calculate() }

core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/
└── NoteFeeAllocationTest.kt   # Testes: cenários canônicos + edge cases

core/domain/entity/docs/
└── DOMAIN.md                  # Atualizado com pacote brokeragenotes e novas entidades
```

## Complexity Tracking

> Nenhuma violação da constituição identificada. Tabela omitida conforme instrução do template.
