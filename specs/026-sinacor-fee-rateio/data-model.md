# Data Model: Rateio de Taxas de Nota de Corretagem SINACOR

Feature: `026-sinacor-fee-rateio` | Módulo: `:domain:entity` | Pacote base: `com.eferraz.entities.brokeragenotes`

---

## Diagrama de Entidades

```
[BrokerageNote]
  ├── metadata: BrokerageNoteMetadata
  │     ├── noteNumber: String
  │     ├── tradingDate: LocalDate
  │     ├── settlementDate: LocalDate
  │     ├── brokerage: String
  │     ├── brokerageDocument: String   (CNPJ)
  │     └── netValue: Double            (sinal contábil SINACOR)
  ├── financialSummary: FinancialSummary
  │     ├── totalVolumeTraded: Double
  │     ├── totalBuys: Double
  │     ├── totalSells: Double
  │     ├── apportionableFees: ApportionableFees
  │     │     ├── settlement: Double
  │     │     ├── emoluments: Double
  │     │     ├── transfer: Double
  │     │     ├── brokerage: Double
  │     │     ├── iss: Double
  │     │     ├── others: Double
  │     │     └── total: Double  (derived)
  │     └── withheldTaxes: WithheldTaxes
  │           ├── irrfOperations: Double
  │           └── irrfDayTrade: Double
  └── assets: List<NoteAsset>
        ├── ticker: String
        ├── specification: String      (participa da igualdade estrutural)
        ├── tradeType: TradeType
        ├── quantity: Double
        ├── unitPrice: Double
        └── grossValue: Double         (valor_bruto_total informado pela fonte; validado em Etapa 1)

NoteFeeAllocation = Map<NoteAsset, Double>   ←  BrokerageNote.calculateFeeAllocation()
  (chave = instância NoteAsset estruturalmente única; valor = netValue final por ativo)
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
| BUY | `netValue = grossValue + allocatedFee` | FR-014 |
| SELL | `netValue = grossValue − allocatedFee` | FR-015 |

---

### ApportionableFees
**Arquivo**: `ApportionableFees.kt`

| Campo | Tipo | Restrição | Descrição |
|-------|------|-----------|-----------|
| settlement | Double | ≥ 0 | Taxa de liquidação |
| emoluments | Double | ≥ 0 | Emolumentos |
| transfer | Double | ≥ 0 | Taxa de transferência |
| brokerage | Double | ≥ 0 | Corretagem |
| iss | Double | ≥ 0 | ISS |
| others | Double | ≥ 0 | Outras taxas rateáveis |

**Campo derivado**: `total: Double = settlement + emoluments + transfer + brokerage + iss + others`

**Nota**: Somente os campos de `ApportionableFees` entram em `Soma_Taxas` (FR-011). `WithheldTaxes` **não** participa do rateio.

---

### WithheldTaxes
**Arquivo**: `WithheldTaxes.kt`

| Campo | Tipo | Restrição | Descrição |
|-------|------|-----------|-----------|
| irrfOperations | Double | ≥ 0 | IRRF sobre operações normais |
| irrfDayTrade | Double | ≥ 0 | IRRF sobre day trade |

**Padrão**: `data class` simples. Não participa de nenhum cálculo de rateio — informativo.

---

### FinancialSummary
**Arquivo**: `FinancialSummary.kt`

| Campo | Tipo | Restrição | Descrição |
|-------|------|-----------|-----------|
| totalVolumeTraded | Double | > 0 (validado em Etapa 1) | Volume total operado (COMPRA + VENDA) |
| totalBuys | Double | ≥ 0 | Total compras à vista |
| totalSells | Double | ≥ 0 | Total vendas à vista |
| apportionableFees | ApportionableFees | — | Taxas sujeitas a rateio |
| withheldTaxes | WithheldTaxes | — | Retenções fiscais (fora do rateio) |

---

### BrokerageNoteMetadata
**Arquivo**: `BrokerageNoteMetadata.kt`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| noteNumber | String | Número da nota |
| tradingDate | LocalDate | Data do pregão |
| settlementDate | LocalDate | Data de liquidação |
| brokerage | String | Nome da corretora |
| brokerageDocument | String | CNPJ da corretora |
| netValue | Double | Valor líquido com sinal contábil SINACOR: positivo = débito do cliente, negativo = crédito ao cliente |

---

### NoteAsset
**Arquivo**: `NoteAsset.kt`

| Campo | Tipo | Restrição | Descrição |
|-------|------|-----------|-----------|
| ticker | String | — | Identificador opaco do ativo; sem validação de formato |
| specification | String | — | Especificação textual da linha (ex.: "BRB111F UNT N2"); participa de `equals`/`hashCode` |
| tradeType | TradeType | — | BUY ou SELL |
| quantity | Double | > 0 (validado em Etapa 1) | Quantidade negociada |
| unitPrice | Double | > 0 (validado em Etapa 1) | Preço unitário informado |
| grossValue | Double | ≥ 0 (validado em Etapa 1) | `valor_bruto_total` informado pela fonte; validado contra `quantity × unitPrice` em FR-007 |

**Padrão**: `data class` — todos os campos participam de `equals`/`hashCode` por padrão.

**Nota**: `specification` diferencia linhas do mesmo ticker com preços distintos na nota canônica (ex.: "BRB111F UNT N2" vs. "BRB111 UNT N2" para BRBI11). `grossValue` é campo de entrada, não derivado — validado em FR-007 mas usado nos cálculos de rateio como valor de referência.

---

### BrokerageNote
**Arquivo**: `BrokerageNote.kt`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| metadata | BrokerageNoteMetadata | Cabeçalho da nota |
| financialSummary | FinancialSummary | Totais declarados pela corretora |
| assets | List\<NoteAsset\> | Ativos negociados; validação de não-vazio em Etapa 1 (FR-021) |

**Padrão**: `data class` simples.

---

## Entidade de Output

### NoteFeeAllocation
**Arquivo**: `NoteFeeAllocation.kt`

```
typealias NoteFeeAllocation = Map<NoteAsset, Double>
```

| Aspecto | Descrição |
|---------|-----------|
| Chave | `NoteAsset` (igualdade estrutural por todos os campos) |
| Valor | Valor líquido final (`netValue`) já com taxa proporcional aplicada |
| Unicidade | Garantida pela igualdade estrutural de `NoteAsset` — tickers repetidos com specs/preços distintos produzem chaves distintas |
| Tamanho | Sempre `== note.assets.size` quando retornado com sucesso |

**Invariantes** (garantidos por `calculateFeeAllocation` antes de retornar):
- `Σ(taxas alocadas) == Soma_Taxas` (SC-001)
- `Σ(BUY netValue) − Σ(SELL netValue) == note.metadata.netValue` (SC-002)

A função de cálculo reside em `NoteFeeAllocation.kt` como extensão de `BrokerageNote`:
```
fun BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation
```

---

## Validador

### BrokerageNoteValidator
**Arquivo**: `BrokerageNoteValidator.kt`

Objeto responsável exclusivamente pela Etapa 1 (validação pré-cálculo). Lança `IllegalArgumentException` com mensagem descritiva ao primeiro erro encontrado.

---

## Algoritmo de Cálculo

`BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation`

```
═══════════════════════════════════════════════════════════
ETAPA 1 — Validação pré-cálculo (BrokerageNoteValidator)
Todas lançam IllegalArgumentException se falharem (FR-010)
═══════════════════════════════════════════════════════════

