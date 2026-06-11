# Contrato da API Kotlin — Rateio de Taxas SINACOR

Feature: `026-sinacor-fee-rateio` | Módulo: `:domain:entity` | Pacote: `com.eferraz.entities.brokeragenotes`

---

## Tipos de Entrada (Input)

```kotlin
// Trade direction on the brokerage note
public enum class TradeType {
    BUY,
    SELL,
}

// Apportionable fees (sujeitas a rateio proporcional)
public data class ApportionableFees(
    public val settlement: Double,    // taxa de liquidação
    public val emoluments: Double,    // emolumentos
    public val transfer: Double,      // taxa de transferência
    public val brokerage: Double,     // corretagem
    public val iss: Double,           // ISS
    public val others: Double,        // outras taxas rateáveis
) {
    public val total: Double get() = settlement + emoluments + transfer + brokerage + iss + others
}

// Withheld taxes (retenções fiscais; NÃO entram no rateio)
public data class WithheldTaxes(
    public val irrfOperations: Double,   // IRRF operações normais
    public val irrfDayTrade: Double,     // IRRF day trade
)

// Financial summary declared by the brokerage
public data class FinancialSummary(
    public val totalVolumeTraded: Double,
    public val totalBuys: Double,
    public val totalSells: Double,
    public val apportionableFees: ApportionableFees,
    public val withheldTaxes: WithheldTaxes,
)

// Note metadata (header)
public data class BrokerageNoteMetadata(
    public val noteNumber: String,
    public val tradingDate: LocalDate,
    public val settlementDate: LocalDate,
    public val brokerage: String,
    public val brokerageDocument: String,    // CNPJ
    public val netValue: Double,             // accounting sign: + = client debit, - = client credit
)

// Asset traded on the note (key in NoteFeeAllocation map)
public data class NoteAsset(
    public val ticker: String,
    public val specification: String,        // participates in equals/hashCode
    public val tradeType: TradeType,
    public val quantity: Double,
    public val unitPrice: Double,
    public val grossValue: Double,           // valor_bruto_total from source; validated in Etapa 1
)

// SINACOR brokerage note
public data class BrokerageNote(
    public val metadata: BrokerageNoteMetadata,
    public val financialSummary: FinancialSummary,
    public val assets: List<NoteAsset>,
)
```

---

## Tipo de Saída (Output)

```kotlin
// Maps each NoteAsset to its final net value (grossValue ± allocatedFee)
public typealias NoteFeeAllocation = Map<NoteAsset, Double>
```

---

## Ponto de Entrada do Cálculo

```kotlin
// Extension function on BrokerageNote — throws on validation failure or closure mismatch
public fun BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation
```

---

## Componentes Internos (visíveis em jvmTest do mesmo módulo)

```kotlin
// Etapa 1 validator — called by calculateFeeAllocation before any computation
// Internal visibility: accessible from jvmTest (same Gradle module) for direct unit testing
internal object BrokerageNoteValidator {
    // Throws IllegalArgumentException at the first failed rule
    internal fun validate(note: BrokerageNote): Unit
}
```

`BrokerageNoteValidator.validate()` é chamado internamente por `calculateFeeAllocation()`. Por ser `internal`, os testes em `jvmTest` do módulo `:domain:entity` podem chamá-lo diretamente sem depender de `calculateFeeAllocation()` — isto permite testar os cenários de Etapa 1 antes de T010 (US2).

---

## Comportamento de `calculateFeeAllocation`

### Entradas válidas → sucesso

| Condição de entrada | Comportamento |
|--------------------|---------------|
| Nota com N ≥ 1 ativos, todos com `quantity > 0`, `unitPrice > 0` e `grossValue ≥ 0` | Retorna `NoteFeeAllocation` com N entradas (uma por ativo) |
| Ativo com `tradeType = BUY` | `netValue = grossValue + allocatedFee` |
| Ativo com `tradeType = SELL` | `netValue = grossValue − allocatedFee` |
| Nota com todas as taxas rateáveis = 0 | `allocatedFee = 0.0` para todos; `netValue = grossValue` |
| Nota com apenas 1 ativo | Ativo absorve 100% de `Soma_Taxas` (é o último e único) |
| Último ativo do array | `allocatedFee = Soma_Taxas − Σ(taxas dos N-1 ativos anteriores)` |

### Etapa 1 — Entradas inválidas → `IllegalArgumentException`

