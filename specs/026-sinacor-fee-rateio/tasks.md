# Tasks: Rateio de Taxas de Nota de Corretagem SINACOR

**Feature**: `026-sinacor-fee-rateio` | **Branch**: `026-sinacor-fee-rateio` | **Date**: 2026-06-10

**Input**: Design documents from `/specs/026-sinacor-fee-rateio/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/kotlin-api.md ✅ | quickstart.md ✅

**Testes**: Obrigatórios — `plan.md` ("testes unitários em `jvmTest` são obrigatórios — todos os cenários da spec").

**Organização**: Tarefas agrupadas por user story. Referência canônica: `data-model.md` (algoritmo), `contracts/kotlin-api.md` (API), `quickstart.md` (valores esperados).

---

## Formato: `[ID] [P?] [Story?] Descrição com caminho de arquivo`

- **[P]**: Pode ser executada em paralelo (arquivo diferente, sem dependências incompletas)
- **[Story]**: User story a que a tarefa pertence (US1–US4)
- Pacote base: `com.eferraz.entities.brokeragenotes`
- Caminho base: `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/`
- Testes: `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/`

---

## Phase 1: Foundational — Entidades de Input

**Propósito**: Criar os 7 tipos de input que TODAS as user stories dependem. Sem novas dependências Gradle — o módulo `:domain:entity` já suporta `commonMain` + `jvmTest`.

**⚠️ CRÍTICO**: Nenhuma implementação de user story começa antes desta fase estar completa.

- [x] T001 [P] Criar `TradeType.kt` com `public enum class TradeType { BUY, SELL }` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/TradeType.kt`
- [x] T002 [P] Criar `ApportionableFees.kt` com `public data class ApportionableFees(val settlement: Double, val emoluments: Double, val transfer: Double, val brokerage: Double, val iss: Double, val others: Double)` e propriedade derivada `val total: Double` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/ApportionableFees.kt`
- [x] T003 [P] Criar `WithheldTaxes.kt` com `public data class WithheldTaxes(val irrfOperations: Double, val irrfDayTrade: Double)` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/WithheldTaxes.kt`
- [x] T004 [P] Criar `FinancialSummary.kt` com `public data class FinancialSummary(val totalVolumeTraded: Double, val totalBuys: Double, val totalSells: Double, val apportionableFees: ApportionableFees, val withheldTaxes: WithheldTaxes)` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/FinancialSummary.kt` (usa T002, T003)
- [x] T005 [P] Criar `BrokerageNoteMetadata.kt` com `public data class BrokerageNoteMetadata(val noteNumber: String, val tradingDate: LocalDate, val settlementDate: LocalDate, val brokerage: String, val brokerageDocument: String, val netValue: Double)` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNoteMetadata.kt`
- [x] T006 [P] Criar `NoteAsset.kt` com `public data class NoteAsset(val ticker: String, val specification: String, val tradeType: TradeType, val quantity: Double, val unitPrice: Double, val grossValue: Double)` — sem propriedade derivada; `grossValue` é campo fornecido pela fonte, validado em Etapa 1 (FR-007); todos os 6 campos participam de `equals`/`hashCode` por default (usa T001) em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteAsset.kt`
- [x] T007 Criar `BrokerageNote.kt` com `public data class BrokerageNote(val metadata: BrokerageNoteMetadata, val financialSummary: FinancialSummary, val assets: List<NoteAsset>)` em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNote.kt` (depende de T004, T005, T006)

**Checkpoint**: Entidades de input completas — implementação das user stories pode começar.

---

## Phase 2: User Story 1 — Validar integridade dos dados brutos (Priority: P1)

**Goal**: Implementar `BrokerageNoteValidator` que verifica integridade da nota antes de qualquer cálculo (Etapa 1): não-negatividade, totalVolume > 0, Σ grossValue == totalVolumeTraded, quantity×unitPrice == grossValue por ativo, subtotais BUY/SELL batem com declarado.