1.1  note.assets.isNotEmpty()
     → erro: "assets must not be empty"                           — FR-021

1.2  ∀ field in apportionableFees: field >= 0
     totalVolumeTraded >= 0, totalBuys >= 0, totalSells >= 0
     → erro: "fee/volume fields must not be negative"             — FR-009

1.3  ∀ a: a.quantity > 0 && a.unitPrice > 0 && a.grossValue >= 0
     → erro: "asset {ticker}: quantity and unitPrice must be > 0" — FR-009

1.4  totalVolumeTraded > 0
     → erro: "total volume must be > 0"                           — FR-022

1.5  round(Σ a.grossValue × 100) == round(totalVolumeTraded × 100)
     → erro: "volume mismatch: sum of assets ≠ totalVolumeTraded" — FR-006

1.6  ∀ a: round(a.quantity × a.unitPrice × 100) == round(a.grossValue × 100)
     → erro: "asset {ticker}: quantity×unitPrice ≠ grossValue"    — FR-007

1.7  round(Σ BUY a.grossValue × 100) == round(totalBuys × 100)
     round(Σ SELL a.grossValue × 100) == round(totalSells × 100)
     → erro: "buy/sell totals mismatch"                           — FR-008

═══════════════════════════════════════════════════════════
ETAPA 2 — Algoritmo de rateio (em centavos Long)
═══════════════════════════════════════════════════════════

