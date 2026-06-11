# Feature Specification: Rateio de Taxas de Nota de Corretagem SINACOR

**Feature Branch**: `026-sinacor-fee-rateio`

**Created**: 2026-06-10

**Status**: Draft

## Clarifications

### Session 2026-06-10

- Q: Quando a equação de fechamento falha (FR-009), o chamador recebe os dados de rateio por ativo ou não? → A: Retorna erro imediato sem nenhum dado de rateio; cálculo é abortado (Opção B).
- Q: O sistema deve validar os campos individuais de cada ativo (`quantidade`, `valor_unitario`)? → A: Sim — validar `quantidade > 0` e `valor_unitario > 0` por ativo; erro imediato para qualquer ativo inválido (Opção A).
- Q: Qual é a convenção de sinal de `nota.valor` em notas exclusivamente de venda? → A: Sinal contábil natural — positivo (débito líquido do cliente) ou negativo (crédito líquido ao cliente); a equação `Σ(compras) − Σ(vendas) == nota.valor` funciona universalmente (Opção A).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Calcular rateio de taxas entre ativos de uma nota mista (Priority: P1)

Um sistema de gestão de carteira recebe uma nota de corretagem SINACOR contendo múltiplos ativos — incluindo operações de COMPRA e VENDA — junto com as taxas cobradas pela corretora (emolumentos, liquidação e IR). O sistema precisa distribuir essas taxas proporcionalmente entre os ativos e apurar o custo líquido real de cada operação.

**Why this priority**: É o fluxo central da funcionalidade. Sem esse cálculo, nenhum ativo terá custo médio correto. Diretamente ligado à precisão fiscal e contábil das operações.

**Independent Test**: Pode ser testado fornecendo uma nota com 2 ativos (um de COMPRA e um de VENDA) e verificando que as taxas são distribuídas proporcionalmente e que os valores líquidos são consistentes com o tipo de movimentação.

**Acceptance Scenarios**:

1. **Given** uma nota datada de 01/01/2026 com valor líquido R$ 1.004,54, taxas (emolumentos=R$1,00, liquidação=R$3,54, IR=R$0,00) e 3 ativos — AJFI11 COMPRA 100×R$10,00, BRCO11 VENDA 10×R$100,00, VILG11 COMPRA 1000×R$1,00 — **When** o rateio é calculado, **Then** o volume total é R$3.000,00, o total de taxas é R$4,54, e cada ativo recebe participação de 1/3 (todos com volume R$1.000,00), resultando em: AJFI11=R$1,52 (ajuste de +R$0,01 por ser primeiro com maior volume), BRCO11=R$1,51, VILG11=R$1,51; somando exatamente R$4,54.
2. **Given** um ativo com movimentação COMPRA, **When** o valor líquido é calculado, **Then** o valor líquido = valor bruto + taxa proporcional (taxa aumenta o custo).
3. **Given** um ativo com movimentação VENDA, **When** o valor líquido é calculado, **Then** o valor líquido = valor bruto − taxa proporcional (taxa reduz o recebimento líquido).

---

### User Story 2 — Garantir fechamento contábil da nota (Priority: P1)

Após o rateio, o sistema valida que a equação de fechamento da nota está correta: a diferença entre a soma dos valores líquidos das compras e a soma dos valores líquidos das vendas deve ser igual ao valor líquido informado na nota.

**Why this priority**: Sem esta validação, erros silenciosos de arredondamento ou lógica podem gerar inconsistências contábeis irrastreáveis. É um requisito de auditabilidade do padrão SINACOR.

**Independent Test**: Pode ser testado calculando o rateio e verificando que `Σ(compras líquidas) − Σ(vendas líquidas) == valor líquido da nota`.

**Acceptance Scenarios**:

1. **Given** a nota do exemplo canônico (valor líquido=R$1.004,54), **When** o rateio é calculado, **Then** Σ compras líquidas = R$1.001,52 + R$1.001,51 = R$2.003,03; Σ vendas líquidas = R$998,49; diferença = R$2.003,03 − R$998,49 = R$1.004,54 ✓.
2. **Given** uma nota onde a equação de fechamento não é satisfeita, **When** a validação é executada, **Then** o sistema retorna um erro descritivo com a discrepância calculada e nenhum dado de rateio é retornado.

