# Data Model: Sincronização de Histórico via Importação B3

**Feature**: `006-b3-import-sync-history` | **Date**: 2026-05-27

## Entidades Novas

### `B3Record`

**Módulo**: `:core:domain:usecases`
**Pacote**: `com.eferraz.usecases.entities`
**Arquivo**: `B3Record.kt`

Objeto de domínio imutável que transporta o resultado de uma posição lida do arquivo XLSX exportado da B3.

```kotlin
public data class B3Record(
    val identifier: String,  // identificador da posição (ticker ou código B3)
    val value: Double,       // valor financeiro atualizado, já convertido de String
)
```

> Não há `B3SyncLogEvent` nem sealed interface de eventos — log emitido via `println` diretamente no `SyncB3HistoryUseCase`, consistente com o padrão existente no projeto.

---

## Interfaces Modificadas

### `B3ImportDataSource` — assinatura atualizada

**Módulo**: `:core:domain:usecases`
**Pacote**: `com.eferraz.usecases.repositories`

```kotlin
// ANTES
public interface B3ImportDataSource {
    public suspend fun importAndLog(): Result<Unit>
}

// DEPOIS
public interface B3ImportDataSource {
    public suspend fun import(): Result<List<B3Record>>
}
```

**Impacto**: `B3ImportDataSourceImpl` e `ImportB3FileUseCase` precisam ser atualizados.

---

### `B3Position` — métodos abstratos adicionados

**Módulo**: `:core:data:filestore`
**Pacote**: `com.eferraz.filestore.b3.dto`

```kotlin
// ANTES
internal sealed interface B3Position {
    fun isBlankRow(): Boolean
}

// DEPOIS
internal sealed interface B3Position {
    fun isBlankRow(): Boolean
    fun b3Identifier(): String?   // null para tipos sem identificador (fundos)
    fun b3Value(): Double         // lança se valor não parseável; capturado no DataSourceImpl
}
```

---

## Implementações Modificadas

### Mapeamento `b3Identifier()` e `b3Value()` por subtipo

| Subtipo | `b3Identifier()` | `b3Value()` | Nota |
|---------|------------------|-------------|------|
| `B3StockPosition` | `ticker` | `updatedValue.toDouble()` | |
| `B3EtfPosition` | `ticker` | `updatedValue.toDouble()` | |
| `B3FundPosition` | `ticker` | `updatedValue.toDouble()` | FIIs = VariableIncomeAsset no sistema |
| `B3FixedIncomePosition` | `code` | `curveValue.toDouble()` | |
| `B3TreasuryPosition` | `isinCode` | `updatedValue.toDouble()` | |

> **Nota de parsing**: Os campos são `String` no DTO. A conversão deve usar `replace(",", ".").toDouble()` para suportar formato brasileiro ("1.234,56") ou `toDouble()` diretamente se o FileMapper já normaliza o separador decimal.

---

## Use Cases Modificados / Novos

### `ImportB3FileUseCase` — orquestrador atualizado

**Responsabilidade**: Chamar `B3ImportDataSource.import()` e passar o resultado para `SyncB3HistoryUseCase`.

```kotlin
// Fluxo simplificado
override suspend fun execute(param: Unit) {
    val records = port.import().getOrThrow()
    syncUseCase(records).getOrThrow()
}
```

**Colaboradores**: `B3ImportDataSource` (port), `SyncB3HistoryUseCase`

---

### `SyncB3HistoryUseCase` — novo use case

**Módulo**: `:core:domain:usecases`
**Pacote**: `com.eferraz.usecases.services`
**Param**: `List<B3Record>`
**Return**: `Unit` (log via `println`)

**Algoritmo** (dois passes):

```
currentMonth ← dateProvider.getCurrentYearMonth()
historyEntries ← holdingHistoryRepository.getByReferenceDate(currentMonth)

Passe 1 — perspectiva da importação:
  Para cada record em List<B3Record>:
    matches ← historyEntries.filter { entry →
      (entry.holding.asset as? VariableIncomeAsset)?.ticker == record.identifier
      || (entry.holding.asset as? FixedIncomeAsset)?.b3Identifier == record.identifier
    }
    Se matches não-vazio:
      Para cada match em matches:
        holdingHistoryRepository.upsert(match.copy(endOfMonthValue = record.value))
      println("ATUALIZADO: ${record.identifier} → ${record.value} (${matches.size} registro(s))")
    Senão:
      println("NÃO REGISTRADO: ${record.identifier}")
  // Nota: filter+forEach garante que todos os holdings coincidentes sejam atualizados
  // (comportamento correto para portfolios com posições duplicadas do mesmo ativo)

Passe 2 — perspectiva do histórico:
  importedIds ← records.map { it.identifier }.toSet()
  Para cada entry em historyEntries:
    when (entry.holding.asset) {
      is VariableIncomeAsset:
        se ticker NOT IN importedIds → println("IDENTIFICADOR INEXISTENTE: $ticker")
      is FixedIncomeAsset && b3Identifier != null:
        se b3Identifier NOT IN importedIds → println("IDENTIFICADOR INEXISTENTE: $b3Identifier")
      is FixedIncomeAsset && b3Identifier == null:
        println("IGNORADO: ${issuer.name} — renda fixa sem identificador B3")
      is InvestmentFundAsset:
        println("IGNORADO: ${name} — fundo sem identificador B3")
    }
```

**Colaboradores**:
- `HoldingHistoryRepository` (busca e upsert)
- `DateProvider` (período atual)

---

## Entidades Não Modificadas

As entidades de domínio em `:core:domain:entity` **não são alteradas** nesta feature:
- `HoldingHistoryEntry` — campo `endOfMonthValue: Double` já existente é o destino da atualização
- `FixedIncomeAsset` — campo `b3Identifier: String?` já existe (introduzido pela feature `004-fixed-income-b3-id`)
- `VariableIncomeAsset` — campo `ticker: String` já existente é a chave de match
- `InvestmentFundAsset` — sem alteração; não participa do matching
- `InvestmentCategory` — enum sem alteração; usado para diferenciar tipos no matching

## Diagrama de Fluxo

```
Usuário seleciona XLSX
        ↓
ImportB3FileUseCase.execute()
        ↓
B3ImportDataSource.import()          ← interface em :domain:usecases
        ↓                               implementada por B3ImportDataSourceImpl
B3ImportDataSourceImpl               ← em :data:filestore
  para cada aba do XLSX:
    para cada B3Position não-blank:
      try { B3Record(b3Identifier(), b3Value()) }
      catch { println WARN; skip }
        ↓
List<B3Record>
        ↓
SyncB3HistoryUseCase.execute(records)
        ↓
  [Passe 1] — perspectiva da importação
    B3Record com id null → log IGNORADO
    B3Record com id → busca HoldingHistoryEntry
      encontrado → upsert(copy(endOfMonthValue)) → log ATUALIZADO
      não encontrado → log NÃO_REGISTRADO
        ↓
  [Passe 2] — perspectiva do histórico
    HoldingHistoryEntry com chave fora da importação → log IDENTIFICADOR_INEXISTENTE
        ↓
Unit (resultado)
```
