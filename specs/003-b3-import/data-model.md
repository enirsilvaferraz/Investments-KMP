# Data Model: Importação de Dados da B3

**Feature**: `003-b3-import` | **Phase**: 1 — Design & Contracts | **Date**: 2026-05-23

---

## Decisões Arquiteturais

| Decisão | Escolha |
|---------|---------|
| Onde ficam os DTOs B3 | `:data:filestore` — são tipos de infraestrutura, não de domínio |
| Onde fica a lógica de log | `:data:filestore` — o UseCase não conhece os dados B3 |
| O que o UseCase recebe/retorna | `Unit` → `Result<Unit>` — operação opaca do ponto de vista do domínio |
| Quantos ports para esta feature | Um único `B3ImportPort` — encapsula picking + parsing + logging |
| Nomes dos DTOs | Inglês (`B3StockPosition`, `B3EtfPosition`, etc.) |
| Biblioteca de parsing | FileMapper-KMP — mapeia sheets XLSX para data classes via `@ColumnName` |
| File picker | `FileMapperPicker.pickFile()` — nativo KMP, sem `JFileChooser` |

---

## Entidades de Domínio (`:domain:entity`)

> **Nenhuma entidade nova** para esta fase. O domínio não precisa conhecer o formato B3 enquanto o objetivo é apenas logar no console. Entidades de domínio ricas (ex.: `AssetPosition`) serão adicionadas quando houver persistência ou lógica de negócio sobre os dados importados.

---

## DTOs de Arquivo (`:data:filestore`)

Os DTOs vivem em `core/data/filestore/src/commonMain/kotlin/com/eferraz/filestore/b3/dto/` e são **internos** ao módulo — nunca expostos para `:domain:usecases` ou `:features:*`.

> **Fase atual**: todos os campos são `String`. Valores numéricos (`"3616.08"`) e datas (`"23/05/2026"`) ficam como texto. Conversão para tipos ricos (`BigDecimal`, `LocalDate`) é reservada para fase futura.

---

### `B3StockPosition` — Guia "Acoes" (14 colunas)

```kotlin
// core/data/filestore/src/commonMain/.../b3/dto/B3StockPosition.kt
package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.ColumnName
import kotlinx.serialization.Serializable

@Serializable
internal data class B3StockPosition(
    @ColumnName("Produto")                  val product: String,
    @ColumnName("Instituição")              val institution: String,
    @ColumnName("Conta")                    val account: String,
    @ColumnName("Código de Negociação")     val ticker: String,
    @ColumnName("Tipo")                     val type: String,
    @ColumnName("Escriturador")             val registrar: String,
    @ColumnName("Quantidade")               val quantity: String,
    @ColumnName("Quantidade Disponível")    val availableQuantity: String,
    @ColumnName("Quantidade Indisponível")  val unavailableQuantity: String,
    @ColumnName("Motivo")                   val reason: String,
    @ColumnName("Preço de Fechamento")      val closingPrice: String,
    @ColumnName("Valor Atualizado")         val updatedValue: String,
    @ColumnName("% Carteira")               val portfolioPercent: String,
    @ColumnName("% Variação")               val variationPercent: String,
)
```

---

### `B3EtfPosition` — Guia "ETF" (13 colunas)

```kotlin
// core/data/filestore/src/commonMain/.../b3/dto/B3EtfPosition.kt
package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.ColumnName
import kotlinx.serialization.Serializable

@Serializable
internal data class B3EtfPosition(
    @ColumnName("Produto")                      val product: String,
    @ColumnName("Instituição")                  val institution: String,
    @ColumnName("Conta")                        val account: String,
    @ColumnName("Código de Negociação")         val ticker: String,
    @ColumnName("CNPJ do Fundo")                val fundCnpj: String,
    @ColumnName("Código ISIN / Distribuição")   val isinCode: String,
    @ColumnName("Tipo")                         val type: String,
    @ColumnName("Quantidade")                   val quantity: String,
    @ColumnName("Quantidade Disponível")        val availableQuantity: String,
    @ColumnName("Quantidade Indisponível")      val unavailableQuantity: String,
    @ColumnName("Motivo")                       val reason: String,
    @ColumnName("Preço de Fechamento")          val closingPrice: String,
    @ColumnName("Valor Atualizado")             val updatedValue: String,
)
```

---

### `B3FundPosition` — Guia "Fundo de Investimento" (14 colunas)

```kotlin
// core/data/filestore/src/commonMain/.../b3/dto/B3FundPosition.kt
package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.ColumnName
import kotlinx.serialization.Serializable

@Serializable
internal data class B3FundPosition(
    @ColumnName("Produto")                      val product: String,
    @ColumnName("Instituição")                  val institution: String,
    @ColumnName("Conta")                        val account: String,
    @ColumnName("Código de Negociação")         val ticker: String,
    @ColumnName("CNPJ do Fundo")                val fundCnpj: String,
    @ColumnName("Código ISIN / Distribuição")   val isinCode: String,
    @ColumnName("Tipo")                         val type: String,
    @ColumnName("Administrador")                val administrator: String,
    @ColumnName("Quantidade")                   val quantity: String,
    @ColumnName("Quantidade Disponível")        val availableQuantity: String,
    @ColumnName("Quantidade Indisponível")      val unavailableQuantity: String,
    @ColumnName("Motivo")                       val reason: String,
    @ColumnName("Preço de Fechamento")          val closingPrice: String,
    @ColumnName("Valor Atualizado")             val updatedValue: String,
)
```

