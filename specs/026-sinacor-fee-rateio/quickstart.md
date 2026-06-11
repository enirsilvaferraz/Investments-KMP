# Quickstart: Validação — Rateio de Taxas SINACOR

Feature: `026-sinacor-fee-rateio` | Módulo: `:domain:entity`

Este guia descreve como validar que a implementação está correta, com cenários prontos para executar como testes ou verificar manualmente.

---

## Pré-requisitos

- JDK 17+
- Branch `026-sinacor-fee-rateio` ativa
- Módulo `:domain:entity` compilando

## Execução dos Testes

```bash
./gradlew :domain:entity:jvmTest
```

Os testes estão em:
```
core/domain/entity/src/jvmTest/kotlin/com/eferraz/entities/brokeragenotes/NoteFeeAllocationTest.kt
```

---

## Cenário Canônico (User Stories 1 + 2 + 3)

Nota com 3 ativos de volumes iguais → verifica rateio proporcional, ROUND_HALF_UP, ajuste de resíduo no último ativo e fechamento contábil.

**Nota de entrada**:
```kotlin
val note = BrokerageNote(
    metadata = BrokerageNoteMetadata(
        noteNumber = "12345",
        tradingDate = LocalDate(2026, 1, 1),
        settlementDate = LocalDate(2026, 1, 3),
        brokerage = "Corretora Teste",
        brokerageDocument = "00.000.000/0001-00",
        netValue = 1004.54,
    ),
    financialSummary = FinancialSummary(
        totalVolumeTraded = 3000.00,
        totalBuys = 2000.00,
        totalSells = 1000.00,
        apportionableFees = ApportionableFees(
            settlement = 3.54, emoluments = 1.00,
            transfer = 0.00, brokerage = 0.00, iss = 0.00, others = 0.00,
        ),
        withheldTaxes = WithheldTaxes(irrfOperations = 0.00, irrfDayTrade = 0.00),
    ),
    assets = listOf(
        NoteAsset("AJFI11", "AJFI11 CI",  TradeType.BUY,  100.0,  10.00, 1000.00),
        NoteAsset("BRCO11", "BRCO11 CI",  TradeType.SELL,  10.0, 100.00, 1000.00),
        NoteAsset("VILG11", "VILG11 CI",  TradeType.BUY, 1000.0,   1.00, 1000.00), // ← último
    ),
)
```

**Soma_Taxas** = 3.54 + 1.00 = **4.54** | **totalVolume** = 3000.00

**Saída esperada** (`note.calculateFeeAllocation()`):
```
AJFI11 (BUY,  i=0, não último): allocatedFee = ROUND_HALF_UP(1000/3000 × 4.54) = ROUND_HALF_UP(1.5133) = 1.51
BRCO11 (SELL, i=1, não último): allocatedFee = ROUND_HALF_UP(1000/3000 × 4.54) = 1.51
VILG11 (BUY,  i=2, último):     allocatedFee = 4.54 − 1.51 − 1.51 = 1.52  ← resíduo

NoteAsset("AJFI11","AJFI11 CI",BUY,100.0,10.00,1000.00)  → netValue = 1000.00 + 1.51 = 1001.51
NoteAsset("BRCO11","BRCO11 CI",SELL,10.0,100.00,1000.00) → netValue = 1000.00 − 1.51 =  998.49
NoteAsset("VILG11","VILG11 CI",BUY,1000.0,1.00,1000.00)  → netValue = 1000.00 + 1.52 = 1001.52
```

**Verificações**:
- Σ allocatedFee = 1,51 + 1,51 + 1,52 = **4,54** ✓ (SC-001)
- Σ BUY netValue = 1001,51 + 1001,52 = 2003,03
- Σ SELL netValue = 998,49
- Fechamento = 2003,03 − 998,49 = **1004,54** = `metadata.netValue` ✓ (SC-002)
- VILG11 (último) absorveu o resíduo de 1 centavo ✓ (FR-013)

