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

// Fees charged on the note
public data class BrokerageNoteFees(
    public val emoluments: Double,
    public val settlement: Double,
    public val incomeTax: Double,
) {
    public val total: Double get() = emoluments + settlement + incomeTax
}

// Asset traded on the note
public data class NoteAsset(
    public val ticker: String,
    public val tradeType: TradeType,
    public val quantity: Double,
    public val unitPrice: Double,
) {
    public val grossValue: Double get() = quantity * unitPrice
}

// SINACOR brokerage note
public data class BrokerageNote(
    public val date: LocalDate,
    public val netValue: Double,    // accounting sign: + = client debit, - = client credit
    public val fees: BrokerageNoteFees,
    public val assets: List<NoteAsset>,
)
```

---

## Tipos de Saída (Output)

```kotlin
// Fee allocation result for a single asset
public data class AssetFeeAllocation internal constructor(
    public val ticker: String,
    public val grossValue: Double,
    public val allocatedFee: Double,
    public val netValue: Double,
)

// Consolidated fee allocation result for the entire note
public data class NoteFeeAllocation internal constructor(
    public val allocations: List<AssetFeeAllocation>,
) {
    public companion object {
        // Calculation entry point — throws on invalid input or closure failure
        public fun calculate(note: BrokerageNote): NoteFeeAllocation
    }
}
```

---

## Comportamento de `NoteFeeAllocation.calculate`

### Entradas válidas → sucesso

| Condição de entrada | Comportamento |
|--------------------|---------------|
| Nota com N ≥ 1 ativos, todos com `quantity > 0` e `unitPrice > 0` | Retorna `NoteFeeAllocation` com N `AssetFeeAllocation` na mesma ordem dos ativos de entrada |
| Ativo com `tradeType = BUY` | `netValue = grossValue + allocatedFee` |
| Ativo com `tradeType = SELL` | `netValue = grossValue − allocatedFee` |
| Nota com todas as taxas = 0 | `allocatedFee = 0.0` para todos; `netValue = grossValue` |
| Nota com apenas 1 ativo | Ativo absorve 100% das taxas |

### Entradas inválidas → `IllegalArgumentException`

| Condição | Mensagem |
|----------|---------|
| `note.assets.isEmpty()` | `"assets must not be empty"` |
| `asset.quantity <= 0` ou `asset.unitPrice <= 0` | `"asset {ticker}: quantity and unitPrice must be > 0"` |
| `totalVolume calculado == 0` | `"total volume must be > 0"` |

### Falha de fechamento contábil → `IllegalStateException`

| Condição | Mensagem |
|----------|---------|
| `Σ(buys.netValue) − Σ(sells.netValue) ≠ note.netValue` (em centavos) | `"accounting closure failed: expected {note.netValue}, got {computed}"` |

---

## Garantias do Contrato

| ID | Garantia |
|----|---------|
| SC-001 | `allocations.sumOf { it.allocatedFee } == note.fees.total` (sem diferença de centavos) |
| SC-002 | `Σ(BUY.netValue) − Σ(SELL.netValue) == note.netValue` |
| SC-003 | Entradas inválidas produzem exceções descritivas; nenhuma exceção não tratada é propagada |
| SC-004 | Determinístico: mesma nota → mesmo resultado, independentemente de quando ou quantas vezes for chamado |

---

## Exemplo de Uso

```kotlin
val note = BrokerageNote(
    date = LocalDate(2026, 1, 1),
    netValue = 1004.54,
    fees = BrokerageNoteFees(emoluments = 1.00, settlement = 3.54, incomeTax = 0.00),
    assets = listOf(
        NoteAsset("AJFI11", TradeType.BUY,  100.0,  10.00),
        NoteAsset("BRCO11", TradeType.SELL,  10.0, 100.00),
        NoteAsset("VILG11", TradeType.BUY, 1000.0,   1.00),
    )
)

val result = NoteFeeAllocation.calculate(note)
// result.allocations[0] → AJFI11: grossValue=1000.00, allocatedFee=1.52, netValue=1001.52
// result.allocations[1] → BRCO11: grossValue=1000.00, allocatedFee=1.51, netValue= 998.49
// result.allocations[2] → VILG11: grossValue=1000.00, allocatedFee=1.51, netValue=1001.51
// Σ allocatedFee = 4.54 ✓  |  Closure: 1001.52 + 1001.51 − 998.49 = 1004.54 ✓
```

---

## Notas de Compatibilidade

- Esta API é interna ao projeto (`:domain:entity`). Não é publicada em artefato externo (Maven/GitHub Packages).
- Os construtores de `AssetFeeAllocation` e `NoteFeeAllocation` são `internal` — apenas o mesmo módulo Gradle pode instanciá-los diretamente.
- A dependência de `kotlinx.datetime.LocalDate` já é transitiva no módulo `:domain:entity`.