---

### User Story 3 — Tratar arredondamento de centavos sem perda nem ganho (Priority: P2)

O rateio proporcional gera valores com casas decimais infinitas (dízimas). O sistema deve arredondar cada taxa proporcional para 2 casas decimais e ajustar a diferença residual de centavos no ativo de maior volume financeiro, garantindo que a soma das taxas individuais seja idêntica ao total de taxas da nota.

**Why this priority**: Sem tratamento de arredondamento, a soma das taxas individuais nunca será exatamente igual ao total cobrado, gerando erro de validação em 100% dos casos reais.

**Independent Test**: Pode ser testado com 3 ativos de volumes iguais (1/3 cada) e verificando que a soma das taxas proporcionais arredondadas é igual ao total de taxas da nota.

**Acceptance Scenarios**:

1. **Given** 3 ativos com volumes iguais de R$1.000,00 cada e total de taxas de R$4,54, **When** o rateio é calculado, **Then** cada taxa proporcional bruta é R$1,5133... → arredondada para R$1,51 (soma=R$4,53, falta R$0,01); o R$0,01 é adicionado ao primeiro ativo da lista (AJFI11, pois todos têm o mesmo volume), resultando em R$1,52, R$1,51 e R$1,51, cuja soma é exatamente R$4,54.
2. **Given** qualquer nota válida, **When** o rateio é calculado, **Then** a soma das taxas proporcionais individuais é sempre igual ao total de taxas da nota (sem diferença de centavos).

---

### Edge Cases

- O que acontece quando a nota contém apenas um ativo? (Ele absorve 100% das taxas.)
- O que acontece quando um ativo tem `quantidade <= 0` ou `valor_unitario <= 0`? (Erro imediato antes do cálculo — sinal de parsing incorreto da nota.)
- O que acontece quando o volume total é zero? (Erro: impossível calcular rateio.)
- O que acontece quando a lista de ativos está vazia? (Erro: nota sem ativos.)
- O que acontece quando todas as taxas são zero? (Taxa proporcional = 0 para todos; valor líquido = valor bruto.)
- O que acontece quando o total de taxas é maior que o volume financeiro de um ativo? (Situação válida; o ativo pode ter valor líquido negativo em VENDA.)
- O que acontece quando todos os ativos são VENDA? (`nota.valor` será negativo; `Σ(compras)=0`, logo `0 − Σ(vendas_líquidas) = nota.valor` — validação funciona normalmente com sinal.)
- O que acontece quando dois ativos têm exatamente o mesmo volume? (O ajuste de centavos vai para o primeiro ativo encontrado na lista com maior volume — critério estável e determinístico.)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE calcular o valor bruto de cada ativo como `quantidade × valor_unitario`.
- **FR-002**: O sistema DEVE calcular o volume total da nota somando os valores brutos de todos os ativos, ignorando o tipo de movimentação (absoluto).
- **FR-003**: O sistema DEVE calcular o total de taxas como `emolumentos + liquidacao + ir`.
- **FR-004**: O sistema DEVE calcular a taxa proporcional de cada ativo como `(valor_bruto_ativo / volume_total) × total_taxas`.
- **FR-005**: O sistema DEVE arredondar cada taxa proporcional para 2 casas decimais, utilizando aritmética inteira em centavos para evitar erros de ponto flutuante.
- **FR-006**: O sistema DEVE identificar o ativo de maior volume financeiro e ajustar nele a diferença de centavos entre a soma das taxas arredondadas e o total de taxas original.
- **FR-007**: Para ativos com movimentação COMPRA, o valor líquido DEVE ser `valor_bruto + taxa_proporcional`.
- **FR-008**: Para ativos com movimentação VENDA, o valor líquido DEVE ser `valor_bruto − taxa_proporcional`.
- **FR-009**: O sistema DEVE validar que `Σ(compras_líquidas) − Σ(vendas_líquidas) == valor_líquido_nota` (comparação em centavos inteiros); em caso de divergência, DEVE retornar imediatamente um erro descritivo com a discrepância calculada, sem retornar nenhum dado de rateio.
- **FR-010**: O sistema DEVE retornar erro imediato quando a lista de ativos estiver vazia.
- **FR-011**: O sistema DEVE retornar erro imediato quando o volume total calculado for zero.
- **FR-012**: O sistema DEVE validar cada ativo individualmente: `quantidade > 0` e `valor_unitario > 0`; qualquer ativo com valor inválido DEVE causar erro imediato antes do cálculo.

