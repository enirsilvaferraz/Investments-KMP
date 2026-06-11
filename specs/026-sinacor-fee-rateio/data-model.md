# Data Model: Rateio de Taxas de Nota de Corretagem SINACOR

Feature: `026-sinacor-fee-rateio` | Módulo: `:domain:entity` | Pacote base: `com.eferraz.entities.brokeragenotes`

---

## Diagrama de Entidades

```
[BrokerageNote]
  ├── date: LocalDate
  ├── netValue: Double
  ├── fees: BrokerageNoteFees
  │     ├── emoluments: Double
  │     ├── settlement: Double
  │     ├── incomeTax: Double
  │     └── total: Double  (derived)
  └── assets: List<NoteAsset>
        ├── ticker: String
        ├── tradeType: TradeType
        ├── quantity: Double
        ├── unitPrice: Double
        └── grossValue: Double  (derived)

[NoteFeeAllocation]  ←  NoteFeeAllocation.calculate(BrokerageNote)
  └── allocations: List<AssetFeeAllocation>
        ├── ticker: String
        ├── grossValue: Double
        ├── allocatedFee: Double
        └── netValue: Double
```

---

## Entidades de Input

### TradeType
**Arquivo**: `TradeType.kt`

```
enum TradeType { BUY, SELL }
```

| Valor | Efeito na taxa | FR |
|-------|---------------|-----|
| BUY | `netValue = grossValue + allocatedFee` | FR-007 |
| SELL | `netValue = grossValue − allocatedFee` | FR-008 |

---

### BrokerageNoteFees
**Arquivo**: `BrokerageNoteFees.kt`

| Campo | Tipo | Obrigatório | Restrição |
|-------|------|-------------|-----------|
| emoluments | Double | ✅ | ≥ 0 |
| settlement | Double | ✅ | ≥ 0 |
| incomeTax | Double | ✅ | ≥ 0 |

**Campo derivado**: `total: Double = emoluments + settlement + incomeTax` (FR-003)

**Padrão**: `data class` simples (sem construtor privado — todos os campos são válidos por definição).

---

### NoteAsset
**Arquivo**: `NoteAsset.kt`

| Campo | Tipo | Obrigatório | Restrição |
|-------|------|-------------|-----------|
| ticker | String | ✅ | identificador opaco; sem validação de formato |
| tradeType | TradeType | ✅ | BUY ou SELL |
| quantity | Double | ✅ | > 0 (validado em `calculate`, não no construtor) |
| unitPrice | Double | ✅ | > 0 (validado em `calculate`, não no construtor) |

**Campo derivado**: `grossValue: Double = quantity * unitPrice` (FR-001)

**Nota**: A validação de `quantity > 0` e `unitPrice > 0` (FR-012) ocorre em `NoteFeeAllocation.calculate`, não no construtor, pois o construtor segue o padrão `data class` simples desta camada.

---

### BrokerageNote
**Arquivo**: `BrokerageNote.kt`

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| date | LocalDate | ✅ | Data de liquidação da nota |
| netValue | Double | ✅ | Valor líquido com sinal contábil SINACOR: positivo = débito do cliente, negativo = crédito ao cliente |
| fees | BrokerageNoteFees | ✅ | Taxas cobradas na nota |
| assets | List\<NoteAsset\> | ✅ | Ativos negociados; validação de não-vazio em `calculate` (FR-010) |

**Padrão**: `data class` simples.

---

## Entidades de Output

### AssetFeeAllocation
**Arquivo**: `AssetFeeAllocation.kt`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| ticker | String | Identificador do ativo (copiado de `NoteAsset.ticker`) |
| grossValue | Double | `quantity × unitPrice` (FR-001) |
| allocatedFee | Double | Fração das taxas atribuída ao ativo, ajustada de centavos (FR-004 a FR-006) |
| netValue | Double | `grossValue ± allocatedFee` conforme tradeType (FR-007, FR-008) |

**Padrão**: `data class` com construtor `internal` — criado exclusivamente por `NoteFeeAllocation.calculate`.

---

### NoteFeeAllocation
**Arquivo**: `NoteFeeAllocation.kt`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| allocations | List\<AssetFeeAllocation\> | Um item por `NoteAsset`, na mesma ordem da nota de entrada |

**Padrão**: `data class` com construtor `internal` + `companion object` com função `calculate`.

**Invariante**: Quando criado por `calculate`, `allocations.sumOf { it.allocatedFee } == note.fees.total` (SC-001) e a equação de fechamento é satisfeita (SC-002).

---

## Algoritmo de Cálculo

`NoteFeeAllocation.calculate(note: BrokerageNote): NoteFeeAllocation`

```
Pre-conditions (throw IllegalArgumentException):
  1. note.assets.isNotEmpty()                                         — FR-010
  2. ∀ a ∈ note.assets: a.quantity > 0 && a.unitPrice > 0            — FR-012

Calculation in cents (Long):
  3. grossValueCents[i] = round(a[i].grossValue × 100)               — FR-001
  4. totalVolumeCents = Σ grossValueCents[i]                          — FR-002
  5. Verify totalVolumeCents > 0                                      — FR-011
  6. totalFeesCents = round(note.fees.total × 100)                    — FR-003
  7. feeCents[i] = floor(grossValueCents[i] × totalFeesCents / totalVolumeCents)
                                                                      — FR-004, FR-005
  8. remainder = totalFeesCents - Σ feeCents[i]                       — FR-006
  9. feeCents[idxMaxVolume] += remainder                              — FR-006
     (idxMaxVolume = index of first asset with highest grossValueCents)

Output per asset:
  10. grossValue[i] = grossValueCents[i] / 100.0
  11. allocatedFee[i] = feeCents[i] / 100.0
  12. netValue[i] = grossValue[i] + allocatedFee[i]  if BUY           — FR-007
                  = grossValue[i] - allocatedFee[i]  if SELL          — FR-008

Accounting closure validation (throw IllegalStateException if fails):
  13. buysTotalCents  = Σ round(netValue[i] × 100) for BUY
      sellsTotalCents = Σ round(netValue[i] × 100) for SELL
      noteNetValueCents = round(note.netValue × 100)
      differenceCents = buysTotalCents - sellsTotalCents - noteNetValueCents
      Verify differenceCents == 0L                                    — FR-009
```

---

## Regras de Erro

| Condição | Tipo de Exceção | Mensagem |
|----------|----------------|---------|
| `note.assets.isEmpty()` | `IllegalArgumentException` | `"assets must not be empty"` |
| `asset.quantity <= 0 \|\| asset.unitPrice <= 0` | `IllegalArgumentException` | `"asset {ticker}: quantity and unitPrice must be > 0"` |
| `totalVolume == 0` | `IllegalArgumentException` | `"total volume must be > 0"` |
| Equação de fechamento falha | `IllegalStateException` | `"accounting closure failed: expected {note.netValue}, got {computed}"` |

---

## Sem Relações com Entidades Existentes

As entidades do pacote `brokeragenotes` são **independentes** do modelo existente (`AssetTransaction`, `AssetHolding`, etc.). Não há persistência, não há `@Relation`, não há dependência de Room. Isso é intencional — o cálculo de rateio opera sobre a nota antes de qualquer registro de transação.
