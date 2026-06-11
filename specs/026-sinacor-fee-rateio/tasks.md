# Tasks: Rateio de Taxas de Nota de Corretagem SINACOR

**Feature**: `026-sinacor-fee-rateio` | **Branch**: `026-sinacor-fee-rateio` | **Date**: 2026-06-10

**Input**: Design documents from `/specs/026-sinacor-fee-rateio/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/kotlin-api.md ✅ | quickstart.md ✅

**Testes**: Obrigatórios — constituição princípio V + `plan.md` ("testes unitários em `jvmTest` são obrigatórios — todos os cenários da spec").

**Organização**: Tarefas agrupadas por user story para permitir implementação e teste independentes de cada história.

---

## Formato: `[ID] [P?] [Story?] Descrição com caminho de arquivo`

- **[P]**: Pode ser executada em paralelo (arquivo diferente, sem dependências incompletas)
- **[Story]**: User story a que a tarefa pertence (US1, US2, US3)
- Caminhos base: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/`

---

## Phase 1: Foundational (Entidades de Input)

**Propósito**: Criar as quatro entidades de input que TODAS as user stories dependem. Nenhuma user story pode ser implementada ou testada sem este conjunto completo.

> **Nota de setup**: O módulo `:domain:entity` já está configurado para `commonMain` + `jvmTest`. Não há alteração em `build.gradle.kts` nem em `settings.gradle.kts` — o pacote `brokeragenotes` é criado implicitamente ao criar os primeiros arquivos.

**⚠️ CRÍTICO**: Nenhuma implementação de user story começa antes desta fase estar completa.

- [x] T002 [P] Criar `TradeType.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/TradeType.kt` — enum `public enum class TradeType { BUY, SELL }` com `explicitApi()`
- [x] T003 [P] Criar `BrokerageNoteFees.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNoteFees.kt` — data class com `emoluments`, `settlement`, `incomeTax: Double` e propriedade derivada `total` (FR-003)
- [x] T004 [P] Criar `NoteAsset.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteAsset.kt` — data class com `ticker: String`, `tradeType: TradeType`, `quantity: Double`, `unitPrice: Double` e propriedade derivada `grossValue` (FR-001); validação ocorre em `calculate`, não no construtor
- [x] T005 Criar `BrokerageNote.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNote.kt` — data class com `date: LocalDate`, `netValue: Double`, `fees: BrokerageNoteFees`, `assets: List<NoteAsset>` (depende de T002, T003, T004)

**Checkpoint**: Entidades de input completas — implementação das user stories pode começar.

---

## Phase 2: User Story 1 — Calcular rateio proporcional entre ativos (Priority: P1) 🎯 MVP

**Goal**: Distribuir as taxas da nota proporcionalmente pelo volume financeiro de cada ativo, apurando o valor líquido real por operação com aritmética inteira em centavos (sem FR-009 ainda).

**Independent Test**: Fornecer nota com 3 ativos (2 BUY + 1 SELL), verificar que as taxas são distribuídas proporcionalmente, que BUY aumenta `netValue` e SELL diminui, e que erros de entrada inválida lançam `IllegalArgumentException`.

### Implementação da User Story 1

- [x] T006 [P] [US1] Criar `AssetFeeAllocation.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/AssetFeeAllocation.kt` — data class de output com construtor `internal`: `ticker: String`, `grossValue: Double`, `allocatedFee: Double`, `netValue: Double`
- [x] T007 [US1] Criar `NoteFeeAllocation.kt` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt` — data class com construtor `internal`, `allocations: List<AssetFeeAllocation>`, e `companion object { fun calculate(note: BrokerageNote): NoteFeeAllocation }` implementando o algoritmo completo SEM FR-009: (a) validações de entrada FR-010/FR-011/FR-012 → `IllegalArgumentException`; (b) aritmética em centavos Long para cálculo de `grossValueCents`, `totalVolumeCents`, `totalFeesCents`; (c) distribuição proporcional com `floor` (FR-004/FR-005); (d) ajuste do resíduo no ativo de maior volume (FR-006); (e) cálculo de `netValue` por `tradeType` (FR-007/FR-008); retorna `NoteFeeAllocation` com lista de `AssetFeeAllocation` (depende de T005, T006)

### Testes da User Story 1 ⚠️ Escrever ANTES de T007, garantir que FALHAM inicialmente

- [x] T008 [US1] Criar `NoteFeeAllocationTest.kt` em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt` com os seguintes testes (depende de T007 para passar; escrever estrutura antes): cenário canônico 3 ativos (AJFI11/BRCO11/VILG11 com volumes iguais → verificar `allocatedFee` e `netValue` de cada ativo e que `allocatedFee[0] + allocatedFee[1] + allocatedFee[2] == 4.54` — SC-001); ativo único BUY absorve 100% das taxas; ativo único SELL absorve 100% das taxas; nota com todos os ativos SELL (`note.netValue` negativo); todas as taxas zero → `allocatedFee=0.0` e `netValue=grossValue`; lista de ativos vazia → `IllegalArgumentException("assets must not be empty")`; ativo com `quantity <= 0` → `IllegalArgumentException("asset {ticker}: quantity and unitPrice must be > 0")`; ativo com `unitPrice <= 0` → mesma exceção; volume total zero (impossível com validações anteriores, mas verificar)

