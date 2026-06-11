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

## Cenário Canônico (User Story 1 + 2 + 3)

Nota com 3 ativos de volumes iguais → verifica rateio, ajuste de centavos e fechamento.

**Entrada**:
```
date = 2026-01-01
netValue = R$ 1.004,54
fees = emoluments=1,00 + settlement=3,54 + incomeTax=0,00 → total=4,54
assets:
  AJFI11  BUY   100 × R$ 10,00 → grossValue = R$ 1.000,00
  BRCO11  SELL   10 × R$100,00 → grossValue = R$ 1.000,00
  VILG11  BUY  1000 × R$  1,00 → grossValue = R$ 1.000,00
totalVolume = R$ 3.000,00
```

**Saída esperada**:
```
AJFI11: grossValue=1000.00  allocatedFee=1.52  netValue=1001.52  (BUY)
BRCO11: grossValue=1000.00  allocatedFee=1.51  netValue= 998.49  (SELL)
VILG11: grossValue=1000.00  allocatedFee=1.51  netValue=1001.51  (BUY)
```

**Verificações**:
- Σ allocatedFee = 1,52 + 1,51 + 1,51 = **4,54** ✓ (SC-001)
- Σ BUY netValue = 1001,52 + 1001,51 = 2003,03
- Σ SELL netValue = 998,49
- Diferença = 2003,03 − 998,49 = **1004,54** = `note.netValue` ✓ (SC-002)
- AJFI11 recebeu o centavo extra por ser o primeiro com maior volume (empate → primeiro) ✓

**Referência**: [contracts/kotlin-api.md](contracts/kotlin-api.md#exemplo-de-uso)

---

## Cenários de Erro

### Lista de ativos vazia (FR-010)
```kotlin
NoteFeeAllocation.calculate(note.copy(assets = emptyList()))
// → IllegalArgumentException: "assets must not be empty"
```

### Ativo com quantity inválida (FR-012)
```kotlin
val invalidAsset = NoteAsset("XPTO3", TradeType.BUY, -5.0, 10.0)
NoteFeeAllocation.calculate(note.copy(assets = listOf(invalidAsset)))
// → IllegalArgumentException: "asset XPTO3: quantity and unitPrice must be > 0"
```

### Equação de fechamento falha (FR-009)
```kotlin
// note com netValue declarado diferente do calculado
NoteFeeAllocation.calculate(note.copy(netValue = 9999.00))
// → IllegalStateException: "accounting closure failed: expected 9999.0, got 1004.54"
```

---

## Edge Cases a Verificar

| Cenário | Resultado esperado |
|---------|-------------------|
| Nota com 1 ativo BUY | Ativo absorve 100% das taxas; `netValue = grossValue + fees.total` |
| Nota com 1 ativo SELL | Ativo absorve 100% das taxas; `netValue = grossValue − fees.total` |
| Todos os ativos são SELL | `note.netValue` negativo; equação `0 − Σ(sells) = note.netValue` satisfeita |
| Todas as taxas = 0 | `allocatedFee = 0.0` para todos; `netValue = grossValue` |
| Dois ativos com mesmo volume | Centavo extra vai para o primeiro na lista |
| `quantity <= 0` ou `unitPrice <= 0` | `IllegalArgumentException` por FR-012 antes de chegar em FR-011 |

---

## Referências

- [Spec completa](spec.md)
- [Modelo de dados](data-model.md)
- [Contrato Kotlin](contracts/kotlin-api.md)
