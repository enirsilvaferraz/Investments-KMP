# Tasks: Imposto de renda regressivo sobre rendimentos da posição

**Input**: Design documents from `/specs/019-holding-income-tax/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/IncomeTaxContract.md, quickstart.md

**Tests**: Incluídos — spec exige cenários de teste; constituição V exige `IncomeTaxTest.kt` em `:domain:entity` (`jvmTest`). Gradle só sob pedido (princípio IX).

**Organization**: Tarefas agrupadas por user story. Implementação central em `IncomeTax.kt` (fase foundational); testes separados por US1 (faixas e lucro) e US2 (fronteiras).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências de tarefas incompletas)
- **[Story]**: US1, US2 — apenas em fases de user story
- Caminhos absolutos ao repo: `core/domain/entity/...`

## Path Conventions

- **Domínio**: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/`
- **Testes**: `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/`
- **Docs**: `core/domain/entity/docs/DOMAIN.md`
- **Sem** alterações em `:domain:usecases`, `:data`, `:features` (v1)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar branch, contrato e padrão de referência antes de codificar.

- [x] T001 Confirmar branch `019-holding-income-tax` e alinhar escopo com `specs/019-holding-income-tax/plan.md` e `specs/019-holding-income-tax/contracts/IncomeTaxContract.md`
- [x] T002 [P] Rever padrão `Growth` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/Growth.kt` e estilo de teste em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/GrowthTest.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Motor de cálculo completo — **bloqueia** testes US1/US2.

**⚠️ CRITICAL**: Nenhum trabalho de user story (testes de aceitação) até `IncomeTax.calculate` estar implementado.

- [x] T003 Criar `public data class IncomeTax` (construtor `private`, `public val taxRate`/`taxValue`, `public companion object`) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/IncomeTax.kt` conforme contrato e `explicitApi()` (princípio VI)
- [x] T004 Implementar `public fun IncomeTax.calculate(profit, purchaseDate, referenceDate)` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/holdings/IncomeTax.kt`: `purchaseDate.daysUntil(referenceDate)`, `when` monotónico 180/360/720 (FR-004, FR-007), `taxValue = if (profit > 0) profit * taxRate / 100 else 0.0` (FR-005, FR-006), `IllegalArgumentException` se `purchaseDate > referenceDate` (FR-008); KDoc em inglês; lógica de faixas `private` no companion; **sem** imports de `AssetTransaction`/`AssetHolding`

**Checkpoint**: `IncomeTax.calculate` compilável e alinhado a FR-001–FR-008 — pronto para testes por user story

---

## Phase 3: User Story 1 - Estimar IR sobre lucro realizado ou projetado (Priority: P1) 🎯 MVP

**Goal**: Dado lucro, data de compra e data de referência, devolver `taxRate` da faixa regressiva e `taxValue` (zero se lucro ≤ 0).

**Independent Test**: Lucro positivo em cada faixa (ex.: 181d → 20% e R$ 200 sobre R$ 1000); lucro zero/negativo → `taxValue == 0` com `taxRate` da faixa temporal; mesmo dia (0 dias) → 22,5%.

### Tests for User Story 1

- [x] T005 [US1] Criar esqueleto `IncomeTaxTest.kt` (`package com.eferraz.entities.holdings`, nomes `GIVEN_…_WHEN_…_THEN_…`, KDoc em inglês, marcadores `// GIVEN`/`// WHEN`/`// THEN`) em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt` conforme `test-patterns.mdc`
- [x] T006 [P] [US1] Adicionar testes de aceitação spec cenários 1–4 (lucro 1000/181d, 500 com 90d e 180d, 800/361d, 2000/721d) com delta `0.01` em `taxValue` em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt`
- [x] T007 [US1] Adicionar testes lucro zero/negativo (`taxValue == 0`, `taxRate` da faixa — cenário 5 / SC-002) e lucro `0.01` com `taxValue` proporcional em `Double` bruto (edge case spec) em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt`
- [x] T008 [US1] Adicionar teste mesmo dia compra/referência (0 dias → `taxRate` 22.5) em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt`

**Checkpoint**: US1 verificável só com `./gradlew :domain:entity:jvmTest --tests '*IncomeTax*'` (sob pedido)

---

## Phase 4: User Story 2 - Fronteiras exatas da tabela (Priority: P2)

**Goal**: Comportamento determinístico nos limites 180, 181, 360, 361, 720 e 721 dias.

**Independent Test**: Para cada par de datas com exatamente N dias, `taxRate` coincide com a tabela (22,5 / 20 / 17,5 / 15).

### Tests for User Story 2

- [x] T009 [P] [US2] Adicionar testes de fronteira **exaustivos** para 180, 181, 360, 361, 720 e 721 dias (`taxRate` e `taxValue` com lucro fixo de smoke) em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt` — complementa T006 (aceitação), cobre SC-001
- [x] T010 [US2] Adicionar teste `purchaseDate` posterior a `referenceDate` → `assertFailsWith<IllegalArgumentException>` em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/holdings/IncomeTaxTest.kt`

**Checkpoint**: SC-001 (100% casos de fronteira) coberto por testes automatizados

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Documentação e validação estática da entrega mínima.

- [x] T011 [P] Atualizar `core/domain/entity/docs/DOMAIN.md`: tabela `holdings` + subsecção `IncomeTax` (tabela regressiva, parâmetros, sem `earliestPurchaseDate`)
- [x] T012 Executar verificações estáticas de `specs/019-holding-income-tax/quickstart.md` (`rg` em `IncomeTax`, ausência de `earliestPurchaseDate` e refs em usecases/data/features)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — iniciar imediatamente
- **Foundational (Phase 2)**: Depende de Phase 1 — **bloqueia** Phase 3 e 4
- **User Story 1 (Phase 3)**: Depende de T004 (implementação)
- **User Story 2 (Phase 4)**: Depende de T004; testes US2 podem seguir ou paralelizar com T006–T008 após T005
- **Polish (Phase 5)**: Depende de T004; documentação T011 após comportamento estável; T012 no fim

### User Story Dependencies

- **US1 (P1)**: Após foundational — sem dependência de US2
- **US2 (P2)**: Após foundational — complementa US1 no mesmo `IncomeTaxTest.kt`; independentemente testável pelos métodos de fronteira

### Within Each User Story

- Esqueleto de teste (T005) antes dos casos US1
- US2 assume `IncomeTax` já implementado (T004); não altera API pública

### Parallel Opportunities

- T002 em paralelo com T001
- T006 em paralelo com T007/T008 após T005
- T009 em paralelo com T010 após T005
- T011 em paralelo com T009/T010 (ficheiro diferente)
- US1 e US2: dois agentes podem escrever blocos de teste distintos no mesmo ficheiro **após** T005 — coordenar para evitar conflitos de merge

---

## Parallel Example: User Story 1

```bash
# Após T004 e T005, em paralelo:
Task T006: cenários 1–4 em IncomeTaxTest.kt
Task T007: lucro ≤ 0 em IncomeTaxTest.kt
Task T008: 0 dias em IncomeTaxTest.kt
```

---

## Parallel Example: User Story 2

```bash
# Após T005, em paralelo:
Task T009: dias 180/181/360/361/720/721 em IncomeTaxTest.kt
Task T010: data inválida em IncomeTaxTest.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1: Setup (T001–T002)
2. Phase 2: Foundational (T003–T004) — **obrigatório**
3. Phase 3: US1 tests (T005–T008)
4. **PARAR e validar**: `jvmTest` `*IncomeTax*` (sob pedido) ou revisão manual dos cenários da spec
5. Phase 5 opcional imediato: T011 (docs)