**Checkpoint**: US1 completa — rateio proporcional funciona e erros de entrada são tratados.

---

## Phase 3: User Story 2 — Garantir fechamento contábil da nota (Priority: P1)

**Goal**: Validar que `Σ(compras_líquidas) − Σ(vendas_líquidas) == note.netValue` após o rateio, retornando erro descritivo quando a equação não é satisfeita.

**Independent Test**: Calcular o rateio do cenário canônico e verificar que a equação de fechamento é satisfeita; calcular com `note.netValue` incorreto e verificar que `IllegalStateException` é lançada com mensagem contendo a discrepância.

### Implementação da User Story 2

- [x] T009 [US2] Adicionar validação FR-009 ao `companion object { fun calculate() }` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt` — após o cálculo de `netValue` por ativo: converter `netValue` de cada ativo para centavos com `Math.round`; somar centavos de BUY e centavos de SELL separadamente; calcular `differenceCents = buysTotalCents - sellsTotalCents - noteNetValueCents`; se `differenceCents != 0L` lançar `IllegalStateException("accounting closure failed: expected ${note.netValue}, got ${(buysTotalCents - sellsTotalCents) / 100.0}")` (depende de T007)

### Testes da User Story 2 ⚠️ Escrever ANTES de T009 para os cenários de falha, garantir que FALHAM inicialmente

- [x] T010 [US2] Adicionar testes US2 a `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt` — cenário canônico: verificar que `Σ BUY netValue − Σ SELL netValue == note.netValue` em centavos (1001.52 + 1001.51 − 998.49 = 1004.54 ✓); fechamento falha: nota com `netValue = 9999.00` → `IllegalStateException` com mensagem contendo `"accounting closure failed"` e os valores calculados (depende de T009)

**Checkpoint**: US1 + US2 completas — rateio e fechamento contábil validados.

---

## Phase 4: User Story 3 — Tratar arredondamento de centavos sem perda nem ganho (Priority: P2)

**Goal**: Garantir via testes que o algoritmo de aritmética inteira em centavos com ajuste de resíduo no ativo de maior volume é determinístico e que a soma das taxas individuais é sempre idêntica ao total da nota.

**Independent Test**: Fornecer 3 ativos com volumes exatamente iguais (1/3 cada), verificar que o centavo residual vai para o primeiro da lista e que a soma das taxas alocadas é igual ao total de taxas da nota.

> **Nota**: A implementação correta já está em T007/T009 — esta fase adiciona testes de regressão específicos para o comportamento de arredondamento.

### Testes da User Story 3

- [x] T011 [US3] Adicionar testes US3 a `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt` — 3 ativos volumes iguais (R$1.000 cada, taxas R$4,54): verificar AJFI11=1.52, BRCO11=1.51, VILG11=1.51 e soma=4.54; 2 ativos com volume exatamente igual: verificar que centavo residual vai para o primeiro ativo da lista (critério determinístico); invariante geral: para qualquer nota válida `allocations.sumOf { Math.round(it.allocatedFee * 100) } == Math.round(note.fees.total * 100)` (depende de T007)

**Checkpoint**: US1 + US2 + US3 completas — todas as user stories funcionais e testadas independentemente.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Propósito**: Atualizar documentação e confirmar cobertura de edge cases.

- [x] T012 [P] Atualizar `core/domain/entity/docs/DOMAIN.md` adicionando seção do pacote `com.eferraz.entities.brokeragenotes` com descrição das entidades `TradeType`, `BrokerageNoteFees`, `NoteAsset`, `BrokerageNote`, `AssetFeeAllocation`, `NoteFeeAllocation` e referência ao algoritmo de rateio

---

## Dependencies & Execution Order

### Dependências entre Fases

- **Foundational (Phase 1)**: Sem dependências — pode começar imediatamente; **BLOQUEIA todas as user stories**
- **US1 (Phase 2)**: Depende do Foundational — primeiro incremento entregável
- **US2 (Phase 3)**: Depende de US1 (adiciona ao mesmo arquivo `NoteFeeAllocation.kt`)
- **US3 (Phase 4)**: Depende de US1 (testes apenas, sem nova implementação)
- **Polish (Phase 5)**: Depende de US1 + US2 + US3 estarem completas

### Dependências entre User Stories

- **US1 (P1)**: Inicia após Foundational — sem dependência de outras stories
- **US2 (P1)**: Inicia após US1 — adiciona validação ao `calculate()` existente
- **US3 (P2)**: Inicia após US1 — apenas testes, pode ser feita em paralelo com US2

### Dentro de Cada User Story

- T007 (NoteFeeAllocation) depende de T005 (BrokerageNote) e T006 (AssetFeeAllocation)
- T008 (testes US1) depende de T007 para passar, mas a estrutura pode ser escrita antes
- T009 (FR-009) depende de T007 (mesmo arquivo)
- T010 (testes US2) depende de T009
- T011 (testes US3) depende de T007

### Oportunidades de Paralelismo

- **Phase 1**: T002, T003, T004 podem ser escritas simultaneamente (arquivos independentes)
- **Phase 2**: T006 pode começar em paralelo com T007 (arquivos diferentes)
- **Phase 4 + Phase 5**: T011 e T012 podem ser feitas em paralelo (sem dependência entre si)

---

## Parallel Example: User Story 1

```text
# Paralelizar criação das entidades de input (Phase 1):
Task T002: TradeType.kt
Task T003: BrokerageNoteFees.kt
Task T004: NoteAsset.kt

