# Requisitos Funcionais - Tela de Consulta de Posição do Ativo por Período

## Índice

1. [Objetivo](#1-objetivo)
2. [Dados Exibidos na Tabela de Posições](#2-dados-exibidos-na-tabela-de-posições)
3. [Composição da Tela](#3-composição-da-tela)
4. [Ações da Tela](#4-ações-da-tela)
5. [Regras de Negócio e Cálculos](#5-regras-de-negócio-e-cálculos)
6. [Regras de Preenchimento da Tabela](#6-regras-de-preenchimento-da-tabela)
7. [Exemplos](#7-exemplos)
8. [Casos de Uso](#8-casos-de-uso)

---

## 1. Objetivo

Apresentar ao usuário uma visão consolidada de suas posições de ativos (`holdings`) em um determinado período (mês/ano), permitindo comparar com o período imediatamente anterior, analisar a valorização e registrar movimentações.

A principal funcionalidade da tela é uma tabela que exibe as posições de ativos no período selecionado, comparando-as com o período anterior.

---

## 2. Dados Exibidos na Tabela de Posições

A tabela principal deverá exibir os seguintes dados para cada posição (`HoldingHistoryEntry`) encontrada no período de referência:

| Coluna                  | Descrição                                                                               | Fonte                                                                                                                                             |
|:------------------------|:----------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------|
| **Corretora**           | Nome da corretora onde o ativo está custodiado.                                         | `HoldingHistoryEntry.holding.brokerage.name`                                                                                                      |
| **Categoria**           | Tipo de investimento do ativo.                                                          | Calculado em tempo de execução, baseado no tipo do `Asset`.                                                                                       |
| **SubCategoria**        | Subclassificação do tipo de investimento do ativo.                                      | Calculado em tempo de execução: `FixedIncomeAsset.subType.name`, `VariableIncomeAsset.type.formated()`, ou `InvestmentFundAsset.type.formated()`. |
| **Descrição**           | Nome principal do ativo.                                                                | `HoldingHistoryEntry.holding.asset.name`                                                                                                          |
| **Observações**         | Notas e observações adicionais sobre o ativo.                                           | `HoldingHistoryEntry.holding.asset.observations`                                                                                                  |
| **Vencimento**          | Data de vencimento do ativo, se aplicável.                                              | `HoldingHistoryEntry.holding.asset.expirationDate`                                                                                                |
| **Emissor**             | Nome da entidade que emitiu o ativo.                                                    | `HoldingHistoryEntry.holding.asset.issuer.name`                                                                                                   |
| **Liquidez**            | Regra de liquidez do ativo.                                                             | `HoldingHistoryEntry.holding.asset.liquidity`                                                                                                     |
| **Qtde. Ant.**          | Quantidade do ativo detida no final do mês **anterior**.                                | `HoldingHistoryEntryAnterior.endOfMonthQuantity`                                                                                                  |
| **Qtde. Atual**         | Quantidade do ativo detida no final do mês **atual**. Campo de entrada de dados.        | `HoldingHistoryEntryAtual.endOfMonthQuantity`                                                                                                     |
| **Valor Mercado Ant.**  | Valor de mercado total da posição no final do mês **anterior**.                         | `HoldingHistoryEntryAnterior.endOfMonthValue`                                                                                                     |
| **Valor Mercado Atual** | Valor de mercado total da posição no final do mês **atual**. Campo de entrada de dados. | `HoldingHistoryEntryAtual.endOfMonthValue`                                                                                                        |
| **Valorização**         | Variação percentual do valor de mercado no mês.                                         | Ver seção **5.1. Cálculo de Valorização**.                                                                                                        |
| **Situação**            | Classificação da movimentação da posição no mês.                                        | Ver seção **5.2. Cálculo da Situação**.                                                                                                           |

### 2.1. Formatação do Campo Categoria

A categoria é calculada dinamicamente com base no tipo do `Asset`:

- **Renda Fixa**: Para `FixedIncomeAsset` → exibido como `"Renda Fixa"`
- **Renda Variável**: Para `VariableIncomeAsset` → exibido como `"Renda Variável"`
- **Fundos**: Para `InvestmentFundAsset` → exibido como `"Fundos"`

### 2.2. Formatação do Campo SubCategoria

A subcategoria é calculada conforme o tipo de ativo:

- **Renda Fixa** (`FixedIncomeAsset`): `asset.subType.name`
- **Renda Variável** (`VariableIncomeAsset`): `asset.type.formated()`
- **Fundos** (`InvestmentFundAsset`): `asset.type.formated()`

### 2.3. Formatação do Campo Liquidez

A liquidez é formatada conforme o tipo de regra aplicada ao ativo:

- **Diária** (`Liquidity.DAILY`): Exibido como `"Diária"` - resgate pode ser solicitado a qualquer momento
- **No vencimento** (`Liquidity.AT_MATURITY`): Exibido como `"No vencimento"` - liquidez apenas na data de vencimento do título
- **Dias após venda** (`Liquidity.D_PLUS_DAYS`): Exibido como `"D+{days}"` onde `{days}` é o número de dias para o resgate ser efetivado (armazenado na propriedade `liquidityDays`)
  - Exemplo: `"D+2"` (resgate em 2 dias após solicitação)
  - Exemplo: `"D+60"` (resgate em 60 dias após solicitação)

**Nota**: `Liquidity.DAILY` e `Liquidity.AT_MATURITY` são aplicáveis apenas a ativos de Renda Fixa (`FixedIncomeAsset`). `Liquidity.D_PLUS_DAYS` é aplicável a ativos de Renda Variável (`VariableIncomeAsset`) e Fundos de Investimento (`InvestmentFundAsset`), que possuem a propriedade `liquidityDays` para armazenar o número de dias.

---

## 3. Composição da Tela

A tela é composta pelos seguintes elementos:

- **Título**: "Posicionamento no Período de ${mes}/${ano}" (deve ser preenchido de acordo com o campo de seleção de período)
- **Seletor de Período**: Componente (dropdown) para seleção do período (mês/ano)
- **Tabela Principal**: Tabela que exibe as posições de ativos conforme descrito na seção 2

---

## 4. Ações da Tela

### 4.1. Selecionar Período de Referência

- **Componente**: Um seletor para que o usuário escolha o mês e o ano de referência.
- **Ação**: Ao selecionar um novo período, a tabela é atualizada para refletir os dados do período escolhido e do período anterior correspondente.

### 4.2. Editar Posição do Ativo

- **Componente**: Campo de entrada de dados para o `Valor de Mercado Atual` em cada linha da tabela.
- **Ação**: Ao inserir um valor no componente acima, o valor é salvo na posição do mês corrente daquele ativo (`HoldingHistoryEntryAtual.endOfMonthValue`).

---

## 5. Regras de Negócio e Cálculos

### 5.1. Cálculo de Valorização

- **Fórmula**: `Valorização = (Valor de Mercado Atual / Valor de Mercado Anterior) - 1`
- **Regras**:
  - O cálculo só deve ser feito se houver posição no mês anterior com valor de mercado maior que zero.
  - Se o `Valor de Mercado Atual` for zero (Venda Total), a valorização deve ser exibida como "—".
  - Se não houver dados no mês anterior, a valorização deve ser exibida como "—".

### 5.2. Cálculo da Situação

A situação é calculada comparando as quantidades do ativo entre o período atual (`Qtde. Atual`) e o anterior (`Qtde. Ant.`). A existência de um registro (`HoldingHistoryEntry`) para o mês atual é o principal fator.

- Se existe posição no mês anterior mas não há registro para o mês atual: **"Não Registrado"**
- Se não há registro no mês anterior e um novo registro é criado no mês atual: **"Compra"**
- `Qtde. Ant. > 0` e `Qtde. Atual == 0`: **"Venda Total"**
- `Qtde. Ant. > 0` e `Qtde. Atual < Qtde. Ant.`: **"Venda Parcial"**
- `Qtde. Ant. > 0` e `Qtde. Atual > Qtde. Ant.`: **"Aporte"**
- `Qtde. Ant. > 0` e `Qtde. Atual == Qtde. Ant.`: **"Manutenção"**

---

## 6. Regras de Preenchimento da Tabela

- Consultar o histórico de posicionamento do mês selecionado e do mês anterior.
- Se não existir dados no mês anterior, apresentar um "—" no campo que corresponde ao valor e quantidade faltante.
- Se não existir dados no mês atual, apresentar os campos relacionados a esse mês como vazio (permitindo entrada de dados pelo usuário).

---

## 7. Exemplos

### 7.1. Exemplo de Tabela de Consulta

| Corretora | Categoria      | SubCategoria | Descrição          | Observações | Vencimento | Emissor | Liquidez | Qtde. Ant. | Qtde. Atual | Valor Mercado Ant. | Valor Mercado Atual | Valorização | Situação       |
|-----------|----------------|--------------|--------------------|-------------|------------|---------|----------|------------|-------------|--------------------|---------------------|-------------|----------------|
| NuBank    | Renda Fixa     | CDB          | CDB de 100% do CDI | —           | 2028-01-01 | NuBank  | Diária   | 1,0        | 1,0         | R$ 1.000,00        | R$ 1.010,00         | 1,0%        | Manutenção     |
| BTG       | Renda Variável | ETF          | ETF IVVB11         | —           | —          | BTG     | D+2      | —          | 100,0       | —                  | R$ 26.000,00        | —           | Compra         |
| XP        | Renda Variável | Ação         | Ação AAPL          | —           | —          | Apple   | D+2      | 50,0       | 60,0        | R$ 3.200,00        | R$ 3.456,00         | 8,0%        | Aporte         |
| Clear     | Renda Variável | Ação         | Ação MGLU3         | —           | —          | Magalu  | D+2      | 200,0      | 80,0        | R$ 5.500,00        | R$ 2.200,00         | -60,0%      | Venda Parcial  |
| Rico      | Renda Variável | Ação         | Ação B3SA3         | —           | —          | B3      | D+2      | 100,0      | 0,0         | R$ 1.400,00        | R$ 0,00             | —           | Venda Total    |
| Rico      | Renda Variável | Ação         | Ação B3SA3         | —           | —          | B3      | D+2      | 100,0      | —           | R$ 1.400,00        | —                   | —           | Não Registrado |

---

## 8. Casos de Uso

### UC-01: Consultar Posicionamento do Período

**Ator**: Usuário do sistema

**Pré-condições**:

- Sistema inicializado
- Acesso ao repositório de posições históricas
- Pelo menos um período com dados cadastrados

**Fluxo Principal**:

1. Usuário acessa a Tela de Consulta de Posicionamento do Ativo por Período
2. Sistema exibe a tela com o período atual selecionado por padrão
3. Sistema recupera o histórico de posicionamento do período selecionado e do período anterior
4. Sistema calcula os valores de valorização e situação para cada posição
5. Sistema formata os dados de cada posição conforme as regras estabelecidas
6. Sistema exibe tabela com todas as posições do período
7. Usuário visualiza os dados na tabela

**Pós-condições**:

- Tela exibindo lista de posições do período selecionado
- Valores de valorização e situação calculados e exibidos

---

### UC-02: Selecionar Período de Referência

**Ator**: Usuário do sistema

**Pré-condições**:

- Usuário está na Tela de Consulta de Posicionamento do Ativo por Período
- Existem dados históricos disponíveis para outros períodos

**Fluxo Principal**:

1. Usuário clica no seletor de período (dropdown)
2. Sistema exibe lista de períodos disponíveis (mês/ano)
3. Usuário seleciona um período diferente do atual
4. Sistema atualiza o título da tela com o período selecionado
5. Sistema recupera o histórico de posicionamento do período selecionado e do período anterior
6. Sistema recalcula os valores de valorização e situação
7. Sistema atualiza a tabela com os dados do novo período

**Pós-condições**:

- Tela atualizada com dados do período selecionado
- Título da tela reflete o período escolhido

---

### UC-03: Editar Posição do Ativo

**Ator**: Usuário do sistema

**Pré-condições**:

- Usuário está na Tela de Consulta de Posicionamento do Ativo por Período
- Existe uma posição (`HoldingHistoryEntry`) para o período atual ou o usuário deseja criar uma nova

**Fluxo Principal**:

1. Usuário localiza a linha da tabela correspondente ao ativo desejado
2. Usuário insere ou modifica o valor no campo "Valor Mercado Atual"
3. Sistema valida o valor inserido
4. Sistema salva o valor na posição do mês corrente (`HoldingHistoryEntryAtual.endOfMonthValue`)
5. Sistema recalcula a valorização da posição
6. Sistema recalcula a situação da posição
7. Sistema atualiza a linha da tabela com os novos valores calculados

**Fluxo Alternativo - Criar Nova Posição**:

3a. Se não existe registro para o período atual, sistema cria um novo `HoldingHistoryEntry` para o período
4a. Sistema salva o valor no novo registro criado
5a. Continua no passo 5 do fluxo principal

**Pós-condições**:

- Valor de mercado atual salvo no banco de dados
- Tabela atualizada com valorização e situação recalculadas
