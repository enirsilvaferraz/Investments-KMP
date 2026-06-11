# Research: Rateio de Taxas de Nota de Corretagem SINACOR

Feature: `026-sinacor-fee-rateio` | Data: 2026-06-10

---

## 1. Aritmética de ponto flutuante vs. inteiros em centavos

**Decisão**: Aritmética inteira em centavos (`Long`) para cálculo de rateio e comparação de fechamento.

**Rationale**: A spec exige explicitamente (FR-005): "utilizar aritmética inteira em centavos para evitar erros de ponto flutuante". Valores monetários com 2 casas decimais são convertidos para `Long` (×100) antes dos cálculos e convertidos de volta para `Double` ao fim. Isso garante determinismo e elimina problemas de representação IEEE-754 (ex.: `1.51 + 1.51 + 1.52 ≠ 4.54` em ponto flutuante).

**Alternativas consideradas**:
- `BigDecimal` — disponível no JVM mas não em `commonMain` KMP sem wrapper; rejeitado.
- Ponto flutuante com `round` — impreciso e não determinístico em série de operações; rejeitado.

---

## 2. Tratamento de erros: exceções vs. Result

**Decisão**: Lançar `IllegalArgumentException` para erros de entrada inválida (FR-010, FR-011, FR-012) e `IllegalStateException` para falha de fechamento contábil (FR-009).

**Rationale**: Consistente com o padrão estabelecido em `IncomeTax.calculate` (lança `IllegalArgumentException` para `purchaseDate > referenceDate`). Mantém a API pública sem wrapper `Result<>`, que adicionaria boilerplate sem benefício claro para entidades puras. O chamador captura exceções quando precisar de recuperação suave.

**Alternativas consideradas**:
- `Result<NoteFeeAllocation>` — mais funcional, mas quebra consistência com entidades existentes e impõe `fold`/`getOrThrow` ao chamador; rejeitado.
- Tipo `sealed class AllocationResult` — over-engineering para este escopo; rejeitado (princípio X).

---

## 3. Localização do código: módulo e pacote

**Decisão**: Novo pacote `com.eferraz.entities.brokeragenotes` em `:domain:entity/src/commonMain`.

**Rationale**: O cálculo é puramente de domínio sem dependências externas — pertence a `:domain:entity`. O projeto organiza entidades por domínio funcional (`entities.holdings`, `entities.transactions`, `entities.goals`). O nome `brokeragenotes` descreve o conceito em inglês (princípio VIII) e não conflita com `Brokerage.kt` existente em `holdings`.

**Alternativas consideradas**:
- `entities.brokerage` — o projeto já tem `Brokerage.kt` em `holdings`; potencial confusão; rejeitado.
- Colocar em `:domain:usecases` — o cálculo não tem dependências externas (sem repositories ou ports); pertence a `:domain:entity` como as demais entidades calculadas (`IncomeTax`, `TransactionBalance`); rejeitado.

---

## 4. Algoritmo de ajuste de centavos residuais

**Decisão**: O resíduo de centavos (diferença entre `Σ(allocatedFeeCents)` e `totalFeesCents`) é adicionado ao **primeiro ativo na lista com o maior volume financeiro** (`grossValue`). Critério estável e determinístico.

**Rationale**: A spec (User Story 3, cenário 1 + Edge Cases) define explicitamente: "o ajuste vai para o primeiro ativo da lista com maior volume — critério estável e determinístico". O índice é determinado por `assets.indexOfFirst { grossValueCents[it] == grossValueCents.max() }`.

**Implementação**:
1. Calcular `feeCents[i] = floor(grossValueCents[i] * totalFeesCents / totalVolumeCents)`
2. Calcular `remainder = totalFeesCents - feeCents.sum()`
3. Somar `remainder` a `feeCents[idxMaxVolume]`

**Alternativas consideradas**:
- Distribuir resíduo entre múltiplos ativos — não especificado; rejeitado para manter determinismo.
- Usar `round` em vez de `floor` — pode gerar resíduo negativo; mais complexo; rejeitado.

---

## 5. Reuso de `TransactionType` vs. novo enum `TradeType`

**Decisão**: Criar novo enum `TradeType` (BUY, SELL) no pacote `brokeragenotes`.

**Rationale**: `TransactionType.PURCHASE/SALE` pertence ao modelo de transações persistidas em posições de carteira. `TradeType` é uma entrada de parsing de nota SINACOR — conceito distinto, em camada distinta. Misturá-los violaria SOLID (princípio I — segregação de interfaces) e criaria acoplamento desnecessário entre modelos.

**Alternativas consideradas**:
- Reutilizar `TransactionType` — rejeitado por acoplamento de domínios distintos.

---

## 6. Equação de fechamento: comparação em centavos vs. Double

**Decisão**: Comparação da equação de fechamento em centavos inteiros (`Long`).

**Rationale**: A spec (FR-009) especifica "comparação em centavos inteiros". Converter `note.netValue`, `Σ(buys.netValue)` e `Σ(sells.netValue)` para centavos antes da comparação elimina erros de arredondamento. A discrepância reportada no erro é calculada como `differenceCents / 100.0`.

**Implementação**:
- `noteNetValueCents = Math.round(note.netValue * 100)`
- `differenceCents = buysTotalCents - sellsTotalCents - noteNetValueCents`
- Se `differenceCents != 0L` → lançar `IllegalStateException`

---

## 7. Visibilidade dos construtores das entidades de output

**Decisão**: Construtores de `AssetFeeAllocation` e `NoteFeeAllocation` são `internal` (não `private`).

**Rationale**: O cálculo ocorre dentro do mesmo módulo (`:domain:entity`); `internal` é suficiente para impedir criação externa sem bloquear testes no mesmo módulo. Construtores `private` exigiriam fábricas de teste adicionais, aumentando complexidade sem benefício (princípio X).

**Alternativa considerada**: `private constructor` com `companion object` (padrão de `IncomeTax`) — adequado quando o construtor só é chamado pelo próprio `companion object`; aqui o resultado é construído por `NoteFeeAllocation.calculate`, em arquivo diferente mas no mesmo módulo; `internal` é mais simples.