# Após T005 (BrokerageNote), paralelizar output entity e implementação:
Task T006: AssetFeeAllocation.kt
Task T007: NoteFeeAllocation.kt  ← depende de T005 e T006
```

---

## Implementation Strategy

### MVP First (User Story 1 apenas)

1. Completar Phase 1: Foundational (T002–T005) — **CRÍTICO**
2. Completar Phase 2: US1 (T006–T008)
3. **(Opcional — princípio IX)** Validar executando `./gradlew :domain:entity:jvmTest` — todos os testes US1 devem passar
4. Entregar: rateio proporcional com aritmética em centavos funcionando

### Entrega Incremental

1. Foundational → base pronta
2. US1 → rateio proporcional testado → **MVP**
3. US2 → fechamento contábil → auditabilidade garantida
4. US3 → testes de regressão de arredondamento → confiança extra
5. Polish → documentação atualizada → feature completa

### Estratégia de Um Desenvolvedor

Sequência recomendada: T002+T003+T004 (paralelo) → T005 → T006+T007 (paralelo, T007 aguarda T006) → T008 → T009 → T010 → T011 → T012

---

## Notes

- **Escopo mínimo** (princípio X): cada tarefa implementa apenas o pedido na spec/plano — sem abstrações prematuras, sem refactor não solicitado, sem persistência ou UI.
- **Sem `BigDecimal`** (research.md §1): toda aritmética monetária usa `Long` em centavos no `commonMain`.
- **Exceções, não `Result`** (research.md §2): consistente com `IncomeTax.calculate` existente no módulo.
- **`internal` construtor** (research.md §7): `AssetFeeAllocation` e `NoteFeeAllocation` — construtores `internal` permitem testes no mesmo módulo sem fábricas adicionais.
- **Algoritmo canônico**: ver `data-model.md` seção "Algoritmo de Cálculo" para pseudocódigo passo-a-passo completo.
- **Cenário de referência**: `quickstart.md` — AJFI11/BRCO11/VILG11 com resultado esperado documentado.
- [P] = arquivos diferentes, sem dependências incompletas — podem ser abertos em paralelo pelo agente
- Cada checkpoint é um bom momento para validar de forma independente — executar `./gradlew :domain:entity:jvmTest` é **opcional para o agente** (princípio IX); obrigatório apenas quando o utilizador pedir ou em CI