2.1  somaFeesCents       = round(apportionableFees.total × 100)   — FR-011
2.2  totalVolumeCents    = round(totalVolumeTraded × 100)         — FR-016
2.3  grossValueCents[i]  = round(a[i].grossValue × 100)          — FR-016

Para cada ativo i de 0 até N-2 (todos exceto o último):
2.4  feeCents[i] = ROUND_HALF_UP(grossValueCents[i] × somaFeesCents / totalVolumeCents)
                                                                   — FR-012

     Implementação ROUND_HALF_UP em inteiros:
     feeCents[i] = (grossValueCents[i] * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents

Para o último ativo (índice N-1):
2.5  feeCents[N-1] = somaFeesCents − Σ feeCents[0..N-2]          — FR-013
     (absorve qualquer resíduo de centavos)

Para cada ativo i:
2.6  allocatedFee[i] = feeCents[i] / 100.0
2.7  netValue[i]  = a[i].grossValue + allocatedFee[i]  se BUY    — FR-014
                  = a[i].grossValue − allocatedFee[i]  se SELL   — FR-015

═══════════════════════════════════════════════════════════
ETAPA 3 — Validação pós-cálculo
Lançam IllegalStateException se falharem (FR-020)
═══════════════════════════════════════════════════════════

3.1  round(Σ allocatedFee × 100) == somaFeesCents
     → erro: "fee distribution mismatch"                          — FR-018

3.2  buysTotalCents − sellsTotalCents == noteNetValueCents
     onde buysTotalCents  = Σ round(netValue[i] × 100) for BUY
          sellsTotalCents = Σ round(netValue[i] × 100) for SELL
          noteNetValueCents = round(metadata.netValue × 100)
     → erro: "accounting closure failed: expected {metadata.netValue}, got {computed}" — FR-019

═══════════════════════════════════════════════════════════
RETORNO
═══════════════════════════════════════════════════════════

4.1  Map<NoteAsset, Double> { a[i] → netValue[i] }               — FR-017
     (um entry por ativo, na mesma ordem da lista de entrada)
```

---

## Regras de Erro

| Etapa | Condição | Tipo de Exceção | Mensagem |
|-------|----------|----------------|---------|
| 1 | `note.assets.isEmpty()` | `IllegalArgumentException` | `"assets must not be empty"` |
| 1 | `apportionableFees.*` ou volumes negativos | `IllegalArgumentException` | `"fee/volume fields must not be negative: {field}"` |
| 1 | `asset.quantity <= 0 \|\| asset.unitPrice <= 0` | `IllegalArgumentException` | `"asset {ticker}: quantity and unitPrice must be > 0"` |
| 1 | `totalVolumeTraded <= 0` | `IllegalArgumentException` | `"total volume must be > 0"` |
| 1 | Soma grossValue ≠ totalVolumeTraded | `IllegalArgumentException` | `"volume mismatch: assets sum {sum} ≠ declared {total}"` |
| 1 | `quantity × unitPrice ≠ grossValue` | `IllegalArgumentException` | `"asset {ticker}: quantity×unitPrice {computed} ≠ grossValue {declared}"` |
| 1 | Subtotais COMPRA/VENDA inválidos | `IllegalArgumentException` | `"buys/sells totals mismatch: expected {expected}, got {computed}"` |
| 3 | Soma taxas alocadas ≠ Soma_Taxas | `IllegalStateException` | `"fee distribution mismatch: allocated {sum} ≠ somaFees {total}"` |
| 3 | Equação de fechamento falha | `IllegalStateException` | `"accounting closure failed: expected {metadata.netValue}, got {computed}"` |

---

## Sem Relações com Entidades Existentes

As entidades do pacote `brokeragenotes` são **independentes** do modelo existente (`AssetTransaction`, `AssetHolding`, etc.). Não há persistência, não há `@Relation`, não há dependência de Room. Isso é intencional — o cálculo de rateio opera sobre a nota antes de qualquer registro de transação.
