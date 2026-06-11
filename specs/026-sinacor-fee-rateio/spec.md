# Feature Specification: Rateio de Taxas de Nota de Corretagem SINACOR

**Feature Branch**: `026-sinacor-fee-rateio`

**Created**: 2026-06-10

**Status**: Draft

**Input**: Expansão com novo modelo de entrada (estrutura `docs/nota.json`), validação pré/pós-cálculo em três etapas, rateio proporcional com tratamento de resíduo no último ativo e retorno em mapa ativo→valor líquido.

## Clarifications

### Session 2026-06-10

- Q: Quando a equação de fechamento falha (FR-009), o chamador recebe os dados de rateio por ativo ou não? → A: Retorna erro imediato sem nenhum dado de rateio; cálculo é abortado (Opção B).
- Q: O sistema deve validar os campos individuais de cada ativo (`quantidade`, `valor_unitario`)? → A: Sim — validar `quantidade > 0` e `valor_unitario > 0` por ativo; erro imediato para qualquer ativo inválido (Opção A).
- Q: Qual é a convenção de sinal de `nota.valor` em notas exclusivamente de venda? → A: Sinal contábil natural — positivo (débito líquido do cliente) ou negativo (crédito líquido ao cliente); a equação `Σ(compras) − Σ(vendas) == nota.valor` funciona universalmente (Opção A).

### Session 2026-06-10 (expansão)

- Q: Onde aplicar o centavo residual após arredondamento proporcional? → A: No **último ativo** do array de entrada, calculado como `Soma_Taxas − Total_Taxas_Distribuidas_Anteriores` (substitui critério anterior de maior volume).
- Q: Quais taxas entram no rateio proporcional? → A: Somente os campos de `taxas_rateáveis` (liquidação, emolumentos, transferência, corretagem, ISS, outras). `impostos_retidos` **não** entram em `Soma_Taxas`.
- Q: Como arredondar cada quota proporcional (exceto o último ativo)? → A: Arredondamento para 2 casas decimais com regra **meio para cima** (ROUND_HALF_UP).
- Q: Qual formato de saída do cálculo? → A: Mapa associando cada ativo da nota ao seu **valor líquido final** (já considerando taxas rateadas), retornado somente se validações pré e pós-cálculo passarem.
- Q: Qual é o tipo da chave do mapa de saída (FR-017)? → A: Instância `NoteAsset` como chave (`Map<NoteAsset, Double>`); a entidade deve garantir igualdade estrutural por todos os seus campos (`data class` satisfaz por padrão) para lidar corretamente com tickers repetidos a preços distintos.
- Q: Como realizar a comparação de igualdade na validação FR-007 (`quantidade × valor_unitario == valor_bruto_total`)? → A: Converter ambos os lados para centavos inteiros (Long) antes de comparar — consistente com FR-016; elimina falsos positivos por erros de representação IEEE 754.
- Q: O campo `especificacao` de `NoteAsset` participa da igualdade estrutural (`equals`/`hashCode`) ou é apenas metadado de exibição? → A: Participa da identidade estrutural completa — com `data class` todos os campos contribuem por padrão; diferencia linhas do mesmo ticker que divergem na especificação (ex.: "BRB111F UNT N2" vs. "BRB111 UNT N2" para BRBI11 na nota canônica).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Validar integridade dos dados brutos antes do cálculo (Priority: P1)

Um sistema de gestão de carteira recebe uma nota de corretagem no formato estruturado (metadados, resumo financeiro e lista de ativos). Antes de qualquer rateio, o sistema verifica que os totais informados pela corretora são internamente consistentes — volume total, integridade por linha de ativo, totais por tipo de movimentação e não-negatividade dos valores monetários.

**Why this priority**: Dados inconsistentes na origem geram custos médios incorretos de forma silenciosa. A validação pré-cálculo é a primeira barreira de qualidade e evita processar notas corrompidas ou mal parseadas.

**Independent Test**: Pode ser testado fornecendo uma nota com `volume_total_operado` divergente da soma dos `valor_bruto_total` dos ativos e verificando que o fluxo é interrompido com mensagem descritiva, sem produzir rateio.