**Referência**: [contracts/kotlin-api.md](contracts/kotlin-api.md#exemplo-de-uso)

---

## Nota Canônica (30 ativos, docs/nota.json) — SC-004

A nota de `docs/nota.json` deve ser processada com sucesso pelo pipeline completo.

> **Nota sobre contagem**: O array `ativos` contém **30** entradas. O item nº 8 (SBFG3, 1 unidade) usa o campo `"unitario"` em vez de `"valor_unitario"` — ao construir o `NoteAsset` correspondente, usar `unitPrice = 10.60` e `grossValue = 10.60` (validação FR-007 passará: `1 × 10.60 == 10.60`).

**Parâmetros-chave**:
```
volume_total_operado = 48912.22
Soma_Taxas = 10.95 + 2.44 + 1.27 + 0.00 + 0.00 + 0.00 = 14.66
valor_liquido_nota  = −33705.98
Último ativo (índice 29) = KNSC11 VENDA (absorve resíduo)
```

**Verificações esperadas após cálculo**:
- Σ(allocatedFee) == **14,66** ✓
- Σ(BUY netValue) − Σ(SELL netValue) == **−33705,98** ✓

---

## Cenários de Erro (Etapa 1 — IllegalArgumentException)

### Lista de ativos vazia (FR-021)
```kotlin
note.copy(assets = emptyList()).calculateFeeAllocation()
// → IllegalArgumentException: "assets must not be empty"
```

### Taxa de liquidação negativa (FR-009)
```kotlin
note.copy(
    financialSummary = note.financialSummary.copy(
        apportionableFees = note.financialSummary.apportionableFees.copy(settlement = -1.00)
    )
).calculateFeeAllocation()
// → IllegalArgumentException: "fee/volume fields must not be negative: settlement"
```

### Ativo com quantity inválida (FR-009)
```kotlin
val invalidAsset = NoteAsset("XPTO3", "XPTO3 ON", TradeType.BUY, -5.0, 10.0, -50.0)
note.copy(assets = listOf(invalidAsset)).calculateFeeAllocation()
// → IllegalArgumentException: "asset XPTO3: quantity and unitPrice must be > 0"
```

### Volume total divergente da soma dos ativos (FR-006)
```kotlin
note.copy(
    financialSummary = note.financialSummary.copy(totalVolumeTraded = 9999.00)
).calculateFeeAllocation()
// → IllegalArgumentException: "volume mismatch: assets sum 3000.00 ≠ declared 9999.00"
```

### grossValue inconsistente com quantity × unitPrice (FR-007)
```kotlin
val badAsset = NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 999.00)
// → IllegalArgumentException: "asset AJFI11: quantity×unitPrice 1000.00 ≠ grossValue 999.00"
```

### Subtotais BUY/SELL inválidos (FR-008)
```kotlin
note.copy(
    financialSummary = note.financialSummary.copy(totalBuys = 9999.00)
).calculateFeeAllocation()
// → IllegalArgumentException: "buys/sells totals mismatch: ..."
```

## Cenário de Erro (Etapa 3 — IllegalStateException)

### Equação de fechamento falha (FR-019)
```kotlin
note.copy(
    metadata = note.metadata.copy(netValue = 9999.00)
).calculateFeeAllocation()
// → IllegalStateException: "accounting closure failed: expected 9999.0, got 1004.54"
```

---

## Edge Cases a Verificar

| Cenário | Resultado esperado |
|---------|-------------------|
| Nota com 1 ativo BUY | Ativo absorve 100% das taxas (é o último e único); `netValue = grossValue + fees.total` |
| Nota com 1 ativo SELL | Ativo absorve 100% das taxas; `netValue = grossValue − fees.total` |
| Todos os ativos são SELL | `metadata.netValue` negativo; equação `0 − Σ(sells) = metadata.netValue` satisfeita |
| Todas as taxas = 0 | `allocatedFee = 0.0` para todos; `netValue = grossValue` |
| Ticker repetido com specs distintas | Chaves distintas no mapa de saída (ex.: BRBI11 com "BRB111F UNT N2" e "BRB111 UNT N2") |
| Ticker repetido, mesma spec, preço distinto | Chaves distintas pelo `grossValue` diferente |
| `quantity <= 0` ou `unitPrice <= 0` | `IllegalArgumentException` por Etapa 1 antes de calcular |

---

## Referências

- [Spec completa](spec.md)
- [Modelo de dados](data-model.md)
- [Contrato Kotlin](contracts/kotlin-api.md)
- [Nota canônica](../../docs/nota.json)
