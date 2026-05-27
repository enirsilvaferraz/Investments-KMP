# Research: Sincronização de Histórico via Importação B3

**Feature**: `006-b3-import-sync-history` | **Date**: 2026-05-27

## Decisões Técnicas

### 1. Localização de `B3Record`

**Decisão**: `com.eferraz.usecases.entities.B3Record` em `:domain:usecases`

**Rationale**: `B3Record` é o objeto retornado pela porta `B3ImportDataSource` (que vive em `:domain:usecases`). Colocar no mesmo módulo evita criar dependência bidirecional e segue o padrão dos demais objetos de domínio do módulo (ex.: `B3IdentifierStatus`, `HoldingHistoryResult`).

**Alternativas consideradas**:
- `:domain:entity` — rejeitado porque `B3Record` é específico do fluxo de importação, não uma entidade de negócio persistida.
- Arquivo separado por classe — aceito; cada entidade em arquivo próprio (padrão existente).

---

### 2. Mecanismo de Log Progressivo

**Decisão**: `println` direto — sem sealed interface `B3SyncLogEvent`.

**Rationale**: Todos os use cases existentes (`SyncVariableIncomeValuesUseCase`, `ImportB3FileUseCase`) usam `println`. Introduzir uma sealed interface adiciona abstração sem benefício concreto nesta entrega. YAGNI — a refatoração para `Flow<LogEvent>` pode ser feita quando houver necessidade real de consumo na UI.

**Alternativas consideradas**:
- `B3SyncLogEvent` sealed interface — rejeitado; complexidade prematura para MVP.
- `Flow<String>` como retorno — deferido; exige refactor da base `AppUseCase`.

---

### 3. Assinatura de `B3Position` — métodos `b3Identifier()` e `b3Value()`

**Decisão**:
```
fun b3Identifier(): String?   // null = tipo sem identificador (ex.: fundo)
fun b3Value(): Double         // lança NumberFormatException se valor inválido
```

**Rationale**: `String?` para `b3Identifier()` é o modelo mais direto para "sem identificador" — sem wrapper desnecessário. `Double` (não `Double?`) para `b3Value()` é suficiente; linhas com valor inválido são capturadas com `try/catch` em `B3ImportDataSourceImpl` antes de construir o `B3Record`.

**Alternativas consideradas**:
- `Double?` para `b3Value()` — rejeitado; Double? exigiria unwrapping desnecessário no call site; um valor de zero nunca é válido num extrato real.
- `B3IdentifierStatus` como retorno de `b3Identifier()` — rejeitado; acoplaria `:data:filestore` a um tipo de `:domain:usecases`; `String?` é mais simples e suficiente.

---

### 4. Campo de valor por subtipo de `B3Position`

**Decisão** (conforme clarificação do utilizador):

| Subtipo | Campo `b3Value()` | Coluna XLSX |
|---------|--------------------|-------------|
| `B3StockPosition` | `updatedValue` | "Valor Atualizado" |
| `B3EtfPosition` | `updatedValue` | "Valor Atualizado" |
| `B3FundPosition` | `updatedValue` | "Valor Atualizado" |
| `B3FixedIncomePosition` | `curveValue` | "Valor Atualizado CURVA" |
| `B3TreasuryPosition` | `updatedValue` | "Valor Atualizado" |

**Rationale**: Renda fixa usa CURVA pois representa o valor contábil acumulado pela taxa contratada; os demais ativos usam "Valor Atualizado" que já é o valor de mercado de fechamento.

---

### 5. Campo de identificador por subtipo de `B3Position`

**Decisão** (conforme clarificação do utilizador):