**Acceptance Scenarios**:

1. **Given** uma nota onde `resumo_financeiro.volume_total_operado` = R$ 48.912,22 e a soma dos `valor_bruto_total` dos 29 ativos também é R$ 48.912,22 (conforme `docs/nota.json`), **When** a validação pré-cálculo é executada, **Then** a regra de consistência do volume total (1.1) é satisfeita e o fluxo prossegue para o rateio.
2. **Given** um ativo com `quantidade` = 53, `valor_unitario` = R$ 51,06 e `valor_bruto_total` = R$ 2.706,18, **When** a validação de integridade do ativo (1.2) é executada, **Then** o produto quantidade × valor unitário coincide exatamente com o valor bruto informado.
3. **Given** uma nota onde a soma dos `valor_bruto_total` de ativos COMPRA ≠ `total_compras_vista`, **When** a validação pré-cálculo é executada, **Then** o sistema retorna erro de negócio descritivo e nenhum rateio é produzido.
4. **Given** qualquer campo monetário negativo em `volume_total_operado`, `valor_bruto_total`, `quantidade`, `valor_unitario` ou em qualquer item de `taxas_rateáveis`, **When** a validação pré-cálculo é executada, **Then** o sistema retorna erro de negócio descritivo.

---

### User Story 2 — Calcular rateio de taxas entre ativos de uma nota mista (Priority: P1)

Após validação bem-sucedida, o sistema distribui proporcionalmente as taxas rateáveis entre todos os ativos da nota — incluindo operações de COMPRA e VENDA na mesma nota — e apura o custo ou recebimento líquido real de cada linha de operação.

**Why this priority**: É o fluxo central da funcionalidade. Sem esse cálculo, nenhum ativo terá custo médio correto. Diretamente ligado à precisão fiscal e contábil das operações.

**Independent Test**: Pode ser testado com a nota canônica de `docs/nota.json` (30 ativos, `Soma_Taxas` = R$ 14,66) e verificando que cada ativo recebe quota proporcional ao seu `valor_bruto_total` e que o último ativo absorve o resíduo de centavos.

**Acceptance Scenarios**:

1. **Given** a nota canônica com `volume_total_operado` = R$ 48.912,22 e `Soma_Taxas` = R$ 14,66 (soma de `taxa_liquidacao` + `emolumentos` + `taxa_transferencia` + `corretagem` + `iss` + `outras`), **When** o rateio é calculado, **Then** para cada ativo exceto o último a taxa proporcional = `(valor_bruto_total / volume_total_operado) × Soma_Taxas` arredondada para 2 casas com meio para cima; o último ativo (KNSC11 VENDA, índice 29) recebe `Soma_Taxas − soma das taxas dos 29 ativos anteriores`.
2. **Given** um ativo com movimentação COMPRA, **When** o valor líquido é calculado, **Then** `valor_liquido = valor_bruto_total + taxa_proporcional` (taxa aumenta o custo).
3. **Given** um ativo com movimentação VENDA, **When** o valor líquido é calculado, **Then** `valor_liquido = valor_bruto_total − taxa_proporcional` (taxa reduz o recebimento líquido).
4. **Given** uma nota válida processada com sucesso, **When** o cálculo conclui, **Then** o resultado é um mapa com uma entrada por ativo da lista de entrada, associando cada ativo ao seu valor líquido final.

---

### User Story 3 — Garantir fechamento contábil da nota (Priority: P1)

Após o rateio, o sistema valida salvaguardas matemáticas no resultado antes de entregá-lo ao chamador: batimento das taxas distribuídas e equação de fechamento contábil contra o `valor_liquido_nota` informado nos metadados.

**Why this priority**: Sem esta validação, erros silenciosos de arredondamento ou lógica podem gerar inconsistências contábeis irrastreáveis. É um requisito de auditabilidade do padrão SINACOR.

**Independent Test**: Pode ser testado calculando o rateio da nota canônica e verificando que `Σ(taxas_proporcionais) = Soma_Taxas` e que `Σ(compras_líquidas) − Σ(vendas_líquidas) = valor_liquido_nota` (com sinal).