### Incremental Delivery

1. Setup + Foundational → motor pronto
2. US1 → estimativa de IR utilizável (MVP)
3. US2 → regressão de fronteiras
4. Polish → DOMAIN.md + quickstart `rg`

### Parallel Team Strategy

1. Dev A: T003–T004 (`IncomeTax.kt`)
2. Após T004: Dev B — T005–T008 (US1); Dev C — T009–T010 (US2)
3. Dev D: T011 em paralelo quando T004 estável

---

## Traceability (FR / SC → tasks)

| Key | Has Task? | Task IDs | Notes |
|-----|-----------|----------|-------|
| FR-001 | ✅ | T003, T006 | `taxRate` + `taxValue` |
| FR-002 | ✅ | T004 | três parâmetros em `calculate` |
| FR-003 | ✅ | T004 | `daysUntil` |
| FR-004 | ✅ | T004, T006, T009 | tabela + fronteiras |
| FR-005 | ✅ | T004, T006, T007 | fórmula; sem arredondamento |
| FR-006 | ✅ | T007 | lucro ≤ 0 |
| FR-007 | ✅ | T004, T009 | `when` monotónico |
| FR-008 | ✅ | T004, T010 | data inválida |
| FR-009 | ✅ | T002, T003, T004 | padrão `Growth` |
| SC-001 | ✅ | T006, T009 | faixas documentadas |
| SC-002 | ✅ | T007 | lucro ≤ 0 |
| SC-003 | — | — | UI/apresentação fora de v1 |

## Notes

- **YAGNI**: não criar use case, UI, Room, `earliestPurchaseDate`
- **`taxRate`**: percentual legível (22.5, não 0.225); **`taxValue`**: `Double` bruto
- **Proibido** no motor: importar transações/posição (contrato)
- **T006 vs T009**: T006 valida cenários de negócio da spec; T009 garante off-by-one nos limites (US2)
- Commit após cada fase ou grupo lógico; Gradle apenas se o utilizador pedir ou em CI