### Key Entities

- **NotaCorretagem**: Representa uma nota de corretagem SINACOR. Atributos: data da nota, valor líquido com sinal contábil (positivo = débito líquido do cliente; negativo = crédito líquido ao cliente), objeto de taxas e lista de ativos.
- **TaxasNota**: Agrega as taxas cobradas na nota. Atributos: emolumentos, liquidação, IR.
- **AtivoNota**: Representa um ativo negociado na nota. Atributos: ticker (identificador do ativo), tipo de movimentação (COMPRA ou VENDA), quantidade (número positivo), valor unitário (decimal positivo).
- **ResultadoRateioAtivo**: Saída do cálculo para um ativo. Atributos: ticker, valor bruto calculado, taxa proporcional atribuída, valor líquido final.
- **ResultadoRateioNota**: Saída consolidada do rateio, retornada somente quando a equação de fechamento é satisfeita. Atributos: lista de `ResultadoRateioAtivo`.

> **Mapeamento de nomes (pt-BR → código)**: Os identificadores em código seguem a convenção em inglês (princípio VIII).
>
> | Nome spec (pt-BR) | Nome em código (inglês) |
> |-------------------|------------------------|
> | `NotaCorretagem` | `BrokerageNote` |
> | `TaxasNota` | `BrokerageNoteFees` |
> | `AtivoNota` | `NoteAsset` |
> | `ResultadoRateioAtivo` | `AssetFeeAllocation` |
> | `ResultadoRateioNota` | `NoteFeeAllocation` |
> | `COMPRA` / `VENDA` | `TradeType.BUY` / `TradeType.SELL` |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para qualquer nota válida com N ativos, o cálculo é concluído e a soma das taxas proporcionais individuais é idêntica ao total de taxas da nota (diferença = R$ 0,00).
- **SC-002**: Para qualquer nota válida, a equação de fechamento `Σ(compras_líquidas) − Σ(vendas_líquidas) = valor_líquido_nota` é satisfeita em 100% dos casos sem arredondamento manual pelo chamador.
- **SC-003**: Notas com entrada inválida (lista vazia, volume zero) produzem erros descritivos sem lançar exceções não tratadas.
- **SC-004**: O algoritmo de ajuste de centavos é determinístico: para a mesma nota, sempre produz o mesmo resultado independentemente da ordem de execução.

## Assumptions

- A nota de entrada é proveniente de um sistema externo (corretora/B3) e seus campos numéricos já foram parseados; a spec não cobre parsing de arquivos.
- O valor líquido da nota (`nota.valor`) segue sinal contábil SINACOR: positivo quando o saldo final é um débito para o cliente (compras superam vendas após taxas), negativo quando é um crédito (vendas superam compras após taxas). Este valor é fornecido pela corretora e serve como referência de fechamento, não sendo recalculado pelo sistema.
- Todos os valores monetários são expressos em Reais (BRL) com 2 casas decimais de precisão relevante.
- A lista de ativos pode conter simultaneamente operações de COMPRA e VENDA (nota mista), o que é o caso mais comum em notas SINACOR.
- O ticker é um identificador opaco (string); nenhuma validação de formato de ticker está no escopo.
- Persistência da nota ou dos resultados não está no escopo desta feature — o cálculo é stateless.
- O código segue o padrão de entidades do projeto: `data class` com construtor privado e `companion object` como ponto de entrada, localizado em `:domain:entity`.