---

### `B3FixedIncomePosition` — Guia "Renda Fixa" (19 colunas)

```kotlin
// core/data/filestore/src/commonMain/.../b3/dto/B3FixedIncomePosition.kt
package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.ColumnName
import kotlinx.serialization.Serializable

@Serializable
internal data class B3FixedIncomePosition(
    @ColumnName("Produto")                          val product: String,
    @ColumnName("Instituição")                      val institution: String,
    @ColumnName("Emissor")                          val issuer: String,
    @ColumnName("Código")                           val code: String,
    @ColumnName("Indexador")                        val indexer: String,
    @ColumnName("Tipo de regime")                   val regimeType: String,
    @ColumnName("Data de Emissão")                  val issueDate: String,
    @ColumnName("Vencimento")                       val maturityDate: String,
    @ColumnName("Quantidade")                       val quantity: String,
    @ColumnName("Quantidade Disponível")            val availableQuantity: String,
    @ColumnName("Quantidade Indisponível")          val unavailableQuantity: String,
    @ColumnName("Motivo")                           val reason: String,
    @ColumnName("Contraparte")                      val counterparty: String,
    @ColumnName("Preço Atualizado MTM")             val mtmPrice: String,
    @ColumnName("Valor Atualizado MTM")             val mtmValue: String,
    @ColumnName("Preço Atualizado CURVA")           val curvePrice: String,
    @ColumnName("Valor Atualizado CURVA")           val curveValue: String,
    @ColumnName("Preço Atualizado FECHAMENTO")      val closingPrice: String,
    @ColumnName("Valor Atualizado FECHAMENTO")      val closingValue: String,
)
```

---

### `B3TreasuryPosition` — Guia "Tesouro Direto" (13 colunas)

```kotlin
// core/data/filestore/src/commonMain/.../b3/dto/B3TreasuryPosition.kt
package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.ColumnName
import kotlinx.serialization.Serializable

@Serializable
internal data class B3TreasuryPosition(
    @ColumnName("Produto")                  val product: String,
    @ColumnName("Instituição")              val institution: String,
    @ColumnName("Código ISIN")              val isinCode: String,
    @ColumnName("Indexador")               val indexer: String,
    @ColumnName("Vencimento")               val maturityDate: String,
    @ColumnName("Quantidade")               val quantity: String,
    @ColumnName("Quantidade Disponível")    val availableQuantity: String,
    @ColumnName("Quantidade Indisponível")  val unavailableQuantity: String,
    @ColumnName("Motivo")                   val reason: String,
    @ColumnName("Valor Aplicado")           val appliedValue: String,
    @ColumnName("Valor bruto")              val grossValue: String,
    @ColumnName("Valor líquido")            val netValue: String,
    @ColumnName("Valor Atualizado")         val updatedValue: String,
)
```

---

## Mapeamento Real das Guias do Arquivo B3

Baseado na inspeção do arquivo `posicao-2026-05-23-12-53-16.xlsx`.

| # | Guia | Linhas de dados | Colunas | DTO |
|---|------|-----------------|---------|-----|
| 1 | Acoes | 21 + 1 total | 14 | `B3StockPosition` |
| 2 | ETF | 4 + 1 total | 13 | `B3EtfPosition` |
| 3 | Fundo de Investimento | 28 + 1 total | 14 | `B3FundPosition` |
| 4 | Renda Fixa | 26 + totais nas cols. 17 e 19 | 19 | `B3FixedIncomePosition` |
| 5 | Tesouro Direto | 4 + 1 total | 13 | `B3TreasuryPosition` |

> **Nota**: A última linha de cada guia contém `"Total"` e o valor acumulado do arquivo. Essa linha **é descartada** do parse (não mapeada para DTO) — assim como linhas totalmente em branco. O log exibe: (1) header reconstruído a partir das `@ColumnName` do DTO, (2) as linhas de dados reais, e (3) totais calculados pela implementação sobre essas linhas.

---

## Port (Interface de Domínio em `:domain:usecases`)

### `B3ImportPort`

Port único que encapsula **toda** a operação: seleção de arquivo, parsing e log. O domínio não conhece DTOs, XLSX, guias ou FileMapper.

```kotlin
// core/domain/usecases/src/commonMain/.../repositories/B3ImportPort.kt
package com.eferraz.usecases.repositories

public interface B3ImportPort {
    /**
     * Abre o seletor de arquivo nativo, lê o XLSX da B3 e loga as posições no console.
     * Retorna Result.success(Unit) em caso de sucesso ou cancelamento silencioso pelo usuário.
     * Retorna Result.failure(...) em caso de arquivo inválido, erro de leitura ou timeout.
     */
    public suspend fun importAndLog(): Result<Unit>
}
```

