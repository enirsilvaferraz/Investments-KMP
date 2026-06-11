# Research: Rateio de Taxas de Nota de Corretagem SINACOR

Feature: `026-sinacor-fee-rateio` | Data: 2026-06-10

---

## 1. Aritmética de ponto flutuante vs. inteiros em centavos

**Decisão**: Aritmética inteira em centavos (`Long`) para cálculo de rateio e comparação de fechamento.

**Rationale**: A spec exige explicitamente (FR-016): "utilizar aritmética de alta precisão em centavos inteiros para evitar erros de ponto flutuante". Valores monetários com 2 casas decimais são convertidos para `Long` (×100) antes dos cálculos e convertidos de volta para `Double` ao fim. Isso garante determinismo e elimina problemas de representação IEEE-754 (ex.: `1.51 + 1.51 + 1.52 ≠ 4.54` em ponto flutuante). A comparação de igualdade em FR-007 (grossValue vs. quantity×unitPrice) e o fechamento contábil (FR-019) também ocorrem em centavos.

**Alternativas consideradas**:
- `BigDecimal` — disponível no JVM mas não em `commonMain` KMP sem wrapper; rejeitado.
- Ponto flutuante com `round` — impreciso e não determinístico em série de operações; rejeitado.

---

## 2. Tratamento de erros: exceções vs. Result

**Decisão**: Lançar `IllegalArgumentException` para erros de entrada inválida (Etapa 1, FR-010) e `IllegalStateException` para falha de fechamento contábil (Etapa 3, FR-020).

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

**Decisão**: O resíduo de centavos (diferença entre `somaFeesCents` e `Σ feeCents[0..N-2]`) é atribuído ao **último ativo do array de entrada** (`assets[N-1]`).

**Rationale**: A spec (clarificação de expansão) define explicitamente: "No **último ativo** do array de entrada, calculado como `Soma_Taxas − Total_Taxas_Distribuidas_Anteriores` (substitui critério anterior de maior volume)". O algoritmo é:
1. Para cada ativo `[0..N-2]`: `feeCents[i] = ROUND_HALF_UP(grossValueCents[i] × somaFeesCents / totalVolumeCents)`
2. Para o ativo `[N-1]` (último): `feeCents[N-1] = somaFeesCents − Σ feeCents[0..N-2]`

Isso garante determinismo absoluto (depende apenas da ordem da lista, não do volume) e que `Σ feeCents == somaFeesCents` sempre.

**Implementação em aritmética inteira** (ROUND_HALF_UP de `a × b / c`):
```
feeCents[i] = (grossValueCents[i] * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents
```
Alternativa equivalente (multiplicar numerador por 2, comparar remainder):
```
val num = grossValueCents[i] * somaFeesCents
feeCents[i] = (num + totalVolumeCents / 2) / totalVolumeCents
```

**Alternativas descartadas**:
- Resíduo para o ativo de maior volume — era o critério anterior, explicitamente substituído pela clarificação da spec.
- `floor` sem ROUND_HALF_UP — rejeitado pela spec que exige ROUND_HALF_UP para os N-1 primeiros.
- Distribuir resíduo entre múltiplos ativos — não especificado; rejeitado para manter determinismo.

---

## 5. Reuso de `TransactionType` vs. novo enum `TradeType`

**Decisão**: Criar novo enum `TradeType` (BUY, SELL) no pacote `brokeragenotes`.

**Rationale**: `TransactionType.PURCHASE/SALE` pertence ao modelo de transações persistidas em posições de carteira. `TradeType` é uma entrada de parsing de nota SINACOR — conceito distinto, em camada distinta. Misturá-los violaria SOLID (princípio I — segregação de interfaces) e criaria acoplamento desnecessário entre modelos.

**Alternativas consideradas**:
- Reutilizar `TransactionType` — rejeitado por acoplamento de domínios distintos.

---

## 6. Equação de fechamento: comparação em centavos vs. Double

**Decisão**: Comparação da equação de fechamento em centavos inteiros (`Long`).

**Rationale**: A spec (FR-019) especifica que a validação pós-cálculo deve verificar `Σ(BUY netValue) − Σ(SELL netValue) = metadata.netValue`. Converter todos os valores para centavos antes da comparação elimina erros de arredondamento. A discrepância reportada no erro é calculada como `differenceCents / 100.0`.