**Acceptance Scenarios**:

1. **Given** qualquer nota válida após rateio, **When** a validação pós-cálculo (3.1) é executada, **Then** a soma de todas as taxas proporcionais atribuídas é estritamente igual a `Soma_Taxas`.
2. **Given** a nota canônica com `metadados.valor_liquido_nota` = −R$ 33.705,98, **When** o rateio é calculado e a validação pós-cálculo (3.2) é executada, **Then** `Σ(valor_liquido dos ativos COMPRA) − Σ(valor_liquido dos ativos VENDA) = −33.705,98` (sinal contábil preservado).
3. **Given** uma nota onde a equação de fechamento não é satisfeita após o rateio, **When** a validação pós-cálculo é executada, **Then** o sistema retorna erro de cálculo descritivo com a discrepância e nenhum mapa de resultado é retornado.

---

### User Story 4 — Tratar arredondamento de centavos sem perda nem ganho (Priority: P2)

O rateio proporcional gera valores com casas decimais infinitas (dízimas). O sistema arredonda cada quota individual (exceto a do último ativo) para 2 casas decimais com meio para cima e confia o resíduo ao último ativo da lista, garantindo que a soma das taxas individuais seja idêntica ao total de taxas rateáveis da nota.

**Why this priority**: Sem tratamento de arredondamento, a soma das taxas individuais nunca será exatamente igual ao total cobrado, gerando falha de validação em casos reais.

**Independent Test**: Pode ser testado com a nota canônica de 29 ativos e verificando que a soma das taxas proporcionais é exatamente R$ 14,66.

**Acceptance Scenarios**:

1. **Given** 30 ativos com volumes distintos e `Soma_Taxas` = R$ 14,66, **When** o rateio é calculado, **Then** as taxas dos 29 primeiros ativos são arredondadas individualmente (meio para cima) e o 30º ativo recebe o resíduo; a soma total é exatamente R$ 14,66.
2. **Given** uma nota com um único ativo, **When** o rateio é calculado, **Then** esse ativo absorve 100% de `Soma_Taxas` via fórmula de resíduo (único elemento = último).
3. **Given** qualquer nota válida, **When** o rateio é calculado, **Then** a soma das taxas proporcionais individuais é sempre igual a `Soma_Taxas` (diferença = R$ 0,00).

---

### Edge Cases

- O que acontece quando a nota contém apenas um ativo? (Absorve 100% das taxas via resíduo no último — e único — ativo.)
- O que acontece quando um ativo tem `quantidade < 0` ou `valor_unitario < 0`? (Erro na validação pré-cálculo — regra de não-negatividade 1.4.)
- O que acontece quando `quantidade = 0` ou `valor_unitario = 0` com `valor_bruto_total = 0`? (Falha na regra 1.2 ou impossibilidade de rateio com volume zero.)
- O que acontece quando o volume total operado é zero? (Erro na validação pré-cálculo ou impossibilidade de calcular fator de rateio.)
- O que acontece quando a lista de ativos está vazia? (Erro na validação pré-cálculo.)
- O que acontece quando todas as taxas rateáveis são zero? (Taxa proporcional = 0 para todos; valor líquido = valor bruto.)
- O que acontece quando `impostos_retidos` contém valores positivos? (Não afetam o rateio; ficam fora de `Soma_Taxas`.)
- O que acontece quando o mesmo ticker aparece em múltiplas linhas (compras parciais a preços distintos)? (Cada linha é um ativo independente no rateio; não há consolidação por ticker.)
- O que acontece quando dois ativos são idênticos em **todos** os 6 campos (ticker, especificação, movimentação, quantidade, preço e valor bruto)? (Erro na Etapa 1 — as duas linhas colidiriam como chave no mapa de saída; a nota é rejeitada com `IllegalArgumentException` descritiva.)
- O que acontece quando o total de taxas é maior que o volume financeiro de um ativo? (Situação válida; em VENDA o valor líquido pode ser menor que zero.)
- O que acontece quando todos os ativos são VENDA? (`valor_liquido_nota` será negativo; `Σ(compras)=0`, logo `0 − Σ(vendas_líquidas) = valor_liquido_nota`.)
- O que acontece quando dados brutos são consistentes mas o `valor_liquido_nota` informado não fecha com o rateio? (Erro pós-cálculo 3.2; nenhum resultado entregue.)