**Independent Test**: Fornecer nota com `totalVolumeTraded` divergente da soma dos ativos → `IllegalArgumentException` descritiva sem produzir rateio.

### Implementação da User Story 1

- [x] T008 [US1] Criar `BrokerageNoteValidator.kt` como `internal object BrokerageNoteValidator` com `internal fun validate(note: BrokerageNote): Unit` (visível de jvmTest do mesmo módulo — permite testar Etapa 1 independentemente de T010), implementando as 8 regras: (1.1) `assets.isNotEmpty()` → `"assets must not be empty"`; (1.2) sem negativos em `apportionableFees.*`, `totalVolumeTraded`, `totalBuys`, `totalSells`, `quantity`, `unitPrice`, `grossValue` → `"fee/volume fields must not be negative: {field}"`; (1.3) `quantity > 0 && unitPrice > 0` por ativo → `"asset {ticker}: quantity and unitPrice must be > 0"`; (1.4) `totalVolumeTraded > 0` → `"total volume must be > 0"`; (1.5) `round(Σ grossValue×100) == round(totalVolumeTraded×100)` — comparação em Long — → `"volume mismatch: assets sum {sum} ≠ declared {total}"`; (1.6) por ativo `round(quantity×unitPrice×100) == round(grossValue×100)` → `"asset {ticker}: quantity×unitPrice {computed} ≠ grossValue {declared}"`; (1.7) `round(Σ BUY grossValue×100) == round(totalBuys×100)` e SELL análogo → `"buys/sells totals mismatch: expected {expected}, got {computed}"`; **(1.8) sem ativos estruturalmente idênticos**: `assets.size == assets.toSet().size` → `"duplicate asset detected: {ticker} at index {i} is identical to asset at index {j}"` — lançar `IllegalArgumentException` ao primeiro erro; em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/BrokerageNoteValidator.kt` (depende de T007)

### Testes da User Story 1

- [x] T009 [US1] Criar `NoteFeeAllocationTest.kt` com testes de Etapa 1 chamando **`BrokerageNoteValidator.validate(note)`** diretamente (internal — acessível de jvmTest do mesmo módulo; não depende de T010): `assets vazia → IllegalArgumentException("assets must not be empty")`; `taxa de liquidação negativa → IllegalArgumentException contendo "must not be negative"`; `quantity <= 0 → IllegalArgumentException("asset {ticker}: quantity and unitPrice must be > 0")`; `totalVolumeTraded divergente → IllegalArgumentException("volume mismatch")`; `grossValue divergente de quantity×unitPrice → IllegalArgumentException contendo "quantity×unitPrice"` (ex.: NoteAsset ticker="XPTO3", grossValue=999.00 mas 100×10.00=1000.00); `subtotais BUY incorretos → IllegalArgumentException("buys/sells totals mismatch")`; **dois ativos idênticos em todos os campos → IllegalArgumentException contendo "duplicate asset"** (ex.: inserir AJFI11 duas vezes com mesmos 6 campos na lista) — em `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt` (depende de T008)

**Checkpoint**: US1 completa — todos os 7 cenários de validação da Etapa 1 estão testados.

---

## Phase 3: User Story 2 — Calcular rateio de taxas entre ativos (Priority: P1)

**Goal**: Implementar `fun BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation` com Etapa 2 do algoritmo: invocar validator, calcular `Soma_Taxas`, distribuir proporcionalmente com ROUND_HALF_UP para os N-1 primeiros ativos e resíduo no último, calcular `netValue` por `tradeType`, retornar `Map<NoteAsset, Double>`.

**Independent Test**: Nota canônica 3 ativos (AJFI11/BRCO11/VILG11, volumes iguais R$1.000, Soma_Taxas=R$4,54) → verificar AJFI11=1.51, BRCO11=1.51, VILG11=1.52 (último absorve resíduo) e Σ allocatedFee=4.54.

### Implementação da User Story 2

- [x] T010 [US2] Criar `NoteFeeAllocation.kt` com: `public typealias NoteFeeAllocation = Map<NoteAsset, Double>` e `public fun BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation` implementando Etapa 2 (SEM Etapa 3 ainda): (a) invocar `BrokerageNoteValidator.validate(this)`; (b) calcular `somaFeesCents = round(financialSummary.apportionableFees.total × 100)` e `totalVolumeCents = round(financialSummary.totalVolumeTraded × 100)`; (c) para cada ativo `[0..N-2]`: `feeCents[i] = (grossValueCents[i] * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents` (ROUND_HALF_UP inteiro); (d) último ativo: `feeCents[N-1] = somaFeesCents − Σ feeCents[0..N-2]`; (e) `netValue = grossValue + allocatedFee` se BUY, `grossValue - allocatedFee` se SELL; (f) retornar `mapOf(asset → netValue)` preservando ordem — em `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt` (depende de T007, T008)

### Testes da User Story 2

- [x] T011 [US2] Adicionar testes US2 a `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt` — ver valores esperados em `quickstart.md`: (a) cenário simplificado 3 ativos volumes iguais — `result[NoteAsset("AJFI11","AJFI11 CI",BUY,100.0,10.00,1000.00)] == 1001.51` e `result[...BRCO11...] == 998.49` e `result[...VILG11...] == 1001.52` (último absorve resíduo de 1 centavo); (b) **SC-004 — nota canônica `docs/nota.json`**: construir `BrokerageNote` com os 30 ativos do arquivo, chamar `calculateFeeAllocation()`, verificar que retorna mapa com 30 entradas, que `Σ(allocatedFee) == 14.66` (Soma_Taxas) e que `Σ(BUY netValue) − Σ(SELL netValue) == −33705.98` (valor_liquido_nota); (c) ativo único BUY absorve 100%; (d) ativo único SELL absorve 100%; (e) nota com todos ativos SELL; (f) todas as taxas zero → `allocatedFee=0.0` e `netValue=grossValue`; (g) dois tickers iguais com `specification` diferente → chaves distintas no mapa (ex.: BRBI11 "BRB111F UNT N2" e BRBI11 "BRB111 UNT N2") (depende de T010)

**Checkpoint**: US2 completa — algoritmo de rateio funciona; mapa retornado com valores corretos.

---

## Phase 4: User Story 3 — Garantir fechamento contábil da nota (Priority: P1)

**Goal**: Adicionar Etapa 3 à `calculateFeeAllocation()`: validar `Σ(allocatedFee) == Soma_Taxas` (FR-018) e `Σ(BUY netValue) − Σ(SELL netValue) == metadata.netValue` em centavos (FR-019); lançar `IllegalStateException` descritiva em caso de falha.

**Independent Test**: Calcular rateio com `metadata.netValue` incorreto → `IllegalStateException("accounting closure failed: expected 9999.0, got 1004.54")`.

### Implementação da User Story 3

- [x] T012 [US3] Adicionar Etapa 3 a `core/domain/entity/src/commonMain/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocation.kt` — após calcular o mapa de resultado, antes de retornar: (3.1) verificar `Σ round(allocatedFee[i]×100) == somaFeesCents` → `IllegalStateException("fee distribution mismatch: allocated {sum} ≠ somaFees {total}")`; (3.2) calcular `buysTotalCents = Σ round(netValue[BUY]×100)` e `sellsTotalCents = Σ round(netValue[SELL]×100)` e `noteNetValueCents = round(metadata.netValue×100)`; verificar `buysTotalCents - sellsTotalCents == noteNetValueCents` → `IllegalStateException("accounting closure failed: expected {metadata.netValue}, got {(buysTotalCents-sellsTotalCents)/100.0}")` (depende de T010)

### Testes da User Story 3

- [x] T013 [US3] Adicionar testes US3 a `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt`: cenário canônico: verificar `Σ BUY netValue − Σ SELL netValue == metadata.netValue` (1001.51 + 1001.52 − 998.49 = 1004.54 ✓); fechamento falha com `metadata.netValue = 9999.00` → `IllegalStateException` contendo `"accounting closure failed"`; nota só SELL: `Σ BUY (=0) − Σ SELL == metadata.netValue` (negativo) satisfeita (depende de T012)

**Checkpoint**: US1 + US2 + US3 completas — pipeline de 3 etapas validado end-to-end. 🎯 **MVP entregável**

---

## Phase 5: User Story 4 — Tratar arredondamento sem perda (Priority: P2)

**Goal**: Confirmar via testes de regressão que ROUND_HALF_UP + resíduo no último ativo garante `Σ(allocatedFee) == Soma_Taxas` em 100% dos casos, incluindo casos onde a dízima de rateio proporcional gera erro de arredondamento.

> **Nota**: A implementação correta já está em T010/T012 — esta fase adiciona cobertura de edge cases de arredondamento.

### Testes da User Story 4

- [x] T014 [US4] Adicionar testes US4 a `core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt`: invariante geral — para qualquer nota válida `Σ round(allocatedFee×100) == somaFeesCents` (verificar com cenário 3 ativos iguais e soma exata=4.54); caso empate de volumes — dois ativos com mesmo `grossValue`: verificar que o SEGUNDO (último) recebe resíduo, não o de maior volume; ativo único: `allocatedFee == somaFees` (resíduo total para o único ativo); nota com `somaFees = 0` → todos `allocatedFee = 0.0` e `netValue = grossValue` e fechamento satisfeito; **determinismo (FR-023)** — chamar `note.calculateFeeAllocation()` duas vezes consecutivas na mesma instância de nota e verificar `result1 == result2` e `result1.keys.toList() == result2.keys.toList()` (mesma ordem de iteração) (depende de T010)

**Checkpoint**: US4 completa — comportamento de arredondamento verificado com edge cases.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Propósito**: Documentação e verificação final.

- [x] T015 [P] Atualizar `core/domain/entity/docs/DOMAIN.md` adicionando seção para o pacote `com.eferraz.entities.brokeragenotes` com: lista das 9 entidades (`TradeType`, `ApportionableFees`, `WithheldTaxes`, `FinancialSummary`, `BrokerageNoteMetadata`, `NoteAsset`, `BrokerageNote`, `BrokerageNoteValidator`, `NoteFeeAllocation`), descrição do algoritmo de rateio (Etapa 1/2/3), referência ao `data-model.md` e `contracts/kotlin-api.md`

---

## Dependencies & Execution Order

### Dependências entre Fases

- **Foundational (Phase 1)**: Sem dependências — pode começar imediatamente; **BLOQUEIA todas as user stories**
- **US1 (Phase 2)**: Depende de Phase 1 completo (T001–T007)
- **US2 (Phase 3)**: Depende de US1 completo (T008–T009)
- **US3 (Phase 4)**: Depende de US2 completo (T010–T011) — adiciona ao mesmo arquivo `NoteFeeAllocation.kt`
- **US4 (Phase 5)**: Depende de US3 completo (T012–T013) — apenas testes de regressão
- **Polish (Phase 6)**: Depende de US1 + US2 + US3 + US4 completos

### Dependências entre Tarefas

| Tarefa | Depende de |
|--------|-----------|
| T004 (FinancialSummary) | T002, T003 (mesma phase, ordem interna) |
| T006 (NoteAsset) | T001 (TradeType) |
| T007 (BrokerageNote) | T004, T005, T006 |
| T008 (BrokerageNoteValidator) | T007 |
| T009 (testes US1) | T008 (para compilar; estrutura pode ser escrita antes) |
| T010 (NoteFeeAllocation) | T007, T008 |
| T011 (testes US2) | T010 |
| T012 (Etapa 3 em calculateFeeAllocation) | T010 |
| T013 (testes US3) | T012 |
| T014 (testes US4) | T010, T012 |
| T015 (DOMAIN.md) | Nenhuma (pode ser escrita em qualquer momento) |

### Oportunidades de Paralelismo

- **Phase 1**: T001, T002, T003, T005 podem ser escritas simultaneamente; T004 e T006 aguardam T002/T003 e T001 respectivamente
- **Phase 4 + Phase 6**: T012 e T015 podem ser iniciadas em paralelo (arquivos distintos)
- **Phase 5**: T014 pode ser iniciado em paralelo com T015

---

## Parallel Example: User Story 1 (Phase 1)

```text
# Lançar em paralelo (arquivos independentes):
T001: TradeType.kt
T002: ApportionableFees.kt
T003: WithheldTaxes.kt
T005: BrokerageNoteMetadata.kt

# Após T002+T003:
T004: FinancialSummary.kt

# Após T001:
T006: NoteAsset.kt

# Após T004+T005+T006:
T007: BrokerageNote.kt  ← gate antes de US1
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 + 3)