| Condição | Mensagem |
|----------|---------|
| `note.assets.isEmpty()` | `"assets must not be empty"` |
| `apportionableFees.*` ou volumes negativos | `"fee/volume fields must not be negative: {field}"` |
| `asset.quantity <= 0` ou `asset.unitPrice <= 0` | `"asset {ticker}: quantity and unitPrice must be > 0"` |
| `totalVolumeTraded <= 0` | `"total volume must be > 0"` |
| `Σ grossValue ≠ totalVolumeTraded` (em centavos) | `"volume mismatch: assets sum {sum} ≠ declared {total}"` |
| `round(quantity×unitPrice×100) ≠ round(grossValue×100)` | `"asset {ticker}: quantity×unitPrice {computed} ≠ grossValue {declared}"` |
| Subtotais BUY/SELL não batem com declarados | `"buys/sells totals mismatch: expected {expected}, got {computed}"` |

### Etapa 3 — Falha de fechamento → `IllegalStateException`

| Condição | Mensagem |
|----------|---------|
| `Σ(allocatedFee) ≠ Soma_Taxas` (em centavos) | `"fee distribution mismatch: allocated {sum} ≠ somaFees {total}"` |
| `Σ(BUY.netValue) − Σ(SELL.netValue) ≠ metadata.netValue` (em centavos) | `"accounting closure failed: expected {metadata.netValue}, got {computed}"` |

---

## Garantias do Contrato

| ID | Garantia |
|----|---------|
| SC-001 | `Σ(allocatedFee) == financialSummary.apportionableFees.total` (sem diferença de centavos) |
| SC-002 | `Σ(BUY.netValue) − Σ(SELL.netValue) == metadata.netValue` (em centavos) |
| SC-003 | Entradas inválidas produzem `IllegalArgumentException` descritivas antes de qualquer cálculo |
| SC-004 | Determinístico: mesma nota com mesma ordem de ativos → mesmo mapa de resultado |
| SC-005 | O último ativo da lista absorve o resíduo de centavos do arredondamento ROUND_HALF_UP |

---

## Exemplo de Uso

```kotlin
val note = BrokerageNote(
    metadata = BrokerageNoteMetadata(
        noteNumber = "12345",
        tradingDate = LocalDate(2026, 6, 9),
        settlementDate = LocalDate(2026, 6, 11),
        brokerage = "Nu Investimentos S.A.",
        brokerageDocument = "62.169.875/0001-79",
        netValue = 1004.54,                      // + = client debit
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
        NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY,  100.0,  10.00, 1000.00),
        NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL,  10.0, 100.00, 1000.00),
        NoteAsset("VILG11", "VILG11 CI", TradeType.BUY, 1000.0,   1.00, 1000.00),
    ),
)

val result: NoteFeeAllocation = note.calculateFeeAllocation()
// Soma_Taxas = 4.54; totalVolume = 3000.00
// AJFI11 (i=0, não último): ROUND_HALF_UP(1000/3000 * 4.54) = ROUND_HALF_UP(1.5133) = 1.51
// BRCO11 (i=1, não último): ROUND_HALF_UP(1000/3000 * 4.54) = 1.51
// VILG11 (i=2, último):     4.54 - 1.51 - 1.51 = 1.52  ← absorve resíduo

// result[NoteAsset("AJFI11","AJFI11 CI",BUY,100.0,10.00,1000.00)]  → netValue = 1001.51 (BUY)
// result[NoteAsset("BRCO11","BRCO11 CI",SELL,10.0,100.00,1000.00)] → netValue =  998.49 (SELL)
// result[NoteAsset("VILG11","VILG11 CI",BUY,1000.0,1.00,1000.00)]  → netValue = 1001.52 (BUY, último)
// Σ allocatedFee = 1.51 + 1.51 + 1.52 = 4.54  ✓  (SC-001)
// Closure: (1001.51 + 1001.52) - 998.49 = 1004.54 = metadata.netValue  ✓  (SC-002)
```

---

## Notas de Compatibilidade

- Esta API é interna ao projeto (`:domain:entity`). Não é publicada em artefato externo (Maven/GitHub Packages).
- A dependência de `kotlinx.datetime.LocalDate` já é transitiva no módulo `:domain:entity`.
- `WithheldTaxes` está disponível nos dados da nota mas não influencia nenhum cálculo de rateio — é informativo.
