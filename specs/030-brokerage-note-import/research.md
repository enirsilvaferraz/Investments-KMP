# Research: Importação de Nota de Corretagem JSON

**Feature**: 030-brokerage-note-import  
**Date**: 2026-06-12

---

## Decisão 1: Campo `allocatedFee` em `AssetTransaction`

**Decisão**: Adicionar `val allocatedFee: Double = 0.0` à `data class AssetTransaction`.

**Rationale**:  
- Para transações importadas de nota, a taxa alocada é um atributo da transação (calculada no momento do import).  
- O valor padrão `0.0` mantém compatibilidade retroativa com todas as transações manuais existentes.  
- O `netValue` é derivado (`totalValue ± allocatedFee`) e não precisa ser persistido — calculado em tempo real.

**Alternativas rejeitadas**:  
- Criar `BrokerageNoteTransaction` subtipo de `AssetTransaction`: viola o princípio de tipo único definido em `AGENTS.md`.  
- Adicionar `allocatedFee` só na UI (sem persistir): perderia a informação entre sessões.

---

## Decisão 2: Ticker na estrutura `BrokerageNote`

**Decisão**: Adicionar `data class BrokerageNoteAsset(val ticker: String, val transaction: AssetTransaction)` em `:domain:entity` e mudar `BrokerageNote.assets` de `List<AssetTransaction>` para `List<BrokerageNoteAsset>`.

**Rationale**:  
- `AssetTransaction` é um tipo de domínio geral (RF, RV, fundos) — não deve carregar `ticker` como campo obrigatório.  
- `BrokerageNote` é contexto exclusivo de nota de corretagem; faz sentido que seus "ativos" carreguem o ticker.  
- `NoteFeeAllocation` aceita adaptação mínima (extrair `it.transaction` no lugar de `it` diretamente).

**Alternativas rejeitadas**:  
- Lista paralela `assetTickers: List<String>` em `BrokerageNote`: lista paralela é frágil (índice pode desalinhar).  
- Adicionar `ticker: String?` em `AssetTransaction`: polui entidade genérica com dado específico de nota.

---

## Decisão 3: Lookup `AssetHolding` por ticker + corretora Nubank

**Decisão**: Adicionar `getByTicker(ticker: String): AssetHolding?` ao `AssetHoldingRepository` (novo método na interface), filtrando também por `brokerage.id == 2` (Nubank fixo) na implementação.

**Rationale**:  
- `getByAssetId(assetId)` não se aplica: no import só se tem o ticker, não o ID.  
- `getAll()` + filtro em memória: simples, mas carrega todos os holdings para filtrar 1. Aceitável para app pessoal, mas expõe intenção ruim à camada de domínio.  
- Método específico `getByTicker` documenta a intenção e é indexável na implementação Room.

**Alternativas rejeitadas**:  
- `getAll()` + filter no use case: funciona, mas ineficiente e não comunica intenção.

---

## Decisão 4: Caso de uso de importação

**Decisão**: Substituir o stub `LoadBrokerageNoteUseCase` por `ImportBrokerageNoteUseCase` com retorno `Unit` (falhas logadas no console via `println` ou `Logger`, sem propagar exceção para a UI).

**Rationale**:  
- `LoadBrokerageNoteUseCase` é atualmente um stub com `println("OK!")` — sem valor de produção.  
- Faz sentido renomear para comunicar a intenção real: importar e persistir.  
- A operação é **transacional** em nível de use case: se qualquer holding não for encontrado, nenhuma transação é salva (falha rápida com log).

**Alternativas rejeitadas**:  
- Expandir `LoadBrokerageNoteUseCase` sem renomear: manteria nome enganoso.  
- Retornar `Result<Unit>` e propagar para UI: fora do escopo (US3 removida).

---

## Decisão 5: Atomicidade da persistência do lote

**Decisão**: Adicionar `saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>)` em `AssetTransactionRepository`, implementado com `@Transaction` no DAO do Room. A atomicidade é garantida em dois níveis:
1. **Use case (early-return)**: nenhum insert é iniciado se qualquer holding não for encontrado.
2. **Banco de dados (`@Transaction`)**: todos os inserts do lote ocorrem em uma única transação SQLite; falha em qualquer insert → rollback automático de todo o lote.

**Rationale**:
- Chamar `SaveTransactionUseCase` N vezes de forma sequencial não oferece garantia de rollback — se o 3º insert falhar, os 2 primeiros já estão gravados (dado corrompido).
- `@Transaction` no Room é o mecanismo correto para operações de batch atômico; padrão recomendado pelo Android.

**Alternativas rejeitadas**:
- `withTransaction {}` via `RoomDatabase` diretamente no use case: vaza detalhe de infraestrutura para a camada de domínio.
- Chamar `SaveTransactionUseCase` N vezes: sem rollback em caso de falha parcial.

---

## Decisão 6: Migração Room v10 → v11

**Decisão**: AutoMigration `@AutoMigration(from = 10, to = 11)` — adição de coluna simples com `defaultValue`.

**Rationale**:  
- Adicionar `allocatedFee REAL NOT NULL DEFAULT 0` à tabela `asset_transactions` é uma migração auto-detectável pelo Room (sem spec customizada).  
- O projeto já usa `AutoMigration` para casos similares (v4→v5, v5→v6, v7→v8).  
- `defaultValue = "0"` na anotação `@ColumnInfo` é suficiente para Room gerar o DDL correto.

**Alternativas rejeitadas**:  
- `MigrationSpec` manual: desnecessário para adição de coluna com default.

---

## Decisão 7: Coluna "Valor Líquido" em `TransactionManagementView`

**Decisão**: Adicionar campo `netValue: String` em `TransactionDraftUi` (derivado, somente leitura), calculado em `syncTotal()` com `allocatedFee`, e exibir nova coluna "Valor Líq." na tabela de transações.

**Rationale**:  
- `TransactionDraftUi` já possui `totalValue` somente leitura; `netValue` segue o mesmo padrão.  
- O cálculo: compra → `grossValue + allocatedFee`; venda → `grossValue - allocatedFee`.  
- Para transações manuais `allocatedFee = 0.0`, então `netValue == totalValue` (sem impacto visual relevante).

**Alternativas rejeitadas**:  
- Mostrar `netValue` no lugar de `totalValue`: `totalValue` ainda é necessário como coluna base.
