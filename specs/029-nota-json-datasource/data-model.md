# Data Model: Importação de Nota de Corretagem via JSON

Feature: `029-nota-json-datasource` | Módulo: `:data:filestore` (único alterado) · retorno: `:domain:entity` (somente leitura de tipos existentes)

---

## Diagrama de fluxo

```
Nota2JsonFixture.raw (constante Kotlin)
    │
    ▼
Json.decodeFromString<BrokerageNoteJsonDocument>(raw)   ← DTOs @Serializable
    │
    ▼
BrokerageNoteJsonMapper.toDomain(dto)                    ← conversão explícita
    │
    ▼
BrokerageNote (domínio — feature 026, inalterado)
```

---

## Documento JSON raiz (`BrokerageNoteJsonDocument`)

Espelha a raiz de `docs/nota2.json`.

| Campo JSON | Tipo JSON | DTO (Kotlin) | Obrigatório |
|------------|-----------|--------------|-------------|
| `metadados` | object | `metadata: NoteMetadataJson` | sim |
| `resumo_financeiro` | object | `financialSummary: NoteFinancialSummaryJson` | sim |
| `ativos` | array | `assets: List<NoteAssetJson>` | sim (pode ser `[]`) |

Propriedades desconhecidas na raiz: ignoradas (`ignoreUnknownKeys`).

---

## `NoteMetadataJson` ← `metadados`

| Campo JSON | Tipo | Propriedade DTO | Domínio (`BrokerageNoteMetadata`) |
|------------|------|-----------------|-----------------------------------|
| `numero_nota` | string | `noteNumber: String` | `noteNumber` |
| `data_pregao` | string `dd/MM/yyyy` | `tradingDate: String` | `tradingDate: LocalDate` |
| `data_liquidacao` | string `dd/MM/yyyy` | `settlementDate: String` | `settlementDate: LocalDate` |
| `corretora` | string | `brokerage: String` | `brokerage` |
| `cnpj_corretora` | string | `brokerageDocument: String` | `brokerageDocument` |
| `valor_liquido_nota` | number | `netValue: Double` | `netValue` (pass-through) |

---

## `NoteFinancialSummaryJson` ← `resumo_financeiro`

| Campo JSON | Propriedade DTO | Domínio (`FinancialSummary`) |
|------------|-----------------|------------------------------|
| `volume_total_operado` | `totalVolumeTraded: Double` | `totalVolumeTraded` |
| `total_compras_vista` | `totalBuys: Double` | `totalBuys` |
| `total_vendas_vista` | `totalSells: Double` | `totalSells` |
| `taxas_rateaveis` | `apportionableFees: ApportionableFeesJson` | `apportionableFees` |
| `impostos_retidos` | `withheldTaxes: WithheldTaxesJson` | `withheldTaxes` |

### `ApportionableFeesJson` ← `taxas_rateaveis`

| Campo JSON | Propriedade DTO | Domínio (`ApportionableFees`) |
|------------|-----------------|-------------------------------|
| `taxa_liquidacao` | `settlement: Double` | `settlement` |
| `emolumentos` | `emoluments: Double` | `emoluments` |
| `taxa_transferencia` | `transfer: Double` | `transfer` |
| `corretagem` | `brokerage: Double` | `brokerage` |
| `iss` | `iss: Double` | `iss` |
| `outras` | `others: Double` | `others` |

### `WithheldTaxesJson` ← `impostos_retidos`

| Campo JSON | Propriedade DTO | Domínio (`WithheldTaxes`) |
|------------|-----------------|---------------------------|
| `irrf_operacoes` | `irrfOperations: Double` | `irrfOperations` |
| `irrf_day_trade` | `irrfDayTrade: Double` | `irrfDayTrade` |

---

## `NoteAssetJson` ← item de `ativos`

| Campo JSON | Propriedade DTO | Domínio (`NoteAsset`) |
|------------|-----------------|----------------------|
| `ticker` | `ticker: String` | `ticker` |
| `especificacao` | `specification: String` | `specification` |
| `movimentacao` | `movement: String` | `tradeType: TradeType` |
| `quantidade` | `quantity: Double` | `quantity` (inteiros JSON → Double) |
| `valor_unitario` | `unitPrice: Double` | `unitPrice` |
| `valor_bruto_total` | `grossValue: Double` | `grossValue` |

### Regras de mapeamento `movimentacao`

| Valor JSON | `TradeType` |
|------------|-------------|
| `"COMPRA"` | `BUY` |
| `"VENDA"` | `SELL` |
| outro | **falha** (`IllegalArgumentException`) |

---

## Validação e limites de responsabilidade

| Regra | Camada | Comportamento |
|-------|--------|---------------|
| JSON sintaticamente inválido | Data source | `Result.failure` |
| Campo obrigatório ausente | kotlinx.serialization | `Result.failure` |
| Data malformada | Mapper | `Result.failure` |
| `movimentacao` desconhecida | Mapper | `Result.failure` |
| Propriedade JSON extra | Deserialização | ignorada |
| `ativos` vazio | Data source | sucesso estrutural; validador contábil rejeita downstream |
| Volume/subtotais inconsistentes | — | **fora de escopo** (FR-009; `BrokerageNoteValidator`) |
| Zeros monetários (`0.0`) | Mapper | preservados como `0.0` |

---

## Entidades de domínio (existentes — sem alteração)

Referência canônica: `core/domain/entity/docs/DOMAIN.md` §6.5 e feature `026-sinacor-fee-rateio`.

```
BrokerageNote
├── metadata: BrokerageNoteMetadata
├── financialSummary: FinancialSummary
│     ├── apportionableFees: ApportionableFees
│     └── withheldTaxes: WithheldTaxes
└── assets: List<NoteAsset>
      └── tradeType: TradeType { BUY, SELL }
```

---

## Fixture de referência

| Atributo | Valor (`nota2.json`) |
|----------|----------------------|
| Número da nota | `8827829` |
| Pregão | `10/06/2026` |
| Liquidação | `12/06/2026` |
| Corretora | Nu Investimentos S.A. Corretora… |
| CNPJ | `62.169.875/0001-79` |
| `valor_liquido_nota` | `12294.92` |
| Volume total | `56402.04` |
| Total compras | `22043.78` |
| Total vendas | `34358.26` |
| Taxas rateáveis (6 campos) | `12.63, 3.76, 1.46, 0.0, 0.0, 0.0` |
| Impostos retidos | `1.71, 0.0` |
| Ativos | **47** linhas (COMPRA + VENDA) |

---

## Estrutura de ficheiros (camada de dados)

```text
core/data/filestore/src/                    # ÚNICO módulo alterado
├── commonMain/kotlin/com/eferraz/filestore/brokeragenote/
│   ├── BrokerageNoteJsonDataSource.kt      # interface public
│   ├── BrokerageNoteJsonDataSourceImpl.kt
│   ├── BrokerageNoteJsonMapper.kt
│   ├── Nota2JsonFixture.kt
│   └── dto/ ...
└── jvmTest/kotlin/com/eferraz/filestore/brokeragenote/
    └── BrokerageNoteJsonMapperTest.kt
```