> `FilePickerPort` e `XlsxReaderPort` anteriores são **removidos** — suas responsabilidades foram absorvidas pela implementação de `B3ImportPort` em `:data:filestore`, onde os detalhes de infraestrutura pertencem.

---

## Implementação (`:data:filestore`)

### `B3ImportPortImpl`

```kotlin
// core/data/filestore/src/commonMain/.../B3ImportPortImpl.kt
package com.eferraz.filestore.b3

// Responsabilidades internas (não expostas ao domínio):
// 1. FileMapperPicker.pickFile(FileType.XLSX) → PlatformFile?
// 2. fileMapper.importData<B3StockPosition>(bytes) → List<B3StockPosition>
//    (repetido para cada tipo de guia)
// 3. println de cada posição por guia
// 4. Retorna Result<Unit>
internal class B3ImportPortImpl : B3ImportPort {
    override suspend fun importAndLog(): Result<Unit> = runCatching {
        val file = FileMapperPicker.pickFile(FileType.XLSX) ?: return@runCatching
        val bytes = file.readBytes()
        // parse + log de cada guia (ver contracts/XlsxImportContract.md)
    }
}
```

---

## UseCase (`:domain:usecases`)

### `ImportB3FileUseCase`

```kotlin
// Input:  Unit
// Output: Unit (log feito internamente pelo port; erros via Result.failure)
// Dispatcher: Dispatchers.Default (injetado)
// Timeout: withTimeout(30_000L) envolvendo a chamada ao port
```

O UseCase é estritamente um **orquestrador de timeout e estado**. Não tem conhecimento de XLSX, guias, DTOs ou log:

1. Chama `B3ImportPort.importAndLog()` dentro de `withTimeout(30_000L)`.
2. Propaga `Result.failure` se o port lançar ou o timeout expirar.
3. `Result.success(Unit)` inclui tanto sucesso real quanto cancelamento silencioso (usuário não selecionou arquivo).

---

## Estado da UI (`:features:composeApp`)

### `HistoryState` — alteração mínima

```kotlin
internal data class HistoryState(
    // ... campos existentes ...
    val isImporting: Boolean = false,   // NOVO — controla spinner no botão de importação
)
```

### `HistoryIntent` — alteração mínima

```kotlin
internal sealed interface HistoryIntent {
    // ... intents existentes ...
    data object ImportB3File : HistoryIntent  // NOVO
}
```

---

## Diagrama de Fluxo

```
[UI: AssetHistoryScreen]
    │
    ├─► onImportClick → HistoryIntent.ImportB3File
    │
[HistoryViewModel]
    │
    ├─► state.isImporting = true
    ├─► importB3FileUseCase(Unit)
    │       │
    │   [ImportB3FileUseCase]
    │       └─► withTimeout(30s)
    │               └─► B3ImportPort.importAndLog()
    │                       │
    │                   [B3ImportPortImpl  — :data:filestore]
    │                       ├─► FileMapperPicker.pickFile(XLSX)
    │                       │       └─► null → return (cancelado, Result.success)
    │                       ├─► file.readBytes()
    │                       ├─► importData<B3StockPosition>(bytes) → println header → linhas → totais calculados
    │                       ├─► importData<B3EtfPosition>(bytes)   → println header → linhas → totais calculados
    │                       ├─► importData<B3FundPosition>(bytes)  → println header → linhas → totais calculados
    │                       ├─► importData<B3FixedIncomePosition>(bytes) → println header → linhas → totais calculados
    │                       └─► importData<B3TreasuryPosition>(bytes)   → println header → linhas → totais calculados
    │
    ├─► onSuccess → state.isImporting = false
    └─► onFailure → state.isImporting = false + (futuro: snackbar de erro)
```

---

## Validações e Regras de Limpeza

| Regra | Localização |
|-------|-------------|
| Cancelamento silencioso (usuário não selecionou arquivo) | `B3ImportPortImpl` — `null` do picker → `return@runCatching` |
| Arquivo inválido / corrompido | FileMapper-KMP lança `FileMapperException` → capturado por `runCatching` |
| `.trim()` e células `"-"` | Tratados pelo FileMapper-KMP automaticamente |
| Timeout 30 s | `ImportB3FileUseCase` via `withTimeout(30_000L)` |
| Linhas em branco (todas as células vazias/nulas) | Filtradas em `parseAndLog` — não chegam ao resultado final nem ao log |
| Linhas de total do arquivo (`"Total"`, `"Subtotal"` na 1ª célula) | Filtradas em `parseAndLog` — descartadas do parse; **não mapeadas para DTOs** |
| Header das colunas | Usado pelo FileMapper-KMP para o mapeamento `@ColumnName` → campo; reconstruído a partir das anotações do DTO para ser **impresso no console** antes das linhas de dados |
| Total calculado no log | Calculado pela implementação somando campos numéricos relevantes das `dataRows` — **não lido do arquivo** |