1. Completar Phase 1: Foundational (T001–T007) — **CRÍTICO**
2. Completar Phase 2: US1 (T008–T009) — validator + testes de entrada inválida
3. Completar Phase 3: US2 (T010–T011) — rateio + testes de valores
4. Completar Phase 4: US3 (T012–T013) — fechamento contábil
5. **(Opcional — princípio IX)** `./gradlew :domain:entity:jvmTest` — apenas quando o utilizador pedir
6. **STOP e VALIDAR**: pipeline completo de 3 etapas funcional

### Entrega Incremental

1. T001–T007 → entidades de input prontas
2. T008–T009 → US1: validação pré-cálculo testada → inspecionável
3. T010–T011 → US2: rateio com mapa de saída testado → **MVP**
4. T012–T013 → US3: fechamento contábil garantido → auditável
5. T014 → US4: cobertura de arredondamento → confiança extra
6. T015 → documentação → feature completa

### Estratégia de Um Agente

Sequência recomendada: T001+T002+T003+T005 (paralelo) → T004+T006 (paralelo após dependências) → T007 → T008 → T009 → T010 → T011 → T012 → T013 → T014 → T015

---

## Notes

- **Escopo mínimo** (princípio X): cada tarefa implementa apenas o pedido na spec/plano — sem abstrações prematuras, sem refactor não solicitado, sem persistência ou UI.
- **Sem `BigDecimal`** (research.md §1): toda aritmética monetária usa `Long` em centavos no `commonMain`.
- **Exceções, não `Result`** (research.md §2): consistente com `IncomeTax.calculate` existente no módulo.
- **`grossValue` como campo fornecido** (research.md §7): `NoteAsset.grossValue` é informado pela fonte e validado em T008 (FR-007); NÃO é calculado como `quantity * unitPrice`.
- **Residual no último ativo** (research.md §4): `feeCents[N-1] = somaFeesCents − Σ feeCents[0..N-2]` — NÃO no ativo de maior volume.
- **ROUND_HALF_UP em inteiros**: `(grossValueCents * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents` — ver `data-model.md` passo 2.4.
- **Valores esperados do cenário canônico**: AJFI11=1.51, BRCO11=1.51, VILG11=1.52 (último absorve resíduo) — ver `quickstart.md`.
- **API de saída**: `typealias NoteFeeAllocation = Map<NoteAsset, Double>` — chave é instância `NoteAsset`; igualdade estrutural por todos os 6 campos (FR-017).
- [P] = arquivos diferentes, sem dependências incompletas — podem ser abertos em paralelo pelo agente
- Checkpoint após Phase 4 (US1+US2+US3) é o momento ideal para validar `./gradlew :domain:entity:jvmTest` se o utilizador quiser