| Subtipo | Campo `b3Identifier()` | Entidade de destino — campo de match |
|---------|------------------------|--------------------------------------|
| `B3StockPosition` | `ticker` | `VariableIncomeAsset.ticker` |
| `B3EtfPosition` | `ticker` | `VariableIncomeAsset.ticker` |
| `B3FundPosition` | `ticker` | `VariableIncomeAsset.ticker` (FIIs = renda variável no sistema) |
| `B3FixedIncomePosition` | `code` | `FixedIncomeAsset.b3Identifier` |
| `B3TreasuryPosition` | `isinCode` | `FixedIncomeAsset.b3Identifier` |

**Rationale**: A aba "Fundo de Investimento" do extrato B3 contém FIIs (Fundos de Investimento Imobiliário), que no sistema são cadastrados como `VariableIncomeAsset` com tipo `REAL_ESTATE_FUND`. Os fundos multimercado/previdência (`InvestmentFundAsset`) não aparecem no extrato B3 — são conceitos distintos. Renda fixa e Tesouro Direto compartilham `b3Identifier` em `FixedIncomeAsset`.

---

### 6. Campo atualizado em `HoldingHistoryEntry`

**Decisão**: Atualizar `endOfMonthValue` com `entry.copy(endOfMonthValue = b3Record.value)`.

**Rationale**: `endOfMonthValue` é o campo que representa o valor de mercado total da posição no final do mês — exatamente o que a B3 reporta no extrato. Padrão idêntico ao usado em `SyncVariableIncomeValuesUseCase` e `UpdateFixedIncomeAndFundsHistoryValueUseCase`.

---

### 7. Algoritmo de matching — dois passes

**Decisão**: O `SyncB3HistoryUseCase` executa em dois passes sobre os dados já carregados em memória.

**Passe 1** — perspectiva da importação (gera: `atualizado`, `não registrado`):
```
Para cada B3Record:
  Buscar HoldingHistoryEntry no mês corrente onde:
    asset is VariableIncomeAsset && asset.ticker == identifier  OU
    asset is FixedIncomeAsset && asset.b3Identifier == identifier
  Se encontrado → atualizar endOfMonthValue → upsert → println("ATUALIZADO: $identifier")
  Se não encontrado → println("NÃO REGISTRADO: $identifier")
```

**Passe 2** — perspectiva do histórico (gera: `identificador inexistente`, `ignorado`):
```
Construir set de identificadores importados
Para cada HoldingHistoryEntry do mês corrente:
  key = when (asset) {
    is VariableIncomeAsset → asset.ticker
    is FixedIncomeAsset && asset.b3Identifier != null → asset.b3Identifier
    is FixedIncomeAsset && asset.b3Identifier == null → println("IGNORADO: ${asset.issuer.name}"); null
    is InvestmentFundAsset → println("IGNORADO: ${asset.name}"); null
  }
  Se key != null && key NOT IN set → println("IDENTIFICADOR INEXISTENTE: $key")
```

**Rationale**: O "ignorado" sai do passe 2 (histórico), não do passe 1 (importação). Isso mantém a semântica correta: "ignorado" = ativo no banco sem chave de correspondência alguma com a B3. Os dados são carregados uma única vez antes dos passes.

---

### 8. Sensibilidade de case no matching

**Decisão**: Case-sensitive (conforme clarificação do utilizador).

**Rationale**: Tickers e identificadores B3 são padronizados em maiúsculas; comparação exata evita falsos positivos. O utilizador assume responsabilidade pelo cadastro consistente.

---

### 9. Parsing de número e tratamento de valores inválidos

**Decisão**: `toDouble()` diretamente, sem `replace`. Capturar `NumberFormatException` por linha em `B3ImportDataSourceImpl`.

**Rationale**: Verificado com arquivo real — valores numéricos usam ponto decimal padrão (ex.: `3616.08`). Os campos `Valor Atualizado MTM` e `Valor Atualizado FECHAMENTO` de renda fixa chegam como `"-"` quando indisponíveis, mas como usamos apenas `curveValue` (sempre preenchido no real), o problema não se manifesta. A captura de `NumberFormatException` é uma salvaguarda para formatos inesperados em arquivos futuros.