## Requirements *(mandatory)*

### Functional Requirements

#### Modelo de entrada

- **FR-001**: O sistema DEVE aceitar notas no formato estruturado com três blocos: `metadados`, `resumo_financeiro` e `ativos` (conforme estrutura canônica em `docs/nota.json`).
- **FR-002**: `metadados` DEVE conter: número da nota, data do pregão, data de liquidação, corretora, CNPJ da corretora e `valor_liquido_nota` (com sinal contábil).
- **FR-003**: `resumo_financeiro` DEVE conter: `volume_total_operado`, `total_compras_vista`, `total_vendas_vista`, `taxas_rateáveis` e `impostos_retidos`.
- **FR-004**: Cada item de `ativos` DEVE conter: ticker, especificação, movimentação (COMPRA ou VENDA), quantidade, valor unitário e `valor_bruto_total` informado pela fonte.
- **FR-005**: `taxas_rateáveis` DEVE agregar: taxa de liquidação, emolumentos, taxa de transferência, corretagem, ISS e outras — todos sujeitos a rateio quando positivos.

#### Etapa 1 — Validação pré-cálculo

- **FR-006**: O sistema DEVE validar que `round(volume_total_operado × 100)` em centavos inteiros (Long) é exatamente igual a `round(Σ valor_bruto_total × 100)` — mesma aritmética de centavos de FR-016 (regra 1.1).
- **FR-007**: Para cada ativo, o sistema DEVE validar que `round(quantidade × valor_unitario × 100)` em centavos inteiros (Long) é exatamente igual a `round(valor_bruto_total × 100)` — mesma aritmética de centavos de FR-016 (regra 1.2).
- **FR-008**: O sistema DEVE validar que a soma dos `valor_bruto_total` dos ativos COMPRA é igual a `total_compras_vista` e que a soma dos ativos VENDA é igual a `total_vendas_vista` (regra 1.3).
- **FR-009**: O sistema DEVE rejeitar valores negativos em `volume_total_operado`, `valor_bruto_total`, `quantidade`, `valor_unitario` e em qualquer campo de `taxas_rateáveis` (regra 1.4).
- **FR-010**: Se qualquer regra da Etapa 1 falhar, o sistema DEVE interromper o fluxo com `IllegalArgumentException` descritiva (dados inválidos de entrada) e NÃO iniciar o rateio. A mensagem DEVE identificar a regra violada, o campo afetado e, onde aplicável, o ticker do ativo.

#### Etapa 2 — Algoritmo de rateio

- **FR-011**: O sistema DEVE calcular `Soma_Taxas` como a soma de todos os valores em `taxas_rateáveis` (excluindo `impostos_retidos`).
- **FR-012**: Para cada ativo exceto o último, o sistema DEVE calcular a taxa proporcional em centavos inteiros (Long), conforme FR-016: `feeCents[i] = ROUND_HALF_UP(grossValueCents[i] × somaFeesCents / totalVolumeCents)`, onde o arredondamento é realizado inteiramente em inteiros antes de qualquer conversão de volta a Double.
- **FR-013**: Para o último ativo do array, o sistema DEVE calcular a taxa em centavos inteiros (Long): `feeCents[N-1] = somaFeesCents − Σ feeCents[0..N-2]` — o resíduo é calculado inteiramente em inteiros (Long), garantindo que `Σ feeCents == somaFeesCents` sem drift de arredondamento.
- **FR-014**: Para ativos COMPRA, o valor líquido DEVE ser `valor_bruto_total + taxa_proporcional`.
- **FR-015**: Para ativos VENDA, o valor líquido DEVE ser `valor_bruto_total − taxa_proporcional`.
- **FR-016**: Todos os cálculos monetários (somas, divisões, fatores e arredondamentos) DEVEM utilizar aritmética de alta precisão em centavos inteiros, sem depender de representações de ponto flutuante imprecisas para dinheiro.
- **FR-017**: O método de cálculo DEVE retornar `Map<NoteAsset, Double>` associando cada instância de ativo da nota ao seu valor líquido final (já com taxas aplicadas); a unicidade da chave é garantida pela igualdade estrutural de `NoteAsset` (todos os campos), preservando linhas do mesmo ticker com preços ou quantidades distintas. Se dois ativos da lista forem estruturalmente idênticos em todos os 6 campos, o sistema DEVE lançar `IllegalArgumentException` descritiva antes de calcular o rateio — a nota de entrada é considerada inválida, pois duas linhas com a mesma combinação (ticker, especificação, movimentação, quantidade, preço e valor bruto) são indistinguíveis como chave e não podem ser representadas no mapa de saída.