**Implementação**:
```
noteNetValueCents = Math.round(note.metadata.netValue * 100)
buysTotalCents = Σ Math.round(netValue[i] * 100) for BUY
sellsTotalCents = Σ Math.round(netValue[i] * 100) for SELL
differenceCents = buysTotalCents - sellsTotalCents - noteNetValueCents
Verify differenceCents == 0L
```

---

## 7. `grossValue` em `NoteAsset`: derivado vs. campo fornecido

**Decisão**: `grossValue` é um campo fornecido pela fonte (`valor_bruto_total` da nota SINACOR), não um valor derivado de `quantity × unitPrice`.

**Rationale**: A spec (FR-004) define `valor_bruto_total` como "informado pela fonte". A spec (FR-007) exige validação explícita de que `round(quantity × unitPrice × 100) == round(grossValue × 100)` — validação que só faz sentido se `grossValue` for um campo independente. Em casos reais (nota canônica `docs/nota.json`), a corretora pode arredondar internamente; forçar `grossValue = quantity * unitPrice` perderia o valor original da fonte e tornaria FR-007 uma tautologia.

**Alternativas consideradas**:
- `val grossValue get() = quantity * unitPrice` — rejeitado; tornaria FR-007 tautologia e perderia o valor original da nota.

---

## 8. Campo `specification` em `NoteAsset` e igualdade estrutural

**Decisão**: `NoteAsset` inclui campo `specification: String` que **participa da igualdade estrutural** (`data class` com todos os campos).

**Rationale**: A spec (clarificação, Key Entities) define: "`especificacao` participa da identidade estrutural completa — diferencia entradas como 'BRB111F UNT N2' e 'BRB111 UNT N2' para o mesmo ticker BRBI11". Na nota canônica, o mesmo ticker BRBI11 aparece com especificações distintas em linhas distintas — sem `specification`, essas linhas seriam indiferenciáveis como chave de `Map<NoteAsset, Double>`.

**Alternativas consideradas**:
- Não incluir `specification` na API — rejeitado; causa colisão de chave no mapa de saída para tickers repetidos com especificações distintas.
- Campo apenas para display, fora de `equals`/`hashCode` — rejeitado pela clarificação explícita da spec.

---

## 9. Estrutura de taxas: simplificada vs. completa (FR-005)

**Decisão**: Modelar `ApportionableFees` com os 6 campos definidos pela spec (liquidação, emolumentos, transferência, corretagem, ISS, outras) mais `WithheldTaxes` separado (IRRF operações, IRRF day trade).

**Rationale**: A spec (FR-005) define explicitamente os 6 campos de `taxas_rateáveis`. A separação de `impostos_retidos` em `WithheldTaxes` é necessária pois eles **não entram em `Soma_Taxas`** (FR-011) — incluí-los em `ApportionableFees` violaria a semântica. O modelo simplificado anterior (emoluments, settlement, incomeTax) era inadequado para a nota canônica que tem `taxa_liquidacao` e `emolumentos` distintos.

**Alternativas consideradas**:
- Estrutura simplificada com 3 campos — rejeitado; não cobre todos os campos do formato SINACOR real.
- Um único campo `total` sem itemização — rejeitado; perderia rastreabilidade por tipo de taxa.

---

## 10. Separação de responsabilidades: validador vs. calculador

**Decisão**: `BrokerageNoteValidator` é responsável exclusivo pela Etapa 1 (validação pré-cálculo). A lógica de cálculo (Etapa 2) e validação pós-cálculo (Etapa 3) ficam na função `calculateFeeAllocation`.

**Rationale**: A spec (Key Entities) define `ValidadorNota` como "Responsável exclusivo pela Etapa 1". Separar responsabilidades segue SOLID (princípio S). O validador pode ser usado independentemente (ex.: validar nota antes de persistir, sem calcular rateio).

**Alternativas consideradas**:
- Tudo em uma única função monolítica — rejeitado por violar princípio S.
- Validador inline dentro de `calculateFeeAllocation` sem objeto separado — aceito como trade-off se o objeto separado gerar boilerplate desnecessário; o plano mantém separação por clareza.