#### Etapa 3 — Validação pós-cálculo

- **FR-018**: O sistema DEVE validar, em centavos inteiros (Long), que `Σ feeCents[i] == somaFeesCents` — a comparação é feita antes de converter os valores de volta a Double (regra 3.1).
- **FR-019**: O sistema DEVE validar, em centavos inteiros (Long), que `Σ netValueCents[BUY] − Σ netValueCents[SELL] == round(metadata.netValue × 100)` — a comparação é feita em Long antes de qualquer conversão a Double, preservando o sinal contábil informado em `metadados` (regra 3.2).
- **FR-020**: Se qualquer regra da Etapa 3 falhar, o sistema DEVE interromper o fluxo com `IllegalStateException` descritiva (invariante matemática violada após cálculo) e NÃO retornar o mapa de resultado. A mensagem DEVE incluir o valor esperado e o valor obtido para diagnóstico. O uso de `IllegalStateException` (e não `IllegalArgumentException`) permite ao chamador distinguir programaticamente entre falha de entrada (Etapa 1) e falha de fechamento (Etapa 3).

#### Comportamento geral

- **FR-021**: O sistema DEVE rejeitar, como parte da Etapa 1 (coberto por FR-010), notas com lista de ativos vazia — lança `IllegalArgumentException("assets must not be empty")`. Esta regra é executada antes de qualquer outra verificação da Etapa 1.
- **FR-022**: O sistema DEVE rejeitar, como parte da Etapa 1 (coberto por FR-010), notas com `volume_total_operado = 0` — lança `IllegalArgumentException` descritiva. Zero não é negativo (não capturado por FR-009), por isso exige verificação explícita: divisão por zero no fator de rateio tornaria o cálculo impossível.
- **FR-023**: O pipeline completo (validar → ratear → validar resultado) DEVE ser determinístico: a mesma nota de entrada (mesma lista de ativos na mesma ordem) produz sempre o mesmo mapa de saída com a mesma ordem de iteração.

### Key Entities

- **NotaCorretagem** (`BrokerageNote`): Nota SINACOR completa. Atributos: metadados, resumo financeiro, lista de ativos.
- **MetadadosNota**: Cabeçalho da nota. Atributos: número, datas, corretora, CNPJ, `valor_liquido_nota` (sinal contábil).
- **ResumoFinanceiro**: Totais declarados pela corretora. Atributos: volume total operado, total compras à vista, total vendas à vista, taxas rateáveis, impostos retidos.
- **TaxasRateáveis** (`ApportionableFees`): Taxas sujeitas a rateio proporcional. Atributos: liquidação, emolumentos, transferência, corretagem, ISS, outras.
- **ImpostosRetidos**: Retenções fiscais fora do rateio. Atributos: IRRF operações, IRRF day trade.
- **AtivoNota** (`NoteAsset`): Linha de negociação. Atributos: ticker, especificação, movimentação, quantidade, valor unitário, valor bruto total informado. Todos os campos participam da igualdade estrutural (`equals`/`hashCode`), incluindo `especificacao` — isso diferencia entradas como "BRB111F UNT N2" e "BRB111 UNT N2" para o mesmo ticker BRBI11.
- **ValidadorNota**: Responsável exclusivo pela Etapa 1 (validação de integridade dos dados brutos).
- **ResultadoRateio** (`NoteFeeAllocation`): `Map<NoteAsset, Double>` — ativo (instância estruturalmente única) → valor líquido final; retornado somente após sucesso nas três etapas.

> **Mapeamento de nomes (pt-BR → código)**: Os identificadores em código seguem a convenção em inglês (princípio VIII).
>
> | Nome spec (pt-BR) | Nome em código (inglês) |
> |-------------------|------------------------|
> | `NotaCorretagem` | `BrokerageNote` |
> | `MetadadosNota` | `BrokerageNoteMetadata` |
> | `ResumoFinanceiro` | `FinancialSummary` |
> | `TaxasRateáveis` | `ApportionableFees` |
> | `ImpostosRetidos` | `WithheldTaxes` |
> | `AtivoNota` | `NoteAsset` |
> | `ValidadorNota` | `BrokerageNoteValidator` |
> | `ResultadoRateio` | `NoteFeeAllocation` |
> | `COMPRA` / `VENDA` | `TradeType.BUY` / `TradeType.SELL` |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para qualquer nota válida com N ativos, a soma das taxas proporcionais individuais é idêntica a `Soma_Taxas` (diferença = R$ 0,00) em 100% dos casos.
- **SC-002**: Para qualquer nota válida, a equação de fechamento `Σ(compras_líquidas) − Σ(vendas_líquidas) = valor_liquido_nota` é satisfeita em 100% dos casos sem arredondamento manual pelo chamador.
- **SC-003**: Notas com dados brutos inconsistentes (regras 1.1–1.4) produzem erros de negócio descritivos antes de qualquer rateio, em 100% dos casos de violação detectável.
- **SC-004**: A nota canônica de `docs/nota.json` (30 ativos, `Soma_Taxas` = R$ 14,66, `valor_liquido_nota` = −R$ 33.705,98) é processada com sucesso pelo pipeline completo, produzindo mapa com 30 entradas e passando validações 3.1 e 3.2.
- **SC-005**: O algoritmo de resíduo no último ativo é determinístico: para a mesma nota e mesma ordem de ativos, sempre produz o mesmo mapa de resultado.

## Assumptions

- A nota de entrada é proveniente de um sistema externo (corretora/B3) e seus campos numéricos já foram parseados a partir de JSON; a spec não cobre parsing de arquivos nem desserialização.
- O documento `docs/nota.json` é o exemplo canônico de entrada para testes e validação do pipeline.
- O `valor_liquido_nota` segue sinal contábil SINACOR: positivo quando o saldo final é débito do cliente; negativo quando é crédito. Este valor é fornecido pela corretora e serve como referência de fechamento na Etapa 3, não sendo recalculado pelo sistema.
- `impostos_retidos` são informativos nesta feature e não participam do rateio proporcional nem do `Soma_Taxas`.
- Todos os valores monetários são expressos em Reais (BRL) com precisão relevante de 2 casas decimais na saída apresentada ao chamador.
- A lista de ativos pode conter simultaneamente operações de COMPRA e VENDA (nota mista), incluindo múltiplas linhas do mesmo ticker a preços distintos.
- O ticker é identificador opaco; nenhuma validação de formato de ticker está no escopo.
- Persistência da nota ou dos resultados não está no escopo — o cálculo é stateless.
- Múltiplas linhas com o mesmo ticker são tratadas como ativos distintos (chave do mapa de saída é a instância/linha do ativo, não apenas o ticker).
- Cada entidade de domínio encapsula os cálculos que lhe competem (valor bruto, quotas, valor líquido), mantendo o orquestrador do rateio enxuto.

## Out of Scope

- Parsing ou importação de arquivos de nota (PDF, TXT, API da corretora).
- Persistência em base de dados.
- Interface de utilizador para visualização da nota.
- Consolidação de linhas por ticker.
- Rateio de `impostos_retidos` entre ativos.
